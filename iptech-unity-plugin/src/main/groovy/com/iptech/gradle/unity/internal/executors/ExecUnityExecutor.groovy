package com.iptech.gradle.unity.internal.executors

import com.iptech.gradle.unity.UnityExtension
import com.iptech.gradle.unity.api.ExecUnitySpec

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
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

        addRequiredArguments(spec, argList)
        addUserNameArgument(spec, argList)
        printCommand(spec, argList)

        // add this after so it's not logged
        addPasswordArgument(spec, argList)

        return execOperations.exec {
            if(spec.logFile.isPresent()) {
                FileOutputStream fos = new FileOutputStream(spec.logFile.get().asFile)
                standardOutput = fos
                errorOutput = fos
            }
            executable = unityCmdFile.getAbsolutePath()
            args = argList
            ignoreExitValue = (spec.ignoreExitValue.isPresent() ? spec.ignoreExitValue.get() : false)
            if(spec.environment.isPresent()) {
                environment = spec.environment.get()
            }
        }
    }

    private void addRequiredArguments(ExecUnitySpec spec, List<String> argList) {
        argList.addAll([
            '-projectPath', spec.projectPath.get().asFile.absolutePath,
            '-buildTarget', spec.buildTarget.get(),
            '-logFile'
        ])
        argList.addAll(spec.arguments.get())
    }

    private void printCommand(ExecUnitySpec spec, List<String> argList) {
        String msg = "Calling: ${spec.unityCmdPath.get().asFile.absolutePath} ${argList.join(' ')}"
        if(spec.password.isPresent()) {
            msg += " -password (hidden)"
        }
        println msg
    }

    private void addUserNameArgument(ExecUnitySpec spec, List<String> argList) {
        if(spec.userName.isPresent()) {
            argList.addAll(['-username', spec.userName.get()])
        }
    }

    private void addPasswordArgument(ExecUnitySpec spec, List<String> argList) {
        if(spec.password.isPresent()) {
            argList.addAll(['-password', spec.password.get()])
        }
    }
}
