package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.plagiarismcheckerx.plagx.preflight.PlagxPreflight;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Settings | Tools | Plagx Writing Companion. */
public final class PlagxConfigurable implements Configurable {
    private final ComboBox<String> style = new ComboBox<>(new String[]{"apa", "mla"});
    private final JBTextField minWords = new JBTextField();
    private final JBTextField maxWords = new JBTextField();
    private final JBTextField headings = new JBTextField();
    private final JBTextField extensions = new JBTextField();
    private final Map<String, JBCheckBox> checks = new LinkedHashMap<>();

    @Override
    public @Nls String getDisplayName() {
        return "Plagx Writing Companion";
    }

    @Override
    public JComponent createComponent() {
        FormBuilder form = FormBuilder.createFormBuilder()
                .addLabeledComponent("Citation style:", style)
                .addLabeledComponent("Minimum words (0 = no limit):", minWords)
                .addLabeledComponent("Maximum words (0 = no limit):", maxWords)
                .addLabeledComponent("Required headings (comma separated):", headings)
                .addLabeledComponent("File extensions checked (comma separated):", extensions)
                .addComponent(new JBLabel("Checks:"));
        for (String c : PlagxPreflight.ALL_CHECKS) {
            JBCheckBox box = new JBCheckBox(c);
            checks.put(c, box);
            form.addComponent(box);
        }
        form.addComponent(new JBLabel("<html>By Plagiarism Checker X. This free plugin gives writing suggestions only; "
                + "it does not detect plagiarism or AI-generated text.</html>"));
        JPanel panel = form.addComponentFillVertically(new JPanel(), 0).getPanel();
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        PlagxSettings s = PlagxSettings.getInstance();
        return !Objects.equals(style.getSelectedItem(), s.style) || number(minWords) != s.minWords
                || number(maxWords) != s.maxWords || !list(headings).equals(s.requiredHeadings)
                || !list(extensions).equals(s.extensions) || !selectedChecks().equals(s.checks);
    }

    @Override
    public void apply() {
        PlagxSettings s = PlagxSettings.getInstance();
        s.style = (String) style.getSelectedItem();
        s.minWords = number(minWords);
        s.maxWords = number(maxWords);
        s.requiredHeadings = list(headings);
        s.extensions = list(extensions);
        s.checks = selectedChecks();
        for (Project p : ProjectManager.getInstance().getOpenProjects()) {
            DaemonCodeAnalyzer.getInstance(p).restart();
        }
    }

    @Override
    public void reset() {
        PlagxSettings s = PlagxSettings.getInstance();
        style.setSelectedItem(s.style);
        minWords.setText(String.valueOf(s.minWords));
        maxWords.setText(String.valueOf(s.maxWords));
        headings.setText(String.join(", ", s.requiredHeadings));
        extensions.setText(String.join(", ", s.extensions));
        checks.forEach((name, box) -> box.setSelected(s.checks.contains(name)));
    }

    private List<String> selectedChecks() {
        List<String> out = new ArrayList<>();
        checks.forEach((name, box) -> {
            if (box.isSelected()) out.add(name);
        });
        return out;
    }

    private static int number(@NotNull JBTextField field) {
        try {
            return Math.max(0, Integer.parseInt(field.getText().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<String> list(@NotNull JBTextField field) {
        List<String> out = new ArrayList<>();
        Arrays.stream(field.getText().split(",")).map(String::trim).filter(v -> !v.isEmpty()).forEach(out::add);
        return out;
    }
}
