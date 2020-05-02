package com.iptech.gradle.unity.api

import com.iptech.gradle.unity.UnityExtension
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

abstract class BuildConfig {
    @Internal final UnityExtension unity
    private final ObjectFactory objectFactory

    @Input final String name
    @Input abstract Property<String> getPlatform()
    @Internal DomainObjectSet<Closure> steps

    @Inject
    BuildConfig(String name, UnityExtension unity, ObjectFactory objectFactory) {
        this.name = name
        this.unity = unity
        this.steps = objectFactory.domainObjectSet(Closure)
        this.objectFactory = objectFactory
    }

    void steps(Closure stepsClosure) {
        try {
            this.steps.add(stepsClosure)
        } catch(Exception e) {
            GradleScriptException se = new GradleScriptException(e.message, e.cause)
            se.setStackTrace(e.stackTrace)
            throw se
        }
    }

    /*void steps(Action<? super BuildConfig> action) {
        this.steps.add(action)
    }*/

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
        return objectFactory.directoryProperty().value(unity.buildCachePath.map {
            it.dir("Cached-${unity.productName.get().replaceAll(" ", "_")}-${name}")
        })
    }

    @Internal
    DirectoryProperty getBuildDirectory() {
        return objectFactory.directoryProperty().value(unity.buildDirectory.dir(this.name))
    }
}