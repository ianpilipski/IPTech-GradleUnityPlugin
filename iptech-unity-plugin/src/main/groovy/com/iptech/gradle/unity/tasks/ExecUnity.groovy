package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.UnityExtension
import com.iptech.gradle.unity.api.UnityExecSpec
import com.iptech.gradle.unity.internal.DefaultUnityExecSpec
import com.iptech.gradle.unity.internal.UnityLog
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*
import org.gradle.api.DefaultTask
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.util.ConfigureUtil

@CompileStatic
class ExecUnity extends DefaultTask {
    protected ExecResult _execResult

    @Internal final Provider<ExecResult> execResult = project.provider({ (ExecResult)_execResult })

    @Input final ListProperty<String> arguments = project.objects.listProperty(String)
    @Input final Property<String> projectPath = project.objects.property(String)
    @Input final Property<String> buildTarget = project.objects.property(String)
    @Input final Property<Boolean> ignoreExitValue = project.objects.property(Boolean)
    @Input @Optional final Property<String> userName = project.objects.property(String)
    @Input @Optional final Property<String> password = project.objects.property(String)
    @Input @Optional final Property<String> logFile = project.objects.property(String)
    @Input @Optional final Property<Object> unityCmdPath = project.objects.property(Object)
    @Internal final MapProperty<String, Object> environment = project.objects.mapProperty(String, Object)

    ExecUnity() {
        UnityExtension unity = (UnityExtension)project.extensions.getByName('unity')
        ignoreExitValue.set(false)
        userName.convention(project.provider({unity.unityUserName}))
        password.convention(project.provider({unity.unityPassword}))
        unityCmdPath.convention(project.provider( {unity.unityCmdPath}))
        environment.convention(project.provider( {[:] << System.getenv()}))
    }

    void environment(String key, Object value) {
        environment.put(key, project.provider({value}))
    }

    @TaskAction
    ExecResult taskExec() {
        def self = this
        _execResult = ExecUnity.exec(project,new Action<UnityExecSpec>() {
            @Override
            void execute(UnityExecSpec unityExecSpec) {
                unityExecSpec.arguments( (List<String>)self.arguments.get() )
                unityExecSpec.projectPath self.projectPath.get()
                unityExecSpec.buildTarget self.buildTarget.get()
                unityExecSpec.ignoreExitValue true
                unityExecSpec.userName self.userName.isPresent() ? self.userName.get() : null
                unityExecSpec.password self.password.isPresent() ? self.password.get() : null
                unityExecSpec.logFile self.logFile.get()
                unityExecSpec.unityCmdPath self.unityCmdPath.get()
                unityExecSpec.environment self.environment.get()
            }
        })

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

    static ExecResult exec(Project project, Action<? super UnityExecSpec> action) {
        UnityExtension unity = (UnityExtension)project.extensions.getByName('unity')

        DefaultUnityExecSpec spec = new DefaultUnityExecSpec()
        spec.unityCmdPath = unity.unityCmdPath
        spec.userName = unity.unityUserName
        spec.password = unity.unityPassword
        action.execute(spec)

        List<String> argList = []
        argList.addAll(
                '-projectPath', "${spec.projectPath}".toString(),
                '-buildTarget', spec.buildTarget
        )

        argList.addAll(spec.arguments)
        argList.add("-logFile")
        argList = (spec.userName!=null) ? (argList + ['-username', spec.userName]) : argList

        File unityCmdFile = project.file(spec.unityCmdPath!=null ? spec.unityCmdPath : unity.unityCmdPath)

        String msg = "Calling: ${unityCmdFile.getAbsolutePath()} ${argList.join(' ')}"
        if(spec.password!=null) {
            msg += " -password (hidden)"
        }
        println msg

        // add this after so it's not logged
        argList = (spec.password!=null) ? (argList + ['-password', spec.password]) : argList

        return project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                if(spec.logFile) {
                    File lf = new File(spec.logFile)
                    FileOutputStream fos = new FileOutputStream(lf)
                    execSpec.setStandardOutput(fos)
                    execSpec.setErrorOutput(fos)
                }
                execSpec.executable unityCmdFile.getAbsolutePath()
                execSpec.args argList
                execSpec.setIgnoreExitValue(spec.ignoreExitValue)
                if(spec.environmentVars) {
                    execSpec.environment spec.environmentVars
                }
            }
        })

    }
}
