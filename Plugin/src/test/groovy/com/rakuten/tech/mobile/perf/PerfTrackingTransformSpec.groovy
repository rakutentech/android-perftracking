package com.rakuten.tech.mobile.perf

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformOutputProvider
import com.rakuten.tech.mobile.perf.rewriter.DummyRewriter
import com.rakuten.tech.mobile.perf.rewriter.PerformanceTrackingRewriter
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

public class PerfTrackingTransformSpec extends UnitSpec {

  // Mocks
  Context ctx
  TransformOutputProvider outputProvider
  def agp

  // System under test
  PerfTrackingTransform transform

  @Rule public final TemporaryFolder tempDir = new TemporaryFolder()

  @Before public void setup() {
    def android = [
        bootClasspath    : [""],
        compileSdkVersion: "android-23"
    ]
    agp = [
        group  : 'com.android.tools.build',
        name   : 'gradle',
        version: '2.3.3',
    ]
    def script = [configurations: [classpath: [dependencies: [agp]]]]
    def rootScript = [configurations: [classpath: [dependencies: []]]]
    def project = new MockFor(Project)
    def rootProject = new MockFor(Project)
    rootProject.ignore('getBuildscript') { rootScript }
    project.ignore('android') { android }
    project.ignore('getAndroid') { android }
    project.ignore('getBuildscript') { script }
    project.ignore('getRootProject') { rootProject.proxyInstance() }
    transform = new PerfTrackingTransform(project.proxyInstance())

    ctx = [getTemporaryDir: { tempDir.getRoot() }] as Context
    outputProvider = [
        getContentLocation: { name, types, scopes, format ->
          new File(tempDir.root, "outputJar")
        }
    ] as TransformOutputProvider
  }

  @Test public void "should use performance tracking rewriter when enabled"() {
    def (inputs, referenceInputs) = [[], []]
    transform.enableRewrite = true

    transform.transform(ctx, inputs, referenceInputs, outputProvider, false)

    assert transform.rewriter instanceof PerformanceTrackingRewriter
  }

  @Test public void "should use dummy rewriter when disabled"() {
    def (inputs, referenceInputs) = [[], []]
    transform.enableRewrite = false

    transform.transform(ctx, inputs, referenceInputs, outputProvider, false)

    assert transform.rewriter instanceof DummyRewriter
  }

  @Test public void "should limit transformation scopes for apg 3"() {
    agp.version = '3.0.0'

    assert transform.getScopes().size() == 3
    assert !transform.getScopes().contains(QualifiedContent.Scope.PROJECT_LOCAL_DEPS)
    assert !transform.getScopes().contains(QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS)
  }

  @Test public void "should announce all transformation scopes for apg 2"() {
    agp.version = '2.3.3'

    assert transform.getScopes().size() == 5
    assert transform.getScopes().contains(QualifiedContent.Scope.PROJECT_LOCAL_DEPS)
    assert transform.getScopes().contains(QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS)
  }
}
