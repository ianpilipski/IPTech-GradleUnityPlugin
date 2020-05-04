package com.iptech.gradle.unity.tasks

import com.sun.nio.zipfs.ZipFileSystem
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.file.*

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
                URL jar = ExtractUnityFiles.class.getProtectionDomain().getCodeSource().getLocation()
                //jar.toString() begins with file:
                //i want to trim it out...
                Path jarFile = Paths.get(jar.toString().substring("file:".length()))

                ZipFileSystem fs = FileSystems.newFileSystem(jarFile, null)
                Path basePath = fs.getPath(resourcePath)
                Files.walk(basePath).filter {
                    Files.isRegularFile(it)
                }.each { p ->
                    getClass().getResourceAsStream(p.toString()).withStream {
                        String relativePath = p.toString().substring(basePath.toString().length()+2)
                        copyFilesFromInputStream(relativePath, it)
                    }
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
                    p.toFile().withInputStream {
                        String relativePath = p.toString().substring(path.toString().length()+2)
                        copyFilesFromInputStream(relativePath, it)
                    }
                }
            } catch (IOException e) {
                throw new GradleException(e.getMessage(), e)
            }
        }
    }

    protected void copyFilesFromInputStream(String pathName, InputStream is) {
        File file = outputDir.file(pathName).asFile
        if(!file.parentFile.exists()) {
            project.mkdir(file.parentFile)
        }
        file.text = is.text
    }
}
