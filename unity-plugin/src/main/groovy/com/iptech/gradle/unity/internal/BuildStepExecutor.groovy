package com.iptech.gradle.unity.internal

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.Task
import org.gradle.internal.UncheckedException

import java.lang.reflect.Method
import java.security.Security

class BuildStepExecutor {
    private final @Delegate BuildConfig buildConfig

    private final BuildStepManager buildStepManager
    private final Task endTask
    protected final Task checkTask
    private Task lastTaskCreated
    private Integer stepCount
    private Object originalDelegate


    BuildStepExecutor(
        BuildStepManager buildStepManager,
        BuildConfig buildConfig,
        Task dependsOnTask, Task endTask, Task checkTask
    ) {
        this.buildStepManager = buildStepManager
        this.buildConfig = buildConfig
        this.lastTaskCreated = dependsOnTask
        this.endTask = endTask
        this.stepCount = 0
        this.checkTask = checkTask

        buildStepManager.registeredBuildStepFuncs.each {
            buildConfig.ext[it.key] = createBuildStepFuncWrapper(it.value.func, it.value.isTestTask)
        }
    }

    Closure createBuildStepFuncWrapper(Closure buildStepFunc, Boolean isTestTask) {
        buildStepFunc.curry("one", "two")
        return { Object... args ->
            stepCount++
            String stepString = "000${stepCount}".substring(stepCount.toString().length())
            String taskName = "step_${stepString}_${buildConfig.name}_${name}"

            def inner = buildStepFunc.curry(taskName, buildConfig)
            def retTask = inner.call(args)

            if (retTask) {
                endTask.dependsOn(retTask)
                retTask.dependsOn(lastTaskCreated)
                if (isTestTask) {
                    checkTask.dependsOn(retTask)
                }
                lastTaskCreated = retTask
            }
            return retTask
        }
    }

    void evaluateClosure(Closure closure) {
        try {
            //action.execute(this)
            originalDelegate = closure.getDelegate()
            closure.setDelegate(this)
            closure.setResolveStrategy(Closure.DELEGATE_FIRST)
            closure()
        } catch(Exception e) {
            throw e // new GradleScriptException(e.message, e)
        }
    }
}
