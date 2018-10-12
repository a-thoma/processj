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
                 #* ######   P R O C E S S   J   ##(#####  #   
                  ## .####    {v.1.2 # alpha}    ##(###( (#    
                   ##* /##############################  ##     
                     ###  #########################  *##       
                        ##*  *#################(   ##          
                           ###(      ,*,.     *###             

If called without options, the program may terminate. Use "-help" for a
list of possible commands and options

Usage: [-array-visitor] [-g|-debug] [-error-code=<number>] [-h|-help] 
       [-import-visitor] [-I|-include <DIR>] [-name-checker-visitor] 
       [-package-type-visitor] [-sts] [-t|-target <LANGUAGE>] 
       [-top-decls-visitor] [-V|-verbose] [-v|-version] [-visitor-all] <FILE>

Parameters: 
  <FILE>                 The file (or files) to compile

Options: 
 -array-visitor          Generate parse tree visitor only when converting array 
                         types to canonical forms
 -g,-debug               Generate all debugging info
 -error-code=<number>    What error code information do you want?
 -h,-help                Show this help message and exit
 -import-visitor         Generate parse tree visitor when resolving imported 
                         files
 -I,-include <DIR>       Override the default include directory which is set to 
                         be the Include subdirectory of the ProcessJ directory
 -name-checker-visitor   Generate parse tree visitor when identifying and 
                         translating name symbols
 -package-type-visitor   Generate parse tree visitor when resolving packages
 -sts                    Dump global symbol table structure
 -t,-target <LANGUAGE>   Specify the target language. C: C source code is 
                         written, compiled, and linked with the CSSP runtime; 
                         C++: C++ source code is generated and compiled into an 
                         executable; JVM: JVM class files are written; JS: 
                         JavaScript is written
 -top-decls-visitor      Generate parse tree visitor when inserting all 
                         top-level declarations into symbol tables
 -V,-verbose             Output messages of the exact sequence of commands used 
                         to compile a ProcessJ program
 -v,-version             Print version information and exit
 -visitor-all            Generate all parse tree visitors (not default)

Full documentation at: https://processj.org
Bug reports, feedback, complains, love, food, etc, to matt.pedersen@unlv.edu
```

### TODOs

- [ ] Mercury
- [x] Venus
- [x] Earth (Orbit/Moon)
- [x] Mars
- [ ] Jupiter
- [ ] Saturn
- [ ] Uranus
- [ ] Neptune
- [ ] Comet Haley
