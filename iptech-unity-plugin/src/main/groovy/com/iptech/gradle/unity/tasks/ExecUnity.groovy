package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.UnityExtension
import com.iptech.gradle.unity.api.ExecUnitySpec

import com.iptech.gradle.unity.internal.UnityLog
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

abstract class ExecUnity extends DefaultTask implements ExecUnitySpec {
    @Internal final Property<ExecResult> execResult = project.objects.property(ExecResult.class)

    @Input
    private String getProjectPath_() {
        return projectPath.get().asFile.absolutePath
    }

    @TaskAction
    ExecResult taskExec() {
        execResult.set(exec())
        return execResult.get()
    }

    private ExecResult exec() {
        def self = this
        /*
        ExecResult _execResult = project.unity.exec(new Action<ExecUnitySpec>() {
            @Override
            void execute(ExecUnitySpec unityExecSpec) {
                unityExecSpec.arguments = self.arguments
                unityExecSpec.projectPath = self.projectPath
                unityExecSpec.buildTarget = self.buildTarget
                unityExecSpec.ignoreExitValue = self.ignoreExitValue
                unityExecSpec.userName = self.userName
                unityExecSpec.password = self.password
                unityExecSpec.logFile = self.logFile
                unityExecSpec.unityCmdPath = self.unityCmdPath
                unityExecSpec.environment = self.environment
            }
        })*/
        ExecResult _execResult = project.unity.exec(this)

        if(!self.ignoreExitValue.get()) {
            if(_execResult.exitValue!=0) {
                String errorMsg = "Unity finished with non-zero exit value ${_execResult.exitValue}"
                try {
                    File uLogFile = project.file(logFile.get())
                    if (uLogFile.exists()) {
                        UnityLog uLog = new UnityLog(uLogFile)
                        errorMsg += "\n\n" + uLog.parseLogForBuildErrors()
                    }
                } catch(Exception ex) { println 'Failed to parse unity log for errors..  ' + ex.toString() }
                throw new GradleException(errorMsg)
            }
        }
        return _execResult
    }
}
