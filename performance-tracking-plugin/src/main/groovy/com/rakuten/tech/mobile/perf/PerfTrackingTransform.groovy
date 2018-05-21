package com.rakuten.tech.mobile.perf

import com.android.build.api.transform.*
import com.rakuten.tech.mobile.perf.rewriter.DummyRewriter
import com.rakuten.tech.mobile.perf.rewriter.PerformanceTrackingRewriter
import com.rakuten.tech.mobile.perf.rewriter.Rewriter
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class PerfTrackingTransform extends Transform {

  private final Project project
  private final Logger log
  boolean enableRewrite

  PerfTrackingTransform(Project project) {
    this.project = project
    this.log = Logging.getLogger(PerfTrackingTransform.simpleName)
  }

  @Override
  String getName() {
    'PerfTracking'
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  Set<QualifiedContent.ContentType> getOutputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  Set<QualifiedContent.Scope> getScopes() {
    def allClasspathDeps = project.buildscript.configurations.classpath.dependencies +
        project.rootProject.buildscript.configurations.classpath.dependencies

    def agp = allClasspathDeps.find {
      it.group == 'com.android.tools.build' && it.name == 'gradle'
    }

    def majorVersion = (agp ? agp.version : '3').tokenize('.')*.toInteger().head();

    log.info("Detected android gradle plugin version ${agp ? agp.version : '[unknown]'}, " +
        "adjusting transformations scopes.")
    def scopes = EnumSet.of(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES)

    if (majorVersion < 3) {
      scopes.add(QualifiedContent.Scope.PROJECT_LOCAL_DEPS)
      scopes.add(QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS)
    }
    return scopes
  }

  @Override
  boolean isIncremental() {
    return false
  }

  /* expose for test */
  Rewriter rewriter;

  @Override
  void transform(
      Context context,
      Collection<TransformInput> inputs,
      Collection<TransformInput> referencedInputs,
      TransformOutputProvider outputProvider,
      boolean isIncremental
  ) throws IOException, TransformException, InterruptedException {

    def input = []
    inputs.each {
      [it.jarInputs, it.directoryInputs]*.each { input << "$it.file" }
    }

    rewriter = enableRewrite ? new PerformanceTrackingRewriter() : new DummyRewriter();

    rewriter.input = input.join(File.pathSeparator)
    rewriter.outputJar = outputProvider.getContentLocation("classes", outputTypes, scopes, Format.JAR).toString()
    rewriter.tempJar = "${context.temporaryDir}${File.separator}classes.jar"
    rewriter.classpath = project.android.bootClasspath.join(File.pathSeparator)
    rewriter.compileSdkVersion = project.android.compileSdkVersion

    log.debug("INPUT:  $rewriter.input")
    log.debug("OUTPUT:  $rewriter.outputJar")
    log.debug("TMP JAR:  $rewriter.tempJar")
    log.debug("PATH:  $rewriter.classpath")

    rewriter.rewrite();

  }
}