import java.io.*;
import java.util.*;

import ast.AST;
import ast.Compilation;
import clp.FormatterHelp;
import clp.OptionBuilder;
import clp.StringUtil;
import codegeneratorjava.CodeGeneratorJava;
import library.Library;
import parser.parser;
import scanner.Scanner;
import utilities.Error;
import utilities.ErrorMessage;
import utilities.Language;
import utilities.Log;
import utilities.Settings;
import utilities.SymbolTable;

/**
 * @author Ben Cisneros
 * @version 07/01/2018
 * @since 1.2
 */
public class ProcessJc {
    
    private static final String HELP_ERROR_MSG = "What would you like me to do?";
    
    /**
     * Pretty prints AST-like structures.
     * 
     * @param c
     *          A {@link Compilation} unit representation of an AST.
     */
    private static void writeTree(Compilation c) {
        try {
            FileOutputStream fileOut = new FileOutputStream("Tree.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(c);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in Tree.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void whatMessage() {
        System.err.println(HELP_ERROR_MSG);
        System.exit(0);
    }
    
    // TODO: Only for testing purposes!
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_UNDERLINE = "\033[4m";
    //
    
    public static void main(String[] args) {
        if (args.length == 0) {
//            System.out.println("[" + ANSI_UNDERLINE + "INFO" + ANSI_RESET + "] pjc: " + ANSI_RED + "error: " + ANSI_RESET + "no input file(s)");
//            System.out.println("-> " + ErrorMessage.RESOLVE_IMPORTS_100.format("Blah!!"));
            whatMessage();
        }
        
        AST root = null;
        
        // -----------------------------------------------------------------------------
        // COMMAND LINE PROCESSOR
        OptionBuilder optionBuilder = null;
        PJMain pjMain = null;
        try {
            optionBuilder = new OptionBuilder()
                                .addCommand(PJMain.class)
                                .handlerArgs(args);
            pjMain = optionBuilder.getCommand(PJMain.class);
        } catch(Exception e) {
            System.out.println(e.getMessage() + ">>>>>?????");
            System.exit(1);
        }
        
        System.out.println(">>>>>> ");
        
        // These fields have default values, see PJMain.java for more information
        Settings.includeDir = pjMain.include;
        Settings.targetLanguage = pjMain.target;
        boolean sts = pjMain.sts;
        boolean visitorAll = pjMain.visitorAll;
        List<File> files = pjMain.files;
        
        if (pjMain.help) {
            FormatterHelp formatHelp = new FormatterHelp();
            formatHelp.setSorted(true);
            System.out.println(formatHelp.usagePage(optionBuilder));
            System.exit(0);
        }
        
        if (pjMain.version) {
            try {
                String[] list = pjMain.versionPrinter.getVersionPrinter();
                for (String text : list)
                    System.out.println(text);
                System.exit(0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        if (!StringUtil.isStringEmpty(pjMain.info)) {
            System.out.println(String.format("Information about @Option '%s' is not available.", pjMain.info));
            System.exit(0);
        }
        
        if (files == null || files.isEmpty()) {
            // At least one file must be provided otherwise throw an error
            // Throw error messages
            System.exit(0);
        }
        
        // -----------------------------------------------------------------------------
        
        for (File inFile : files) {
            Scanner s = null;
            parser p = null;
            try {
                String fileAbsolutePath = inFile.getAbsolutePath();
                Error.setFileName(fileAbsolutePath);
                Error.setPackageName(fileAbsolutePath);
                s = new Scanner(new java.io.FileReader(fileAbsolutePath));
                p = new parser(s);
            } catch (java.io.FileNotFoundException e) {
                // TODO: error message
                System.err.println(String.format("File not found: \"%s\"", inFile));
                System.exit(1);
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
            if (visitorAll) {
                Log.startLogging();
            }

            // Dump the symbol table structure
            if (sts) {
                globalTypeTable.printStructure("");
            }

            // -----------------------------------------------------------------------------
            // VISIT IMPORT DECLARATIONS
            c.visit(new namechecker.ResolveImports<AST>(globalTypeTable));
            if (Error.errorCount != 0) {
                System.out.println("** COMPILATION FAILED #0 **");
                System.exit(1);
            }

            // -----------------------------------------------------------------------------
            // TOP LEVEL DECLARATIONS
            
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));
            globalTypeTable = SymbolTable.hook;

            // Resolve types from imported packages.
            c.visit(new namechecker.ResolvePackageTypes());

            // -----------------------------------------------------------------------------
            // NAME CHECKER
            
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

            // -----------------------------------------------------------------------------
            // TYPE CHECKER
            
            c.visit(new typechecker.TypeChecker(globalTypeTable));

            if (Error.errorCount != 0) {
//                System.out.println("---------- Error Report ----------");
//                System.out.println(String.format("%d errors in type checking - fix these before code generation.",
//                        Error.errorCount));
//                System.out.println(Error.errors);
                System.out.println("** COMPILATION FAILED #2 **");
                System.exit(1);
            }

            // -----------------------------------------------------------------------------
            // OTHER SEMANTIC CHECKS
            
            c.visit(new reachability.Reachability());
            c.visit(new parallel_usage_check.ParallelUsageCheck());
            c.visit(new yield.Yield());

            if (Error.errorCount != 0) {
//                System.out.println("---------- Error Report ----------");
//                System.out.println(Error.errors);
                System.out.println("** COMPILATION FAILED #3 **");
                System.exit(1);
            }
            
            // -----------------------------------------------------------------------------
            // CODE GENERATOR
            
            if (Settings.targetLanguage == Language.JVM) {
//                generateCodeJava(c, inFile, globalTypeTable);
                System.out.println("NO CODE GENERATOR");
                System.exit(1);
            } else {
                System.err.println(String.format("Unknown target language '%s' selected.", Settings.targetLanguage));
                System.exit(1);
            }

            System.out.println("** COMPILATION SUCCEEDED **");
        }
    }
    
    /**
     * Given a ProcessJ {@link Compilation} unit, e.g,. an abstract syntax tree
     * object, we will generate the code for the JVM. The source range for this
     * type of tree is the entire source file, not including leading and trailing
     * whitespace characters and comments.
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

    /**
     * Pretty prints a hash table with fancy formatting.
     *
     * @param table
     *          HashTable of values to print.
     */
    private static void printTable(Hashtable<String, Integer> table) {
        String dashLine = "---------------------------------------------------------";
        Log.log(dashLine);
        Log.log(String.format("|%-25s\t|\t%15s\t|", "functionName", "Size"));
        Log.log(dashLine);
        Set<String> myKeys = table.keySet();

        for (String name : myKeys) {
            int size = table.get(name);
            String msg = String.format("|%-25s\t|\t%15d\t|", name, size);
            Log.log(msg);
        }

        Log.log(dashLine);
    }
}
