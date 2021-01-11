package me.maxandroid.router.gradle

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RouterMappingTransform extends Transform {

    @Override
    String getName() {
        return "RouterMappingTransform"
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

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {

        RouterMappingCollector collector = new RouterMappingCollector()


        // 遍历所有的输入
        transformInvocation.inputs.each {

            // 把 文件夹 类型的输入，拷贝到目标目录
            it.directoryInputs.each { directoryInput ->
                def destDir = transformInvocation.outputProvider
                        .getContentLocation(
                                directoryInput.name,
                                directoryInput.contentTypes,
                                directoryInput.scopes,
                                Format.DIRECTORY)
                collector.collect(directoryInput.file)
                FileUtils.copyDirectory(directoryInput.file, destDir)
            }

            // 把 JAR 类型的输入，拷贝到目标目录
            it.jarInputs.each { jarInput ->
                def dest = transformInvocation.outputProvider
                        .getContentLocation(
                                jarInput.name,
                                jarInput.contentTypes,
                                jarInput.scopes, Format.JAR)
                collector.collectFromJarFile(jarInput.file)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        println("${getName()}  all mapping class name = ${collector.mappingClassName}")

        File mappingJarFile = transformInvocation.outputProvider.
                getContentLocation(
                        "router_mapping",
                        getOutputTypes(),
                        getScopes(),
                        Format.JAR)

        println("${getName()}  mappingJarFile = $mappingJarFile")

        if (mappingJarFile.getParentFile().exists()) {
            mappingJarFile.getParentFile().mkdirs()
        }

        if (mappingJarFile.exists()) {
            mappingJarFile.delete()
        }

        // 将生成的字节码，写入本地文件
        FileOutputStream fos = new FileOutputStream(mappingJarFile)
        JarOutputStream jarOutputStream = new JarOutputStream(fos)
        ZipEntry zipEntry =
                new ZipEntry(RouterMappingByteCodeBuilder.CLASS_NAME + ".class")
        jarOutputStream.putNextEntry(zipEntry)
        jarOutputStream.write(
                RouterMappingByteCodeBuilder.get(collector.mappingClassName))
        jarOutputStream.closeEntry()
        jarOutputStream.close()
        fos.close()
    }
}