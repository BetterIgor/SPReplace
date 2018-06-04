package com.igor.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.util.TraceClassVisitor

class TraceVisitor extends ClassVisitor {

    private String className

    private String superName

    private String[] interfaces

    TraceVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor)
    }

    /**
     * 进入类时回调
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.superName = superName
        this.interfaces = interfaces
    }


    @Override
    void visitSource(String source, String debug) {
//        println "visitSource: source:" + source + ", debug:" +debug
        super.visitSource(source, debug)
    }

    /**
     * 进入到类的方法时进行回调
     */
    @Override
    MethodVisitor visitMethod(final int access, final String name,
                              final String desc,
                              final String signature, String[] exceptions) {
        if (name == "putBooleanWithApply") {
            println "remove mtd: " + name
            return
        }
//        println "visitMethod: access:" + access + ", name:" + name + ", desc:" + desc + ", signature:" + signature
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {

            private boolean isInject() {
                //如果父类名是AppCompatActivity则拦截这个方法
                if (superName.contains("AppCompatActivity")) {
                    return true
                }
                return false
            }

            @Override
            void visitCode() {
                super.visitCode()
//                println "visitCode"
            }

            @Override
            AnnotationVisitor visitAnnotation(String desc1, boolean visible) {

//                println "visitAnnotation: desc1:" + desc1 + ", visible:" + visible
                return super.visitAnnotation(desc1, visible)
            }

            @Override
            void visitFieldInsn(int opcode, String owner, String name2, String desc2) {

//                println "visitFieldInsn : opcode:" + opcode + ", owner:" + owner + ", name2:" + name2 + ", desc2:" + desc2
                super.visitFieldInsn(opcode, owner, name2, desc2)
            }

            /**
             * 在方法开始之前回调
             */
            @Override
            protected void onMethodEnter() {
//                println "mtd enter: "
//                if (isInject()) {
                if (desc != null && desc.contains("SharedPreferences")) {
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitMethodInsn(INVOKESTATIC,
                            "com/igor/sample/myapplication1/utils/TraceUtil",
                            "onCreate", "(Landroid/app/Activity;)V",
                            false)
                }
//                }
            }

            /**
             * 在方法结束时回调
             */
            @Override
            protected void onMethodExit(int i) {
                super.onMethodExit(i)
//                println "mtd exit: " + ", param is " + i
            }
        }
        return methodVisitor
    }
}