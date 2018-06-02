package com.igor.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.TraceClassVisitor

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

class SecurityPlugin extends Transform implements Plugin<Project> {
    public static final String EX_SECURITY = "replaceSP"
    private static final String FLAG_ERROR = "error"

    private SecurityExt mReplaceSPExt
    private String[] mExceptClass

    void apply(Project project) {
        println "================================================"
        println "           WELCOME TO SECURITY PLUGIN"
        println "================================================"
        project.extensions.getByType(AppExtension).registerTransform(this)
        project.extensions.create(EX_SECURITY, SecurityExt)
        project.afterEvaluate {
            mReplaceSPExt = project[EX_SECURITY]
            if (mReplaceSPExt.exceptClass != null) {
                mExceptClass = mReplaceSPExt.exceptClass.split(",")
            }
        }
    }

    @Override
    String getName() {
        return "SecurityPlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    private void logReplaceNode(MethodInsnNode node) {
        if (node == null) {
            return
        }
        println "---------------------replace instruction------------------------"
        println "name: " + ((MethodInsnNode) node).name
        println "owner: " + ((MethodInsnNode) node).owner
        println "desc: " + ((MethodInsnNode) node).desc
        println "opcode: " + ((MethodInsnNode) node).opcode
    }

    private void logASMSourceCode(final byte[] bytes, final String filePath) {
        ClassReader classReader = new ClassReader(bytes)
        OutputStream outputStream = new ByteArrayOutputStream()
        PrintWriter printWriter = new PrintWriter(outputStream)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        TraceClassVisitor cv = new TraceClassVisitor(classWriter, printWriter)
        classReader.accept(cv, EXPAND_FRAMES)

        // 将修改后的asm源码输出到文件中，用作和源文件对比
        FileOutputStream fos = new FileOutputStream(filePath)
        fos.write(outputStream.toString().bytes)
        fos.close()
    }

    private String fieldName = "CURRENT_SP_NAME", fieldDesc = "Ljava/lang/String;"

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        println '===============Security Plugin visit start==============='
        if (outputProvider != null)
            outputProvider.deleteAll()  //删除之前的输出

        inputs.each { TransformInput input ->
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    if (directoryInput.file.isDirectory()) {
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def name = file.name
                                def isChange
                                if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                        "R.class" != name && "BuildConfig.class" != name) {

                                    isChange = false
                                    def spName

                                    ClassReader classReader = new ClassReader(file.bytes)
                                    ClassNode classNode = new ClassNode()
                                    classReader.accept(classNode, 0)
                                    if (classNode.name != mReplaceSPExt.className || (mExceptClass != null && !mExceptClass.contains(classNode.name))) {
                                        for (MethodNode methodNode : classNode.methods) {
//                                            println "---------------------------------------"
//                                            println "mtd owner: " + methodNode.name
//                                            println "mtd desc: " + methodNode.desc
                                            String[] putMtdInfo
                                            int putMtdType
                                            for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                                                String className = node.getClass().toString()
//                                                if (methodNode.name == "getEditor") {
//                                                    println "instructions:" + node.getClass()
//                                                }
                                                if ("class org.objectweb.asm.tree.MethodInsnNode" == className) {

                                                    // 获取SP存储的文件名称和文件的访问模式
                                                    if (((MethodInsnNode) node).desc == "(Ljava/lang/String;I)Landroid/content/SharedPreferences;"
                                                            && ((MethodInsnNode) node).owner == "android/content/Context"
                                                            && ((MethodInsnNode) node).name == "getSharedPreferences") {

                                                        def labelCount = 0
                                                        AbstractInsnNode currentNode = node
                                                        AbstractInsnNode pre
                                                        while (labelCount <= 2 && currentNode != null) {
                                                            pre = currentNode.previous
                                                            if (pre.getClass().toString() == "class org.objectweb.asm.tree.LabelNode") {
                                                                labelCount++
                                                            } else if (pre.getClass().toString() == "class org.objectweb.asm.tree.InsnNode") {
//                                                                println "InsnNode toString: " + ((InsnNode)pre).toString()
                                                            } else if (pre.getClass().toString() == "class org.objectweb.asm.tree.LdcInsnNode") {
                                                                spName = (String) ((LdcInsnNode) pre).cst
                                                                // 创建当前的SP文件名称属性并赋值
                                                                classNode.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                                                                        fieldName, fieldDesc, null, spName).visitEnd()
                                                            }
                                                            currentNode = pre
                                                        }
                                                    }

//                                                    if ((((MethodInsnNode) node).name == "edit" && ((MethodInsnNode) node).owner == "android/content/SharedPreferences" && ((MethodInsnNode) node).desc == "()Landroid/content/SharedPreferences\$Editor;")
//                                                            || (((MethodInsnNode) node).owner == "android/content/SharedPreferences\$Editor" && ((MethodInsnNode) node).desc.contains("Landroid/content/SharedPreferences\$Editor;"))
//                                                            || ((MethodInsnNode) node).desc == "(Landroid/content/Context;)Landroid/content/SharedPreferences;") {

                                                    // 弹出当前方法的第一个参数：context
//                                                    if (((MethodInsnNode) node).name == "edit"
//                                                            && ((MethodInsnNode) node).owner == "android/content/SharedPreferences"
//                                                            && ((MethodInsnNode) node).desc == "()Landroid/content/SharedPreferences\$Editor;") {
//                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 0))
//                                                    }

                                                    // 删除put系列方法的字节码,同时记录当前的参数用于修改提交时使用
                                                    if ((((MethodInsnNode) node).owner == "android/content/SharedPreferences\$Editor"
                                                            && ((MethodInsnNode) node).desc.contains("Landroid/content/SharedPreferences\$Editor;"))) {
                                                        putMtdInfo = getMtdInfo(node.name).split(",")
                                                        putMtdType = getLoadType(node.name)
//                                                        logReplaceNode(node)
//                                                        methodNode.instructions.remove(node)
                                                    }

                                                    // 替换原有存储的提交方式
                                                    if (((MethodInsnNode) node).owner == "android/content/SharedPreferences\$Editor"
                                                            && (((MethodInsnNode) node).name == "commit" || ((MethodInsnNode) node).name == "apply")
                                                            && putMtdInfo != null && putMtdInfo.length > 1) {
                                                        isChange = true
                                                        logReplaceNode(node)
                                                        println "extras: " + putMtdInfo[0] + "," + putMtdInfo[1]
                                                        methodNode.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, mReplaceSPExt.className, putMtdInfo[0], putMtdInfo[1]))
                                                        methodNode.instructions.insert(node, new VarInsnNode(putMtdType, 2))
                                                        methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldName, fieldDesc))
                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 1))
                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 0))
                                                        methodNode.instructions.remove(node)
                                                    }

                                                    String[] getMtdInfo = getMtdInfo(node.name).split(",")
                                                    // 替换get系列的逻辑
                                                    if (((MethodInsnNode) node).owner == "android/content/SharedPreferences"
                                                            && ((MethodInsnNode) node).name.contains("get")
                                                            && getMtdInfo != null && getMtdInfo.length > 1) {
                                                        isChange = true
                                                        logReplaceNode(node)
                                                        methodNode.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, mReplaceSPExt.className, getMtdInfo[0], getMtdInfo[1]))
                                                        methodNode.instructions.insert(node, new VarInsnNode(getLoadType(node.name), 2))
                                                        methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldName, fieldDesc))
                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 1))
                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 0))
                                                        methodNode.instructions.insert(node, new VarInsnNode(Opcodes.ASTORE, 1))
                                                        methodNode.instructions.insert(node, new VarInsnNode(getStoreType(node.name), 2))
                                                        methodNode.instructions.remove(node)
                                                    }
                                                }
                                            }
                                        }

                                        // 如果有修改则覆盖原文件
                                        if (isChange) {
                                            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                                            classNode.accept(classWriter)
                                            FileOutputStream fos = new FileOutputStream(
                                                    file.parentFile.absolutePath + File.separator + name)
                                            fos.write(classWriter.toByteArray())
                                            fos.close()
                                            println "-----------------------------------------------"
                                            println "REPLACE CLASS : " + classNode.name
                                            println "-----------------------------------------------"
                                            logASMSourceCode(classWriter.toByteArray(), file.parentFile.absolutePath + File.separator + name + ".asm")
                                        }
                                    }
                                }
                        }
                    }

                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }


            input.jarInputs.each { JarInput jarInput ->
                /**
                 * 重名名输出文件,因为可能同名,会覆盖
                 */
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
//                println "scan jar : " + jarName

                File tmpFile = null
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    JarFile jarFile = new JarFile(jarInput.file)
                    Enumeration enumeration = jarFile.entries()
                    tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_trace.jar")
                    //避免上次的缓存被重复插入
                    if (tmpFile.exists()) {
                        tmpFile.delete()
                    }
                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
                    //用于保存
                    ArrayList<String> processorList = new ArrayList<>()
                    while (enumeration.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                        String entryName = jarEntry.getName()
                        ZipEntry zipEntry = new ZipEntry(entryName)
                        //println "MeetyouCost entryName :" + entryName
                        InputStream inputStream = jarFile.getInputStream(jarEntry)
                        //如果是inject文件就跳过
//                        println "=======" + entryName

                        //插桩class
                        if (entryName.endsWith(".class") && !entryName.contains("R\$") &&
                                !entryName.contains("R.class") && !entryName.contains("BuildConfig.class")) {
                            //class文件处理
                            jarOutputStream.putNextEntry(zipEntry)
                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                            println "------class name :" + entryName.split(".class")[0]
                            ClassVisitor cv = new TraceVisitor(classWriter)
                            classReader.accept(cv, EXPAND_FRAMES)
                            byte[] code = classWriter.toByteArray()
                            jarOutputStream.write(code)

                        } else if (entryName.contains("META-INF/services/javax.annotation.processing.Processor")) {
                            if (!processorList.contains(entryName)) {
                                processorList.add(entryName)
                                jarOutputStream.putNextEntry(zipEntry)
                                jarOutputStream.write(IOUtils.toByteArray(inputStream))
                            } else {
                                println "duplicate entry:" + entryName
                            }
                        } else {

                            jarOutputStream.putNextEntry(zipEntry)
                            jarOutputStream.write(IOUtils.toByteArray(inputStream))
                        }

                        jarOutputStream.closeEntry()
                    }
                    jarOutputStream.close()
                    jarFile.close()
                }

                //处理jar进行字节码注入处理 TODO
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (tmpFile == null) {
                    FileUtils.copyFile(jarInput.file, dest)
                } else {
                    FileUtils.copyFile(tmpFile, dest)
                    tmpFile.delete()
                }
            }
        }
        println '===============Security Plugin visit end==============='
    }

    private String getMtdInfo(String name) {
        def result
        switch (name) {
            case "getString":
                result = mReplaceSPExt.getString + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
                break
            case "putString":
                result = mReplaceSPExt.putString + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
                break
            case "getBoolean":
                result = mReplaceSPExt.getBoolean + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Z)Z"
                break
            case "putBoolean":
                result = mReplaceSPExt.putBoolean + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Z)V"
                break
            case "getInt":
                result = mReplaceSPExt.getInt + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;I)I"
                break
            case "putInt":
                result = mReplaceSPExt.putInt + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;I)V"
                break
            case "getLong":
                result = mReplaceSPExt.getLong + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;J)J"
                break
            case "putLong":
                result = mReplaceSPExt.putLong + "," + "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;J)V"
                break
        }
        if (result == null) {
            result = FLAG_ERROR
        }
        return result
    }

    private int getStoreType(String name) {
        def result
        if (name == null) {
            return Opcodes.ASTORE
        }

        switch (name) {
            case "getString":
            case "putString":
                result = Opcodes.ASTORE
                break
            case "getBoolean":
            case "putBoolean":
            case "getInt":
            case "putInt":
                result = Opcodes.ISTORE
                break
            case "getLong":
            case "putLong":
                result = Opcodes.LSTORE
                break
            default:
                result = Opcodes.ASTORE
        }
        return result
    }

    private int getLoadType(String name) {
        def result
        if (name == null) {
            return Opcodes.ALOAD
        }

        switch (name) {
            case "getString":
            case "putString":
                result = Opcodes.ALOAD
                break
            case "getBoolean":
            case "putBoolean":
            case "getInt":
            case "putInt":
                result = Opcodes.ILOAD
                break
            case "getLong":
            case "putLong":
                result = Opcodes.LLOAD
                break
            default:
                result = Opcodes.ALOAD
        }
        return result
    }
}