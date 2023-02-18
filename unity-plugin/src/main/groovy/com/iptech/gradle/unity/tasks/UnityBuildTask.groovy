package com.iptech.gradle.unity.tasks


import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.internal.UnityLog
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

abstract class UnityBuildTask extends ExecUnity {
    @Input @Optional abstract Property<String> getBundleIdentifier()

    UnityBuildTask() {
        project.afterEvaluate { 
            String bundleId = bundleIdentifier.getOrNull()
            if(bundleId?.size()>0) {
                arguments.addAll(['-bundleIdentifier', bundleId])
            }
        }
    }
}
