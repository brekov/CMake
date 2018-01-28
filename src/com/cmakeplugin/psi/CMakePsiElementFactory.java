package com.cmakeplugin.psi;

import com.cmakeplugin.CMakeLanguage;
import com.cmakeplugin.psi.impl.CMakePsiImplUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

public class CMakePsiElementFactory {

  private static PsiFile createFile(@NotNull Project project, @NotNull String text) {
    return PsiFileFactory.getInstance(project)
            .createFileFromText("a.flex", CMakeLanguage.INSTANCE, text, false, false);
  }

  public static PsiElement createIdFromText(@NotNull Project project, @NotNull String text) {
    return CMakePsiImplUtil.computeUnquotedArguments(createFile(project, "%%\n" + text+"="), CMakeUnquotedArgumentContainer.class)
            .get(0).getUnquotedArgument();
  }

//  public static PsiElement createJavaCodeFromText(@NotNull Project project, @NotNull String text) {
//    return SyntaxTraverser.psiTraverser(createFile(project, text)).filter(JFlexJavaCode.class).first();
//  }
//
//  public static PsiElement createJavaTypeFromText(@NotNull Project project, @NotNull String text) {
//    return SyntaxTraverser.psiTraverser(createFile(project, "%%\n%extends " + text)).filter(JFlexJavaType.class).first();
//  }

}