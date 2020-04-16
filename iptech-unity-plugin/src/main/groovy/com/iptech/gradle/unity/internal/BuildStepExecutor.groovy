package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.gradle.api.Task

class BuildStepExecutor {
    private final BuildStepManager buildStepManager
    private final BuildConfig buildConfig
    private final Task endTask
    private Task lastTaskCreated
    private Integer stepCount

    BuildStepExecutor(BuildStepManager buildStepManager, BuildConfig buildConfig, Task dependsOnTask, Task endTask) {
        this.buildStepManager = buildStepManager
        this.buildConfig = buildConfig
        this.lastTaskCreated = dependsOnTask
        this.endTask = endTask
        this.stepCount = 0
    }

    void evaluateClosure(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
    }

    Object invokeMethod(String name, Object args) {
        if(!tryExecBuildStep(name, args)) {
            return metaClass.invokeMethod(this, name, args)
        }
    }

    @Override
    Object getProperty(String name) {
        //println "getProperty: $name"
        MetaProperty property = metaClass.hasProperty(this, name)
        if(property) {
            return property.getProperty(this)
        }

        tryExecBuildStep(name, null)
        return null
    }

    private Boolean tryExecBuildStep(String name, Object args) {
        //println "tryExecBuildStep: ${name} args: $args"
        if(buildStepManager.hasBuildStep(name)) {
            BuildStep bs = buildStepManager.getBuildStep(name)
            stepCount++
            String stepString = "000${stepCount}".substring(stepCount.toString().length())
            String taskName = "step_${stepString}_${buildConfig.name}_${name}"
            Iterable<Task> createdTasks = bs.createTasks(name, taskName, buildConfig, args)
            if (createdTasks) {
                createdTasks.each {
                    endTask.dependsOn(it)
                    it.dependsOn(lastTaskCreated)
                }
                lastTaskCreated = createdTasks.last()
            }
            return true
        }
        return false
    }
}
