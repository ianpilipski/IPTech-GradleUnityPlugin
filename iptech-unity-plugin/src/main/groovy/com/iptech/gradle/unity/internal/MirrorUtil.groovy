package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.MirrorSpec
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.FileVisitDetails
import org.gradle.process.ExecSpec

import javax.inject.Inject

class MirrorUtil {
    private final Project project

    @Inject
    MirrorUtil(Project project) {
        this.project = project
    }

    Project getProject() {
        return this.project
    }

    void mirror(Action<? super MirrorSpec> action) {
        DefaultMirrorSpec ms = project.objects.newInstance(DefaultMirrorSpec.class, project)
        action.execute(ms)

        List<FileCopyDetails> copyDetailsAll = []
        List<FileCopyDetails> copyDetailsCopied = []
        Set<String> inc = ms.includeInCopy.includes
        if(inc.size()==0) {
            inc.add('**')
        }
        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec cs) {
                cs.from ms.sourceDir
                cs.into ms.destDir
                cs.include inc
                cs.eachFile(new Action<FileCopyDetails>() {
                    @Override
                    void execute(FileCopyDetails d) {
                        File f = project.file("${ms.destDir}/${d.path}")
                        if(f.exists() && f.lastModified() == d.lastModified) {
                            //println "NOTMODIFIED: ${d.path}"
                            d.exclude()
                        } else {
                            //println "COPYING: ${d.path}"
                            copyDetailsCopied << d
                        }
                        copyDetailsAll << d
                    }
                })
            }
        })

        copyDetailsCopied.each { FileCopyDetails details ->
            def target = new File(ms.destDir, details.path)
            if(target.exists()) { target.setLastModified(details.lastModified) }
        }

        // remove dangling / broken symlinks which fail the fileTree recursion
        project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                execSpec.commandLine 'bash', '-cl', "find \"${ms.destDir.absolutePath}\" -type l -exec test ! -e {} \\; -delete"
            }
        })

        project.fileTree(ms.destDir, new Action<ConfigurableFileTree>() {
            @Override
            void execute(ConfigurableFileTree files) {
                files.include '**'
                files.exclude copyDetailsAll.collect { FileCopyDetails d -> d.path }
                files.exclude ms.getPreserveInDestination().includes
            }
        }).forEach { File it ->
            //println "DELETING: ${it.path}"
            it.delete()
        }

        List<File> emptyDirs = []
        project.fileTree(ms.destDir, new Action<ConfigurableFileTree>() {
            @Override
            void execute(ConfigurableFileTree files) {
                files.exclude ms.getPreserveInDestination().includes
            }
        }).visit { FileVisitDetails d ->
            if (d.isDirectory()) {
                def children = project.fileTree(d.file).filter { File it -> it.isFile() }.files
                if (children.size() == 0) {
                    emptyDirs << d.file
                }
            }
        }
        emptyDirs.reverseEach { File it ->
            //println "EMPTYDIR DEL: ${it.path}"
            it.delete()
        }
    }
}
