package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.internal.MirrorUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

abstract class MirrorProject extends DefaultTask {
    @Internal abstract Property<BuildConfig> getBuildConfig()

    @InputFiles
    FileTree getUnityFileTree() {
        return buildConfig.get().unity.mainUnityProjectFileTree
    }

    @OutputFiles
    FileTree getOutputFileTree() {
        BuildConfig bc = buildConfig.get()
        return bc.buildCacheProjectPath.asFileTree.matching(bc.unity.unityProjectFilter.get())
    }

    @TaskAction
    void exec() {
        MirrorUtil mutil = project.objects.newInstance(MirrorUtil, project)

        mutil.mirror {
            from buildConfig.get().unity.projectPath.get().asFile
            include buildConfig.get().unity.unityProjectFilter.get().includes
            preserve {
                include 'Library/**'
                include 'Temp/**'
            }
            into buildConfig.get().buildCacheProjectPath
        }
    }
}
