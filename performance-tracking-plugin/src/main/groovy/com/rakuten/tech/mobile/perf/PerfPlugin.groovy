package com.rakuten.tech.mobile.perf

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Gradle plugin
 */
class PerfPlugin implements Plugin<Project> {
  PerfTrackingTransform perfTrackingTransform;

  @Override
  void apply(Project project) {
    def info = new Properties()
    info.load(PerfPlugin.classLoader.getResourceAsStream('info.properties'))

    project.extensions.add('performanceTracking', project.container(PerfPluginExtension))

    def version = info.getProperty('version')
    def runtime = info.getProperty('runtime')
    def repository = info.getProperty('repository')

    perfTrackingTransform = new PerfTrackingTransform(project)

    def android = project.extensions.findByType(AppExtension)
    android.registerTransform(perfTrackingTransform)

    def perfConfig = project.extensions.getByName('performanceTracking')
    project.gradle.taskGraph.beforeTask { Task task ->
      if (task.name.startsWith("transformClassesWithPerfTrackingFor")) {
        /*Split's String(task.name) when ever a upper case character is encountered.
          Example:
          String s = "thisIsMyString";
          String[] r = s.split("(?=\\p{Upper})");
          Content :["this", "Is", "My", "String"] */
        def strings = task.name.split("(?=\\p{Lu})")
        def buildType = strings[strings.length - 1].toLowerCase()
        def enable = buildType != "debug" // default value
        if (perfConfig.hasProperty(buildType)) enable = perfConfig."$buildType".enable
        perfTrackingTransform.enableRewrite = enable
      }
    }

    project.configure(project) {
      repositories {
        maven {
          url repository
        }
      }
    }

    project.dependencies.compile runtime
  }
}
