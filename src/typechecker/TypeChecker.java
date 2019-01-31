/**
 *
 */
package typechecker;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import ast.AltCase;
import ast.ArrayAccessExpr;
import ast.ArrayLiteral;
import ast.ArrayType;
import ast.Assignment;
import ast.BinaryExpr;
import ast.BreakStat;
import ast.CastExpr;
import ast.ChannelEndExpr;
import ast.ChannelEndType;
import ast.ChannelReadExpr;
import ast.ChannelType;
import ast.ChannelWriteStat;
import ast.ConstantDecl;
import ast.DoStat;
import ast.ErrorType;
import ast.Expression;
import ast.ForStat;
import ast.IfStat;
import ast.Invocation;
import ast.LocalDecl;
import ast.Modifier;
import ast.Name;
import ast.NameExpr;
import ast.NamedType;
import ast.NewArray;
import ast.ParamDecl;
import ast.PrimitiveLiteral;
import ast.PrimitiveType;
import ast.ProcTypeDecl;
import ast.ProtocolCase;
import ast.ProtocolLiteral;
import ast.ProtocolTypeDecl;
import ast.RecordAccess;
import ast.RecordLiteral;
import ast.RecordMember;
import ast.RecordTypeDecl;
import ast.ReturnStat;
import ast.Sequence;
import ast.SuspendStat;
import ast.SwitchGroup;
import ast.SwitchLabel;
import ast.SwitchStat;
import ast.SyncStat;
import ast.Ternary;
import ast.TimeoutStat;
import ast.Type;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import ast.Var;
import ast.VarDecl;
import ast.WhileStat;
import utilities.Error;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * @author Matt Pedersen
 */
public class TypeChecker extends Visitor<Type> {
    
    public TypeChecker(SymbolTable topLevelDecls) { }
    public Type resolve(Type t) { return null; }
    @Override public Type visitAltCase(AltCase ac) { return null; }
    @Override public Type visitArrayAccessExpr(ArrayAccessExpr ae) { return null; }
    @Override public Type visitArrayLiteral(ArrayLiteral al) { return null; }
    @Override public Type visitArrayType(ArrayType at) { return null; }
    @Override public Type visitAssignment(Assignment as) { return null; }
    @Override public Type visitBinaryExpr(BinaryExpr be) { return null; }
    @Override public Type visitCastExpr(CastExpr ce) { return null; }
    @Override public Type visitChannelType(ChannelType ct) { return null; }
    @Override public Type visitChannelEndExpr(ChannelEndExpr ce) { return null; }
    @Override public Type visitChannelEndType(ChannelEndType ct) { return null; }
    @Override public Type visitChannelReadExpr(ChannelReadExpr cr) { return null; }
    @Override public Type visitChannelWriteStat(ChannelWriteStat cw) { return null; }
    @Override public Type visitDoStat(DoStat ds) { return null; }
    @Override public Type visitForStat(ForStat fs) { return null; }
    @Override public Type visitIfStat(IfStat is) { return null; }
    @Override public Type visitInvocation(Invocation in) { return null; }
    @Override public Type visitNamedType(NamedType nt) { return null; }
    @Override public Type visitNameExpr(NameExpr ne) { return null; }
    @Override public Type visitNewArray(NewArray ne) { return null; }
    @Override public Type visitPrimitiveLiteral(PrimitiveLiteral pl) { return null; }
    @Override public Type visitPrimitiveType(PrimitiveType pt) { return null; }
    @Override public Type visitProcTypeDecl(ProcTypeDecl pd) { return null; }
    @Override public Type visitProtocolLiteral(ProtocolLiteral pl) { return null; }
    @Override public Type visitProtocolTypeDecl(ProtocolTypeDecl pt) { return null; }
    @Override public Type visitRecordAccess(RecordAccess ra) { return null; }
    @Override public Type visitRecordLiteral(RecordLiteral rl) { return null; }
    @Override public Type visitRecordTypeDecl(RecordTypeDecl rt) { return null; }
    @Override public Type visitReturnStat(ReturnStat rs) { return null; }
    @Override public Type visitSuspendStat(SuspendStat ss) { return null; }
    @Override public Type visitSwitchStat(SwitchStat ss) { return null; }
    @Override public Type visitSyncStat(SyncStat ss) { return null; }
    @Override public Type visitTernary(Ternary te) { return null; }
    @Override public Type visitTimeoutStat(TimeoutStat ts) { return null; }
    @Override public Type visitUnaryPostExpr(UnaryPostExpr up) { return null; }
    @Override public Type visitUnaryPreExpr(UnaryPreExpr up) { return null; }
    @Override public Type visitVar(Var va) { return null; }
    @Override public Type visitWhileStat(WhileStat ws) { return null; }

}
