package rewriters;

import ast.*;
import utilities.Log;
import utilities.Visitor;

public class IOCallsRewrite extends Visitor<AST> {

	public IOCallsRewrite() {
		Log.logHeader("****************************************");
		Log.logHeader("*   I O   C A L L S   R E W R I T E    *");
		Log.logHeader("****************************************");
	}

	@Override
	public AST visitInvocation(Invocation in) {
		Log.log(in, "Attempting to rewrite invocation of " + in.toString());

		if (in.targetProc != null) {
			Log.log(in,"targetProc is " + in.targetProc.name());
			if ( in.targetProc.filename != null                    &&
				 in.targetProc.filename.equals("io")               &&
				(in.targetProc.name().toString().equals("println") ||
			     in.targetProc.name().toString().equals("print"))) {
				Log.log("This is the function we're looking for");
			} else {
				return in;
			}
		}

		if (in.params() != null) {
			boolean rewritten = false;
			Sequence<Expression> params = in.params();
			Sequence<Expression> newParams = new Sequence<Expression>();
			Log.log(in, "Invocation of " + in.toString() + " has argument(s):");
			for (int i = 0; i < params.size(); ++i) {
				if (params.child(i) instanceof BinaryExpr) {
					Log.log(in, params.child(i).toString());
					if (checkForString((BinaryExpr)params.child(i)) == true) {
						rewritten = true;
						Log.log(in, "string concatenation found, rewriting.");
						newParams.merge(this.extractParams((BinaryExpr)params.child(i)));	
					}
				}
			}

			Log.log(in, "Received params from extractParams():");
			for (int i = 0; i < newParams.size(); ++i) {
				Log.log(in, newParams.child(i).toString());
			}

			// TODO: is this appropriate? is there another way?
			if(rewritten == true) {
				in.children[2] = newParams;	
			}
		}

		return null;
	}

	public Sequence<Expression> extractParams(BinaryExpr be) {
		Log.log(be, "Rewriting binary expression " + be.toString() + " to use , instead of +");

		Sequence<Expression> newParams = new Sequence<Expression>();
		if (be.left() instanceof BinaryExpr) {
			newParams.merge(extractParams((BinaryExpr)be.left()));
		} else {
			newParams.append(be.left());
		}

		if (be.right() instanceof BinaryExpr) {
			newParams.merge(extractParams((BinaryExpr)be.right()));
		} else {
			newParams.append(be.right());
		}

		Log.log(be, "Got new params:");
		for (int i = 0; i < newParams.size(); ++i) {
			Log.log(be, newParams.child(i).toString());
		}
		return newParams;
	}

	public boolean checkForString(BinaryExpr be) {
		Log.log(be, "Checking for string concatenation versus addition");

		// Check if the lhs is a string
		if (be.left() instanceof PrimitiveLiteral) {
			if(((PrimitiveLiteral)be.left()).getKind() == PrimitiveLiteral.StringKind) {
				return true;
			}
		}
		// Now the rhs
		if (be.right() instanceof PrimitiveLiteral) {
			if(((PrimitiveLiteral)be.right()).getKind() == PrimitiveLiteral.StringKind) {
				return true;
			}
		}

		// recursively check on left and right
		boolean left = false;
		boolean right = false;

		if (be.left() instanceof BinaryExpr) {
			left = checkForString((BinaryExpr)be.left());
		}

		if(be.right() instanceof BinaryExpr) {
			right = checkForString((BinaryExpr)be.right());
		}

		return false | left | right;
	}
}