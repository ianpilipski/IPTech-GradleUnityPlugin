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
    private final BuildStepManager buildStepManager
    private final BuildConfig buildConfig
    private final Task endTask
    private final Task checkTask
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

    @Override
    Object invokeMethod(String name, Object args) {
        Task t = tryExecBuildStep(name, args)
        if(t) return t

        throw new GradleException("No build step named ${name}, could be found.")
    }

    private Task tryExecBuildStep(String name, Object args) {
        if (buildStepManager.hasBuildStep(name)) {
            BuildStep bs = buildStepManager.getBuildStep(name)
            stepCount++
            String stepString = "000${stepCount}".substring(stepCount.toString().length())
            String taskName = "step_${stepString}_${buildConfig.name}_${name}"

            Method m = bs.class.methods.find { it.name == name }
            if(!m) throw new GradleException("BuildStep ${name} has bad code, the task generator could not be found.")

            Integer index = 0
            List<Object> typedArgs = [ taskName, buildConfig ]
            m.parameterTypes.each {
                //println "parameterType ${it}"
                if (index>1) {
                    if((index-2) < args.size()) {
                        try {
                            if (args[index - 2] instanceof GString) {
                                typedArgs.add(args[index - 2].toString())
                            } else {
                                typedArgs.add(it.cast(args[index - 2]))
                            }
                        } catch (ClassCastException ex) {
                            throw new GradleScriptException(ex.message + ", for method ${name}", ex.cause)
                        }
                    } else {
                        typedArgs.add(null)
                    }
                }
                index++
            }

            Task retTask
            try {
                retTask = bs.invokeMethod(name, typedArgs)
            } catch(Exception e) {
                throw new GradleScriptException(e.message, e)
            }


            Iterable<Task> createdTasks = [retTask]
            //Iterable<Task> createdTasks = bs.createTasks(name, taskName, buildConfig, args)
            if (createdTasks) {
                createdTasks.each {
                    endTask.dependsOn(it)
                    it.dependsOn(lastTaskCreated)
                    if (bs.isTestTask) {
                        checkTask.dependsOn(it)
                    }
                }
                lastTaskCreated = createdTasks.last()
            }
            return createdTasks.last()
        }
        return null
    }
}
