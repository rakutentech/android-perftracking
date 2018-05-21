package com.rakuten.tech.mobile.perf.rewriter.detours;

import java.util.ArrayList;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;


public class DetourLoader {

  private final Logger _log;

  public DetourLoader(Logger log) {
    _log = log;
  }

  public ArrayList<Detour> load(ClassNode cn) {
    ArrayList<Detour> detours = new ArrayList<Detour>();

    for (Object o : cn.methods) {
      MethodNode mn = (MethodNode) o;

      AnnotationNode a = getAnnotation(mn);
      if (a != null) {
        Detour detour = null;
        if (a.desc
            .equals("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourConstructorParameter;")) {
          detour = constructorParameterDetour(cn, mn, a);
        } else if (a.desc.equals("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourCall;")) {
          detour = callDetour(cn, mn, a);
        } else if (a.desc
            .equals("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourStaticCall;")) {
          detour = staticCallDetour(cn, mn, a);
        }

        if (detour != null) {
          _log.debug("Loaded detour for " + detour.matchMethod + detour.matchDesc);
          detours.add(detour);
        }
      }
    }

    return detours;
  }

  private Detour constructorParameterDetour(ClassNode cn, MethodNode mn, AnnotationNode a) {
    ParameterDetour detour = new ParameterDetour(_log);
    detour.matchMethod = "<init>";
    detour.matchDesc = mn.desc.substring(0, mn.desc.lastIndexOf(')') + 1) + "V";
    detour.owner = ((Type) a.values.get(1)).getClassName();
    detour.detourOwner = cn.name;
    detour.detourName = mn.name;
    detour.detourDesc = mn.desc;
    return detour;
  }

  private Detour callDetour(ClassNode cn, MethodNode mn, AnnotationNode a) {
    String owner = getFirstParameter(mn);

    if (owner == null) {
      _log.debug("Could not get parameter type for detour " + mn.name + mn.desc);
      return null;
    }

    MethodInsnNode mi = getMethodInstruction(mn, owner, mn.name);
    if (mi == null) {
      _log.debug("Could not get method instruction for detour " + mn.name + mn.desc);
      return null;
    }

    CallDetour detour = new CallDetour(_log);
    detour.matchMethod = mn.name;
    detour.matchDesc = mi.desc;
    detour.owner = owner.replace('/', '.');
    detour.detourOwner = cn.name;
    detour.detourDesc = mn.desc;

    return detour;
  }

  private Detour staticCallDetour(ClassNode cn, MethodNode mn, AnnotationNode a) {
    MethodInsnNode mi = getMethodInstruction(mn, null, mn.name);
    if (mi == null) {
      _log.debug("Could not get method instruction for detour " + mn.name + mn.desc);
      return null;
    }

    StaticCallDetour detour = new StaticCallDetour(_log);
    detour.matchMethod = mn.name;
    detour.matchDesc = mi.desc;
    detour.owner = mi.owner;
    detour.detourOwner = cn.name;

    return detour;
  }

  private AnnotationNode getAnnotation(MethodNode mn) {
    if ((mn.visibleAnnotations != null) && (mn.visibleAnnotations.size() > 0)) {
      return (AnnotationNode) mn.visibleAnnotations.get(0);
    }
    if ((mn.invisibleAnnotations != null) && (mn.invisibleAnnotations.size() > 0)) {
      return (AnnotationNode) mn.invisibleAnnotations.get(0);
    }
    return null;
  }

  private String getFirstParameter(MethodNode mn) {
    if (mn.desc.startsWith("(L")) {
      int index = mn.desc.indexOf(';');
      if (index != -1) {
        return mn.desc.substring(2, index);
      }
    }
    return null;
  }

  private MethodInsnNode getMethodInstruction(MethodNode mn, String owner, String method) {
    for (int i = 0; i < mn.instructions.size(); i++) {
      AbstractInsnNode ins = mn.instructions.get(i);
      if (ins instanceof MethodInsnNode) {
        MethodInsnNode mi = (MethodInsnNode) ins;
        if (mi.name.equals(method)) {
          if ((owner == null) || mi.owner.equals(owner)) {
            return mi;
          }
        }
      }
    }
    return null;
  }
}