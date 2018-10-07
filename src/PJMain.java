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
            help = "Output messages of the exact sequence of commands used to compile a "
                    + "ProcessJ program")
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
    
    @Option(names = "-visitor-all",
            help = "Generate all parse tree visitors (not default)")
    public boolean visitorAll;
    
    @Option(names = "-array-visitor",
            help = "Generate parse tree visitor only when converting array types to canonical "
                    + "forms")
    public boolean arrayVisitor;
    
    @Option(names = "-import-visitor",
            help = "Generate parse tree visitor when resolving imported files")
    public boolean importVisitor;
    
    @Option(names = "-package-type-visitor",
            help = "Generate parse tree visitor when resolving packages")
    public boolean packageVisitor;
    
    @Option(names = "-name-checker-visitor",
            help = "Generate parse tree visitor when identifing and translating name symbols")
    public boolean nameCheckerVisitor;
    
    @Option(names = "-top-decls-visitor",
            help = "Generate parse tree visitor when inserting all top-level declarations into "
                    + "symbol tables")
    public boolean topDeclsVisitor;
    
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
