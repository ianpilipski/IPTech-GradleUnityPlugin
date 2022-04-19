package com.iptech.gradle.unity.tasks


import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.internal.UnityLog
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

abstract class ExecUnity extends DefaultTask implements ExecUnitySpec {
    @Internal final Property<ExecResult> execResult = project.objects.property(ExecResult.class)

    @Input
    protected String getProjectPath_() {
        return projectPath.get().asFile.absolutePath
    }

    @InputFiles
    protected FileTree getProjectFiles_() {
        return projectPath.get().asFileTree.matching(unityProjectFilter.get())
    }

    @TaskAction
    ExecResult execute() {
        execResult.set(exec())
        return execResult.get()
    }

    private ExecResult exec() {
        def self = this
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
