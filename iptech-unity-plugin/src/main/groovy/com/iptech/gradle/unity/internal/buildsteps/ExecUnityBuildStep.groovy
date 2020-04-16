package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.UnityExecSpec
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.util.ConfigureUtil

class ExecUnityBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['execUnity', 'execUnityMethod']
    }

    @Override
    Iterable<Task> createTasks(String stepName, String taskPrefix, BuildConfig buildConfig, Object args) {
        if(stepName == 'execUnity') {
            return [createExecUnity(taskPrefix, buildConfig, ConfigureUtil.configureUsing((Closure)args[0]))]
        } else {
            List<String> additionalArgs = (List<String>) args
            String staticMethod = additionalArgs.get(0)
            additionalArgs.remove(0)
            return [createExecUnityMethod(taskPrefix, buildConfig, staticMethod, additionalArgs)]
        }
    }

    Task createExecUnity(String taskPrefix, BuildConfig buildConfig, Action<? super UnityExecSpec> execSpec) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            doLast {
                inputs.files(project.provider({
                    project.fileTree(dir: buildConfig.mirrordProjectPath, includes: ['Assets', 'ProjectSettings', 'Packages'])
                }))
                buildConfig.execUnity(execSpec)
            }
        }
    }

    Task createExecUnityMethod(String taskPrefix, BuildConfig buildConfig, String staticMethod, List<String> additionalArgs) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            doLast {
                buildConfig.execUnity(new Action<UnityExecSpec>() {
                    @Override
                    void execute(UnityExecSpec unityExecSpec) {
                        List<String> args = [
                                '-batchmode', '-quit', '-nographics',
                                '-executeMethod', staticMethod
                        ]
                        if(additionalArgs?.size()>0) args.addAll(additionalArgs)
                        unityExecSpec.arguments(args)
                    }
                })
            }
        }
    }
}
