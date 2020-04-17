package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.internal.MirrorUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

class MirrorProject extends DefaultTask {
    @Nested
    final Property<BuildConfig> buildConfig = project.objects.property(BuildConfig)

    @InputFiles
    FileTree getUnityFileTree() {
        return buildConfig.get().unity.mainUnityProjectFileTree
    }

    @OutputFiles
    FileTree getOutputFileTree() {
        return project.fileTree(dir:buildConfig.get().mirrordProjectPath)
    }

    @TaskAction
    void exec() {
        MirrorUtil mutil = project.objects.newInstance(MirrorUtil.class, project)

        mutil.mirror {
            ConfigurableFileTree fileTree = buildConfig.get().unity.mainUnityProjectFileTree
            from fileTree.getDir()
            include fileTree.getIncludes()
            preserve {
                include 'Library/**'
                include 'Temp/**'
            }
            into buildConfig.get().mirrordProjectPath
        }
    }
}
