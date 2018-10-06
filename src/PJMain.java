import java.io.File;
import java.util.List;

import clp.Argument;
import clp.IVersionPrinter;
import clp.Option;
import clp.OptionParameters;
import clp.Parameters;
import clp.Util;
import utilities.Language;

/**
 * ProcessJ command line.
 * 
 * @see {@link Language}
 * @author Ben Cisneros
 * @version 08/27/2018
 * @since 1.2
 */
@Parameters(name = "ProcessJ",
            help = "The following options are available:",
            versionPrinter = PJMain.VersionPrinter.class)
public class PJMain extends OptionParameters {

    // TODO: This is for imports (libraries, files, etc), pragmas, etc..
    @Option(names = { "-V", "-verbose" },
            help = "Output messages about the sequence of commands used to compile a ProcessJ program")
    public boolean verbose;
    
    @Option(names = { "-g", "-debug" },
            help = "Generate all debugging info")
    public boolean debug;
    
    // TODO: defaultValue = "/opt/ProcessJ/include"
    @Option(names = { "-I", "-include" },
            metavar = "<DIR>",
            defaultValue = "/Users/Ben/Desktop/processj/include", // default include for testing!
            help = "Override the default include directory which is set to be the Include "
                    + "subdirectory of the ProcessJ directory.")
    public String include;
    
    @Option(names = { "-t", "-target" }, defaultValue = "JVM", metavar = "<LANGUAGE>",
            help = "Specify the target language. "
                    + "C: C source code is written, compiled, and linked with the CSSP runtime. "
                    + "C++: C++ source code is generated and compiled into an executable."
                    + "JVM: JVM class files are written. "
                    + "JS: JavaScript is written.")
    public Language target;
    
    @Option(names = { "-v", "-version" },
            help = "Print version information and exit")
    public boolean version;
    
    @Option(names = { "-h", "-help"},
            help = "Show this help message and exit")
    public boolean help;
    
    @Option(names = "-sts",
            help = "Dump global symbol table structure")
    public boolean sts;
    
    @Option(names = "pjc-all",
            help = "Output messages about what the compiler is doing")
    public boolean pjc_all;
    
    @Option(names = "pjc-array-constructor",
            help = "Output messages when the compiler converts array types to canonical forms")
    public boolean array_constructor;
    
    @Option(names = "pjc-resolve-imports",
            help = "Output messages when the compiler resolve imported files")
    public boolean resolve_imports;
    
    @Option(names = "pjc-resolve-pkg-types",
            help = "Output messages when the compiler resolve packages")
    public boolean resolve_pkg_types;
    
    @Option(names = "pjc-name-checker",
            help = "Output messages when the compiler identifies and translates name symbols")
    public boolean name_checker;
    
    @Option(names = "pjc-top-decls",
            help = "Output messages when the compiler inserts all top-level declarations into "
                    + "symbol tables")
    public boolean top_decls;
    
    @Argument(metavar = "<FILE>",
              order = "0..*")
    public List<File> files;
    
    public static class VersionPrinter implements IVersionPrinter {
        @Override
        public String[] getVersionPrinter() throws Exception {
            return new String[] { "ProcessJ Version: 1.2",
                                  "JVM: " + Util.getCurrentJVM(),
                                  "Vendor: " + System.getProperty("java.vm.vendor"),
                                  "OS: " + System.getProperty("os.name")
            };
        }
    }
}
