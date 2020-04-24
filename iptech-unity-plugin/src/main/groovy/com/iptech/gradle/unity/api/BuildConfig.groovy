package com.iptech.gradle.unity.api

import com.iptech.gradle.unity.UnityExtension
import org.gradle.api.DomainObjectSet
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

import javax.inject.Inject

abstract class BuildConfig {
    @Internal final UnityExtension unity
    private final ObjectFactory objectFactory

    @Input final String name
    @Input abstract Property<String> getPlatform()
    @Internal @Optional DomainObjectSet<Closure> steps

    @Inject
    BuildConfig(String name, UnityExtension unity, ObjectFactory objectFactory) {
        this.name = name
        this.unity = unity
        this.steps = objectFactory.domainObjectSet(Closure.class)
        this.objectFactory = objectFactory
    }

    void steps(Closure stepsClosure) {
        this.steps.add(stepsClosure)
    }

    @Internal
    Provider<String> getBuildTarget() {
        return platform.map {
            if(it == 'Amazon') {
                return 'Android'
            }
            return it
        }
    }

    @Internal
    DirectoryProperty getBuildCacheProjectPath() {
        DirectoryProperty retVal = objectFactory.directoryProperty()
        retVal.value(unity.buildCachePath.dir("Cached-UnityProject-${name}"))
        return retVal
    }

    @Internal
    DirectoryProperty getBuildDirectory() {
        DirectoryProperty retVal = objectFactory.directoryProperty()
        retVal.value(unity.buildDirectory.dir(this.name))
        return retVal
    }

    @Internal
    DirectoryProperty getLogDir() {
        DirectoryProperty retVal = objectFactory.directoryProperty()
        retVal.value(buildDirectory.dir('logs'))
        return retVal
    }

    @Internal
    DirectoryProperty getArtifactDir() {
        DirectoryProperty retVal = objectFactory.directoryProperty()
        retVal.value(buildDirectory.dir('artifacts'))
        return retVal
    }
}