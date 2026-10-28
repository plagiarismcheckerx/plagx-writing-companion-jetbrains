package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.plagiarismcheckerx.plagx.cite.CheckResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Runs the inspections and quick fixes inside a headless IntelliJ Platform. */
public class PlagxPluginTest extends BasePlatformTestCase {
    private static String sample(String name) throws IOException {
        return Files.readString(Path.of("src/test/testData", name), StandardCharsets.UTF_8);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PlagxSettings.getInstance().loadState(new PlagxSettings());
        myFixture.enableInspections(DraftInspection.class, BibInspection.class);
    }

    public void testDraftChecksHighlightMarkdown() throws IOException {
        myFixture.configureByText("essay.md", sample("sample-essay.md"));
        List<HighlightInfo> infos = myFixture.doHighlighting();
        List<String> messages = infos.stream().map(HighlightInfo::getDescription).toList();
        assertTrue(messages.toString(), messages.stream().anyMatch(m -> m != null && m.toLowerCase().contains("repeated")));
        assertTrue(messages.toString(), infos.stream().anyMatch(i -> i.getSeverity() == HighlightSeverity.ERROR));
    }

    public void testQuickFixRemovesRepeatedWord() throws IOException {
        myFixture.configureByText("essay.txt", sample("sample-essay.md"));
        myFixture.doHighlighting();
        IntentionAction fix = myFixture.getAllQuickFixes().stream()
                .filter(a -> a.getText().equals("Replace with \"to\""))
                .findFirst().orElse(null);
        assertNotNull("quick fix for 'to to'", fix);
        myFixture.launchAction(fix);
        assertFalse(myFixture.getEditor().getDocument().getText().contains("to to"));
    }

    public void testOtherFilesAreIgnored() throws IOException {
        myFixture.configureByText("Main.java", sample("sample-essay.md"));
        assertTrue(myFixture.doHighlighting().stream()
                .noneMatch(i -> i.getDescription() != null && i.getDescription().contains("Repeated")));
    }

    public void testWordLimitSetting() throws IOException {
        PlagxSettings.getInstance().maxWords = 10;
        myFixture.configureByText("essay.md", sample("sample-essay.md"));
        assertTrue(myFixture.doHighlighting().stream()
                .anyMatch(i -> i.getDescription() != null && i.getDescription().toLowerCase().contains("word")));
    }

    public void testBibTeXProblemsAreHighlighted() throws IOException {
        myFixture.configureByText("refs.bib", sample("sample.bib"));
        List<String> messages = myFixture.doHighlighting().stream().map(HighlightInfo::getDescription).toList();
        assertTrue(messages.toString(), messages.stream().anyMatch(m -> m != null && m.startsWith("notitle: No title")));
        assertTrue(messages.toString(), messages.stream().anyMatch(m -> m != null && m.contains("duplicate")));
    }

    public void testCoreReadinessAndBibliography() throws IOException {
        PlagxCore.Readiness r = PlagxCore.readiness(PlagxCore.checkDraft(sample("sample-essay.md"), new PlagxSettings()));
        assertEquals(r.errors() == 0, r.ready());
        assertTrue(r.statusText().contains("words"));
        List<String> refs = PlagxCore.bibliography(sample("sample.bib"), "apa");
        assertEquals(4, refs.size());
        assertTrue(refs.stream().anyMatch(e -> e.contains("Smith, J. (2020)")));
        CheckResult check = PlagxCore.checkBib(sample("sample.bib"), false);
        assertTrue(check.getErrors() > 0);
    }
}
