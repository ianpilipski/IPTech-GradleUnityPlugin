package com.iptech.gradle.unity.internal

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

class UnityProjectSettings {
    final Provider<Directory> unityProjectPathProvider
    final ProviderFactory providerFactory

    @Inject
    UnityProjectSettings(DirectoryProperty unityProjectPathProvider, ProviderFactory providerFactory) {
        this.unityProjectPathProvider = unityProjectPathProvider.getLocationOnly()
        this.providerFactory = providerFactory
    }

    Provider<String> getProductName() {
        return providerFactory.provider( {
            def productMatcher = readProjectSettings() =~ /(?m)productName: (.*)?/
            if (productMatcher.getCount() > 0) {
                return productMatcher[0][1]
            }
            return null
        })
    }

    Provider<String> getBundleVersion() {
        return providerFactory.provider({
            def versionMatcher = readProjectSettings() =~ /(?m)bundleVersion: ([0-9\.]+)?/
            if(versionMatcher.getCount()>0) {
                return versionMatcher[0][1]
            }
            return null
        })
    }

    protected String readProjectSettings() {
        File f = unityProjectPathProvider.get().file('ProjectSettings/ProjectSettings.asset').asFile
        return f.exists() ? f.text : ""
    }
}
