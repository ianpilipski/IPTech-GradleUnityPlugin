package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.tasks.ExecUnity
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task

class ImportProjectBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['importProject']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task importProject(String taskPrefix, BuildConfig buildConfig) {
        Project project = buildConfig.unity.project
        Task t = project.tasks.create(taskPrefix, ExecUnity) {
            arguments = ['-batchmode', '-quit', '-nographics', '-silent-crashes']
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            outputDir = buildConfig.buildDirectory.dir(taskPrefix)
            logFile = outputDir.file('output.log')

            outputs.dir {
                buildConfig.buildCacheProjectPath.dir("Library/ScriptAssemblies")
            }

            doFirst {
                // This will force the re-compilation of script files
                project.delete "${buildConfig.buildCacheProjectPath.get().asFile.absolutePath}/Library/ScriptAssemblies"
            }
        }
        return t
    }
}
