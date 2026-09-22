package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.api.BuildConfig
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Sync

abstract class CacheSave extends Sync {
    @Internal abstract Property<BuildConfig> getBuildConfig()
    @Input abstract Property<String> getKey()

    CacheSave() {
        doFirst {
            if(!buildConfig.get().unity.sharedCachePath.isPresent()) {
                throw new GradleException("unity.sharedCachePath must be set to use the cacheSave build step")
            }
            println "Saving files to cache '${key.get()}'..."
        }
        from { buildConfig.get().buildCacheProjectPath }
        into { buildConfig.get().unity.sharedCachePath.get().dir(key.get()) }
        doLast {
            File cacheDir = buildConfig.get().unity.sharedCachePath.get().dir(key.get()).asFile
            int count = cacheDir.exists() ? project.fileTree(cacheDir).files.size() : 0
            println "Saved ${count} file(s) to cache '${key.get()}'"
        }
    }
}
