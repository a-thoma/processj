# The ProcessJ Language

A new programming language being developed at the University of Nevada, Las Vegas.

### ProcessJ: Usage Help Message

```bash
$ pjc -help
```
```
                                %############                  
                           ###,     *(((*.     ###             
                        ##.  (##################   ##          
                     %#(  #########################. .##       
                   %#. (#######........,  ,.*####.. ##  ##     
                  ## *####   ./(#######  ###########  ## *#    
                 #, ##(  ############# /##############  #  #   
                #  ## .############### ################, #  #  
               #( ## (################  ################  #  # 
              %# ##  ################## (###############, ## ##
              #, ## .###################  #########, ###* ##  #
              #  ##  #####################.   ##########  ##/ #
              # .##. #########################     /#### #### #
              #  ### ###########((########### ####, ### ####/ #
              #* ###  #######. (###  #######  ###  ##, #####  #
              ## (### ######. ######( ###### .#, ##  (###### ##
               #( ###/        #######        (#,  ######### .# 
                #. #######################################  #  
                 #* ######## P R O C E S S   J ####(#####  #   
                  ## .######   v.1.2 - ALPHA   ####(###( (#    
                   ##* /##############################  ##     
                     ###  #########################  *##       
                        ##*  *#################(   ##          
                           ###(      ,*,.     *###             
                                %############                  

If called without options, the program may terminate. Use '-help' for a
list of possible commands and options

Usage: pjc [-about <arg>] [-clp] [-console-ansi-code=<flag>] [-g | -debug] 
           [-error-code=<number>] [-h | -help] [-I | -include <dir>] [-sts] [-t 
           | -target <language>] [-V | -verbose] [-v | -version] [-visit-all] 
           [-visit-array] [-visit-import] [-visit-name-checker] 
           [-visit-package-type] [-visit-top-decls] <file>

Parameters: 
  <file>                     The file (or files) to compile

Options: 
 -about <arg>                Provide additional information about a specific 
                             command or option
 -clp                        ProcessJ command-line processor and conventions
 -console-ansi-code=<flag>   Try and use color on terminals that support ANSI 
                             espace codes
 -g,-debug                   Generate all debugging info
 -error-code=<number>        What error code information do you want?
 -h,-help                    Show this help message and exit
 -I,-include <dir>           Override the default include directory which is 
                             set to be the Include subdirectory of the ProcessJ 
                             directory
 -sts                        Dump global symbol table structure
 -t,-target <language>       Specify the target language. C: C source code is 
                             written, compiled, and linked with the CSSP 
                             runtime; C++: C++ source code is generated and 
                             compiled into an executable; JVM: JVM class files 
                             are written and compiled; JS: JavaScript is 
                             written
 -V,-verbose                 Output messages of the exact sequence of commands 
                             used to compile a ProcessJ program
 -v,-version                 Print version information and exit
 -visit-all                  Generate all parse tree visitors (not default)
 -visit-array                Generate parse tree visitor only when converting 
                             array types to canonical forms
 -visit-import               Generate parse tree visitor when resolving 
                             imported files
 -visit-name-checker         Generate parse tree visitor when identifying and 
                             translating name symbols
 -visit-package-type         Generate parse tree visitor when resolving packages
 -visit-top-decls            Generate parse tree visitor when inserting all 
                             top-level declarations into symbol tables

Run 'pjc -about <arg>' for help with a specific command or option.

Full documentation at: https://processj.org
Bug reports, feedback, complains, love, food, etc, to matt.pedersen@unlv.edu
```

### TODOs

- [x] Complete Command-Line Processor (CLP)
- [ ] Test ResolveImports
- [ ] Test NameChecker
- [ ] Test ResolvePackagesTypes
- [ ] Test TopLevelDecls
- [ ] Complete Error Messages
- [ ] Complete Ascii color code
