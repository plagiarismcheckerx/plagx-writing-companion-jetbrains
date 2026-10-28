package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Application-wide settings, stored in plagx-writing-companion.xml. */
@State(name = "PlagxWritingCompanion", storages = @Storage("plagx-writing-companion.xml"))
public final class PlagxSettings implements PersistentStateComponent<PlagxSettings> {
    public String style = "apa";
    /** Word limits; 0 means no limit. */
    public int minWords = 0;
    public int maxWords = 0;
    public List<String> requiredHeadings = new ArrayList<>();
    public List<String> checks = new ArrayList<>(Arrays.asList("length", "references", "punctuation", "grammar"));
    /** File extensions checked as drafts. */
    public List<String> extensions = new ArrayList<>(Arrays.asList("md", "markdown", "txt", "tex"));

    public static PlagxSettings getInstance() {
        return ApplicationManager.getApplication().getService(PlagxSettings.class);
    }

    @Override
    public @NotNull PlagxSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PlagxSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
