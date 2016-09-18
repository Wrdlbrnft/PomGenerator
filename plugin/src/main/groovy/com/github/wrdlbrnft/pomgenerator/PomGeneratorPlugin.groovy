package com.github.wrdlbrnft.pomgenerator

import groovy.xml.MarkupBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.tasks.Delete

/**
 * Created by Xaver on 17/09/16.
 */
class PomGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('pomGenerator', PomGeneratorExtension);

        project.afterEvaluate {

            def cleanTask = project.tasks.findByName('clean')
            if (cleanTask == null) {
                cleanTask = project.task('clean') {}
            }

            def buildTask = project.tasks.findByName('build')
            if (buildTask == null) {
                buildTask = project.task('build') {}
            }

            def extension = project.pomGenerator as PomGeneratorExtension
            validateExtension(project, extension);

            def cleanJsonFileTask = createCleanTaskForExtension(project, cleanTask, extension);
            createCreateTaskForExtension(project, buildTask, cleanJsonFileTask, extension);
        }
    }

    static def createCreateTaskForExtension(
            Project project,
            Task buildTask,
            Task cleanJsonFileTask,
            PomGeneratorExtension extension) {

        def taskName = 'create' + capitalizeFirstLetter(project.name) + 'GeneratedPomFile'
        def createJsonFileTask = project.task(taskName) << {
            def sw = new StringWriter()
            sw.append('<?xml version="1.0" encoding="UTF-8"?>\n')
            def xml = new MarkupBuilder(sw)
            xml.setDoubleQuotes(true)
            xml.project(xmlns: "http://maven.apache.org/POM/4.0.0", 'xsi:schemaLocation': "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd", 'xmlns:xsi': "http://www.w3.org/2001/XMLSchema-instance") {
                modelVersion("4.0.0")
                groupId(extension.groupId)
                artifactId(extension.artifactId)
                version(extension.versionName)
                if (extension.packaging != null) {
                    packaging(extension.packaging)
                }
                dependencies {
                    extension.configurations.each { configuration ->
                        configuration.allDependencies.each { dep ->
                            if (dep.group != null && dep.name != null && dep.version != null) {
                                dependency {
                                    groupId(dep.group)
                                    artifactId(dep.name)
                                    version(dep.version)
                                    final dependencyArtifact = dep.artifacts[0] as DependencyArtifact;
                                    if (dependencyArtifact != null) {
                                        if (dependencyArtifact.classifier != null) {
                                            classifier(dependencyArtifact.classifier)
                                        }
                                        if (dependencyArtifact.extension != null) {
                                            type(dependencyArtifact.extension)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            extension.outputLocation.withWriter { out ->
                out.write(sw.toString())
            }
        }
        createJsonFileTask.dependsOn cleanJsonFileTask
        buildTask.dependsOn createJsonFileTask
        createJsonFileTask
    }

    static def createCleanTaskForExtension(Project project, Task cleanTask, PomGeneratorExtension extension) {
        def taskName = 'clean' + capitalizeFirstLetter(project.name) + 'GeneratedPomFile'
        def cleanJsonFileTask = project.task(taskName, type: Delete) {
            delete extension.outputLocation
        }
        cleanTask.dependsOn cleanJsonFileTask
        cleanJsonFileTask
    }

    static def validateExtension(Project project, PomGeneratorExtension extension) {
        if (extension.groupId == null) {
            throw new ProjectConfigurationException('GroupId for PomGenerator is not set.', null)
        }
        if (extension.artifactId == null) {
            throw new ProjectConfigurationException('ArtifactId for PomGenerator is not set.', null)
        }
        if (extension.versionName == null) {
            throw new ProjectConfigurationException('Version for PomGenerator is not set.', null)
        }
        if (extension.configurations == null || extension.configurations.length == 0) {
            def compileConfiguration = project.configurations.compile
            if (compileConfiguration == null) {
                throw new ProjectConfigurationException('You need to set a specific configuration for the pom generator, the default compile configuration could not be found.', null)
            }
            extension.configurations = [compileConfiguration]
        }
        if (extension.outputLocation == null) {
            extension.outputLocation = project.file(extension.artifactId + '-' + extension.versionName + '.pom')
        }
    }

    static def capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1)
    }
}
