import java.io.*;
import java.util.*;

import ast.AST;
import ast.Compilation;
import clp.Formatter;
import clp.OptionBuilder;
import clp.StringUtil;
import codegeneratorjava.CodeGeneratorJava;
import library.Library;
import parser.parser;
import scanner.Scanner;
import utilities.ConfigFileReader;
import utilities.Error;
import utilities.ErrorMessage;
import utilities.VisitorErrorNumber;
import utilities.Language;
import utilities.Log;
import utilities.ErrorTracker;
import utilities.Settings;
import utilities.SymbolTable;

/**
 * @author Ben
 * @version 07/01/2018
 * @since 1.2
 */
public class ProcessJc {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(new ErrorMessage.Builder()
                                   .addError(VisitorErrorNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            System.exit(0);
        }
        
        // ===============================================
        // C O M M A N D   L I N E   P R O C E S S O R
        // ===============================================
        
        // Build options and arguments with user input
        OptionBuilder optionBuilder = null;
        PJMain pjMain = null;
        try {
            optionBuilder = new OptionBuilder()
                                .addCommand(PJMain.class)
                                .handlerArgs(args);
            pjMain = optionBuilder.getCommand(PJMain.class);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        boolean isExitCode = false;
        Properties config = ConfigFileReader.openConfiguration();
        
        // These fields have default values, but could be updated
        // with user input (see `PJMain.java' for more information)
        Settings.includeDir = pjMain.include;
        Settings.targetLanguage = pjMain.target;
        boolean symbolTable = pjMain.symbolTable;
        boolean visitorAll = pjMain.visitorAll;
        List<File> files = pjMain.files;
        
        // Turn on/off colour mode
        if (pjMain.ansiColour == null) {
            // Only set the colour mode if the default value in
            // properties file is `yes'
            if (config.getProperty("colour").equalsIgnoreCase("yes"))
                Settings.isAnsiColour = true;
        } else {
            Settings.isAnsiColour = pjMain.ansiColour;
            String ansiColorvalue = "no";
            if (Settings.isAnsiColour)
                ansiColorvalue = "yes";
            // Update `colour' code value in properties file
            config.setProperty("colour", ansiColorvalue);
            ConfigFileReader.closeConfiguration(config);
            isExitCode = true;
        }
        
        // Display usage page
        if (pjMain.help) {
            Formatter formatHelp = new Formatter(optionBuilder);
            System.out.println(formatHelp.buildUsagePage());
            isExitCode = true;
        }
        // Display version
        else if (pjMain.version) {
            try {
                String[] list = pjMain.getVersion().getVersionPrinter();
                System.out.println(StringUtil.join(Arrays.asList(list), "\n"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            isExitCode = true;
        }
        // TODO: Display error code information
        else if (pjMain.errorCode != null) {
            System.out.println("map -> " + pjMain.errorCode);
            isExitCode = true;
        }
        // Check for input file(s)
        else if (!isExitCode && (files == null || files.isEmpty())) {
            // At least one file must be provided. Otherwise, throw an error
            // if none is given, or (for now) if a file does not exists
            System.out.println(new ErrorMessage.Builder()
                                   .addError(VisitorErrorNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            isExitCode = true;
        }
        
        // Terminate program's execution
        if (isExitCode)
            System.exit(0);
        
        // ===============================================
        // P R O C C E S S I N G   F I L E S
        // ===============================================
        
        AST root = null;
        
        for (File inFile : files) {
            Scanner s = null;
            parser p = null;
            try {
                String fileAbsolutePath = inFile.getAbsolutePath();
                // Set package and filename
                ErrorTracker.INSTANCE.setFileName(fileAbsolutePath);
                ErrorTracker.INSTANCE.setPackageName(fileAbsolutePath);
                
                Error.setFileName(fileAbsolutePath);
                Error.setPackageName(fileAbsolutePath);
                s = new Scanner(new java.io.FileReader(fileAbsolutePath));
                p = new parser(s);
            } catch (java.io.FileNotFoundException e) {
                // This won't execute! The error is handled above
                // by the command
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            
            try {
                java_cup.runtime.Symbol r = ((parser) p).parse();
                root = (AST) r.value;
            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            // Cast the result from the parse to a Compilation - this is the root of the tree
            Compilation c = (Compilation) root;

            // Decode pragmas - these are used for generating stubs from libraries.
            // No regular program would have them.
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            // This table will hold all the top level types
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + Error.fileName);

            // Dump log messages
            if (visitorAll)
                Log.startLogging();
            // Dump the symbol table structure
            if (symbolTable)
                globalTypeTable.printStructure("");
            
            // =====================================================
            // V I S I T   I M P O R T   D E C L A R A T I O N S
            // =====================================================
            
            c.visit(new namechecker.ResolveImports<AST>(globalTypeTable));
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("import declarations");
                System.exit(1);
            }
            
            // ===========================================================
            // V I S I T   T O P   L E V E L   D E C L A R A T I O N S
            // ===========================================================
            
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));

            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("top level declarations");
                System.exit(1);
            }
            
            globalTypeTable = SymbolTable.hook;
            
            // ========================================================
            // V I S I T R E S O L V E   P A C K A G E   T Y P E S
            // ========================================================

            // Resolve types from imported packages.
            c.visit(new namechecker.ResolvePackageTypes());
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("package types");
                System.exit(1);
            }
            
            // =======================================
            // V I S I T   N A M E   C H E C K E R
            // =======================================
            
            c.visit(new namechecker.NameChecker<AST>(globalTypeTable));
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("name checker");
                System.exit(1);
            }
            
            // =======================================
            // V I S I T   A R R A Y   T Y P E S
            // =======================================

            // Re-construct Array Types correctly
            root.visit(new namechecker.ArrayTypeConstructor());
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("array types constructor");
                System.exit(1);
            }
            
            // ========================================
            // V I S I T   T Y P E   C H E C K E R
            // ========================================
            
            c.visit(new typechecker.TypeChecker(globalTypeTable));
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("type checking");
                System.exit(1);
            }
            
            // ========================================
            // V I S I T   R E A C H A B I L I T Y
            // ========================================
            
            c.visit(new reachability.Reachability());
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("reachability");
                System.exit(1);
            }
            
            // ===========================================
            // V I S I T   P A R A L L E L   U S A G E
            // ===========================================
            
            c.visit(new parallel_usage_check.ParallelUsageCheck());
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("parallel usage checking");
                System.exit(1);
            }
            
            // ==========================
            // V I S I T   Y I E L D
            // ==========================
            
            c.visit(new yield.Yield());
            
            if (ErrorTracker.INSTANCE.getErrorCount() != 0) {
                ErrorTracker.INSTANCE.printTrace("yield");
                System.exit(1);
            }
            
            // ===============================
            // C O D E   G E N E R A T O R
            // ===============================
            
            if (Settings.targetLanguage == Language.JVM) {
                generateCodeJava(c, inFile, globalTypeTable);
            } else {
                System.err.println(String.format("Unknown target language '%s' selected.", Settings.targetLanguage));
                System.exit(1);
            }

            System.out.println(String.format("*** File '%s' was compiled successfully ***", inFile.getName()));
        }
    }
    
    /**
     * Given a ProcessJ {@link Compilation} unit, e.g. an abstract
     * syntax tree object, we will generate the code for the JVM.
     * The source range for this type of tree is the entire source
     * file, not including leading and trailing whitespace characters
     * and comments.
     *
     * @param compilation
     *              A {@link Compilation} unit consisting of a single
     *              file.
     * @param inFile
     *              The compiled file.
     * @param topLevelDecls
     *              A symbol table consisting of all the top level types.
     */
    private static void generateCodeJava(Compilation compilation, File inFile, SymbolTable topLevelDecls) {
        // Read in and get the pathname of the input file
        String name = inFile.getName().substring(0, inFile.getName().lastIndexOf("."));
        Properties config = utilities.ConfigFileReader.openConfiguration();

        // Run the code generator to decode pragmas, generate libraries, resolve
        // types, and set the symbol table for top level declarations
        CodeGeneratorJava<Object> generator = new CodeGeneratorJava<>(topLevelDecls);

        // Associate this file to the compiled ProcessJ program and then set the
        // user working directory
        generator.setSourceFile(name);
        //generator.setWorkingDirectory(configFile.getProperty("workingdir"));

        // Visit this compilation unit and recursively build the program
        // by returning strings rendered by the string template
        String templateResult = (String) compilation.visit(generator);

        // Write the output to a file
//        Helper.writeToFile(templateResult, generator.getSourceFile());
    }
}
