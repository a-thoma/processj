import java.io.*;
import java.util.*;

import ast.AST;
import ast.Compilation;
import clp.OptionsBuilder;
import codegeneratorjava.CodeGeneratorJava;
import codegeneratorjava.Helper;
import library.Library;
import parser.parser;
import scanner.Scanner;
import utilities.Error;
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
    
    private static final String HELP_ERROR_MSG = "Usage: pjc [global options] [source files...] "
            + "[command options].\nPlease use [-h|-help] for a list of possible commands or refer "
            + " to\nthe documentation for command parameters and usage.";
    
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
    
    // TODO: /Users/Ben/eclipse-workspace/processj/src/tests
    public static void helpError() {
        System.err.println(HELP_ERROR_MSG);
        System.exit(1);
    }
    
    public static void main(String[] args) {
        AST root = null;

        if (args.length == 0) {
            helpError();
        }
        
        OptionsBuilder optionsBuilder = null;
        try {
            optionsBuilder = new OptionsBuilder()
                                 .addCommand(PJMain.class)
                                 .handlerArgs(args);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            helpError();
        }
        
        PJMain pjMain = optionsBuilder.getCommand(PJMain.class);
        
        // These fields have default values, see PJMain.java for more information
        Settings.includeDir = pjMain.include;
        Settings.targetLanguage = pjMain.target;
        boolean sts = pjMain.sts;
        boolean debug = pjMain.debug;
        List<File> files = pjMain.files;
        
        if (files == null || files.isEmpty()) {
            // At least one file must be provided otherwise throw an error
            helpError();
        }
        
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

            // -----------------------------------------------------------------------------
            // TOP LEVEL DECLARATIONS
            
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));
            globalTypeTable = SymbolTable.hook;

            // Resolve types from imported packages.
            c.visit(new namechecker.ResolvePackageTypes());

            // Dump log messages
            if (debug) {
                Log.startLogging();
            }

            // Dump the symbol table structure
            if (sts) {
                globalTypeTable.printStructure("");
            }

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
                generateCodeJava(c, inFile, globalTypeTable);
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