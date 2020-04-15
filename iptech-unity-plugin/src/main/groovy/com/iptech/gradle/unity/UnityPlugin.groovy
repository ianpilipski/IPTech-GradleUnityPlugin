package com.iptech.gradle.unity

import com.iptech.gradle.unity.internal.BuildStepManager
import com.iptech.gradle.unity.internal.buildsteps.ExecUnityBuildStep
import com.iptech.gradle.unity.internal.buildsteps.ImportProjectBuildStep
import com.iptech.gradle.unity.internal.buildsteps.RunTestsBuildStep
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Plugin

@CompileStatic
class UnityPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if(!project.pluginManager.hasPlugin('base')) {
            project.pluginManager.apply('base')
        }

        if(!project.pluginManager.hasPlugin('com.iptech.gradle.nunit-plugin')) {
            project.pluginManager.apply('com.iptech.gradle.nunit-plugin')
        }

        UnityExtension unity = project.extensions.create('unity', UnityExtension, project, new BuildStepManager())
        unity.buildStepManager.registerBuildStep(new ImportProjectBuildStep())
        unity.buildStepManager.registerBuildStep(new RunTestsBuildStep())
        unity.buildStepManager.registerBuildStep(new ExecUnityBuildStep())
    }
}

