package rewriters;

import ast.ArrayType;
import ast.Literal;
import ast.LocalDecl;
import utilities.Log;
import utilities.Visitor;

public class ArraysRewrite extends Visitor<Object> {
	
	public ArraysRewrite() {
        Log.logHeader("****************************************");
        Log.logHeader("       A R R A Y S   R E W R I T E      ");
        Log.logHeader("****************************************");
	}
	
	@Override
	public Object visitLocalDecl(LocalDecl ld) {
		Log.log(ld, "Attempting to rewrite array");
		
		if (ld.type().isArrayType() && ld.var().init() != null && ld.var().init() instanceof Literal) {
			int depth = ((ArrayType) ld.type()).getDepth();
			// Ask for dimsExpr and dims
		}
		
		return null;
	}
}
