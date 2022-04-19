package com.iptech.gradle.unity.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

abstract class RunTests extends ExecUnity {

    @Input abstract Property<String> getTestPlatform()
    @OutputFile abstract RegularFileProperty getTestResults()

    RunTests() {
        arguments.convention(project.provider({
            [
                '-batchmode', '-runTests', '-nographics', '-silent-crashes',
                '-testPlatform', testPlatform.get(),
                '-testResults', testResults.get().asFile.absolutePath
            ]
        }))
        ignoreExitValue.set(true)
    }

    @TaskAction
    @Override
    ExecResult execute() {
        super.execute()

        Boolean failTask = execResult.get().exitValue != 0
        String failMessage = "Unit Tests Failing"

        File result = testResults.get().asFile
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
