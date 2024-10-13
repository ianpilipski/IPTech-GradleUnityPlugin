package com.iptech.gradle.unity.internal.executors

import com.iptech.gradle.unity.UnityExtension
import com.iptech.gradle.unity.api.ExecUnitySpec

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import javax.inject.Inject


class ExecUnityExecutor {
    private final UnityExtension unityExtension
    private final ObjectFactory objectFactory
    private final ExecOperations execOperations

    @Inject
    ExecUnityExecutor(UnityExtension unityExtension, ObjectFactory objectFactory, ExecOperations execOperations) {
        this.unityExtension = unityExtension
        this.objectFactory = objectFactory
        this.execOperations = execOperations
    }

    ExecResult exec(Action<? super ExecUnitySpec> action) {
        ExecUnitySpec spec = objectFactory.newInstance(ExecUnitySpec)
        spec.unityCmdPath.convention(unityExtension.unityCmdPath)
        spec.userName.convention(unityExtension.userName)
        spec.password.convention(unityExtension.password)
        action.execute(spec)
        return exec(spec)
    }

    ExecResult exec(ExecUnitySpec spec) {
        List<String> argList = []
        File unityCmdFile = spec.unityCmdPath.get().asFile

        addArgument(spec.projectPath,'-projectPath', argList)
        addLogFile(spec.logFile, '-logFile', argList)
        addArgument(spec.buildTarget, '-buildTarget', argList)
        addArgument(spec.userName, "-username", argList)

        addArgument(spec.executeMethod, '-executeMethod', argList)

        addArgument(spec.outputDir, '-outputDir', argList)

        if(spec.arguments.isPresent()) argList.addAll(spec.arguments.get())

        printCommand(spec, argList)

        // add this after so it's not logged
        addArgument(spec.password, "-password", argList)

        return execOperations.exec {
            /*
            if(spec.logFile.isPresent()) {
                FileOutputStream fos = new FileOutputStream(spec.logFile.get().asFile)
                standardOutput = fos
                errorOutput = fos
            }*/
            executable = unityCmdFile.getAbsolutePath()
            args = argList
            ignoreExitValue = (spec.ignoreExitValue.isPresent() ? spec.ignoreExitValue.get() : false)
            if(spec.environment.isPresent()) {
                environment(spec.environment.get())
            }
        }
    }

    private void printCommand(ExecUnitySpec spec, List<String> argList) {
        String msg = "Calling: ${spec.unityCmdPath.get().asFile.absolutePath} ${argList.join(' ')}"
        if(spec.password.isPresent()) {
            msg += " -password (hidden)"
        }
        println msg
    }

    private void addLogFile(Provider<FileSystemLocation> prop, String argSwitch, List<String> argList) {
        argList.add('-logFile')
        if(prop.isPresent()) {
            argList.add(prop.get().asFile.absolutePath)
        }
    }

    private void addArgument(Provider<? super Object> prop, String argSwitch, List<String> argList) {
        if(prop.isPresent()) {
            Object val = prop.get()
            if(val instanceof Boolean) {
                if(val) argList.add(argSwitch)
            } else if(val instanceof FileSystemLocation) {
                argList.addAll([argSwitch, val.asFile.absolutePath])
            } else if(val instanceof String) {
                argList.addAll([argSwitch, val])
            }
        }
    }
}
