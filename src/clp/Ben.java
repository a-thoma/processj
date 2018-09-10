package clp;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import clp.OptionsBuilder;
import clp.Ben.Ben2;
import clp.Ben.FaceBook;
import clp.Ben.Hello;
import clp.parsers.BooleanParser;
import clp.parsers.EnumParser;
import clp.parsers.IntegerParser;
import clp.parsers.ListParser;
import clp.parsers.StringParser;

@Parameters(name = "Ben")
public class Ben extends OptionParameters {

//    @Option(names = { "N", "number" }, required = true, nvalues = 1, split = "=", handler = IntegerParser.class)
//    public int number;
    
    @Option(names = { "-b", "-ben" }, required = false, split = "=", handlers = IntegerParser.class)
    public int ben;
    
    @Option(names = { "-d", "-debug"}, required = false, arity = "1", split = "=", handlers = BooleanParser.class)
    public boolean debug = false;

//    @Option(names = { "-V", "-version" }, required = true, nvalues = 4, handlers = IntegerParser.class)
    @Option(names = { "-V", "-version" }, required = false, arity = "3..4")
    public List<Integer> numbers;
    
//    @Option(names = { "-a", "-aaa" }, required = true, nvalues = 1, handlers = ListParser.class)
    @Option(names = { "-a", "-aaa" }, required = false, arity = "1..*")
    public List<String> argss;
    
    @Option(names = "-map", required = false, arity = "1")
    public Map<String, Integer> mapTest;
    
    /////////////////
    @Option(names = "-numero", arity = "1", defaultValue = "UNO")
    NumberEnum numero;
    /////////////////
    
    ////
    //@Option(names = { "v", "version"}, required = true, nvalues = 1, parser = BooleanParser.class)
    @Argument(order = "0", defaultValue = "50")
    //public boolean version;
    public int num1;

    @Argument(order = "5")
    public boolean bla;

    @Argument(order = "1..4", defaultValue = "56")
    //public boolean version;
    public List<Integer> num2;
    ////

    //@Option(names = { "V", "version" }, required = true, nvalues = 4, parser = IntegerParser.class)
    public void setVersion(int number) {
    	//this.version = version;
    	this.numbers.add(number);
    }

//    public static class Benjamin extends Ben {
//
//        @Param(names = { "print" }, required = true, convertWith = IParamConverter.class)
//        private String print;
//    }
    
    public static class Ben2 extends Ben {
        @Option(names = { "-N", "-number" }, required = false, arity = "1", split = "=")
        public int number;
        
//        @SubParameters
//        public Hello twitter;
    }
    
    /////////////// SUBCOMMAND
    @Parameters(name = "twitter")
    public static class Hello extends OptionParameters {
        @Option(names = { "-twit" }, arity = "1", defaultValue = "hehe22!", handlers = StringParser.class)
        public String hello;
        
        @Argument(order = "0..3", defaultValue = "test0.txt", handler = StringParser.class)
        List<String> files;
    }
    
    @Parameters(name = "facebook")
    public static class FaceBook extends OptionParameters {
        @Option(names = "-account", arity = "3", defaultValue = "benjcisneros", handlers = StringParser.class, required = false)
        public List<String> accounts;
    }
    ///////////////
    
    //////////////////
    public static enum NumberEnum {
        ONE ("uno"), TWO ("dos"), THREE ("tress");
        
        final String text;
        NumberEnum(String text) { this.text = text; }
        public String toString() { return text; }
    }
    //////////////////
    
    public static void main(String[] args) {
        // TODO:
        Ben2 ben;
//        String[] arguments = "-b=34 -debug=1 -map jennie=24 -number=4 -aaa 14,23,3,4 -version 1 2 3 48 -- 10 34 7 23 67 yes".split(" ");
//        String[] arguments = "-b=34 --debug=1 --number=4 --map jennie=23 --aaa 14,23,3,4 --version 1 2 3 48".split(" ");
//        CliBuilder pj = new CliBuilder(Ben2.class);
//        pj.addCommand(Hello.class);
//        pj.addCommand(FaceBook.class);
//        pj.handlerArgs(arguments);
//        String[] arguments = "-d=1 -aaa 2 4 -version 23 45 45 6 -b=34 -map jennie=24 -number=4 twitter text1 text2 text3 text4".split(" ");
        String[] arguments = "-debug=1 -b=23 -aaa 2 4 -version 23 45 45 6 -map jennie=24 10 34 7 23 67 yes -number=4".split(" ");
//        String[] arguments = "-debug=1".split(" ");
        
        // TODO: Transform ClpBuilder => ClpBuilder<Ben> builder = new ClipBuilder<>();
//        ClpBuilder.OptionsBuilder optBuilder = ClpBuilder.newBuilder();
        OptionsBuilder optBuilder = new OptionsBuilder();
        optBuilder.addCommand(Ben2.class);
        optBuilder.addCommand(Hello.class);
        optBuilder.addCommand(FaceBook.class);
        optBuilder.handlerArgs(arguments);
        
//        CliParser parser = new CliParser(Ben.class, arguments);
        ben = optBuilder.getCommand(Ben2.class);
        Hello hello = optBuilder.getCommand(Hello.class);
        FaceBook face = optBuilder.getCommand(FaceBook.class);
        
        System.out.println(ben.ben);
        System.out.println(ben.debug);
        System.out.println(">> " + ben.number);
        System.out.println(ben.argss);
        System.out.println(ben.numbers);
        System.out.println("=> " + ben.mapTest);
        
        System.out.println(">>> " + hello.hello);
        System.out.println(">>> " + hello.files);
        System.out.println("accounts: " + face.accounts);

        System.out.println("args:");
        System.out.println(ben.num1);
        System.out.println(ben.num2);
        System.out.println(ben.bla);
    }
}
