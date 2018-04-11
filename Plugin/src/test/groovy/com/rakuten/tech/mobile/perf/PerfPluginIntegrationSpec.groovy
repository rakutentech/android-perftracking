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

public class PerfPluginIntegrationSpec {
  @Rule public final TemporaryFolder projectDir = new TemporaryFolder(new File("tmp"))
  File buildFile

  @Before def void setup() {
    buildFile = projectDir.newFile('build.gradle')
    def main = projectDir.newFolder('src', 'main')
    def manifest = new File(main, "AndroidManifest.xml")
    manifest << resourceFile("manifest").text
  }

  @Test def void "plugin should add transformation tasks"() {
    buildFile << resourceFile("example_app").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('tasks', '--all')
        .build()

    assert result.output.contains("transformClassesWithPerfTrackingForDebug")
    assert result.output.contains("transformClassesWithPerfTrackingForRelease")
  }

  @Test def void "plugin should use dummy rewriter for debug build"() {
    buildFile << resourceFile("example_app").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleDebug', '--debug')
        .build()

    assert result.task(':transformClassesWithPerfTrackingForDebug').outcome == TaskOutcome.SUCCESS
    assert !result.task(':transformClassesWithPerfTrackingForRelease')
    assert result.output.contains(DummyRewriter.simpleName)
    assert !result.output.contains(PerformanceTrackingRewriter.simpleName)
  }

  @Test def void "plugin should use real rewriter for release build"() {
    buildFile << resourceFile("example_app").text
    def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('assembleRelease', '--debug')
        .build()

    assert !result.task(':transformClassesWithPerfTrackingForDebug')
    assert result.task(':transformClassesWithPerfTrackingForRelease').outcome == TaskOutcome.SUCCESS
    assert !result.output.contains(DummyRewriter.simpleName)
    assert result.output.contains(PerformanceTrackingRewriter.simpleName)
  }

}
