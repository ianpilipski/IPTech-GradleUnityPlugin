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
            return [createExecUnityMethod(taskPrefix, buildConfig, (String)args[0])]
        }
    }

    Task createExecUnity(String taskPrefix, BuildConfig buildConfig, Action<? super UnityExecSpec> execSpec) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            doLast {
                buildConfig.execUnity(execSpec)
            }
        }
    }

    Task createExecUnityMethod(String taskPrefix, BuildConfig buildConfig, String staticMethod) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            doLast {
                buildConfig.execUnity(new Action<UnityExecSpec>() {
                    @Override
                    void execute(UnityExecSpec unityExecSpec) {
                        unityExecSpec.arguments(['-batchmode', '-quit', '-nographics', '-executeMethod', staticMethod ])
                    }
                })
            }
        }
    }
}
