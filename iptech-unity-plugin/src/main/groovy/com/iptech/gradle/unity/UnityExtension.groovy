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

class UnityExtension {
    private static final String GRADLE_PROPERTY_USERNAME = 'iptech.unity.username'
    private static final String GRALDE_PROPERTY_PASSWORD = 'iptech.unity.password'

    @Internal final Project project
    private Task validateConfigurationTask
    private BuildConfig defaultBuildConfig

    @Internal BuildStepManager buildStepManager

    private String fallbackBundleVersion
    private String fallbackProductName
    private String bundleVersion
    private String productName
    @Input String unityCmdPath
    @Input @Optional String userName
    @Input @Optional String password
    @Input String mirroredUnityProject = 'MirroredUnityProject'
    @Input String mirroredPathRoot = 'buildCache'
    @Input String buildNumber
    private ConfigurableFileTree mainUnityProjectFileTree

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

    String getUnityProjectPath() {
        return mainUnityProjectFileTree.dir.path
    }

    void setUnityProjectPath(String projectPath) {
        mainUnityProjectFileTree.setDir(projectPath)
        updateFallbackProductNameAndBundleVersion()
    }

    @Input
    String getBundleVersion() {
        return bundleVersion ?: fallbackBundleVersion
    }

    void setBundleVersion(String newVersion) {
        bundleVersion = newVersion
    }

    @Input
    String getProductName() {
        return productName ?: fallbackProductName
    }

    void setProductName(String newProductName) {
        productName = newProductName
    }

    @InputFiles
    ConfigurableFileTree getMainUnityProjectFileTree() {
        return mainUnityProjectFileTree
    }

    void setMainUnityProjectFileTree(ConfigurableFileTree newFileTree) {
        mainUnityProjectFileTree = newFileTree
        updateFallbackProductNameAndBundleVersion()
    }

    void initializePropertyValues() {
        buildNumber = '0000'
        initializeMainUnityProjectTree()
        updateFallbackProductNameAndBundleVersion()
        initializeUserNamePassword()
    }

    private void updateFallbackProductNameAndBundleVersion() {
        File f = new File(getUnityProjectPath(), 'ProjectSettings/ProjectSettings.asset')
        if(f.exists()) {
            String projectSettings = f.text
            def productMatcher = projectSettings =~ /(?m)productName: (.*)?/
            if(productMatcher.getCount()>0) {
                fallbackProductName = productMatcher[0][1]
            }

            def versionMatcher = projectSettings =~ /(?m)bundleVersion: ([0-9\.]+)?/
            if(versionMatcher.getCount()>0) {
                fallbackBundleVersion = versionMatcher[0][1]
            }
        }
    }

    private void initializeUserNamePassword() {
        userName = project.hasProperty(GRADLE_PROPERTY_USERNAME) ? project.getProperty(GRADLE_PROPERTY_USERNAME) : null
        password = project.hasProperty(GRALDE_PROPERTY_PASSWORD) ? project.getProperty(GRALDE_PROPERTY_PASSWORD) : null
    }

    private void initializeMainUnityProjectTree() {
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
            unity = this
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
        if(!defaultBuildConfig) defaultBuildConfig = bt

        Task checkTask = bt.unity.project.tasks.create("check${bt.name}") {
            group 'verification'
        }

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

        BuildStepExecutor buildStepExecutor = new BuildStepExecutor(buildStepManager, bt, mirrorUnityProject, endTask, checkTask)
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

        Task assembleTask = project.tasks.create("assemble${bt.name}") {
            group 'build'
            dependsOn buildTask, checkTask
        }

        //hook the lifecycle build task to the default build
        if(defaultBuildConfig == bt) {
            project.tasks.assemble.dependsOn(assembleTask)
            project.tasks.check.dependsOn(checkTask)
        }
    }
}
