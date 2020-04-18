package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.ExecResult

class RunTestsBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['runEditModeTests', 'runPlayModeTests']
    }

    @Override
    Iterable<Task> createTasks(String stepName, String taskPrefix, BuildConfig buildConfig, Object args) {
        if(stepName == 'runEditModeTests') {
            return [ createTestTask('EditMode', taskPrefix, buildConfig) ]
        } else {
            return [ createTestTask('PlayMode', taskPrefix, buildConfig) ]
        }
    }

    private Task createTestTask(String testPlatform, String taskPrefix, BuildConfig buildConfig) {
        Project project = buildConfig.unity.project
        Task t = project.tasks.create(taskPrefix) {
            inputs.files(project.provider({
                project.fileTree(dir: buildConfig.mirrordProjectPath, includes: buildConfig.unity.mainUnityProjectFileTree.getIncludes())
            }))

            outputs.file(project.provider({ "${buildConfig.artifactDir}/${taskPrefix}.xml" }))

            doLast {
                String resultFile = "${buildConfig.artifactDir}/${taskPrefix}.xml"
                ExecResult execResult = buildConfig.execUnity {
                    arguments([
                        '-batchmode', '-runTests', '-nographics', '-silent-crashes',
                        '-testPlatform', testPlatform,
                        '-testResults', resultFile
                    ])
                    ignoreExitValue true
                }

                Boolean failTask = execResult.getExitValue() != 0
                String failMessage = "Unit Tests Failing"


                File result = project.file(resultFile)
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

        // add to check task if default lifecycle is included
        project.tasks.all { Task addedTask ->
            if(addedTask.name == 'check') {
                addedTask.dependsOn(t)
            }
        }

        return t
    }
}
