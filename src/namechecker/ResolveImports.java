package namechecker;

import java.io.File;
import java.util.Iterator;

import ast.AST;
import ast.Compilation;
import ast.Name;
import ast.Sequence;
import parser.parser;
import scanner.Scanner;
import utilities.Error;
import utilities.Log;

public class ResolveImports {
    
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
     * @return Returns a Compilation representing the scanned file.
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
            
            // Check that the package name of the imported `file name' matches the
            // import statements found in given `file name'. Otherwise, throw an error
            String packageName = packageNameToString(((Compilation) r.value).packageName());
            String importPathDot = importPath.replaceAll(File.separator, "\\.");
            System.out.println("Validating File Name: `" + importPathDot + "' and Package Name: `" + packageName + "'");
            if (importPathDot.equals(packageName))
                System.out.println(">: " + importPathDot);
            else
                System.out.println(">:" + importPathDot + " | " + packageName + " (" + importPathDot.equals(packageName) + ")");
            if (!importPathDot.equals(packageName)) {
                System.err.println("NOT THE SAME!"); System.exit(1);
//                Error.error(a, "Invalid package name found! Path string `" + packageName
//                        + "' may contain invalid characters or the path string does not "
//                        + "match the import statement `" + importPathDot
//                        + "'.");
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
}
