# IPTech-GradleUnityPlugin
A gradle plugin that coordinates the building of Unity3d projects

## Sharing a warmed Library cache across checkouts

Each build type mirrors the Unity project into its own `build-cache/Cached-{productName}-{buildType}`
directory and preserves `Library/**` there between runs, but a fresh branch checkout/worktree has
no cache to preserve, so its first build always pays for a cold Unity import.

To avoid that, use the generic `cacheSave`/`cacheRestore` build steps to publish and seed named
caches (e.g. a warmed `Library` folder) from `unity.sharedCachePath`, which defaults to
`~/.iptech-unity-plugin/sharedCache` and can be overridden to any absolute directory (e.g. shared
network storage reachable from every branch checkout/agent):

```groovy
unity {
    sharedCachePath = file('/Volumes/BuildCache/unity-shared-cache')

    buildTypes {
        // Run manually (e.g. in CI after merges to main) to refresh the shared cache
        PrewarmAndroidLibrary {
            platform = 'Android'
            steps {
                importProject()
                cacheSave {
                    key = 'android-library'
                    include 'Library/**'
                }
            }
        }

        DebugAndroid {
            platform = 'Android'
            steps {
                cacheRestore { key = 'android-library' }
                importProject()
                // ...
            }
        }
    }
}
```

`cacheSave` syncs the matched files into `sharedCachePath/{key}`, removing anything stale under
that key. `cacheRestore` copies `sharedCachePath/{key}` back into the build type's cache directory
without touching anything else there, and is a no-op if that key hasn't been saved yet. Keys are
plain strings chosen by the build author — the plugin does no locking, so avoid using the same key
from builds that may run concurrently.

TODO:
> Add logging to log files  
> Add tee logging option to console + files  
> 
