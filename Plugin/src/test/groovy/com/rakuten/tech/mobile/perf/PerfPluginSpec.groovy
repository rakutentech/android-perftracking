package com.rakuten.tech.mobile.perf

import groovy.mock.interceptor.StubFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class PerfPluginSpec {
  PerfPlugin plugin
  Project project
  Stubs stubs

  @Before void setup() {
    plugin = new PerfPlugin()
    stubs = new Stubs()
    project = stubs.create()
  }

  @Test void "should add runtime dependency to project"() {
    plugin.apply(project)

    assert !stubs.compileDependencies.isEmpty()
    assert stubs.compileDependencies.find {
      it.startsWith("com.rakuten.tech.mobile.perf:performance-tracking")
    }
  }

  @Test void "should add release artifactory to repositories"() {
    plugin.apply(project)

    assert !stubs.testDelegate.repos.isEmpty()
    assert stubs.testDelegate.repos.contains("http://artifactory.raksdtd.com/artifactory/libs-release")
  }

  @Test void "should add performance tracking extension"() {
    plugin.apply(project)

    assert !stubs.extensions.isEmpty()
    assert stubs.extensions.contains('performanceTracking')
  }

  @Test void "should register performance tracking transformation"() {
    plugin.apply(project)

    assert !stubs.transforms.isEmpty()
    assert stubs.transforms.find { it instanceof PerfTrackingTransform }
  }
}

@RunWith(Parameterized)
class PluginConfigurationSpec {
  PerfPlugin plugin
  Project project
  Stubs stubs
  def expected

  PluginConfigurationSpec(name, config, expected) {
    plugin = new PerfPlugin()
    stubs = new Stubs()
    project = stubs.create()

    stubs.taskName = name

    if (config.containsKey('debug')) {
      stubs.testExtension = new DebugExtension()
      stubs.testExtension.debug = [enable: config.debug]
    } else if (config.containsKey('release')) {
      stubs.testExtension = new ReleaseExtension()
      stubs.testExtension.release = [enable: config.release]
    }

    this.expected = expected
  }


  @Test void "should enable perf tracking according to configuration"() {
    plugin.apply(project)

    assert stubs.transforms[0].enableRewrite == expected
  }

  @Parameterized.Parameters static Collection<Object[]> data() {
    def baseName = "transformClassesWithPerfTrackingFor"

    [       // config name          config              expected enableRewrite
            ["${baseName}Debug", [debug: true], true],
            ["${baseName}Debug", [debug: false], false],
            ["${baseName}Debug", [:], false],
            ["${baseName}Release", [:], true],
            ["${baseName}Debug", [release: false], false],
            ["${baseName}Release", [debug: false], true]
    ]*.toArray()
  }

}

class EmptyExtension {}

class DebugExtension {
  def debug
}

class ReleaseExtension {
  def release
}

class TestDelegate {
  def repos = []

  def repositories(cls) { delegate cls }

  def maven(cls) { delegate cls }

  def delegate(Closure cls) {
    cls.delegate = this
    cls()
  }

  def url(repo) {
    repos << repo
  }
}

class Stubs {
  def compileDependencies = []
  def extensions = []
  def transforms = []
  def taskName = ""
  TestDelegate testDelegate;
  def testExtension

  Project create() {
    def android = [registerTransform: { transforms << it }]

    testExtension = new EmptyExtension()
    def extensionStub = new StubFor(ExtensionContainer)
    extensionStub.ignore('add', { name, extension -> extensions << name })
    extensionStub.ignore('findByType') { android }
    extensionStub.ignore('getByName') { testExtension }

    def gradle = new StubFor(Gradle)
    def graph = new StubFor(TaskExecutionGraph)
    def task = new StubFor(Task)
    task.ignore('getName') { taskName }
    graph.ignore('beforeTask') { it(task.proxyInstance()) }
    gradle.ignore('getTaskGraph') { graph.proxyInstance() }

    testDelegate = new TestDelegate()

    def project = new StubFor(Project)
    project.ignore('getDependencies') { [compile: { compileDependencies << it }] }
    project.ignore('getExtensions') { extensionStub.proxyDelegateInstance() }
    project.ignore('getGradle') { gradle.proxyInstance() }
    project.ignore('container') { new Object() }
    project.ignore('configure') { p, Closure c -> testDelegate.delegate c }
    project.proxyInstance()
  }
}