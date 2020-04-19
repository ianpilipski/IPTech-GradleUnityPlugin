package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.UnityExecSpec
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.util.ConfigureUtil

class ExecUnityBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['execUnity', 'execUnityMethod']
    }

    @Override
    Boolean getIsTestTask() {
        return false
    }

    @Override
    Iterable<Task> createTasks(String stepName, String taskPrefix, BuildConfig buildConfig, Object args) {
        if(stepName == 'execUnity') {
            return [createExecUnity(taskPrefix, buildConfig, ConfigureUtil.configureUsing((Closure)args[0]))]
        } else {
            List<Object> argObjs = (List<Object>)args
            String staticMethod = (String)argObjs.remove(0)
            Closure configureClosure
            if(argObjs && argObjs.last() instanceof Closure) {
                configureClosure = (Closure)argObjs.removeLast()
            }
            List<String> additionalArgs = (List<Object>)argObjs

            return [createExecUnityMethod(taskPrefix, buildConfig, staticMethod, additionalArgs, configureClosure)]
        }
    }

    Task createExecUnity(String taskPrefix, BuildConfig buildConfig, Action<? super UnityExecSpec> execSpec) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            inputs.files(project.provider({
                ConfigurableFileTree ft = buildConfig.unity.mainUnityProjectFileTree.clone()
                ft.setDir(buildConfig.mirrordProjectPath)
                return ft
            }))

            doLast {
                buildConfig.execUnity(execSpec)
            }
        }
    }

    Task createExecUnityMethod(String taskPrefix, BuildConfig buildConfig, String staticMethod, List<String> additionalArgs, Closure configureClosure) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            doLast {
                buildConfig.execUnity {
                    List<String> args = [
                            '-batchmode', '-quit', '-nographics',
                            '-executeMethod', staticMethod
                    ]
                    if(additionalArgs?.size()>0) {
                        args.addAll(additionalArgs)
                    }
                    arguments(args)
                }
            }

            if(configureClosure) {
                configureClosure.delegate = delegate
                configureClosure.resolveStrategy = Closure.DELEGATE_FIRST
                configureClosure.call(delegate)
            }
        }
    }
}
