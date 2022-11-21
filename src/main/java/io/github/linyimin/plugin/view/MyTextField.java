package io.github.linyimin.plugin.view;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yiminlin
 * @date 2022/02/13 2:11 上午
 **/
public class MyTextField extends LanguageTextField {
    private final Project myProject;
    private final Language language;
    private final LanguageFileType fileType;

    public MyTextField(Project myProject, Language language, LanguageFileType fileType) {
        super(language, myProject, "", false);
        this.myProject = myProject;
        this.language = language;
        this.fileType = fileType;
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        setUpEditor(editor);
        return editor;
    }

    @Override
    public void setText(@Nullable String text) {
        super.setFileType(fileType);
        ReadAction.run(() -> {
            Document document = createDocument(text, language, myProject, new SimpleDocumentCreator());
            setDocument(document);
            PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
            if (psiFile != null) {
                WriteCommandAction.runWriteCommandAction(
                        myProject,
                        () -> {
                            CodeStyleManager.getInstance(getProject()).reformat(psiFile);
                        }
                );
            }
        });
    }

    private void setUpEditor(EditorEx editor) {
        editor.offsetToVisualPosition(100);
        editor.offsetToLogicalPosition(100);
        editor.setHorizontalScrollbarVisible(true);
        editor.setShowPlaceholderWhenFocused(true);
        editor.setVerticalScrollbarVisible(true);
        editor.setCaretEnabled(true);
        editor.setEmbeddedIntoDialogWrapper(true);
        EditorSettings settings = editor.getSettings();
        settings.setLeadingWhitespaceShown(true);
        settings.setTrailingWhitespaceShown(true);
        settings.setGutterIconsShown(true);
        settings.setSmartHome(true);
        settings.setLineNumbersShown(true);
        settings.setIndentGuidesShown(true);
        settings.setUseSoftWraps(true);
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setAllowSingleLogicalLineFolding(true);
        settings.setRightMarginShown(true);
        settings.setCaretRowShown(true);
        settings.setLineMarkerAreaShown(true);
        settings.setDndEnabled(true);
        //开启右侧的错误条纹
        ErrorStripeEditorCustomization.ENABLED.customize(editor);
    }
}
