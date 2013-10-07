package de.janthomae.leiningenplugin.utils;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author Colin Fleming
 */
public class Interop implements ApplicationComponent {
  private static final Logger logger = Logger.getLogger(Interop.class);

  public static Map loadProject(String path) {
    return (Map) Vars.loadProject.invoke(path);
  }

  public static List loadDependencies(String path) {
    return (List) Vars.loadDependencies.invoke(path);
  }

  @Override
  public void initComponent() {
    ClassLoader loader = Interop.class.getClassLoader();
    ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(loader);

      // TODO see if we can piggyback off La Clojure's CL and not load Clojure twice

      // dummy to force RT class load first, since it has a circular static
      // initializer loop with Compiler
      new RT();

      clojure.lang.Compiler.LOADER.bindRoot(loader);

      RT.var("clojure.core", "require").invoke(Symbol.intern("de.janthomae.leiningenplugin.leiningen"));
    } catch (Exception e) {
      logger.error(e, e);
    } finally {
      Thread.currentThread().setContextClassLoader(oldLoader);
    }
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "leiningen.initialise";
  }

  private static class Vars {
    private static final Var loadProject = RT.var("de.janthomae.leiningenplugin.leiningen", "load-project");
    private static final Var loadDependencies = RT.var("de.janthomae.leiningenplugin.leiningen", "load-dependencies");
  }
}
