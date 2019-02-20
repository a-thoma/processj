package ast;

import utilities.Log;

public abstract class Type extends AST {

    public Type() {
        // must only be called from ErrorType
        super();
    }

    public Type(AST a) {
        super(a);
    }

    public Type(Token t) {
        super(t);
    }

    public abstract String typeName();

    /*
     * public boolean DELETE_identical(Type other) { if
     * (signature().equals(other.signature())) return true; return false; }
     */
    public boolean assignable() {
        return (!typeName().equals("null") && !typeName().equals("void"));
    }
    /*
     * public static boolean DELETE_assignmentCompatible(Type var, Type val) {
     * 
     * Log.log("Type: var: " + var); Log.log("Type: val: " + val);
     * 
     * //System.out.println("AC: var: "+var); //System.out.println("AC: val: "+val);
     * 
     * if (var.identical(val)) {// Same type return true; } else if
     * (var.isNumericType() && val.isNumericType()) { // Both are numeric
     * (primitive) types. PrimitiveType pvar = (PrimitiveType) var; PrimitiveType
     * pval = (PrimitiveType) val;
     * 
     * // double :> float :> long :> int :> short :> byte if (pvar.getKind() ==
     * PrimitiveType.CharKind) return false; // do not allow assignment of numeric
     * values to chars if (pval.getKind() == PrimitiveType.CharKind) return
     * (pvar.getKind() != PrimitiveType.ByteKind && pvar .getKind() !=
     * PrimitiveType.ShortKind); return (pvar.getKind() >= pval.getKind()); // ok to
     * assign char value to none byte/short var } else if (var.isProtocolType() &&
     * val.isProtocolType()) { // if P2 extends P1 and P0 then a variable of type P0
     * and P1 may hold a reference to a value of type P2. // check if P0 and P1 are
     * implemented/extended by P2 return protocolExtends((ProtocolTypeDecl) var,
     * (ProtocolTypeDecl) val); } else if (var.isChannelEndType() &&
     * val.isChannelEndType()) { ChannelEndType var_end = (ChannelEndType) var;
     * ChannelEndType val_end = (ChannelEndType) val; if
     * (var_end.baseType().isNamedType() && val_end.baseType().isNamedType()) { Type
     * var_type = ((NamedType) var_end.baseType()).type(); Type val_type =
     * ((NamedType) val_end.baseType()).type();
     * 
     * if (var_type.isProtocolType() && val_type.isProtocolType()) return
     * assignmentCompatible((ProtocolTypeDecl) var_type, (ProtocolTypeDecl)
     * val_type); else return var_type.identical(val_type); } } return false; }
     */
    // Each type must re-implement these

    public abstract String signature();

    public abstract boolean typeEqual(Type other);

    public abstract boolean typeEquivalent(Type other);

    public abstract boolean typeAssignmentCompatible(Type other);

    // Not sure why this one is here - shuld be in protocolType, shouldn't it?

    public boolean protocolExtends(ProtocolTypeDecl sup, ProtocolTypeDecl sub) {
        if (sup.typeEqual(sub))
            return true;
        else {
            boolean b = false;
            for (int i = 0; i < sub.extend().size(); i++) {
                b = b || protocolExtends(sup, (ProtocolTypeDecl) sub.extend().child(i).myDecl);
            }
            return b;
        }
    }

    // The general implementation of all these should be to return 'false'.
    // Each type should in turn implement which ever one applies to it

    // Reimplemented in PrimitiveType
    public boolean isIntegerType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.IntKind);
        return false;
    }

    public boolean isErrorType() {
        // return (this instanceof ErrorType);
        return false;
    }

    public boolean isExternType() {
        return false;
    }

    public boolean isArrayType() {
        // return (this instanceof ArrayType);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isBooleanType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.BooleanKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isByteType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.ByteKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isShortType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.ShortKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isCharType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.CharKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isLongType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.LongKind);
        return false;
    }

    public boolean isTimerType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.TimerKind);
        return false;
    }

    public boolean isBarrierType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.BarrierKind);
        return false;
    }

    public boolean isChannelType() {
        // return (this instanceof ChannelType);
        return false;
    }

    public boolean isChannelEndType() {
        // return (this instanceof ChannelEndType);
        return false;
    }

    public boolean isRecordType() {
        // return (this instanceof RecordTypeDecl);
        return false;
    }

    public boolean isProtocolType() {
        // return (this instanceof ProtocolTypeDecl);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isVoidType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.VoidKind);
        return false;
    }

    // TODO: Do we need this?? Probably ....
    public boolean isNullType() {
        // return (this instanceof NullType);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isStringType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.StringKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isFloatType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.FloatKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isDoubleType() {
        // return (this instanceof PrimitiveType && ((PrimitiveType) this)
        // .getKind() == PrimitiveType.DoubleKind);
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isNumericType() {
        // return (isFloatType() || isDoubleType() || isIntegralType());
        return false;
    }

    // Reimplemented in PrimitiveType
    public boolean isIntegralType() {
        // return (isIntegerType() || isShortType() || isByteType()
        // || isCharType() || isLongType());
        return false;
    }

    // reimplemented in PrimitiveType
    public boolean isPrimitiveType() {
        // return isNumericType() || isVoidType() || isNullType()
        // || isStringType() || isBooleanType();
        return false;
    }

    public boolean isNamedType() {
        // return (this instanceof NamedType);
        return false;
    }

    public boolean isProcType() {
        return false;
    }

    public abstract String defaultType();
}
