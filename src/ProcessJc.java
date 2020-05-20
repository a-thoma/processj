import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

import ast.AST;
import ast.Compilation;
import codegen.Helper;
import codegen.java.CodeGeneratorJava;
import codegen.cpp.CodeGeneratorCPP;
import library.Library;
import namechecker.ResolveImports;
import parser.parser;
import printers.ParseTreePrinter;
import rewriters.CastRewrite;
import scanner.Scanner;
import utilities.CompilerErrorManager;
import utilities.ConfigFileReader;
import utilities.Language;
import utilities.Log;
import utilities.ProcessJMessage;
import utilities.RuntimeInfo;
import utilities.Settings;
import utilities.SymbolTable;
import utilities.VisitorMessageNumber;

/**
 * ProcessJ compiler.
 * 
 * @author Ben
 */
public class ProcessJc {
    
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
            new Option("ansiColor",     "-ansi-color",                          "Use color on terminals that support ansi espace codes"),
            new Option("help",          "-help",                                "Show this help message and exit"),
            new Option("include",       "-include",     OptionType.STRING,      "Override the default include directory"),
            new Option("message",       "-message",                             "Show info of error and warning messages when available"),
            new Option("target",        "-target",      OptionType.STRING,      "Specify the target language -- c++, Java (default), js"),
            new Option("version",       "-version",                             "Print version information and exit"),
            new Option("visitAll",      "-visit-all",                           "Generate all parse tree visitors")
    };
    
    // <--
    // Fields used by the ProcessJ compiler.
    public boolean ansiColor = false;
    public boolean help = false;
    public String include = null;
    public boolean message = false;
    public Language target = Settings.language;
    public boolean version = false;
    public boolean visitAll = false;
    // -->
    
    private ArrayList<String> inputFiles = new ArrayList<String>();
    
    private String[] args = null;
    
    private Properties config = ConfigFileReader.openConfiguration();
    
    /**
     * Program execution begins here.
     * 
     * @param args
     *          A vector of command arguments passed to the compiler.
     */
    public static void main(String[] args) {
        ProcessJc processj = new ProcessJc(args);
        // Do we have any arguments?
        if (args.length == 0 && !processj.help) {
            // At least one file must be provided. Throw an error if no
            // file is given, or if the file does not exists.
            System.out.println(new ProcessJMessage.Builder()
                                   .addError(VisitorMessageNumber.RESOLVE_IMPORTS_100)
                                   .build().getST().render());
            processj.help();
        }
        
        if (processj.help)
            processj.help();
        
        if (processj.version)
            processj.version();
//        TODO: what the fuck is this line for? this breaks the import resolution with a NullPtrException...
//        Settings.includeDir = processj.include;
        
        ArrayList<File> files = processj.createFiles();
        AST root = null;
        // Process source file, one by one.
        for (File inFile : files) {
            Scanner s = null;
            parser p = null;
            try {
                String fileAbsolutePath = inFile.getAbsolutePath();
                // Set the package and filename.
                CompilerErrorManager.INSTANCE.setFileName(fileAbsolutePath);
                CompilerErrorManager.INSTANCE.setPackageName(fileAbsolutePath);
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
            // The parent's path of the compilation.
            String parentPath = inFile.getAbsolutePath();
            // The parent's absolute path of the compilation.
            c.path = parentPath.substring(0, parentPath.lastIndexOf(File.separator));
            // A package declaration is optional, this can therefore be 'null'.
            if (c.packageName() != null)
                c.packageName = ResolveImports.packageNameToString(c.packageName());

            // Decode pragmas -- these are used for generating stubs from libraries.
            // No regular program would have them.
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            // This table will hold all the top level types.
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + CompilerErrorManager.INSTANCE.fileName);

            // Dump log messages.
            if (processj.visitAll)
                Log.startLogging();
            
            SymbolTable.hook = null;
            
            // Visit import declarations.
            System.out.println("-- Resolving imports.");
            c.visit(new namechecker.ResolveImports<AST>(globalTypeTable));
            globalTypeTable.printStructure("");
            
            // Visit top-level declarations.
            System.out.println("-- Declaring Top Level Declarations.");
            c.visit(new namechecker.TopLevelDecls<AST>(globalTypeTable));
            
            // Visit and re-construct record types correctly.
            System.out.println("-- Reconstructing records.");
            c.visit(new rewriters.RecordRewrite(globalTypeTable));
            
            // Visit and re-construct protocol types correctly.
            System.out.println("-- Reconstructing protocols.");
            c.visit(new rewriters.ProtocolRewrite(globalTypeTable));
            
            // Visit and re-construct if-stat, while-stat, for-stat, and do-stat.
            System.out.println("-- Reconstructing statements.");
            c.visit(new rewriters.StatementRewrite());
            
            // Visit and resolve import for top-level declarations.
            System.out.println("-- Checking native Top Level Declarations.");
            c.visit(new namechecker.ResolveImportTopDecls());

            // Visit and resolve types from imported packages.
            System.out.println("-- Resolving imported types.");
            c.visit(new namechecker.ResolvePackageTypes());
            
            // Visit name checker.
            System.out.println("-- Checking name usage.");
            c.visit(new namechecker.NameChecker<AST>(globalTypeTable));
            
            // Visit and re-construct array types correctly
            System.out.println("-- Reconstructing array types.");
            root.visit(new namechecker.ArrayTypeConstructor());
            
//            System.out.println(">> Before");
//            c.visit(new ParseTreePrinter());

            // Visit and re-construct array literals
            System.out.println("-- Reconstructing array literas.");
            c.visit(new rewriters.ArraysRewrite());

//            System.out.println(">> After");
//            c.visit(new ParseTreePrinter());
            
            // Visit type checker.
            System.out.println("-- Checking types.");
            c.visit(new typechecker.TypeChecker(globalTypeTable));
            
            // Visit cast-rewrite.
            c.visit(new CastRewrite());
            
            // Visit reachability.
            System.out.println("-- Computing reachability.");
            c.visit(new reachability.Reachability());
            
            // Visit parallel usage.
            System.out.println("-- Performing parallel usage check.");
            c.visit(new parallel_usage_check.ParallelUsageCheck());
            
            // Visit yield.
            c.visit(new yield.Yield());
            System.out.println("-- Marking yielding statements and expressions.");
            c.visit(new rewriters.Yield());
            //c.visit(new rewriters.Expr());
            
            System.out.println("-- Checking literal inits are free of channel communication.");
            c.visit(new semanticcheck.LiteralInits());
            
            System.out.println("-- Rewriting infinite loops.");
            new rewriters.InfiniteLoopRewrite().go(c);
            
            System.out.println("-- Rewriting loops.");
            c.visit(new rewriters.UnrollLoopRewrite());
            
            System.out.println("-- Rewriting yielding expressions.");
            new rewriters.ChannelReadRewrite().go(c);
            //System.out.println("Lets reprint it all");
            //c.visit(new printers.ParseTreePrinter());
            //c.visit(new printers.PrettyPrinter());
            System.out.println("-- Checking break and continue labels.");
//            new semanticcheck.LabeledBreakContinueCheck().go(c);
            
            System.out.println("-- Collecting left-hand sides for par for code generation.");
            c.visit(new rewriters.ParFor());
            
            // Run the code generator for the known (specified) target language.
//            if (Settings.language == processj.target)
//                processj.generateCodeJava(c, inFile, globalTypeTable);
//            else
//            	;
            
            // switch on language requested
            if(Settings.language == Language.JVM) {
            	System.out.println("Generating code for the JVM");
            	processj.generateCodeJava(c,  inFile,  globalTypeTable);
            }
            else if(Settings.language == Language.CPLUS) {
            	System.out.println("Generating CPP code");
            	processj.generateCodeCPP(c,  inFile, globalTypeTable);
            }
            
            System.out.println("** COMPILATION COMPLITED SUCCESSFULLY **");
        }
    }
    
    /**
     * Given a ProcessJ Compilation unit, e.g., an abstract syntax tree object,
     * we will generate the code for the JVM. The source range for this type of
     * tree is the entire source file, not including leading and trailing
     * whitespace characters and comments.
     *
     * @param co
     *              A Compilation unit consisting of a single file.
     * @param inFile
     *              The compiled file.
     * @param s
     *              A symbol table consisting of all the top level types.
     */
    private void generateCodeJava(Compilation co, File inFile, SymbolTable s) {

        System.out.println("Getting PJConfig");
        Properties p = utilities.ConfigFileReader.getProcessJConfig();
        System.out.println("After getting PJConfig");
        // Run the code generator to decode pragmas, generate libraries,
        // resolve types, and set the symbol table for top level declarations.
        CodeGeneratorJava generator = new CodeGeneratorJava(s);
        // Set the user working directory.
        generator.setWorkingDir(p.getProperty("workingdir"));
        // Visit this compilation unit and recursively build the program
        // after returning strings rendered by the string template.
        String code = (String) co.visit(generator);
        // Write the output to a file
        Helper.writeToFile(code, co.fileNoExtension(), generator.getWorkingDir());
    }
    
    /**
     * Given a ProcessJ Compilation unit, e.g., an abstract syntax tree object,
     * we will generate code for a C++ compiler. The source range for this type of
     * tree is the entire source file, not including leading and trailing
     * whitespace characters and comments.
     *
     * @param co
     *              A Compilation unit consisting of a single file.
     * @param inFile
     *              The compiled file.
     * @param s
     *              A symbol table consisting of all the top level types.
     *              
     * @author Alexander Thomason
     */
    private void generateCodeCPP(Compilation co, File inFile, SymbolTable s) {
    	System.out.println("Beginning of generateCodeCPP");
    	System.out.println("Starting the logger (For debugging)");
    	Log.startLogging();
    	System.out.println("Logging started. Continuing on...");
    	/* TODO: write generateCodeCPP -- should be similar to above method
    	 * ---
    	 * this means we need to make a new object, CodeGeneratorCPP like CodeGeneratorJava
    	 * and slap that into the runtime. we also need to rewrite the stg file to generate
    	 * cpp instead of java. also need to check how jars are made so that we produce a
    	 * real binary instead of accidentally trying to make a (probably malformed) jar
    	 */
    	System.out.println("Before getting PJConfig (necessary?)");
    	Properties p = utilities.ConfigFileReader.getProcessJConfig();
    	System.out.println("After getting PJConfig (necessary?)");
    	
    	System.out.println("Creating code generator");
    	CodeGeneratorCPP generator = new CodeGeneratorCPP(s);
    	System.out.println("Getting working directory from config...");
    	generator.setWorkingDir(p.getProperty("workingdir"));
    	System.out.println("workingdir is " + generator.getWorkingDir());
    	System.out.println("Generating code...");
    	String code = (String) co.visit(generator);
    	System.out.println("Code generated. Writing to file...");
    	Helper.writeToFile(code,  co.fileNoExtension(), generator.getWorkingDir());
    	System.out.println("Files written.");
    	System.out.println("End of generateCodeCPP");
    }
    
    public ProcessJc(String[] args) {
        this.args = args;
        Settings.ansiColor = Boolean.valueOf(config.getProperty("color"));
        // Parse command line arguments.
        parseArgs();
        // Switch to turn color mode on/off.
        ansiColor();
    }
    
    public void ansiColor() {        
        // Check default value before switching on/off.
        if (!Settings.ansiColor && this.ansiColor) // Turn ansi-color mode 'on'.
            config.setProperty("color", String.valueOf(this.ansiColor));
        else if (Settings.ansiColor && this.ansiColor) // Turn ansi-color mode 'off'.
            config.setProperty("color", String.valueOf(Boolean.FALSE));
        Settings.ansiColor = Boolean.valueOf(config.getProperty("color"));
        ConfigFileReader.closeConfiguration(config);
    }
    
    public void parseArgs() {
        for (int pos = 0; pos < args.length;) {
            String arg = args[pos++];
            if (arg.charAt(0) != '-') {
                // We found an '.pj' file.
                if (!inputFiles.contains(arg))
                    inputFiles.add(arg);
            } else {
                boolean foundOption = false;
                for (Option o : options) {
                    if (arg.equals(o.optionName)) {
                        foundOption = true;
                        String optValue = null;
                        if (o.optionType != OptionType.BOOLEAN)
                            optValue = args[pos++];
                        // Same as before with Java reflection.
                        Class<? extends ProcessJc> c = this.getClass();
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
                        break;
                    }
                }
                if (!foundOption) {
                    System.out.println("Invalid option '" + arg + "' found.");
                    exit(1);
                }
            }
        }
    }
    
    public void help() {
        for (Option o : options) {
            String name = String.format("%-20s %s", o.optionName, o.description);
            System.out.println(name);
        }
        this.exit(0);
    }
    
    public void version() {
        String msg = "ProcessJ Version: " + RuntimeInfo.runtimeVersion();
        System.out.println(msg);
        this.exit(0);
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
