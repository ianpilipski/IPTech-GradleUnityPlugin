package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.tasks.RunTests
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

class RunTestsBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['runEditModeTests', 'runPlayModeTests']
    }

    @Override
    Boolean getIsTestTask() {
        return true
    }

    Task runEditModeTests(String taskPrefix, BuildConfig buildConfig) {
        return createTestTask('EditMode', taskPrefix, buildConfig)
    }

    Task runPlayModeTests(String taskPrefix, BuildConfig buildConfig) {
        return createTestTask('PlayMode', taskPrefix, buildConfig)
    }

    private Task createTestTask(String testPlatformArg, String taskPrefix, BuildConfig buildConfig) {
        Project project = buildConfig.unity.project
        Provider<Directory> outDir = buildConfig.buildDirectory.dir(taskPrefix)

        return project.tasks.create(taskPrefix, RunTests) {
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            outputDir = outDir
            logFile = outputDir.file('output.log')
            testPlatform = testPlatformArg
            testResults = outputDir.file("${testPlatformArg.toLowerCase()}-testresults.xml")
        }
    }
}
