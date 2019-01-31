package ast;

import utilities.Visitor;

public class ChannelEndType extends Type {

    public static final int SHARED = 0;
    public static final int NOT_SHARED = 1;

    public static final int READ_END = 0;
    public static final int WRITE_END = 1;
    public static final int byteSizeC = 4; //32-bit pointer.

    private int shared;
    private int end;

    public ChannelEndType(int shared, ChannelType channelType, int end) {
        super(channelType);
        this.shared = shared;
        this.end = end;
        nchildren = 1;
        children = new AST[] { channelType };
    }

    public String typeName() {
        return "chan<" + channel().baseType() + ">." + (isRead() ? "read" : "write");
    }

    // TODO: perhaps the base type of a channel end type ought to be a channel ;->

    public int byteSizeC() {
        return byteSizeC;
    }

    public String signature() {
        return "{" + channel().baseType().signature() + ";" + (isRead() ? "?" : "!");
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

    public ChannelType channel() {
	return (ChannelType)children[0];
    }

    public Type baseType() {
        return channel().baseType();
    }

    public <S extends Object> S visit(Visitor<S> v) {
        return v.visitChannelEndType(this);
    }

   
    // ********************
    // Type Related Stuff
    // ********************

    @Override public boolean isChannelEndType() {
	return true;
    }

    // α = ChannelEnd(δ, end1) ∧ β = ChannelEnd(γ, end2)
    // α =T β ⇔ ChannelEnd?(α) ∧ ChannelEnd?(β) ∧ Channel?(δ) ∧ Channel?(γ) ∧ (end1 = end2)
    @Override public boolean typeEqual(Type t) {
	// ChannelEnd?(α) ∧ ChannelEnd?(β)
	if (!t.isChannelEndType())
	    return false;
	ChannelEndType other = (ChannelEndType)t; 
	// (end1 = end2) 
	if (this.end != other.end)
	    return false;
	// Channel?(δ) =T Channel?(γ)
	// TODO: This should operate on channels
	return baseType().typeEqual(other.baseType());
    }

    // α∼T β ⇔ α=T β
    @Override public boolean typeEquivalent(Type t) {
	return this.typeEqual(t);
    }

    // if α = ChannelEnd(δ, end1) ∧ β = ChannelEnd(γ, end2)
    // α :=T β ⇔ (end1 = end2) ∧
    // ((Protocol?(δ) ∧ Protocol?(γ) ∧ (δ ≤T γ)) ∨ (¬Protocol?(δ) ∧ ¬Protocol?(γ) ∧ (δ =T γ))
    @Override public boolean typeAssignmentCompatible(Type t) {
	// TODO
        return false;
    }
}