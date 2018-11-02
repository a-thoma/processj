package parallel_usage_check;

import java.util.Hashtable;

import ast.AST;
import ast.AltStat;
import ast.ArrayAccessExpr;
import ast.Assignment;
import ast.NameExpr;
import ast.ParBlock;
import ast.RecordAccess;
import ast.UnaryPostExpr;
import ast.UnaryPreExpr;
import utilities.Error;
import utilities.PJMessage;
import utilities.CompilerMessageManager;
import utilities.Log;
import utilities.Visitor;
import utilities.VisitorMessageNumber;

public class ParallelUsageCheck extends Visitor<Object> {

    private Hashtable<String, AST> readSet;
    private Hashtable<String, AST> writeSet;

    private boolean inPar = false;

    public ParallelUsageCheck() {
        Log.logHeader("********************************************");
        Log.logHeader("* P A R A L L E L   U S A G E    C H E C K *");
        Log.logHeader("********************************************");
    }

    public Object visitParBlock(ParBlock pb) {
        Log.log(pb, "Visiting a Par Block.");
        boolean oldInPar = inPar;
        inPar = true;

        // if this is an outermost par block - create new tables.
        if (!oldInPar) {
            Log.log(pb, "Outermost par - creating new read and wrilte sets.");
            readSet = new Hashtable<String, AST>();
            writeSet = new Hashtable<String, AST>();
        }

        super.visitParBlock(pb);

        inPar = oldInPar;
        return null;
    }

    public Object visitRecordAccess(RecordAccess ra) {
        if (inPar) {
            Log.log(ra, "Visiting a RecordAccess.");
            String name = ra.toString();
            if (writeSet.containsKey(name)) {
//                Error.error(ra, "Parallel read and write access to record member '"
//                        + name + "' illegal.", false, 5100);
                CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                            .addAST(ra)
                            .addFileName(CompilerMessageManager.INSTANCE.fileName)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_700)
                            .addArguments(name)
                            .build());
            } else {
                Log.log(ra, "RecordAccess: '" + name + "' is added to the read set.");
                readSet.put(name, ra);
            }
        }
        return null;
    }

    public Object visitArrayAccessExpr(ArrayAccessExpr aae) {
        if (inPar) {
            Log.log(aae, "Visiting a ArrayAccessExpr.");
            String name = aae.toString();
            if (writeSet.containsKey(name)) {
//                Error.error(aae, "Parallel read and write access to array member '"
//                        + name + "' illegal.", false, 5101);
                CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                            .addAST(aae)
                            .addFileName(CompilerMessageManager.INSTANCE.fileName)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_701)
                            .addArguments(name)
                            .build());
            } else {
                Log.log(aae, "ArrayAccessExpr: '" + name + "' is added to the read set.");
                readSet.put(name, aae);
//                Error.warning(aae, "Parallel usage checking is not fully implemented for array access.", 5102);
                CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                            .addAST(aae)
                            .addFileName(CompilerMessageManager.INSTANCE.fileName)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_701)
                            .addArguments(name)
                            .build());
                aae.index().visit(this);
            }
        }
        return null;
    }

    public Object visitAltStat(AltStat as) {
        /* TODO:

           alt {
             x = c.read() : { x = 1; }
               }
           causes issues!
         */
        Log.log(as, "AltStat ignore in parallel usage checking.");
        return null;
    }

    public Object visitAssignment(Assignment as) {
        if (inPar) {
            Log.log(as, "Visiting an Assignment.");
            // the left hand side must go into the read set!
            // can be NameExpr, ArrayAccessExpr, or RecordAccess
            if (as.left() instanceof NameExpr) {
                String name = ((NameExpr) as.left()).name().getname();
                if (writeSet.containsKey(name))
//                    Error.error(as, "Parallel write access to variable '" + name + "' illegal.", false, 5103);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(as)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_703)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "NameExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
                }
            } else if (as.left() instanceof RecordAccess) {
                // TODO: the toString() of as.left() if probably not complete
                String name = as.left().toString();
                if (writeSet.containsKey(name))
//                    Error.error(as, "Parallel write access to record member '" + name + "' illegal.", false, 5104);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(as)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_704)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "RecordAccess: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
                }
            } else if (as.left() instanceof ArrayAccessExpr) {
                // TODO: the toString() of as.left() is probably not complete!
                String name = as.left().toString();
                if (writeSet.containsKey(name))
//                    Error.error(as, "Parallel write access to array member '" + name + "' illegal.", false, 5105);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(as)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_705)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(as, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, as.left());
//                    Error.warning(as.left(), "Parallel usage checking is not fully implemented for array access.", 5106);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(as.left())
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_706)
                                .addArguments(name)
                                .build());
                }
            }
        }
        return null;
    }

    public Object visitNameExpr(NameExpr ne) {
        if (inPar) {
            Log.log(ne, "Visiting a NameExpr.");
            // This should only be reads!
            String name = ne.name().getname();
            if (writeSet.containsKey(name))
//                Error.error(ne, "Parallel read and write access to variable '" + name + "' illegal.", false, 5107);
                CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                            .addAST(ne)
                            .addFileName(CompilerMessageManager.INSTANCE.fileName)
                            .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_707)
                            .addArguments(name)
                            .build());
            else {
                Log.log(ne, "NameExpr: '" + name + "' is added to the read set.");
                readSet.put(name, ne);
            }
        }
        return null;
    }

    public Object visitUnaryPostExpr(UnaryPostExpr up) {
        if (inPar) {
            Log.log(up, "Visiting a UnaryPostExpr.");
            if (up.expr() instanceof NameExpr) {
                String name = ((NameExpr) up.expr()).name().getname();
                if (writeSet.containsKey(name))
//                    Error.error(up, "Parallel write access to variable '" + name + "' illegal.", false, 5108);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(up)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_708)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "NameExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
                }
            } else if (up.expr() instanceof RecordAccess) {
                // TODO: the toString() of up.expr() if probably not complete
                String name = up.expr().toString();
                if (writeSet.containsKey(name))
//                    Error.error(up, "Parallel write access to record member '" + name + "' illegal.", false, 5109);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(up)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_709)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "RecordAccess: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
                }
            } else if (up.expr() instanceof ArrayAccessExpr) {
                // TODO: the toString() of up.expr() is probably not complete!
                String name = up.expr().toString();
                if (writeSet.containsKey(name))
//                    Error.error(up, "Parallel write access to array member '" + name + "' illegal.", false, 5110);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(up)
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_710)
                                .addArguments(name)
                                .build());
                else {
                    Log.log(up, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                    writeSet.put(name, up.expr());
//                    Error.warning(up.expr(), "Parallel usage checking is not fully implemented for array access.", 5111);
                    CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                .addAST(up.expr())
                                .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_711)
                                .addArguments(name)
                                .build());
                }
            }
        }
        return null;
    }

    public Object visitUnaryPreExpr(UnaryPreExpr up) {
        if (inPar) {
            Log.log(up, "Visiting a UnaryPreExpr.");
            if (up.op() == UnaryPreExpr.PLUSPLUS
                    || up.op() == UnaryPreExpr.MINUSMINUS) {
                if (up.expr() instanceof NameExpr) {
                    String name = ((NameExpr) up.expr()).name().getname();
                    if (writeSet.containsKey(name))
//                        Error.error(up, "Parallel write access to variable '" + name + "' illegal.", false, 5112);
                        CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                    .addAST(up)
                                    .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_712)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "NameExpr: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
                    }
                } else if (up.expr() instanceof RecordAccess) {
                    // TODO: the toString() of up.expr() if probably not complete
                    String name = up.expr().toString();
                    if (writeSet.containsKey(name))
//                        Error.error(up, "Parallel write access to record member '" + name + "' illegal.", false, 5113);
                        CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                    .addAST(up)
                                    .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_713)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "RecordAccess: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
                    }
                } else if (up.expr() instanceof ArrayAccessExpr) {
                    // TODO: the toString() of up.expr() is probably not complete!
                    String name = up.expr().toString();
                    if (writeSet.containsKey(name))
//                        Error.error(up, "Parallel write access to array member '" + name + "' illegal.", false, 5114);
                        CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                    .addAST(up)
                                    .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_714)
                                    .addArguments(name)
                                    .build());
                    else {
                        Log.log(up, "ArrayAccessExpr: '" + name + "' is added to the write set.");
                        writeSet.put(name, up.expr());
//                        Error.warning(up.expr(), "Parallel usage checking is not fully implemented for array access.", 5115);
                        CompilerMessageManager.INSTANCE.printAndContinue(new PJMessage.Builder()
                                    .addAST(up.expr())
                                    .addFileName(CompilerMessageManager.INSTANCE.fileName)
                                    .addError(VisitorMessageNumber.PARALLEL_USAGE_CHECK_715)
                                    .addArguments(name)
                                    .build());
                    }
                }
            } else
                up.expr().visit(this);
        }
        return null;
    }

}