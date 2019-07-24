import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

import ast.AST;
import ast.Compilation;
import codegen.Helper;
import codegen.java.CodeGeneratorJava;
import library.Library;
import namechecker.ResolveImports;
import parser.parser;
import rewriters.CastRewrite;
import scanner.Scanner;
import utilities.CompilerMessageManager;
import utilities.ConfigFileReader;
import utilities.Language;
import utilities.Log;
import utilities.ProcessJMessage;
import utilities.Settings;
import utilities.SymbolTable;
import utilities.VisitorMessageNumber;

/**
 * 
 * @author Ben
 */
public class startProcessJc {
    
    // Kinds of available options for the ProcessJ compiler.
    public static enum OptionType {
        STRING,
        BOOLEAN
        ;
    }
    
    public static class Option {
        protected String fieldName;
        protected String optionName;
        protected OptionType optionType;
        protected String description;
        
        public Option(String field, String name, OptionType type, String desc) {
            fieldName = field;
            optionName = name;
            optionType = type;
            description = desc;
        }
        
        public Option(String field, String name, String desc) {
            this(field, name, OptionType.BOOLEAN, desc);
        }
    }
    
    // List of available options for the ProcessJ compiler.
    public static final Option[] options = {
            new Option("ansiColor",    "-ansi-color",                          "Use color on terminals that support ANSI espace codes"),
            new Option("help",          "-help",                                "Show this help message and exit"),
            new Option("include",       "-include",     OptionType.STRING,      "Override the default include directory"),
            new Option("target",        "-target",      OptionType.STRING,      "Specify the target language: C++, Java, JS"),
            new Option("version",       "-version",                             "Print version information and exit"),
            new Option("visitAll",      "-visit-all",                           "Generate all parse tree visitors")
    };
    
    // <--
    // Fields used by the compiler.
    public boolean ansiColor = false;
    public boolean help = false;
    public String include = null;
    public Language target = Language.JVM;
    public boolean version = false;
    public boolean visitAll = false;
    // -->
    
    private ArrayList<String> inputFiles = new ArrayList<String>();
    
    private String[] args = null;
    
    private Properties config = ConfigFileReader.openConfiguration();
    
    public static void main(String[] args) {
        startProcessJc processj = new startProcessJc(args);
        // Do we have any arguments?
        if (args.length == 0) {
            // At least one file must be provided. Otherwise, throw an error if
            // no file is given, or if the file does not exists
            System.out.println(new ProcessJMessage.Builder()
                                   .addError(VisitorMessageNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            processj.help();
            processj.exit(1);
        }
        
        processj.parseArgs();
        
        // 
        // PROCESS SOURCE FILES
        //
        ArrayList<File> files = processj.createFiles();
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

            // Cast the result from the parse to a Compilation -- this is the root of the tree.
            Compilation c = (Compilation) root;
            // Set the absolute path, file, and package name from where this Compilation is created.
            System.out.println("-- Setting absolute path, file and package name for '" + inFile.getName() + "'.");
            c.fileName = inFile.getName();
            // Get the parent's path of the input file.
            String parentPath = inFile.getAbsolutePath();
            // Get the file's parent absolute path.
            c.path = parentPath.substring(0, parentPath.lastIndexOf(File.separator));
            // A package declaration is optional, this can therefore be 'null'.
            if (c.packageName() != null)
                c.packageName = ResolveImports.packageNameToString(c.packageName());

            // Decode pragmas -- these are used for generating stubs from libraries.
            // No regular program would have them.
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            // This table will hold all the top level types
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + CompilerMessageManager.INSTANCE.fileName);

            // Dump log messages
            if (processj.visitAll)
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
            
            ///
            System.out.println("-- Reconstructing protocols.");
            c.visit(new rewriters.ProtocolRewrite(globalTypeTable));
            ///
            
            System.out.println("-- Checking native Top Level Declarations.");
            c.visit(new namechecker.ResolveImportTopDecls());
            
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
            // VISIT CASTREWRITE
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
            new rewriters.ForeverLoopRewrite().go(c);
            
            System.out.println("-- Collecting left-hand sides for par for code generation.");
            c.visit(new rewriters.ParFor());
            
            // 
            // CODE GENERATOR
            // 
            
            if (Settings.language == processj.target) {
                generateCodeJava(c, inFile, globalTypeTable);
            } else {
                System.err.println(String.format("Unknown target language '%s' selected.", Settings.language));
                System.exit(1);
            }
            
            System.out.println("============= S = U = C = C = E = S = S =================");
            System.out.println(String.format("*** File '%s' was compiled successfully ***", inFile.getName()));
        }
    }
    
    /**
     * Given a ProcessJ Compilation unit, e.g. an abstract syntax tree
     * object, we will generate the code for the JVM. The source range
     * for this type of tree is the entire source file, not including
     * leading and trailing whitespace characters and comments.
     *
     * @param compilation
     *              A Compilation unit consisting of a single file.
     * @param inFile
     *              The compiled file.
     * @param topLevelDecls
     *              A symbol table consisting of all the top level types.
     */
    private static void generateCodeJava(Compilation compilation, File inFile, SymbolTable topLevelDecls) {
        // Read in and get the pathname of the input file.
        String name = inFile.getName().substring(0, inFile.getName().lastIndexOf("."));
        Properties config = utilities.ConfigFileReader.openConfiguration();

        // Run the code generator to decode pragmas, generate libraries,
        // resolve types, and set the symbol table for top level declarations.
        CodeGeneratorJava generator = new CodeGeneratorJava(topLevelDecls);

        // Set the user working directory
        //generator.setWorkingDir(configFile.getProperty("workingdir"));

        // Visit this compilation unit and recursively build the program
        // after returning strings rendered by the string template.
        String code = (String) compilation.visit(generator);

        // Write the output to a file
        Helper.writeToFile(code, compilation.fileNoExtension());
    }
    
    public startProcessJc(String[] args) {
        this.args = args;
        this.ansiColor = config.getProperty("color").equals("yes");
    }
    
    public void parseArgs() {
        if (args == null)
            return;
        
        int pos = 0;
        while (pos < args.length) {
            String arg = args[pos++];
            if (arg.charAt(0) != '-') {
                // We found an '.pj' file.
                if (!inputFiles.contains(arg)) {
                    inputFiles.add(arg);
                }
            } else {
                boolean foundOption = false;
                for (Option o : options) {
                    if (arg.equals(o.optionName)) {
                        foundOption = true;
                        String optValue = null;
                        if (o.optionType != OptionType.BOOLEAN)
                            optValue = args[pos++];
                        // Same as before with Java reflection.
                        Class<? extends startProcessJc> c = this.getClass();
                        try {
                            Field f = c.getField(o.fieldName);
                            if (optValue != null)
                                f.set(this, optValue);
                            else
                                f.set(this, true);
                        } catch (Exception e) {
                            System.out.println("Failed to access field '" + o.fieldName + "'");
                            exit(1);
                        }
                    }
                }
                if (!foundOption) {
                    System.out.println("Invalid option '" + arg + "'");
                    exit(1);
                }
            }
        }
    }
    
    public void help() {
        for (Option o : options) {
            String name = String.format("%-18s %s", o.optionName, o.description);
            System.out.println(name);
        }
    }
    
    public ArrayList<File> createFiles() {
        ArrayList<File> files = new ArrayList<File>(inputFiles.size());
        for (String f : inputFiles)
            files.add(new File(f));
        return files;
    }
    
    public void exit(int code) {
        System.exit(code);
    }
}
