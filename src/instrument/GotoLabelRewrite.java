package instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import utilities.Log;

/**
 * @author ben
 */
public class GotoLabelRewrite {
    /* Invocations to rewrite */
    public static final String LABEL = "LABEL";
    public static final String GOTO = "GOTO";
    /* Map of GOTOs and LABELs instructions */
    HashMap<Integer, LabelNode> d_map = new HashMap<Integer, LabelNode>();
    /* Path for generated class files */
    private String d_path;
    
    public static boolean checkPath(Path path, boolean isFile) {
        if (path == null || !Files.exists(path))
            return false;
        return isFile ? Files.exists(path) : Files.isDirectory(path);
    }
    
    public static void exit(String msg, int code) {
        System.out.println(msg);
        System.exit(code);
    }
    
    public GotoLabelRewrite(String path) {
        d_path = path.isEmpty() ? "" : path;
        Log.doLog = false; /* Change this to 'true' for debugging */
        Log.log("======================================");
        Log.log(" Rewriting goto and labels..");
        Log.log(" Path: " + d_path);
        Log.log("======================================");
    }
    
    private int getOpcode(int label) {
        int operand = Opcodes.ICONST_M1;
        switch (label) {
        case Opcodes.ICONST_0: operand = 0; break;
        case Opcodes.ICONST_1: operand = 1; break;
        case Opcodes.ICONST_2: operand = 2; break;
        case Opcodes.ICONST_3: operand = 3; break;
        case Opcodes.ICONST_4: operand = 4; break;
        case Opcodes.ICONST_5: operand = 5; break;
        }
        return operand;
    }
    
    private void addGotoInsn(MethodNode mn) {
        InsnList insList = mn.instructions;
        if (insList.size() == 0)
            return;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> it = insList.iterator();
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();
            if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mNode = (MethodInsnNode) node;
                if (GOTO.equals(mNode.name)) {
                    Log.log("Found goto/" + mNode.desc);
                    /* Find the correct constant value on the operand stack
                     * that belongs to this 'goto' label */
                    AbstractInsnNode prevIns = mNode.getPrevious();
                    int operand = Opcodes.ICONST_M1;
                    /* Check if the node represents an instruction with a single
                     * integer operand, e.g. the opcode of the instruction must
                     * be one of the following: BIPUSH, SIPUSH, NEWARRAY */
                    if (prevIns instanceof IntInsnNode) {
                        IntInsnNode iiNode = (IntInsnNode) prevIns;
                        operand = iiNode.operand;
                    } else {
                        /* If we are here is because we have a node that represents
                         * a zero operand instruction */
                        InsnNode insNode = (InsnNode) prevIns;
                        operand = getOpcode(insNode.getOpcode());
                    }
                    if (d_map.get(operand) != null)
                        insList.insert(mNode, new JumpInsnNode(Opcodes.GOTO, d_map.get(operand)));
                }
            }
        }
    }
    
    private void addLabelInsn(MethodNode mn) {
        InsnList insList = mn.instructions;
        if (insList.size() == 0)
            return;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> it = insList.iterator();
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();
            if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mNode = (MethodInsnNode) node;
                if (LABEL.equals(mNode.name)) {
                    Log.log("Found label/" + mNode.desc);
                    /* Find the correct constant value on the operand stack
                     * that belongs to this 'label' invocation */
                    AbstractInsnNode prevIns = mNode.getPrevious();
                    /* Replace the invocation with a label instruction */
                    LabelNode ln = new LabelNode();
                    insList.insert(mNode, ln);
                    /* Check if the node represents an instruction with a single
                     * integer operand, e.g. the opcode of the instruction must
                     * be one of the following: BIPUSH, SIPUSH, NEWARRAY */
                    if (prevIns instanceof IntInsnNode) {
                        IntInsnNode iiNode = (IntInsnNode) prevIns;
                        Log.log("Adding operand " + iiNode.operand);
                        d_map.put(iiNode.operand, ln);
                    } else {
                        /* We have a node that represents a zero operand instruction */
                        InsnNode insNode = (InsnNode) prevIns;
                        int operand = getOpcode(insNode.getOpcode());
                        Log.log("Adding operand " + operand);
                        d_map.put(operand, ln);
                    }
                }
            }
        }
    }
    
    private void removeGotoInsn(MethodNode mn) {
        InsnList insList = mn.instructions;
        if (insList.size() == 0)
            return;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> it = insList.iterator();
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();
            if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mNode = (MethodInsnNode) node;
                if (GOTO.equals(mNode.name)) {
                    Log.log("Removing goto instruction");
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    AbstractInsnNode prevInsn1 = mNode.getPrevious();
                    AbstractInsnNode prevInsn2 = prevInsn1.getPrevious();
                    insList.remove(prevInsn2);
                    insList.remove(prevInsn1);
                    insList.remove(mNode);
                }
            }
        }
    }
    
    private void removeLabelInsn(MethodNode mn) {
        InsnList insList = mn.instructions;
        if (insList.size() == 0)
            return;
        @SuppressWarnings("unchecked")
        Iterator<AbstractInsnNode> it = insList.iterator();
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();
            if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mNode = (MethodInsnNode) node;
                if (LABEL.equals(mNode.name)) {
                    Log.log("Removing label instruction");
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    insList.insert(mNode, new InsnNode(Opcodes.NOP));
                    AbstractInsnNode prevInsn1 = mNode.getPrevious();
                    AbstractInsnNode prevInsn2 = prevInsn1.getPrevious();
                    insList.remove(prevInsn2);
                    insList.remove(prevInsn1);
                    insList.remove(mNode);
                }
            }
        }
    }
    
    public void rewrite() {
        /* Verify we have a valid path */
        if (!checkPath(Paths.get(d_path), false))
            exit("File '" + d_path + "' does not exists!", 101);
        /* Grab .class files form given directory */
        File[] cf = new File(d_path).listFiles();
        if (cf == null || cf.length == 0)
            exit("Missing .class files!", 101);
        for (File f : cf) {
            if (!f.isFile())
                continue;
            String name = f.getName();
            if ("class".equals(name.substring(name.lastIndexOf(".") + 1))) {
                Log.log("Rewriting [" + name + "]");
                try {
                    FileInputStream is = new FileInputStream(f);
                    ClassReader cr = new ClassReader(is);
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, ClassReader.SKIP_DEBUG);
                    /* Traverse the list of methods that belong to this class */
                    for (Object o : cn.methods) {
                        /* Ignore anything that is not considered to be a method */
                        if (!(o instanceof MethodNode))
                            continue;
                        MethodNode mn = (MethodNode)o;
                        Log.log("Visiting " + mn.name + "/" + mn.desc);
                        addLabelInsn(mn);
                        addGotoInsn(mn);
                        removeGotoInsn(mn);
                        removeLabelInsn(mn);
                    }
                    /* Rewrite the class file(s) */
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    cn.accept(cw);
                    byte[] newClass = cw.toByteArray();
                    FileOutputStream fo = new FileOutputStream(f, false);
                    fo.write(newClass);
                    fo.close();
                    is.close();
                } catch (IOException ex) {
                    exit(ex.getMessage(), 101);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1)
            exit("Invalid command-line arguments", 101);
        new GotoLabelRewrite(args[0]).rewrite();
        System.out.println("REWRITING DONE");
    }
}
