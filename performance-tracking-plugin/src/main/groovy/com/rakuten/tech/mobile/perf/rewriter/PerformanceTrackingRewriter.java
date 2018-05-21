package com.rakuten.tech.mobile.perf.rewriter;

import com.rakuten.tech.mobile.perf.rewriter.base.BaseLoader;
import com.rakuten.tech.mobile.perf.rewriter.base.Rebaser;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassFilter;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassTrimmer;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter;
import com.rakuten.tech.mobile.perf.rewriter.detours.Detour;
import com.rakuten.tech.mobile.perf.rewriter.detours.DetourLoader;
import com.rakuten.tech.mobile.perf.rewriter.detours.Detourer;
import com.rakuten.tech.mobile.perf.rewriter.mixins.Mixer;
import com.rakuten.tech.mobile.perf.rewriter.mixins.MixinLoader;
import java.io.File;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public class PerformanceTrackingRewriter implements Rewriter {

  public String input;
  public String outputJar;
  public String tempJar;
  public String classpath;
  public String exclude;
  public String compileSdkVersion;
  public final Logger _log;

  public PerformanceTrackingRewriter() {
    _log = Logging.getLogger(PerformanceTrackingRewriter.class.getSimpleName());
  }

  public void rewrite() {
    _log.debug(input);
    _log.debug("Populating temp JAR");
    ClassJarMaker tempMaker = new ClassJarMaker(new File(tempJar));
    try {
      tempMaker.populate(input);
    } finally {
      tempMaker.Close();
    }

    ClassJar temp = new ClassJar(new File(tempJar));
    ClassProvider provider = new ClassProvider(classpath + File.pathSeparator + tempJar);
    ClassTrimmer trimmer = new ClassTrimmer(compileSdkVersion, provider, _log);

    DetourLoader detourLoader = new DetourLoader(_log);
    Detourer detourer = new Detourer(provider);

    MixinLoader mixinLoader = new MixinLoader(_log);
    Mixer mixer = new Mixer(_log);

    BaseLoader baseLoader = new BaseLoader();
    Rebaser rebaser = new Rebaser(temp, provider, _log);

    for (String name : temp.getClasses()) {
      if (name.startsWith("com.rakuten.tech.mobile.perf.core.detours.")) {
        _log.debug("Found detours " + name);
        ClassNode cn = trimmer.trim(temp.getClassNode(name));
        if (cn != null) {
          for (Detour detour : detourLoader.load(cn)) {
            detourer.add(detour);
          }
        }
      } else if (name.startsWith("com.rakuten.tech.mobile.perf.core.mixins.")) {
        _log.debug("Found mixin " + name);
        ClassNode cn = trimmer.trim(temp.getClassNode(name));
        if (cn != null) {
          mixer.add(mixinLoader.loadMixin(cn));
        }
      } else if (name.startsWith("com.rakuten.tech.mobile.perf.core.base.")) {
        _log.debug("Found base " + name);
        ClassNode cn = trimmer.trim(temp.getClassNode(name));
        if (cn != null) {
          rebaser.add(baseLoader.loadBase(cn));
        }
      }
    }

    ClassJarMaker outputMaker = new ClassJarMaker(new File(outputJar));
    try {
      ClassFilter filter = new ClassFilter();
      filter.exclude(exclude);

      _log.info("Rewriting classes of : " + temp.getJarFile().getName());
      for (String name : temp.getClasses()) {

        if (name.startsWith("com.rakuten.tech.mobile.perf.core.base.")) {
          _log.debug("Removing base " + name + " from build");
        } else if (name.startsWith("com.rakuten.tech.mobile.perf.core.mixins.")) {
          _log.debug("Removing mixin " + name + " from build");
        } else if (name.startsWith("com.rakuten.tech.mobile.perf.core")) {
          ClassNode cn = trimmer.trim(temp.getClassNode(name));
          if (cn != null) {
            ClassWriter cw = new ClassWriter(provider,
                ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            outputMaker.add(name, cw.toByteArray());
          }
        } else if (name
            .equals("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")) {
          _log.debug("Modifying " + name + " dynamically with enabled = true");
          outputMaker.add(name, ClassGenerator.generateConfigClass(true));
        } else if (filter.canRewrite(name)) {
          _log.debug("Rewriting class: " + name);

          try {
            Class<?> clazz = provider.getClass(name);
            ClassReader cr = temp.getClassReader(name);
            ClassWriter cw = new ClassWriter(provider,
                ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassVisitor output = cw;
            output = mixer.rewrite(clazz, output);
            output = rebaser.rewrite(clazz, output);
            output = detourer.rewrite(clazz, output);
            cr.accept(output, 0);
            outputMaker.add(name, cw.toByteArray());

          } catch (Throwable e) {
            _log.lifecycle("Skipping rewriting class " + name +
                ", run with --debug for more details");
            _log.debug("Error encountered during rewriting:", e);
            outputMaker.add(name, temp);
          }
        } else {
          _log.debug("Adding class with no rewriting: " + name);
          outputMaker.add(name, temp);
        }
      }

      rebaser.materialize(outputMaker);
    } finally {
      outputMaker.Close();
    }
  }
}
