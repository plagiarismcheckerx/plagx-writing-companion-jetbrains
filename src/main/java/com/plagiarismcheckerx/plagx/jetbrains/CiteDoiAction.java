package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.plagiarismcheckerx.plagx.cite.PlagxCite;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tools | Plagx | Insert Reference from DOI. */
public final class CiteDoiAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;
        String input = Messages.showInputDialog(project, "DOI or doi.org link (e.g. 10.1037/0003-066X.59.1.29):",
                "Plagx: Insert Reference from DOI", null, "", new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        return PlagxCite.normalizeDoi(s) != null;
                    }

                    @Override
                    public boolean canClose(String s) {
                        return checkInput(s);
                    }
                });
        if (input == null) return;
        String doi = PlagxCite.normalizeDoi(input);
        new Task.Backgroundable(project, "Plagx: looking up DOI on Crossref", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                Map<String, Object> ref;
                try {
                    ref = PlagxCite.lookupDoi(doi);
                } catch (Exception ex) {
                    ShowReadinessAction.notify(project, "Plagx: DOI lookup failed", ex.getMessage(),
                            NotificationType.ERROR);
                    return;
                }
                String style = PlagxSettings.getInstance().style;
                Map<String, String> choices = new LinkedHashMap<>();
                choices.put("Reference list entry", PlagxCite.formatReference(ref, style));
                choices.put("In-text citation", PlagxCite.citeInText(ref, style));
                choices.put("Narrative citation", PlagxCite.citeInText(ref, style, null, true));
                ApplicationManager.getApplication().invokeLater(() -> choose(project, editor, choices));
            }
        }.queue();
    }

    private static void choose(Project project, Editor editor, Map<String, String> choices) {
        List<String> labels = choices.entrySet().stream().map(c -> c.getKey() + ": " + c.getValue()).toList();
        JBPopupFactory.getInstance().createPopupChooserBuilder(labels)
                .setTitle("Insert")
                .setItemChosenCallback(label -> {
                    String text = choices.get(label.substring(0, label.indexOf(": ")));
                    WriteCommandAction.runWriteCommandAction(project, "Plagx: Insert Reference", null, () -> {
                        int start = editor.getSelectionModel().getSelectionStart();
                        int end = editor.getSelectionModel().getSelectionEnd();
                        editor.getDocument().replaceString(start, end, text);
                        editor.getCaretModel().moveToOffset(start + text.length());
                    });
                })
                .createPopup()
                .showInBestPositionFor(editor);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && e.getData(CommonDataKeys.EDITOR) != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
