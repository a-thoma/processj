package namechecker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import ast.AST;
import ast.Compilation;
import ast.Import;
import ast.Name;
import ast.Sequence;
import parser.parser;
import scanner.Scanner;
import utilities.Error;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

public class ResolveImports<T extends Object> extends Visitor<T> {
    // Symbol table associated with this file. Set in the constructor.
    private SymbolTable symtab;

    public static String currentFileName = Error.fileName;
    
    public ResolveImports(SymbolTable symtab) {
        this.symtab = symtab;
        Log.logHeader("==============================================================");
        Log.logHeader("*                  R E S O L V E   I M P O R T S             *");
        Log.logHeader("==============================================================");
    }
    
    public static String packageNameToString(Sequence<Name> packageName) {
        StringBuilder sb = new StringBuilder();
        Iterator<Name> it = packageName.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(".");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Imports (by scanning, parsing and tree building) one file.
     *
     * @param a
     *          An AST node - just used for line number information.
     * @param fileName
     *          The name of the file being imported.
     * @param importPath
     *          A fully qualified name that represents the path corresponding to a
     *          file or a directory {@code fileName}.
     * @return Returns a Compilation representing the imported file.
     */
    public static Compilation importFile(AST a, String fileName, String importPath) {
        Log.log(a.line + " Attempting to import: " + fileName);
        Compilation c = TopLevelDecls.alreadyImportedFiles.get(fileName);
        if (c != null) {
            Log.log(a.line + " Import of '" + fileName
                    + "' already done before!");
            return c;
        }
        try {
            Error.setPackageName(fileName);
            Log.log(a.line + " Starting import of file: `" + fileName + "'");
            Scanner s1 = new Scanner(new java.io.FileReader(fileName));
            parser p1 = new parser(s1);
            java_cup.runtime.Symbol r = p1.parse();
            
            // TODO: THIS WILL EXECUTE AND VALIDATE THE EXTENSION (PATH) OF IMPORTED
            // FILES!!
            
            // Checks the path of the imported file and compares the import statements found
            // in `fileName' against this file path format. An error is thrown if the import
            // statements do not match the path of the package name in which `fileName' exists
            String packageName = packageNameToString(((Compilation) r.value).packageName());
            String importPathDot = importPath.replaceAll(File.separator, "\\.");
            System.out.println("|-" + packageName + " && " + importPathDot);
            if (!importPathDot.equals(packageName)) {
                Error.error(a, "Invalid package name found! Path string `" + packageName
                        + "' may contain invalid characters or the path string does not "
                        + "match the import statement `" + importPathDot
                        + "'.");
            }
            
            TopLevelDecls.alreadyImportedFiles.put(fileName,
                    (Compilation) r.value);
            return (Compilation) r.value;
        } catch (java.io.FileNotFoundException e) {
            Error.error(a, "File not found : " + fileName, true, 2104);
        } catch (Exception e) {
            Error.error(a, "Something went wrong while trying to parse "
                    + fileName, true, 2105);
        }
        return null;
    }
    
    /**
     * Static class used for filtering files in imports (only the ones ending in
     * the proper extension will be considered) PJFiles takes a directory and a
     * filename and determines if it should be imported - importFileExtension is
     * ".pj" by default. This is used for importing files in an import statement
     * ending in *.
     */
    static class PJfiles implements FilenameFilter {
        public boolean accept(File dir, String name) {
            String[] result = name.split("\\.");
            return result[result.length - 1]
                    .equals(utilities.Settings.importFileExtension);
        }
    }

    /**
     * Given a directory, makeFileList creates an array list of Strings representing
     * the absolute paths of all the files in the directory and its sub-directories
     * that satisfy the filter in the PJFiles class.
     *
     * @param list
     *            After execution list will contain the list of file names in the
     *            directory given by the `directory' parameter.
     * @param directory
     *            The name of the directory from which to import files.
     */
    public static void makeFileList(ArrayList<String> list, String directory) {
        Log.log("makeFileList(): Called with : " + directory);
        // 'entries' will contain all the files in the directory 'directory' that has the right
        // file extension (typically .pj)
        String entries[] = new File(directory).list(new PJfiles());
        for (String s : entries) {
            File f = new File(directory + "/" + s);
            if (f.isFile()) {
                list.add(directory + "/" + s);
            }
        }
        // 'list' now contains all the appropriate files in 'directory' - now handle the subdirectories in order.
        entries = new File(directory).list();
        for (String s : entries) {
            File f = new File(directory + "/" + s);
            if (f.isDirectory())
                makeFileList(list, directory + "/" + s);
        }
    }
    
    String makeImportPath(Import im) {
        String path = "";
        if (im.path() != null) {
            int i = 0;
            for (Name n : im.path()) {
                path = path + n.getname();
                if (i < im.path().size() - 1)
                    path = path + "/";
                i++;
            }
        }
        return path;
    }

    /**
     * visitImport will read and parse an import statement. The chain of symbol tables
     * will be left in the `symtab' field. The parentage of multiple files imported in
     * the same import is also through the parent link.
     */
    public T visitImport(Import im) {
        Log.log(im.line + ": Visiting an import (of file: " + im + ")");
        // An import is first tried in the local director
        // then in the include directory - unless it is of the form 'f' then it must be local.
        // Make the path for this import
        String path = makeImportPath(im);

        Log.log("visitImport(): Package path is : " + path);
        Log.log("visitImport(): Package file name is : " + im.file().getname());
        
        // Try local first
        String fileName = new File("").getAbsolutePath() + "/" + path;

        // 'fileList' will hold a list of files found in wildcard imports (.*)
        ArrayList<String> fileList = new ArrayList<String>();

        if (im.importAll()) { // a .* import
            // Is it a local directory?
            if ((new File(fileName).isDirectory())) {
                // Yes, so add it's content to the fileList
                makeFileList(fileList, fileName);
            } else {
                // It was not a local directory, but see if it is a library directory
                fileName = new File(utilities.Settings.includeDir)
                        .getAbsolutePath() + "/" + utilities.Settings.targetLanguage + "/" + path;
                Log.log("visitImport(): Not a local, so try a library: " + fileName);
                if (new File(fileName).isDirectory()) {
                    // Yes, it was, so add it's content to the fileList
                    makeFileList(fileList, fileName);
                } else {
                    // Oh no, the directory wasn't found at all!
                    String packageName = path.replaceAll("/", ".");
                    packageName = packageName.substring(0, packageName.length() - 1);
                    Error.error(im, " Package '" + packageName + "' does not exist.", false, 2106);
                }
            }
            Log.log("visitImport(): About to import `" + im.file().getname() + ".pj'");
        } else { // Not a .* import
            fileName = fileName + "/" + im.file().getname() + ".pj";
            Error.setPackageName(path + "." + im.file().getname());

            // Is it a local file
            if (new File(fileName).isFile()) {
                // Yes, so add it to the fileList
                fileList.add(fileName);
            } else {
                // No, so look in the library
                fileName = new File(utilities.Settings.includeDir)
                        .getAbsolutePath() + "/" + utilities.Settings.targetLanguage
                        + "/" + path + (path.equals("") ? "" : "/") + im.file().getname() + ".pj";
                Log.log("visitImport(): Not a local so try a library: " + fileName);
                // But only if it isn't of the form 'import f' cause they can only be local!
                if (!path.equals("") && new File(fileName).isFile()) {
                    fileList.add(fileName);
                } else {
                    // Nope, nothing found!
                    if (path.equals("")) {
                        Error.error(im, "File '" + im.file().getname() + "' not found.", false, 2107);
                    } else {
                        String packageName = path.replaceAll("/", ".");
                        packageName = packageName.substring(0, packageName.length() - 1);
                        Error.error(im, "File '" + im.file().getname() + "' not found in package '" + path + "'.", false, 2108);
                    }
                }
            }
        }

        // 'fileList' now contains the list of all the files that this import caused to be imported
        for (String fn : fileList) {
            // Scan, parse and build tree.
            String oldCurrentFileName = currentFileName;
            currentFileName = fn;
            Error.setFileName(fn);
            Compilation c = ResolveImports.importFile(im, fn, path /* packageName */);

            // Add it to the list of compilations for this import
            im.addCompilation(c);
            // Create a symboltable for it
            SymbolTable importSymtab = new SymbolTable("Import: " + fn);
            // Declare types and constants for handling it's imports
            c.visit(new TopLevelDecls<AST>(importSymtab));
            currentFileName = oldCurrentFileName;
            Error.setFileName(oldCurrentFileName);

            // Insert into the symtab chain along the parent link
            if (symtab == null)
                symtab = importSymtab;
            else {
                importSymtab.setParent(symtab);
                symtab = importSymtab;
            }
        }
        
        return null;
    }
}
