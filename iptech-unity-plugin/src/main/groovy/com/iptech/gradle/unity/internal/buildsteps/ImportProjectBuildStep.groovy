package com.iptech.gradle.unity.internal.buildsteps

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.UnityExecSpec
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task

class ImportProjectBuildStep implements BuildStep {

    @Override
    Iterable<String> getNames() {
        return ['importProject']
    }

    @Override
    Iterable<Task> createTasks(String stepName, String taskPrefix, BuildConfig buildConfig, Object args) {
        Project project = buildConfig.unity.project
        Task t = project.task(taskPrefix) {
            inputs.files(project.provider({
                project.fileTree(dir: buildConfig.mirrordProjectPath.path, includes: buildConfig.unity.mainUnityProjectFileTree.getIncludes())
            }))

            outputs.dir {
                project.file("${buildConfig.mirrordProjectPath}/Library/ScriptAssemblies}")
            }

            doLast {
                // This will force the re-compilation of script files
                project.delete "${buildConfig.mirrordProjectPath}/Library/ScriptAssemblies"

                buildConfig.execUnity(new Action<UnityExecSpec>() {
                    @Override
                    void execute(UnityExecSpec unityExecSpec) {
                        unityExecSpec.arguments(['-batchmode', '-quit', '-nographics', '-silent-crashes'])
                    }
                })
            }
        }
        return [t]
    }
}
