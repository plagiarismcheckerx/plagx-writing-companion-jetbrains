package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.plagiarismcheckerx.plagx.cite.CheckResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/** Actions available on BibTeX files. */
public final class BibActions {
    private BibActions() {
    }

    abstract static class BibAction extends DumbAwareAction {
        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabledAndVisible(e.getProject() != null && e.getData(CommonDataKeys.EDITOR) != null
                    && PlagxCore.isBib(e.getData(CommonDataKeys.VIRTUAL_FILE)));
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }

    /** Tools | Plagx | Format BibTeX as Reference List. */
    public static final class Format extends BibAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (project == null || editor == null) return;
            String style = PlagxSettings.getInstance().style;
            List<String> entries = PlagxCore.bibliography(editor.getDocument().getText(), style);
            if (entries.isEmpty()) {
                ShowReadinessAction.notify(project, "Plagx", "No BibTeX entries found.", NotificationType.WARNING);
                return;
            }
            String heading = "mla".equals(style) ? "Works Cited" : "References";
            String content = "# " + heading + "\n\n" + String.join("\n\n", entries) + "\n";
            FileEditorManager.getInstance(project).openFile(new LightVirtualFile(heading + ".md", content), true);
        }
    }

    /** Tools | Plagx | Check BibTeX References Online: also looks every DOI up on Crossref. */
    public static final class CheckOnline extends BibAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (project == null || editor == null) return;
            String source = editor.getDocument().getText();
            new Task.Backgroundable(project, "Plagx: checking references", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    CheckResult r = PlagxCore.checkBib(source, true);
                    String details = r.getIssues().stream()
                            .map(i -> StringUtil.escapeXmlEntities(i.getRef() + ": " + i.getMessage()))
                            .collect(Collectors.joining("<br>"));
                    String summary = r.getReferences() + " references · " + r.getErrors() + " errors · "
                            + r.getWarnings() + " warnings";
                    ShowReadinessAction.notify(project, "Plagx: " + summary, details.isEmpty() ? "No problems found." : details,
                            r.getErrors() > 0 ? NotificationType.WARNING : NotificationType.INFORMATION);
                }
            }.queue();
        }
    }
}
