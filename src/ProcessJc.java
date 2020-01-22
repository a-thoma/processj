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
 * @author ben
 */
public class ProcessJc {
    /* Kinds of available options for the ProcessJ compiler */
    public static enum OptionType {
        STRING,
        BOOLEAN
        ;
    }
    
    public static class Option {
        protected String d_fieldName;
        protected String d_optionName;
        protected OptionType d_optionType;
        protected String d_desc;
        
        public Option(String field, String name, OptionType type, String desc) {
            d_fieldName = field;
            d_optionName = name;
            d_optionType = type;
            d_desc = desc;
        }
        
        public Option(String field, String name, String desc) {
            this(field, name, OptionType.BOOLEAN, desc);
        }
    }
    
    /* List of available options for the ProcessJ compiler */
    public static final Option[] OPTIONS = {
            new Option("d_showColor", "-showColor", "Use color on terminals that support ansi espace codes"),
            new Option("d_help", "-help", "Show this help message and exit"),
            new Option("d_include", "-include", OptionType.STRING, "Override the default include directory"),
            new Option("d_showMessage", "-showMessage", "Show info of error and warning messages when available"),
            new Option("d_target", "-target", OptionType.STRING, "Specify the target language -- c++, Java (default)"),
            new Option("d_version", "-version", "Print version information and exit"),
            new Option("d_visitor", "-visitor", "Generate all parse tree visitors"),
            new Option("d_showTree", "-showTree", "Show the AST constructed from the parser")
    };
    
    // <--
    /* Fields used by the ProcessJ compiler */
    public boolean d_showColor = false;
    public boolean d_printUsageAndExit = false;
    public String d_include = null;
    public boolean d_showMessage = false;
    public Language d_target = Settings.language;
    public boolean d_version = false;
    public boolean d_visitor = false;
    public boolean d_showTree = false;
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
        ProcessJc pj = new ProcessJc(args);
        /* Do we have any arguments? */
        if (args.length == 0 && !pj.d_printUsageAndExit) {
            /* At least one file must be provided. Throw an error if no
             * file is given, or if the file does not exists */
            System.out.println(new ProcessJMessage.Builder()
                                   .addError(VisitorMessageNumber.RESOLVE_IMPORTS_100)
                                   .build().stTemplate().render());
            pj.printUsageAndExit();
        }
        
        if (pj.d_printUsageAndExit)
            pj.printUsageAndExit();
        else if (pj.d_version)
            pj.version();
        
        Settings.includeDir = pj.d_include;
        AST root = null;
        /* Process source file, one by one */
        for (String f : pj.inputFiles) {
            File inFile = new File(f);
            Scanner s = null;
            parser p = null;
            try {
                String absoluteFilePath = inFile.getAbsolutePath();
                /* Set the package and filename */
                CompilerErrorManager.INSTANCE.setFileName(absoluteFilePath);
                CompilerErrorManager.INSTANCE.setPackageName(absoluteFilePath);
                s = new Scanner(new java.io.FileReader(absoluteFilePath));
                p = new parser(s);
            } catch (java.io.FileNotFoundException e) {
                /* This won't execute! The error is handled above by the command */
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

            /* Cast the result from the parse to a Compilation -- this is the root of the tree */
            Compilation c = (Compilation) root;
            /* Set the absolute path, file, and package name from where this compilation is created */
            System.out.println("-- Setting absolute path, file and package name for '" + inFile.getName() + "'.");
            c.fileName = inFile.getName();
            /* The parent's path of the compiled file */
            String parentPath = inFile.getAbsolutePath();
            /* The parent's absolute path of the compiled */
            c.path = parentPath.substring(0, parentPath.lastIndexOf(File.separator));
            /* A package declaration is optional -- this can be null */
            if (c.packageName() != null)
                c.packageName = ResolveImports.packageNameToString(c.packageName());
            
            /* Decode pragmas -- these are used for generating stubs from libraries.
             * No regular program would have them */
            Library.decodePragmas(c);
            Library.generateLibraries(c);

            /* This table will hold all the top level types */
            SymbolTable globalTypeTable = new SymbolTable("Main file: " + CompilerErrorManager.INSTANCE.fileName);

            /* Dump log messages if true */
            if (pj.d_visitor)
                Log.startLogging();
            
            /* Dump generated AST */
            if (pj.d_showTree)
                c.visit(new ParseTreePrinter());
            
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
            
            // Visit and re-construct if-stmt, while-stmt, for-stmt, and do-stmt.
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
            
            /* Run the code generator for the known (specified) target language */
            if (Settings.language == pj.d_target)
                pj.generateCodeJava(c, inFile, globalTypeTable);
            else
                ; /* TODO: Throw an error message for unknown target language? */
            
            System.out.println("** COMPILATION COMPLITED SUCCESSFULLY **");
        }
    }
    
    /**
     * Given a ProcessJ Compilation unit, e.g., an abstract syntax tree object,
     * we will generate the code for the JVM. The source range for this type of
     * tree is the entire source file, not including leading and trailing
     * whitespace characters and comments.
     *
     * @param c
     *              A Compilation unit consisting of a single file.
     * @param inFile
     *              The compiled file.
     * @param s
     *              A symbol table consisting of all the top level types.
     */
    private void generateCodeJava(Compilation c, File inFile, SymbolTable s) {
        Properties p = utilities.ConfigFileReader.getProcessJConfig();
        /* Run the code generator to decode pragmas, generate libraries,
         * resolve types, and set the symbol table for top level declarations */
        CodeGeneratorJava generator = new CodeGeneratorJava(s);
        /* Set the user working directory */
        generator.workingDir(p.getProperty("workingdir"));
        /* Visit this compilation unit and recursively build the program
         * after returning strings rendered by the string template */
        String code = (String) c.visit(generator);
        /* Write the output to a file */
        Helper.writeToFile(code, c.fileNoExtension(), generator.workingDir());
    }
    
    public ProcessJc(String[] args) {
        this.args = args;
        Settings.ansiColor = Boolean.valueOf(config.getProperty("color"));
        run(); /* Parse command line arguments */
        colorMode(); /* Switch to turn color mode on/off */
    }
    
    public void colorMode() {        
        /* Check default value before switching mode to on/off */
        if (!Settings.ansiColor && this.d_showColor) /* Color mode 'on' */
            config.setProperty("color", String.valueOf(this.d_showColor));
        else if (Settings.ansiColor && this.d_showColor) /* Color mode 'off' */
            config.setProperty("color", String.valueOf(Boolean.FALSE));
        Settings.ansiColor = Boolean.valueOf(config.getProperty("color"));
        ConfigFileReader.closeConfiguration(config);
    }
    
    public void run() {
        for (int pos = 0; pos < args.length;) {
            String arg = args[pos++];
            if (arg.charAt(0) != '-') {
                /* We found a 'Xxx.pj' file */
                if (!inputFiles.contains(arg))
                    inputFiles.add(arg);
            } else {
                boolean foundOption = false;
                for (Option o : OPTIONS) {
                    if (arg.equals(o.d_optionName)) {
                        foundOption = true;
                        String optValue = null;
                        if (o.d_optionType != OptionType.BOOLEAN)
                            optValue = args[pos++];
                        /* Same as before with Java reflection */
                        Class<? extends ProcessJc> c = this.getClass();
                        try {
                            Field f = c.getField(o.d_fieldName);
                            if (optValue != null)
                                f.set(this, optValue);
                            else
                                f.set(this, true);
                        } catch (Exception e) {
                            System.out.println("Failed to access field '" + o.d_fieldName + "'");
                            exit(101);
                        }
                        break;
                    }
                }
                if (!foundOption) {
                    System.out.println("Invalid option '" + arg + "' found.");
                    exit(101);
                }
            }
        }
    }
    
    public void printUsageAndExit() {
        for (Option o : OPTIONS)
            System.out.println(String.format("%-20s %s", o.d_optionName, o.d_desc));
        exit(0);
    }
    
    public void version() {
        String msg = "ProcessJ Version: " + RuntimeInfo.runtimeVersion();
        System.out.println(msg);
        exit(0);
    }
    
    public void exit(int code) {
        System.exit(code);
    }
}
