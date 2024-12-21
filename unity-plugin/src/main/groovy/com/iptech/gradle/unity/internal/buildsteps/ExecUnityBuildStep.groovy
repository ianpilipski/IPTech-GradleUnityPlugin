package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.tasks.ExecUnity
import org.gradle.api.Task

class ExecUnityBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['execUnity']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task execUnity(String taskPrefix, BuildConfig buildConfig, Closure configClosure=null) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, ExecUnity) {
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            outputDir = buildConfig.buildDirectory.dir(taskPrefix)
            logFile = buildConfig.buildDirectory.file("${taskPrefix}/output.log")
            arguments.add('-buildNumber')
            arguments.add(buildConfig.unity.buildNumber)
        }
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
