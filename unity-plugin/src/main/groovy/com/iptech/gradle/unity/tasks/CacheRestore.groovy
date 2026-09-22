package com.iptech.gradle.unity.tasks

import com.iptech.gradle.unity.api.BuildConfig
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class CacheRestore extends Copy {
    @Internal abstract Property<BuildConfig> getBuildConfig()
    @Input abstract Property<String> getKey()

    CacheRestore() {
        doFirst {
            if(!buildConfig.get().unity.sharedCachePath.isPresent()) {
                throw new GradleException("unity.sharedCachePath must be set to use the cacheRestore build step")
            }
        }
        from { buildConfig.get().unity.sharedCachePath.get().dir(key.get()) }
        into { buildConfig.get().buildCacheProjectPath }
    }
}
