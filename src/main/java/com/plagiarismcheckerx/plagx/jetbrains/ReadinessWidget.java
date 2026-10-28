package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Alarm;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.event.MouseEvent;

/** Status bar text: word count, reading time and readiness of the current draft. */
public final class ReadinessWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {
    static final String ID = "PlagxReadiness";

    private final Project project;
    private final Alarm alarm;
    private volatile String text = "";
    private volatile String tooltip = "";
    private StatusBar statusBar;

    ReadinessWidget(Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    }

    @Override
    public @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
        project.getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        schedule(0);
                    }
                });
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                schedule(500);
            }
        }, this);
        schedule(0);
    }

    private void schedule(int delay) {
        if (alarm.isDisposed()) return;
        alarm.cancelAllRequests();
        alarm.addRequest(this::refresh, delay);
    }

    private void refresh() {
        VirtualFile file = ReadAction.compute(() -> {
            VirtualFile[] selected = project.isDisposed() ? VirtualFile.EMPTY_ARRAY
                    : FileEditorManager.getInstance(project).getSelectedFiles();
            return selected.length > 0 ? selected[0] : null;
        });
        String newText = "";
        String newTooltip = "";
        if (PlagxCore.isDraft(file, PlagxSettings.getInstance())) {
            Document doc = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file));
            if (doc != null) {
                PlagxCore.Readiness r = PlagxCore.readiness(
                        PlagxCore.checkDraft(ReadAction.compute(doc::getText), PlagxSettings.getInstance()));
                newText = r.statusText();
                newTooltip = "Plagx: " + r.summary();
            }
        }
        text = newText;
        tooltip = newTooltip;
        ApplicationManager.getApplication().invokeLater(() -> {
            if (statusBar != null) statusBar.updateWidget(ID);
        });
    }

    @Override
    public @NotNull String getText() {
        return text;
    }

    @Override
    public float getAlignment() {
        return Component.CENTER_ALIGNMENT;
    }

    @Override
    public @Nullable String getTooltipText() {
        return tooltip.isEmpty() ? null : tooltip + ". Click for details.";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return event -> ShowReadinessAction.show(project);
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void dispose() {
        Disposer.dispose(alarm);
        statusBar = null;
    }

    /** Registers the widget for every project. */
    public static final class Factory implements com.intellij.openapi.wm.StatusBarWidgetFactory {
        @Override
        public @NotNull String getId() {
            return ID;
        }

        @Override
        public @NotNull String getDisplayName() {
            return "Plagx Writing Readiness";
        }

        @Override
        public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
            return new ReadinessWidget(project);
        }
    }
}
