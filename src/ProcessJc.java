import java.io.File;
import java.util.*;

import ast.*;
import cli.*;
import cli.Formatter;
import cli.StringUtil;
import codegeneratorjava.CodeGeneratorJava;
import codegeneratorjava.Helper;
import library.Library;
import namechecker.ResolveImports;
import parser.parser;
import rewriters.CastRewrite;
import scanner.Scanner;
import utilities.ConfigFileReader;
import utilities.PJMessage;
import utilities.VisitorMessageNumber;
import utilities.Language;
import utilities.Log;
import utilities.CompilerMessageManager;
import utilities.Settings;
import utilities.SymbolTable;

/**
 * ProcessJ JVM Compiler.
 * 
 * @author Ben
 * @version 07/01/2018
 * @since 1.2
 */
public class ProcessJc {
    
    public static CLIBuilder optionBuilder = new CLIBuilder().addCommand(PJMain.class);
    
    public static void help() {
        Formatter formatHelp = new Formatter(optionBuilder);
        System.out.println(formatHelp.buildUsagePage());
        System.exit(0);
    }
    
    public static void main(String[] args) {
        if (args.length == 0)
            help();
        
        //
        // COMMAND-LINE PROCESSOR
        // 
        
        // Build options and arguments with user input
        PJMain pjMain = null;
        try {
            optionBuilder.handleArgs(args);
            pjMain = optionBuilder.getCommand(PJMain.class);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        Properties config = ConfigFileReader.openConfiguration();
        
        // These fields have default values that could be updated with
        // user input (see PJMain.java for more info)
        Settings.includeDir = pjMain.include;
        Settings.targetLanguage = pjMain.target;
        boolean sts = pjMain.symbolTable;
        boolean visitorAll = pjMain.visitorAll;
        List<File> files = pjMain.files;
        
        // Turn on/off color mode
        if (pjMain.ansiColour == null) {
            // Set the color mode if the default value in the
            // properties file is 'yes'
            if (config.getProperty("colour").equalsIgnoreCase("yes"))
                Settings.isAnsiColour = true;
        } else {
            Settings.isAnsiColour = pjMain.ansiColour;
            String ansiColorvalue = "no";
            if (Settings.isAnsiColour)
                ansiColorvalue = "yes";
            // Update color code value in properties file
            config.setProperty("colour", ansiColorvalue);
            ConfigFileReader.closeConfiguration(config);
        }
        
        // Display usage page
        if (pjMain.help)
            help();
        // Display version
        if (pjMain.version) {
            try {
                String[] list = pjMain.getVersion().getVersionPrinter();
                System.out.println(StringUtil.join(Arrays.asList(list), "\n"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.exit(0);
        }
        // Display error code information
        if (pjMain.errorCode != null) {
            System.out.println("Not available..");
            System.exit(0);
        }
        // Check for input file(s)
        if (files == null || files.isEmpty()) {
            // At least one file must be provided. Otherwise, throw an error if
            // no file is given, or if the file does not exists
            System.out.println(new PJMessage.Builder()
                                   .addError(VisitorMessageNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            System.exit(0);
        }
        
        // 
        // PROCESS SOURCE FILES
        //
        
        AST root = null;
        
        for (File inFile : files) {
            Scanner s = null;
            parser p = null;
            try {
                String fileAbsolutePath = inFile.getAbsolutePath();
                // Set package and filename
                CompilerMessageManager.INSTANCE.setFileName(fileAbsolutePath);
                CompilerMessageManager.INSTANCE.setPackageName(fileAbsolutePath);
                
//                Error.setFileName(fileAbsolutePath);
//                Error.setPackageName(fileAbsolutePath);
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

            // Cast the result from the parse to a Compilation -- this is the root of the tree
            Compilation c = (Compilation) root;
            // Set absolute path, file and package name from where this Compilation is created
            System.out.println("-- Setting absolute path, file and package name for '" + inFile.getName() + "'.");
            c.sourceFile = inFile.getName();
            String parentPath = inFile.getAbsolutePath(); // Grab the parent's path of source file
            parentPath = parentPath.substring(0, parentPath.lastIndexOf(File.separator));
            c.path = parentPath; // Get file's parent absolute path
            if (c.packageName() != null)  // A package declaration is optional, so this can be 'null'
                c.packageName = ResolveImports.packageNameToString(c.packageName());

            // Decode pragmas -- these are used for generating stubs from libraries.
            // No regular program would have them.
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            // This table will hold all the top level types
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + CompilerMessageManager.INSTANCE.fileName);

            // Dump log messages
            if (visitorAll)
                Log.startLogging();
            
            //
            // VISIT IMPORT DECLARATIONS
            //
            
            SymbolTable.hook = null;
            System.out.println("-- Resolving imports.");
            c.visit(new namechecker.ResolveImports<AST>(globalTypeTable));
            globalTypeTable.printStructure("");
            
//            if (CompilerMessageManager.INSTANCE.getErrorCount() != 0) {
//                CompilerMessageManager.INSTANCE.printTrace("import declarations");
//                CompilerMessageManager.INSTANCE.writeToFile("PJErrors");
//                System.exit(1);
//            }
//            globalTypeTable.setImportParent(SymbolTable.hook);
            
            // 
            // VISIT TOP LEVEL DECLARATIONS
            // 
            
            System.out.println("-- Declaring Top Level Declarations.");
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));
            
            System.out.println("-- Reconstructing records.");
            c.visit(new rewriters.RecordRewrite(globalTypeTable));
            
            System.out.println("-- Checking native Top Level Declarations.");
            c.visit(new namechecker.ResolveImportTopLevelDecls());
            
            // 
            // VISIT RESOLVE PACKAGE TYPES
            // 

            // Resolve types from imported packages.
            System.out.println("-- Resolving imported types.");
            c.visit(new namechecker.ResolvePackageTypes());
            
            // 
            // VISIT NAME CHECKER
            // 
            
            System.out.println("-- Checking name usage.");
            c.visit(new namechecker.NameChecker<AST>(globalTypeTable));
            
            // 
            // VISIT ARRAY TYPES
            //

            // Re-construct Array Types correctly
            System.out.println("-- Reconstrucing array types.");
            root.visit(new namechecker.ArrayTypeConstructor());
            
            // 
            // VISIT TYPE CHECKER
            // 
            
            System.out.println("-- Checking types.");
            c.visit(new typechecker.TypeChecker(globalTypeTable));
            
            // 
            // VISIT REWRITERS
            // 
            
            c.visit(new CastRewrite());
            
            // 
            // VISIT REACHABILITY
            // 
            
            System.out.println("-- Computing reachability.");
            c.visit(new reachability.Reachability());
            
            // 
            // VISIT PARALLEL USAGE
            // 
            
            System.out.println("-- Performing parallel usage check.");
            c.visit(new parallel_usage_check.ParallelUsageCheck());
            
            // 
            // VISIT YIELD
            // 
            
            c.visit(new yield.Yield());
            System.out.println("-- Marking yielding statements and expressions.");
            c.visit(new rewriters.Yield());
            //c.visit(new rewriters.Expr());
            
            System.out.println("-- Checking literal inits are free of channel communication.");
            c.visit(new semanticcheck.LiteralInits());
            
            System.out.println("-- Rewriting yielding expressions.");
            new rewriters.ChannelReadRewrite().go(c, null);
            //System.out.println("Lets reprint it all");
            //c.visit(new printers.ParseTreePrinter());
            //c.visit(new printers.PrettyPrinter());
            System.out.println("-- Checking break and continue labels.");
            new semanticcheck.LabeledBreakContinueCheck().go(c);
            
            System.out.println("-- Rewriting infinite loops.");
            //new rewriters.ForeverLoopRewrite().go(c);
            
            ////
            System.out.println("-- Rewriting yielding expressions in loops.");
            //new rewriters.LoopReadRewrite().go(c, null);
            ////
            
            System.out.println("-- Collecting left-hand sides for par for code generation");
            c.visit(new rewriters.ParFor());
            
            // 
            // CODE GENERATOR
            // 
            
            if (Settings.targetLanguage == Language.JVM) {
                generateCodeJava(c, inFile, globalTypeTable);
            } else {
                System.err.println(String.format("Unknown target language '%s' selected.", Settings.targetLanguage));
                System.exit(1);
            }
            
            System.out.println("============= S = U = C = C = E = S = S =================");
            System.out.println(String.format("*** File '%s' was compiled successfully ***", inFile.getName()));
        }
    }
    
    /**
     * Given a ProcessJ {@link Compilation} unit, e.g. an abstract syntax
     * tree object, we will generate the code for the JVM. The source range
     * for this type of tree is the entire source file, not including leading
     * and trailing whitespace characters and comments.
     *
     * @param compilation
     *              A {@link Compilation} unit consisting of a single file.
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
        CodeGeneratorJava generator = new CodeGeneratorJava(topLevelDecls);

        // Set the user working directory
        //generator.setWorkingDir(configFile.getProperty("workingdir"));

        // Visit this compilation unit and recursively build the program
        // after returning strings rendered by the string template
        String templateResult = (String) compilation.visit(generator);

        // Write the output to a file
        Helper.writeToFile(templateResult, compilation.fileNoExtension());
    }
}
