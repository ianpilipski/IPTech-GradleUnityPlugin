package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import com.iptech.gradle.unity.tasks.ExecUnity

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

    private Task createTestTask(String testPlatform, String taskPrefix, BuildConfig buildConfig) {
        Project project = buildConfig.unity.project
        return project.tasks.create(taskPrefix, ExecUnity) {
            Provider<RegularFile> resultFile = buildConfig.artifactDir.map { it.file("${taskPrefix}.xml") }
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            ignoreExitValue = true
            arguments = project.provider({
                [
                    '-batchmode', '-runTests', '-nographics', '-silent-crashes',
                    '-testPlatform', testPlatform,
                    '-testResults', resultFile.get().asFile.absolutePath
                ]
            })

            inputs.files {
                buildConfig.buildCacheProjectPath.asFileTree.matching(buildConfig.unity.unityProjectFilter.get())
            }

            outputs.file resultFile

            doLast {
                Boolean failTask = execResult.get().exitValue != 0
                String failMessage = "Unit Tests Failing"

                File result = resultFile.get().asFile
                if(result.exists()) {
                    String testSummary = project.nunit.parseFailures(result)
                    println testSummary
                    failTask = project.nunit.parseFailureCounts(result)>0
                    failMessage += "\n\n${testSummary}"
                }

                if (failTask) {
                    throw new GradleException(failMessage)
                }
            }
        }
    }
}
