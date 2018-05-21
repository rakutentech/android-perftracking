package com.rakuten.tech.mobile.perf.rewriter.base;

import java.util.ArrayList;
import org.objectweb.asm.tree.ClassNode;

public class Base {

  public String name;
  public String internalName;
  public String superName;
  public String internalSuperName;
  public ClassNode cn;
  public final ArrayList<Materialization> materializations = new ArrayList<Materialization>();
}
