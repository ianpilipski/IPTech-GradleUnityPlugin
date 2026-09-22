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
        }
        from { buildConfig.get().buildCacheProjectPath }
        into { buildConfig.get().unity.sharedCachePath.get().dir(key.get()) }
    }
}
