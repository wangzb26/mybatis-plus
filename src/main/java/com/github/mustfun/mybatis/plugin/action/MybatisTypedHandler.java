package com.github.mustfun.mybatis.plugin.action;

import com.github.mustfun.mybatis.plugin.util.DomUtils;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;


import com.intellij.sql.psi.SqlFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 * @update itar
 * 主要是自动补全用
 */
public class MybatisTypedHandler extends TypedHandlerDelegate {

    /**
     * 自动弹出候选项,写.的时候弹出
     * @param charTyped
     * @param project
     * @param editor
     * @param file
     * @return
     */
    @Override
    public Result checkAutoPopup(char charTyped, final Project project, final Editor editor, PsiFile file) {
        //在xml里面打个. 唤醒自动补全，有个蛋用啊...
        if (charTyped == '.' && DomUtils.isMybatisFile(file)) {
            autoPopupParameter(project, editor);
            return Result.STOP;
        }
        return super.checkAutoPopup(charTyped, project, editor, file);
    }


    /**
     * 当特殊符号被输入时候被唤醒
     * 作用条件: #{}  唤醒自动补全
     * 系统会自动去找 CompletionContributor
     * @see com.intellij.codeInsight.completion.CompletionContributor
     * @param c
     * @param project
     * @param editor
     * @param file
     * @return
     */
    @NotNull
    @Override
    public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int index = editor.getCaretModel().getOffset() - 2;
        //比如sqlFile 顶层 file 就是xmlFile
        PsiFile topLevelFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(file);
        boolean parameterCase = c == '{' &&
                index >= 0 &&
                editor.getDocument().getText().charAt(index) == '#' &&
                file instanceof SqlFile &&
                DomUtils.isMybatisFile(topLevelFile);
        if (parameterCase) {
            autoPopupParameter(project, editor);
            return Result.STOP;
        }
        return super.charTyped(c, project, editor, file);
    }

    /**
     * 当特殊符号被输入qian被唤醒
     * @param c
     * @param project
     * @param editor
     * @param file
     * @param fileType
     * @return
     */
    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {
        return super.beforeCharTyped(c, project, editor, file, fileType);
    }

    private static void autoPopupParameter(final Project project, final Editor editor) {
        AutoPopupController.runTransactionWithEverythingCommitted(project, new Runnable() {
            @Override
            public void run() {
                //当前文件是否最新，没有未提交的
                if (PsiDocumentManager.getInstance(project).isCommitted(editor.getDocument())) {
                    //唤醒代码自动补全 ， CompletionContributor
                    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor, 1);
                }
            }
        });
    }

}