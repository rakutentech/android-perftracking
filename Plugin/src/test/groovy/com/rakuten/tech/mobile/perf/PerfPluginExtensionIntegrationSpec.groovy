package com.rakuten.tech.mobile.perf

import com.rakuten.tech.mobile.perf.rewriter.DummyRewriter
import com.rakuten.tech.mobile.perf.rewriter.PerformanceTrackingRewriter
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

class PerfPluginExtensionIntegrationSpec {
  @Rule public final TemporaryFolder projectDir = new TemporaryFolder(new File("tmp"))
  File buildFile

  @Before
  void setup() {
    buildFile = projectDir.newFile('build.gradle')
    def main = projectDir.newFolder('src', 'main')
    def manifest = new File(main, "AndroidManifest.xml")
    manifest << resourceFile("manifest").text
  }

  @Test
  void "plugin should use real rewriter for debug build according to extension"() {
    buildFile << resourceFile("example_app_ext_all_enabled").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleDebug', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForDebug').outcome == TaskOutcome.SUCCESS
    assert result.output.contains(PerformanceTrackingRewriter.simpleName)
    assert !result.output.contains(DummyRewriter.simpleName)
  }

  @Test
  void "plugin should use real rewriter for release build according to extension"() {
    buildFile << resourceFile("example_app_ext_all_enabled").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleRelease', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForRelease').outcome == TaskOutcome.SUCCESS
    assert result.output.contains(PerformanceTrackingRewriter.simpleName)
    assert !result.output.contains(DummyRewriter.simpleName)
  }

  @Test
  void "plugin should use dummy rewriter for debug build config missing"() {
    buildFile << resourceFile("example_app_ext_missing_some_config").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleDebug', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForDebug').outcome == TaskOutcome.SUCCESS
    assert !result.output.contains(PerformanceTrackingRewriter.simpleName)
    assert result.output.contains(DummyRewriter.simpleName)
  }

  @Test
  void "plugin should use real rewriter for qa build config missing"() {
    buildFile << resourceFile("example_app_ext_missing_some_config").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleQa', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForQa').outcome == TaskOutcome.SUCCESS
    assert result.output.contains(PerformanceTrackingRewriter.simpleName)
    assert !result.output.contains(DummyRewriter.simpleName)
  }

  @Test
  void "plugin should use real rewriter for release build config disabled"() {
    buildFile << resourceFile("example_app_ext_missing_some_config").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleRelease', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForRelease').outcome == TaskOutcome.SUCCESS
    assert !result.output.contains(PerformanceTrackingRewriter.simpleName)
    assert result.output.contains(DummyRewriter.simpleName)
  }
}