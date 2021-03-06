package com.cmakeplugin.agent;

import java.lang.instrument.Instrumentation;

public class CMakeInstrumentationAgent {

  static final String CLASS_TO_TRANSFORM_REFERENCES =
      "com.jetbrains.cmake.psi.CMakeLiteralImplMixin";
  static final String CLASS_TO_TRANSFORM_PRESENTATION = "com.intellij.psi.impl.PsiElementBase";
  static final String CLASS_TO_TRANSFORM_RESOLVE =
      "com.jetbrains.cmake.resolve.CMakeElementEvaluator";
  static final String CLASS_TO_TRANSFORM_SHOWUSAGES =
      "com.intellij.codeInsight.navigation.actions.GotoDeclarationAction";
  static final String CLASS_TO_TRANSFORM_FINDUSAGES =
      "com.jetbrains.cmake.search.CMakeFindUsagesProvider";
  static final String CLASS_TO_TRANSFORM_HIGHLIGHT_MULTIRESOLVE =
      "com.intellij.codeInsight.TargetElementUtil";

  private static CMakeAgentLogger LOGGER;

  public static void agentmain(String agentArgs, Instrumentation inst) {
    LOGGER = new CMakeAgentLogger(CMakeInstrumentationAgent.class, inst);

    //    LOGGER.info("In agentmain method");

    String srcInsertAfter;

/*
    srcInsertAfter = "{ "
        + "$_ = ($r) com.cmakeplugin.agent.CMakeInstrumentationUtils.concatArrays( "
        + "  ($r)$_ ,"
        + "  com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry.getReferencesFromProviders(this));"
        + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_REFERENCES, inst),
            "getReferences",
            srcInsertAfter,
            inst),
        inst);
*/

    srcInsertAfter =
        "{ "
            + "$_ = ($r) com.cmakeplugin.agent.CMakeInstrumentationUtils.getPresentation( this, ($r)$_ );"
            + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_PRESENTATION, inst),
            "getPresentation",
            srcInsertAfter,
            inst),
        inst);

    srcInsertAfter =
        "{ "
            + "if (!$_ && "
            + "  (parent instanceof com.jetbrains.cmake.psi.CMakeLiteral) && "
            + "  com.cmakeplugin.agent.CMakeInstrumentationUtils.existReferenceTo(parent)"
            + ") $_ = true;"
            + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_RESOLVE, inst),
            "isAcceptableNamedParent",
            srcInsertAfter,
            inst),
        inst);

    srcInsertAfter =
        "{ "
            + "if ($_ instanceof CMakeLiteral)"
            + "  $_ = CMakeInstrumentationUtils.addNameIdentifierIfVarDef( $_ );"
            + "if ($_ instanceof CMakeCommandName)"
            + "  $_ = CMakeInstrumentationUtils.addNameIdentifierCommand( $_ );"
            + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_SHOWUSAGES, inst),
            new String[] {
              "com.jetbrains.cmake.psi", "com.cmakeplugin.agent.CMakeInstrumentationUtils"
            },
            "findElementToShowUsagesOf",
            srcInsertAfter,
            inst),
        inst);

    srcInsertAfter =
        "{ "
            + "if ($_ == CMakeBundle.message(\"cmake.search.element\", new Object[0])"
            + "  && element instanceof CMakeLiteral)"
            + "  $_ = \"variable\";"
            + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_FINDUSAGES, inst),
            new String[] {
              "com.jetbrains.cmake.psi.CMakeLiteral", "com.jetbrains.cmake.CMakeBundle"
            },
            "getType",
            srcInsertAfter,
            inst),
        inst);

    srcInsertAfter =
        "{ "
            + "if ($_ instanceof CMakeLiteral)"
            + "  $_ = CMakeInstrumentationUtils.getNullIfVarRefMultiResolve($_);"
            + "}";
    transformClass(
        new CMakeClassFileTransformer(
            getLoadedClass(CLASS_TO_TRANSFORM_HIGHLIGHT_MULTIRESOLVE, inst),
            new String[] {
              "com.jetbrains.cmake.psi", "com.cmakeplugin.agent.CMakeInstrumentationUtils"
            },
            "getNamedElement",
            srcInsertAfter,
            inst),
        inst);
  }

  private static void transformClass(CMakeClassFileTransformer cft, Instrumentation inst) {
    inst.addTransformer(cft, true);
    try {
      inst.retransformClasses(cft.getTargetClass());
    } catch (Exception ex) {
      LOGGER.warn("Transformation failed for class: " + cft.getTargetClass().getName() + ex);
    }
  }

  static Class<?> getLoadedClass(String className, Instrumentation inst) {
    for (Class<?> clazz : inst.getAllLoadedClasses()) {
      if (clazz.getName().equals(className)) {
        /*
                logInfo(
                    LOGGER,
                    "class found: " + clazz.getName() + " with ClassLoader: " + clazz.getClassLoader());
        */
        return clazz;
      }
    }
    LOGGER.warn("Failed to find class: " + className);
    return null;
  }
}
