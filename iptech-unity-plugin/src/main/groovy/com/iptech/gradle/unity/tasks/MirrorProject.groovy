package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.MirrorSpec
import com.iptech.gradle.unity.internal.MirrorUtil
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable

@CompileStatic
class MirrorProject extends DefaultTask {
    @Nested
    final Property<BuildConfig> buildConfig = project.objects.property(BuildConfig)

    /*
    @Nested
    Provider<BuildConfig> getBuildConfig() {
        return buildConfig
    }

     */

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
        mirrorUnityProject()
        CreateOutputFile()
    }

    private void mirrorUnityProject() {
        MirrorUtil mutil = new MirrorUtil(project)

        //TODO: can I use a fileTree here and add preserve?
        mutil.mirror(new Action<MirrorSpec>() {
            @Override
            void execute(MirrorSpec mirrorSpec) {
                mirrorSpec.from project.projectDir
                mirrorSpec.include 'Assets/**', 'ProjectSettings/**', 'AssetManifest.xml', 'Packages/**'
                mirrorSpec.preserve(new Action<PatternFilterable>() {
                    @Override
                    void execute(PatternFilterable patternFilterable) {
                        patternFilterable.include 'Library/**'
                        patternFilterable.include 'Temp/**'
                    }
                })
                mirrorSpec.into buildConfig.get().mirrordProjectPath
            }
        })
    }

    void CreateOutputFile() {
        File upToDateFile = project.file( "${buildConfig.get().logDir}/mirroredInfo.txt")
        if(!upToDateFile.exists()) {
            project.mkdir(buildConfig.get().logDir)
            upToDateFile.createNewFile()
        }
        upToDateFile.text = new Date().toString()
    }
}
