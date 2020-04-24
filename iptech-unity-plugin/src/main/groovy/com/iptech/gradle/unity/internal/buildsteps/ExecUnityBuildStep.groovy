package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.tasks.ExecUnity
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
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

    Task execUnity(String taskPrefix, BuildConfig buildConfig, Closure execSpec) {
        return createExecUnity(taskPrefix, buildConfig, ConfigureUtil.configureUsing(execSpec))
    }

    Task execUnityMethod(String taskPrefix, BuildConfig buildConfig, String staticMethod, Closure configClosure) {
        return createExecUnityMethod(taskPrefix, buildConfig, staticMethod, configClosure)
    }

    private Task createExecUnity(String taskPrefix, BuildConfig buildConfig, Action<? super ExecUnitySpec> execSpec) {
        return buildConfig.unity.project.tasks.create(taskPrefix) {
            inputs.files(project.provider({
                ConfigurableFileTree ft = buildConfig.unity.mainUnityProjectFileTree.clone()
                ft.setDir(buildConfig.buildCacheProjectPath)
                return ft
            }))

            doLast {
                buildConfig.execUnity(execSpec)
            }
        }
    }

    Task createExecUnityMethod(String taskPrefix, BuildConfig buildConfig, String staticMethod, Closure configureClosure) {
        Closure additionalConfig = {
            projectPath = buildConfig.buildCacheProjectPath
            buildTarget = buildConfig.buildTarget
            arguments.addAll([
                '-batchmode', '-quit', '-nographics',
                '-executeMethod', staticMethod
            ])
        }
        return buildConfig.unity.project.tasks.create(taskPrefix, ExecUnity.class, configureClosure >> additionalConfig )
    }
}
