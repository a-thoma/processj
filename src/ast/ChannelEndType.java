package ast;

import utilities.Visitor;

public class ChannelEndType extends Type {

    public static final int SHARED = 0;
    public static final int NOT_SHARED = 1;

    public static final int READ_END = 0;
    public static final int WRITE_END = 1;
    public static final int byteSizeC = 4; // 32-bit pointer.

    private int shared;
    private int end;

    public ChannelEndType(int shared, Type baseType, int end) {
        super(baseType);
        this.shared = shared;
        this.end = end;
        nchildren = 1;
        children = new AST[] { baseType };
    }

    public String typeName() {
        return "chan<" + baseType() + ">." + (isRead() ? "read" : "write");
    }

    // TODO: perhaps the base type of a channel end type ought to be a channel ;->

    public int byteSizeC() {
        return byteSizeC;
    }

    public String signature() {
        return "{" + baseType().signature() + ";" + (isRead() ? "?" : "!");
    }

    public String toString() {
        return typeName();
    }

    public boolean isShared() {
        return shared == SHARED;
    }

    public boolean isRead() {
        return end == READ_END;
    }

    public boolean isWrite() {
        return end == WRITE_END;
    }

    public Type baseType() {
        return (Type) children[0];
    }

    public boolean isChannelEndType() {
        return true;
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitChannelEndType(this);
    }

    @Override
    public boolean typeEqual(Type t) {
        return false;
    }

    @Override
    public boolean typeEquivalent(Type t) {
        return false;
    }

    @Override
    public boolean typeAssignmentCompatible(Type t) {
        if (!t.isChannelEndType())
            return false;
        else {
            ChannelEndType cet = (ChannelEndType) t;
            return baseType().typeAssignmentCompatible(cet.baseType());
        }
    }
}