package com.iptech.gradle.unity.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.nio.file.*
import org.apache.tools.ant.taskdefs.condition.Os


class ExtractUnityFiles extends DefaultTask {
    @OutputDirectory
    Directory getOutputDir() {
        return project.layout.buildDirectory.get().dir('tmp/unity-files')
    }

    @TaskAction
    void execute() {
        String resourcePath = "unity-files"
        URI uri
        ClassLoader classLoader = getClass().getClassLoader()
        try {
            uri = classLoader.getResource(resourcePath).toURI()
        } catch(URISyntaxException|NullPointerException e) {
            throw new GradleException(e.message, e)
        }

        if(!uri) {
            throw new GradleException('something is wrong.. internal directory or files missing for unity-files')
        }

        if(uri.getScheme().contains('jar')) {
            try{
                URI jarUri = ExtractUnityFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                jarUri = URI.create("jar:" + jarUri.toString())

                FileSystem fs = null
                try {
                    fs = FileSystems.newFileSystem(jarUri, new HashMap<>())
                    Path basePath = fs.getPath(resourcePath)
                    Files.walk(basePath).filter {
                        Files.isRegularFile(it)
                    }.each { p ->
                        String relativePath = p.toString().substring(basePath.toString().length()+1)
                        copyFileToDest(relativePath, p)
                    }
                } finally {
                    if(fs!=null) fs.close()
                }
            } catch(IOException e) {
                throw new GradleException(e.getMessage(), e)
            }
        } else {
            //IDE
            Path path = Paths.get(uri);
            try {
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)
                Files.walk(path).filter {
                    Files.isRegularFile(it)
                }.each { p ->
                    String relativePath = p.toString().substring(path.toString().length()+1)
                    copyFileToDest(relativePath, p)
                }
            } catch (IOException e) {
                throw new GradleException(e.getMessage(), e)
            }
        }
    }

    protected void copyFileToDest(String pathName, Path srcPath) {
        while(pathName.startsWith('/')) {
            pathName = pathName.substring(1)
        }
        File file = outputDir.file(pathName).asFile
        if(!file.parentFile.exists()) {
            Files.createDirectories(file.parentFile.toPath())
        }
        Files.copy(srcPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
