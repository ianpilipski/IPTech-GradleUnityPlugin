package com.iptech.gradle.unity

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.internal.BuildStepExecutor
import com.iptech.gradle.unity.internal.BuildStepManager
import com.iptech.gradle.unity.internal.buildsteps.*
import com.iptech.gradle.unity.tasks.ExecUnity
import com.iptech.gradle.unity.tasks.ExtractUnityFiles
import com.iptech.gradle.unity.tasks.InstallUnityFilesToProject
import com.iptech.gradle.unity.tasks.MirrorProject
import com.iptech.gradle.unity.tasks.ValidateConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete

class UnityPlugin implements Plugin<Project> {
    private Project project
    private UnityExtension unityExtension
    private Task validateConfigurationTask
    private Task extractUnityFilesTask
    private Task installUnityFilesTask
    private BuildStepManager buildStepManager
    private BuildConfig defaultBuildConfig

    @Override
    void apply(Project project) {
        this.project = project
        this.buildStepManager = new BuildStepManager()
        applyPluginDependencies()
        createUnityExtension()
        registerBuildSteps()
        establishConventions()
        createTasks()
    }

    private void applyPluginDependencies() {
        if(!project.pluginManager.hasPlugin('base')) {
            project.pluginManager.apply('base')
        }

        if(!project.pluginManager.hasPlugin('com.iptech.gradle.nunit-plugin')) {
            project.pluginManager.apply('com.iptech.gradle.nunit-plugin')
        }

        if(!project.pluginManager.hasPlugin('com.iptech.gradle.xcode-plugin')) {
            project.pluginManager.apply('com.iptech.gradle.xcode-plugin')
        }
    }

    private UnityExtension createUnityExtension() {
        unityExtension = project.extensions.create('unity', UnityExtension, project, buildStepManager)
    }

    private void registerBuildSteps() {
        unityExtension.registerBuildStep(new ImportProjectBuildStep())
        unityExtension.registerBuildStep(new RunTestsBuildStep())
        unityExtension.registerBuildStep(new ExecUnityBuildStep())
        unityExtension.registerBuildStep(new ArchiveXcodeProject())
        unityExtension.registerBuildStep(new ExportXcodeArchive())
        unityExtension.registerBuildStep(new InstallProvisioningProfiles())
        unityExtension.registerBuildStep(new BuildGradleProject())
        unityExtension.registerBuildStep(new TestFlightSteps())
    }

    private void establishConventions() {

        project.tasks.withType(ExecUnity).configureEach { ExecUnitySpec t ->
            ignoreExitValue.convention(false)
            userName.convention(unityExtension.userName)
            password.convention(unityExtension.password)
            unityCmdPath.convention(unityExtension.unityCmdPath)
            environment.convention(project.provider( {[:] << System.getenv()}))
            unityProjectFilter.convention(unityExtension.unityProjectFilter)
        }
        project.tasks.withType(ValidateConfig).configureEach {
            it.unity = unityExtension
        }
    }

    private void createTasks() {
        //addBuildAllRule(project)

        project.tasks.create('deleteUnityBuildCache', Delete) {
            group 'Build'
            description 'Deletes the unity build cache directory'
            delete unityExtension.buildCachePath
        }

        validateConfigurationTask = project.tasks.create('validateUnityConfiguration', ValidateConfig)
        extractUnityFilesTask = project.tasks.create('extractUnityFiles', ExtractUnityFiles).dependsOn(validateConfigurationTask)
        installUnityFilesTask = project.tasks.create('installUnityFiles', InstallUnityFilesToProject).dependsOn(extractUnityFilesTask)

        unityExtension.buildTypes.all this.&buildTypeAdded
    }

    private void addBuildAllRule(Project project) {
        UnityExtension config = this

        String rulePrefix = 'buildAll'
        project.tasks.addRule("Pattern: ${rulePrefix}[platform]") { String taskName ->
            if (taskName.startsWith(rulePrefix)) {
                String platform = taskName.substring(rulePrefix.length())
                project.tasks.create(taskName) {
                    if(platform) {
                        dependsOn config.buildTypes.findAll { it.platform == platform }.collect { "build${it.name}" }.toArray()
                    } else {
                        dependsOn project.unity.buildTypes.collect { "build${it.name}" }.toArray()
                    }
                }
            }
        }
    }

    protected void buildTypeAdded(BuildConfig bt) {
        if(!defaultBuildConfig) defaultBuildConfig = bt

        Task checkTask    = project.tasks.create("check${bt.name}") { group 'verification' }
        Task beginTask    = project.tasks.create("unityBegin${bt.name}").dependsOn(validateConfigurationTask)
        Task mirrorTask   = project.tasks.create("step_000_${bt.name}_mirrorProject", MirrorProject) { buildConfig = bt }
        Task endTask      = project.tasks.create("unityEnd${bt.name}")
        Task buildTask    = project.tasks.create("build${bt.name}") { group 'build' }
        Task assembleTask = project.tasks.create("assemble${bt.name}") { group 'build' }

        BuildStepExecutor buildStepExecutor = new BuildStepExecutor(buildStepManager, bt, mirrorTask, endTask, checkTask)
        bt.steps.all { Closure stepClosure -> buildStepExecutor.evaluateClosure(stepClosure) }

        buildTask.dependsOn(
            endTask.dependsOn(
                mirrorTask.dependsOn(
                    beginTask, installUnityFilesTask
                )
            )
        )
        assembleTask.dependsOn(buildTask, checkTask)

        //hook the lifecycle build task to the default build
        if(defaultBuildConfig == bt) {
            project.tasks.assemble.dependsOn(assembleTask)
            project.tasks.check.dependsOn(checkTask)
        }
    }
}

