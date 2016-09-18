package com.github.wrdlbrnft.pomgenerator

import org.gradle.api.artifacts.Configuration

/**
 * Created by Xaver on 17/09/16.
 */
class PomGeneratorExtension {
    String groupId;
    String artifactId;
    String versionName;
    String packaging;
    Configuration[] configurations;
    File outputLocation;

    void groupId(String groupId) {
        this.groupId = groupId
    }

    void artifactId(String artifactId) {
        this.artifactId = artifactId
    }

    void version(String version) {
        this.versionName = version
    }

    void packaging(String packaging) {
        this.packaging = packaging
    }

    void configurations(Configuration[] configurations) {
        this.configurations = configurations
    }

    void outputLocation(File outputLocation) {
        this.outputLocation = outputLocation
    }
}
