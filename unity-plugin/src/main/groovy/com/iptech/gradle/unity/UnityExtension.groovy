package com.iptech.gradle.unity

import com.iptech.gradle.unity.api.BuildConfig
import com.iptech.gradle.unity.api.BuildStep
import com.iptech.gradle.unity.api.ExecUnitySpec
import com.iptech.gradle.unity.internal.BuildStepManager
import com.iptech.gradle.unity.internal.UnityProjectSettings
import com.iptech.gradle.unity.internal.executors.ExecUnityExecutor
import org.gradle.api.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.process.ExecResult

abstract class UnityExtension {
    private static final String GRADLE_PROPERTY_USERNAME = 'iptech.unity.username'
    private static final String GRADLE_PROPERTY_PASSWORD = 'iptech.unity.password'
    private static final String GRADLE_PROPERTY_APPLEUSERNAME = 'iptech.unity.appleusername'
    private static final String GRADLE_PROPERTY_APPLEPASSWORD = 'iptech.unity.applepassword'

    @Internal final Project project
    private final ExecUnityExecutor execUnityExecutor
    private BuildStepManager buildStepManager
    private final UnityProjectSettings unityProjectSettings

    @InputDirectory abstract DirectoryProperty getProjectPath()
    @InputFile abstract RegularFileProperty getUnityCmdPath()
    @Input @Optional abstract Property<String> getUserName()
    @Input @Optional abstract Property<String> getPassword()
    @Input @Optional abstract Property<String> getAppleUserName()
    @Input @Optional abstract Property<String> getApplePassword()
    @Internal abstract DirectoryProperty getBuildCachePath()
    @Input abstract Property<String> getBuildNumber()
    @Input @Optional abstract  Property<String> getBundleVersion()
    @Input @Optional abstract Property<String> getProductName()
    @OutputDirectory abstract DirectoryProperty getBuildDirectory()
    @Internal abstract Property<PatternFilterable> getUnityProjectFilter()
    @Input @Optional abstract Property<Boolean> getExemptEncryption()
    @Input @Optional abstract Property<Boolean> getInstallCSharpFiles()
    @Nested final NamedDomainObjectContainer<BuildConfig> buildTypes

    UnityExtension(Project project, BuildStepManager buildStepManager) {
        this.project = project
        this.buildStepManager = buildStepManager
        this.unityProjectSettings = project.objects.newInstance(UnityProjectSettings, projectPath)
        this.execUnityExecutor = project.objects.newInstance(ExecUnityExecutor, this)
        buildTypes = CreateBuildConfigContainerWithFactory(project)

        initializePropertyValues()
    }

    void projectPath(String value) {
        projectPath = project.layout.projectDirectory.dir(value)
    }

    @InputFiles
    FileTree getMainUnityProjectFileTree() {
        return projectPath.asFileTree.matching(unityProjectFilter.get())
    }

    void initializePropertyValues() {
        PatternSet ps = new PatternSet()
        ps.include(['Assets/**', 'ProjectSettings/**', 'Packages/**'])
        unityProjectFilter.convention(ps)

        projectPath.convention(project.layout.projectDirectory)
        buildNumber.convention('0000')
        productName.convention(unityProjectSettings.getProductName())
        bundleVersion.convention(unityProjectSettings.getBundleVersion())
        buildCachePath.convention(project.layout.projectDirectory.dir('build-cache'))
        buildDirectory.convention(project.layout.buildDirectory.dir('unity'))
        exemptEncryption.convention(false)
        installCSharpFiles.convention(true)
        
        userName.convention(project.provider({
            return project.hasProperty(GRADLE_PROPERTY_USERNAME) ? project.getProperty(GRADLE_PROPERTY_USERNAME) : null
        }))
        password.convention(project.provider({
            return project.hasProperty(GRADLE_PROPERTY_PASSWORD) ? project.getProperty(GRADLE_PROPERTY_PASSWORD) : null
        }))
        appleUserName.convention(project.provider( {
            return project.hasProperty(GRADLE_PROPERTY_APPLEUSERNAME) ? project.getProperty(GRADLE_PROPERTY_APPLEUSERNAME) : null
        }))
        applePassword.convention(project.provider( {
            return project.hasProperty(GRADLE_PROPERTY_APPLEPASSWORD) ? project.getProperty(GRADLE_PROPERTY_APPLEPASSWORD) : null
        }))
    }

    private NamedDomainObjectContainer<BuildConfig> CreateBuildConfigContainerWithFactory(Project project) {
        UnityExtension self = this
        return project.container(BuildConfig, new NamedDomainObjectFactory<BuildConfig>() {
            BuildConfig create(String name) {
                project.objects.newInstance(BuildConfig, name, self)
            }
        })
    }

    void registerBuildStep(BuildStep buildStep) {
        buildStepManager.registerBuildStep(buildStep)
    }

    void registerBuildStepFunction(String name, Closure func, Boolean isTestTask=false) {
        buildStepManager.registerBuildStepFunction(name, func, isTestTask)
    }

    ExecResult exec(ExecUnitySpec action) {
        return execUnityExecutor.exec(action)
    }

    Task dependsOn(final Object... paths) {
        //add depends on the first unity task
        project.validateUnityConfiguration.dependsOn(paths)
    }
}
