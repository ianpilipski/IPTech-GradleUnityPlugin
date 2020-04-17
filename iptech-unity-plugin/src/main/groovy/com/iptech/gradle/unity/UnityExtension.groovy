package com.iptech.gradle.unity

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.UnityExecSpec
import com.iptech.gradle.unity.internal.BuildStepExecutor
import com.iptech.gradle.unity.internal.BuildStepManager
import com.iptech.gradle.unity.tasks.ExecUnity
import com.iptech.gradle.unity.tasks.MirrorProject
import com.iptech.gradle.unity.tasks.ValidateConfig
import org.gradle.api.*
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.util.ConfigureUtil

class UnityExtension {
    @Internal final Project project
    private Task validateConfigurationTask

    @Internal BuildStepManager buildStepManager

    @Input String unityProductName  //TODO: potentially read this from the project settings
    @Input String unityCmdPath
    @Input String unityUserName
    @Input String unityPassword
    @Input String mirroredUnityProject = 'MirroredUnityProject'
    @Input String mirroredPathRoot = 'buildCache'
    @Input Boolean disableUnityCacheSrv = true
    @Input String androidIL2CPPFlag = true

    @Input String buildNumber
    @Input @Optional String branch
    @InputFiles ConfigurableFileTree mainUnityProjectFileTree

    @Nested
    final NamedDomainObjectContainer<BuildConfig> buildTypes

    UnityExtension(Project project, BuildStepManager buildStepManager) {
        this.project = project
        this.buildStepManager = buildStepManager
        initializePropertyValues()
        buildTypes = CreateBuildConfigContainerWithFactory(project)
        registerGlobalTasks(project)

        buildTypes.all { bt -> this.buildTypeAdded(project, bt) }
    }

    void buildTypes(Closure configClosure) {
        buildTypes.configure(configClosure)
    }

    void initializePropertyValues() {
        buildNumber = '0000'
        //TODO: change these properties to be more gradle like?
        unityUserName = project.hasProperty('UNITY_USERNAME') ? project.getProperty('UNITY_USERNAME') : null
        unityPassword = project.hasProperty('UNITY_PASSWORD') ? project.getProperty('UNITY_PASSWORD') : null

        mainUnityProjectFileTree = project.fileTree(
            dir: project.projectDir,
            include: ['Assets/**', 'ProjectSettings/**', 'Packages/**']
        )
    }

    private NamedDomainObjectContainer<BuildConfig> CreateBuildConfigContainerWithFactory(Project project) {
        UnityExtension self = this
        return project.container(BuildConfig, new NamedDomainObjectFactory<BuildConfig>() {
            BuildConfig create(String name) {
                project.objects.newInstance(BuildConfig, name, self)
            }
        })
    }

    @Input
    String getAppVersion() {
        String projectSettings = new File(project.projectDir, 'ProjectSettings/ProjectSettings.asset').text
        def matcher = projectSettings =~ /(?m)bundleVersion: ([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)/
        if(matcher.getCount()>0) {
            return matcher[0][1]
        }
        return '0.1.0'
    }

    ExecResult exec(Closure closure) {
        return exec(ConfigureUtil.configureUsing(closure))
    }

    ExecResult exec(Action<? super UnityExecSpec> action) {
        return ExecUnity.exec(project, action)
    }



    private void registerGlobalTasks(Project project) {
        addBuildAllRule(project)

        project.tasks.create('cleanUnityBuildCache', Delete) { Delete d ->
            d.group 'Build'
            d.description 'Deletes the unity build cache directory'
            d.delete this.mirroredPathRoot
        }
        validateConfigurationTask = project.tasks.create('validateUnityConfiguration', ValidateConfig) {
            unityExtension = this
        }
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

    protected void buildTypeAdded(Project project, BuildConfig bt) {
        // BuildType is created, but properties are not configured yet
        // So you need to reference the properties lazily

        Task beginTask = project.tasks.create("unityBegin${bt.name}").dependsOn(validateConfigurationTask)
        Task mirrorUnityProject = project.tasks.create("step_000_${bt.name}_mirrorProject", MirrorProject) {
            buildConfig = bt
        }
        Task endTask = project.tasks.create("unityEnd${bt.name}")

        Task buildTask = project.tasks.create("build${bt.name}", MirrorProject) {
            group 'build'
            description 'calls unity to build the configuration'
            buildConfig.set(bt)
        }

        BuildStepExecutor buildStepExecutor = new BuildStepExecutor(buildStepManager, bt, mirrorUnityProject, endTask)
        bt.steps.all { Closure stepClosure ->
            buildStepExecutor.evaluateClosure(stepClosure)
        }

        buildTask.dependsOn(
                endTask.dependsOn(
                        mirrorUnityProject.dependsOn(
                                beginTask
                        )
                )
        )
    }
}
