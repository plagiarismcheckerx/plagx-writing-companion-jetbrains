package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/** Tools | Plagx | Show Writing Readiness: summary for the current draft. */
public final class ShowReadinessAction extends DumbAwareAction {
    static void show(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;
        PlagxCore.Readiness r = PlagxCore.readiness(
                PlagxCore.checkDraft(editor.getDocument().getText(), PlagxSettings.getInstance()));
        notify(project, "Plagx writing readiness", r.summary(), NotificationType.INFORMATION);
    }

    static void notify(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance().getNotificationGroup("Plagx Writing Companion")
                .createNotification(title, content, type).notify(project);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() != null) show(e.getProject());
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
