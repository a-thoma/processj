import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import clp.Argument;
import clp.IVersionPrinter;
import clp.Option;
import clp.Command;
import clp.Parameters;
import utilities.Language;

/**
 * ProcessJ command line.
 * 
 * @see {@link Language}
 * @author Ben Cisneros
 * @version 08/27/2018
 * @since 1.2
 */
@Parameters(header = {  "                  %############                  ",
                        "             ###,     *(((*.     ###             ",
                        "          ##.  (##################   ##          ",
                        "       %#(  #########################. .##       ",
                        "     %#. (#######........,  ,.*####.. ##  ##     ",
                        "    ## *####   ./(#######  ###########  ## *#    ",
                        "   #, ##(  ############# /##############  #  #   ",
                        "  #  ## .############### ################, #  #  ",
                        " #( ## (################  ################  #  # ",
                        "%# ##  ################## (###############, ## ##",
                        "#, ## .###################  #########, ###* ##  #",
                        "#  ##  #####################.   ##########  ##/ #",
                        "# .##. #########################     /#### #### #",
                        "#  ### ###########((########### ####, ### ####/ #",
                        "#* ###  #######. (###  #######  ###  ##, #####  #",
                        "## (### ######. ######( ###### .#, ##  (###### ##",
                        " #( ###/        #######        (#,  ######### .# ",
                        "  #. #######################################  #  ",
                        "   #* ######## P R O C E S S   J ####(#####  #   ",
                        "    ## .######   v.1.2 - ALPHA   ####(###( (#    ",
                        "     ##* /##############################  ##     ",
                        "       ###  #########################  *##       ",
                        "          ##*  *#################(   ##          ",
                        "             ###(      ,*,.     *###             ",
                        "                  %############                  "
                        },
            notes = {   "If called without options, the program may terminate. Use '-help' for a",
                        "list of possible commands and options"
                        },
            footer = {  "Run 'pjc -about <arg>' for help with a specific command or option.\n",
                        "Full documentation at: https://processj.org",
                        "Bug reports, feedback, complains, love, food, etc, to matt.pedersen@unlv.edu"
                        },
            name = "pjc",
            help = "The following options are available:",
            versionPrinter = PJMain.VersionPrinter.class)
public class PJMain extends Command {

    // TODO: This is for imports (libraries, files, etc), pragmas, etc..
    @Option(names = {"-V", "-verbose"},
            help = "Output messages of the exact sequence of commands used to compile a "
                   + "ProcessJ program")
    public boolean verbose;
    
    @Option(names = {"-g", "-debug"},
            help = "Generate all debugging info")
    public boolean debug;
    
    // TODO: defaultValue = "/opt/ProcessJ/include"
    @Option(names = {"-I", "-include"},
            metavar = "<dir>",
            defaultValue = "/Users/Ben/Desktop/processj/include", // default include for testing!
            help = "Override the default include directory which is set to be the Include "
                    + "subdirectory of the ProcessJ directory")
    public String include;
    
    @Option(names = {"-t", "-target"},
            defaultValue = "JVM",
            metavar = "<language>",
            help = "Specify the target language. C: C source code is written, compiled, "
                    + "and linked with the CSSP runtime; C++: C++ source code is generated "
                    + "and compiled into an executable; JVM: JVM class files are written "
                    + "and compiled; JS: JavaScript is written")
    public Language target;
    
    @Option(names = {"-v", "-version"},
            help = "Print version information and exit")
    public boolean version;
    
    @Option(names = {"-h", "-help"},
            help = "Show this help message and exit")
    public boolean help;
    
    @Option(names = "-sts",
            help = "Dump global symbol table structure")
    public boolean sts;
    
    @Option(names = "-visit-all",
            help = "Generate all parse tree visitors (not default)")
    public boolean visitorAll;
    
    @Option(names = "-visit-array",
            help = "Generate parse tree visitor only when converting array types to canonical "
                    + "forms")
    public boolean arrayVisitor;
    
    @Option(names = "-visit-import",
            help = "Generate parse tree visitor when resolving imported files")
    public boolean importVisitor;
    
    @Option(names = "-visit-package-type",
            help = "Generate parse tree visitor when resolving packages")
    public boolean packageVisitor;
    
    @Option(names = "-visit-name-checker",
            help = "Generate parse tree visitor when identifying and translating name symbols")
    public boolean nameCheckerVisitor;
    
    @Option(names = "-visit-type-checker",
            help = "Generate parse tree visitor when resolving names and types")
    public boolean typeCheckerVisitor;
    
    @Option(names = "-visit-top-decls",
            help = "Generate parse tree visitor when inserting all top-level declarations into "
                    + "symbol tables")
    public boolean topDeclsVisitor;
    
    @Option(names = "-about",
            help = "Provide additional information about a specific command or option",
            metavar = "<arg>")
    public String info;
    
    @Option(names = "-console-ansi-code",
            help = "Try and use color on terminals that support ANSI espace codes",
            metavar = "<flag>",
            split = "=")
    public boolean color;
    
    // TODO: Change type to Enum instead!
    @Option(names = "-error-code",
            help = "What error code information do you want?",
            metavar = "<number>",
            split = "=")
    public int errorCode;
    
    @Option(names = "-clp",
            help = "ProcessJ command-line processor and conventions")
    public boolean clp;
    
    @Option(names = "-logfile",
            help = "Use given file for log",
            metavar = "<file>")
    public String logFile;
    
    @Argument(metavar = "<file>",
              help = "The file (or files) to compile",
              order = "1..*")
    public List<File> files;
    
    public static class VersionPrinter extends Command implements IVersionPrinter {
        @Override
        public String[] getVersionPrinter() throws Exception {
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
            Date date = new Date();
            return new String[] { "ProcessJ version 1.2 compiled on " + dateFormat.format(date),
                                  "java version \"" + System.getProperty("java.version") + "\"",
                                  "Copyright(c) 2018" };
        }
    }
    
    public VersionPrinter getVersion() throws InstantiationException, IllegalAccessException {
        return (VersionPrinter) PJMain.class.getAnnotation(Parameters.class).versionPrinter().newInstance();
    }
}
