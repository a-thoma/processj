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
import utilities.Error;
import utilities.ErrorMessage;
import utilities.VisitorErrorNumber;
import utilities.Language;
import utilities.Log;
import utilities.ErrorTracker;
import utilities.Settings;
import utilities.SymbolTable;

/**
 * @author Ben Cisneros
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
        
        // These fields have default values, see PJMain.java
        // for more information
        Settings.includeDir = pjMain.include;
        Settings.targetLanguage = pjMain.target;
        boolean sts = pjMain.sts;
        boolean visitorAll = pjMain.visitorAll;
        List<File> files = pjMain.files;
        
        if (pjMain.help) {
            Formatter formatHelp = new Formatter(optionBuilder);
            System.out.println(formatHelp.buildUsagePage());
            System.exit(0);
        } else if (pjMain.version) {
            try {
                String[] list = pjMain.getVersion().getVersionPrinter();
                System.out.println(StringUtil.join(Arrays.asList(list), "\n"));
                System.exit(0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (pjMain.info != null) {
            System.out.println(String.format("Information about @Option '%s' is not available.", pjMain.info));
            System.exit(0);
        } else if (pjMain.errorCode != null) {
            System.out.println("map -> " + pjMain.errorCode);
            System.exit(0);
        } else if (files == null || files.isEmpty()) {
            // At least one file must be provided otherwise throw an error
            // if none is given or (for now) if a file does not exists
            System.out.println(new ErrorMessage.Builder()
                                   .addError(VisitorErrorNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            System.exit(0);
        }
        
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
                // TODO: For now this won't execute! The error is handled above
                System.out.println(new ErrorMessage.Builder()
                                       .addError(VisitorErrorNumber.RESOLVE_IMPORTS_102)
                                       .addArguments(inFile.getName())
                                       .build().getST().render());
                System.exit(0);
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

            // SYNTAX TREE PRINTER
//            c.visit(new Printers.ParseTreePrinter());

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
            if (sts)
                globalTypeTable.printStructure("");
            
            // =====================================================
            // V I S I T   I M P O R T   D E C L A R A T I O N S
            // =====================================================
            
            c.visit(new namechecker.ResolveImports<AST>(globalTypeTable));
            if (Error.errorCount != 0) {
                System.out.println("** COMPILATION FAILED #0 **");
                System.exit(1);
            }
            
            // ================================================
            // T O P   L E V E L   D E C L A R A T I O N S
            // ================================================
            
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));
            globalTypeTable = SymbolTable.hook;

            // Resolve types from imported packages.
            c.visit(new namechecker.ResolvePackageTypes());
            
            // ===========================
            // N A M E   C H E C K E R
            // ===========================
            
            c.visit(new namechecker.NameChecker<AST>(globalTypeTable));
            if (Error.errorCount != 0) {
//                System.out.println("---------- Error Report ----------");
//                System.out.println(String.format("%d errors in symbol resolution - fix these before type checking.",
//                        Error.errorCount));
//                System.out.println(Error.errors);
                System.out.println("** COMPILATION FAILED #1 **");
                System.exit(1);
            }

            // Re-construct Array Types correctly
            root.visit(new namechecker.ArrayTypeConstructor());
            
            // ===========================
            // T Y P E   C H E C K E R
            // ===========================
            
            c.visit(new typechecker.TypeChecker(globalTypeTable));

            if (Error.errorCount != 0) {
//                System.out.println("---------- Error Report ----------");
//                System.out.println(String.format("%d errors in type checking - fix these before code generation.",
//                        Error.errorCount));
//                System.out.println(Error.errors);
                System.out.println("** COMPILATION FAILED #2 **");
                System.exit(1);
            }
            
            // =============================================
            // O T H E R   S E M A N T I C   C H E C K S
            // =============================================
            
            c.visit(new reachability.Reachability());
            c.visit(new parallel_usage_check.ParallelUsageCheck());
            c.visit(new yield.Yield());

            if (Error.errorCount != 0) {
//                System.out.println("---------- Error Report ----------");
//                System.out.println(Error.errors);
                System.out.println("** COMPILATION FAILED #3 **");
                System.exit(1);
            }
            
            // ===============================
            // C O D E   G E N E R A T O R
            // ===============================
            
            if (Settings.targetLanguage == Language.JVM) {
                generateCodeJava(c, inFile, globalTypeTable);
//                System.out.println("NO CODE GENERATOR");
                System.exit(1);
            } else {
                System.err.println(String.format("Unknown target language '%s' selected.", Settings.targetLanguage));
                System.exit(1);
            }

            System.out.println("** COMPILATION SUCCEEDED **");
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
        Properties configFile = utilities.ConfigFileReader.getConfiguration();

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
