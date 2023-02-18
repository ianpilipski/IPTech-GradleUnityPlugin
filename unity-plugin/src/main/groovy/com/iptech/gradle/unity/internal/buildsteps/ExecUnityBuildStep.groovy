package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.tasks.ExecUnity
import com.iptech.gradle.unity.tasks.UnityBuildTask
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider

class ExecUnityBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['execUnity', 'unityBuild', 'unityBuildDev']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    Task unityBuildDev(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        def t = unityBuild(taskPrefix, buildConfig, { arguments.add('-developmentBuild') })
        t.configure(configClosure)
        return t
    }

    Task unityBuild(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        UnityBuildTask t =  buildConfig.unity.project.tasks.create(taskPrefix, UnityBuildTask) {
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            outputDir = buildConfig.buildDirectory.dir(taskPrefix)
            logFile = outputDir.file('output.log')
            arguments.add('-buildNumber')
            arguments.add(buildConfig.unity.buildNumber)

            executeMethod = 'IPTech.UnityGradlePlugin.Commands.Build'
            arguments.addAll(['-batchmode', '-quit', '-nographics'])
            if(buildConfig.unity.exemptEncryption.get()) {
                arguments.add('-exemptEncryption')
            }
        }
        if(configClosure) {
            t.configure(configClosure)
        }

        Provider<Directory> dp = t.outputDir.map {
            if(t.buildTarget.get() == 'iOS') {
                return it.dir('xcode-project')
            } else {
                return it.dir('gradle-project')
            }
        }

        DirectoryProperty resultProjectDir = buildConfig.unity.project.objects.directoryProperty().value(dp)

        buildConfig.ext.unityBuildOutput = resultProjectDir
        t.ext.output = resultProjectDir
        return t
    }


    Task execUnity(String taskPrefix, BuildConfig buildConfig, Closure configClosure) {
        Task t = buildConfig.unity.project.tasks.create(taskPrefix, ExecUnity) {
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            outputDir = buildConfig.buildDirectory.dir(taskPrefix)
            logFile = outputDir.file('output.log')
            arguments.add('-buildNumber')
            arguments.add(buildConfig.unity.buildNumber)
        }
        if(configClosure) {
            t.configure(configClosure)
        }
        return t
    }
}
