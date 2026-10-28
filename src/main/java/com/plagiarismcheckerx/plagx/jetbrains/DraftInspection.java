package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.plagiarismcheckerx.plagx.preflight.DraftIssue;
import com.plagiarismcheckerx.plagx.preflight.DraftResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Draft checks (citations, word limits, punctuation, grammar) for Markdown, text and LaTeX files. */
public final class DraftInspection extends LocalInspectionTool {
    @Override
    public ProblemDescriptor @NotNull [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                                   boolean isOnTheFly) {
        PlagxSettings settings = PlagxSettings.getInstance();
        if (!PlagxCore.isDraft(file.getVirtualFile(), settings)) return ProblemDescriptor.EMPTY_ARRAY;
        String text = file.getText();
        DraftResult result = PlagxCore.checkDraft(text, settings);
        List<ProblemDescriptor> problems = new ArrayList<>();
        for (DraftIssue issue : result.getIssues()) {
            TextRange range = range(issue.getStart(), issue.getEnd(), text.length());
            if (range == null) continue;
            LocalQuickFix[] fixes = issue.getSuggestion() == null
                    ? LocalQuickFix.EMPTY_ARRAY
                    : new LocalQuickFix[]{new ReplaceFix(range, issue.getSuggestion())};
            problems.add(manager.createProblemDescriptor(file, range, issue.getMessage(), highlight(issue.getLevel()),
                    isOnTheFly, fixes));
        }
        return problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }

    static TextRange range(int start, int end, int length) {
        if (length == 0) return null;
        int s = Math.max(0, Math.min(start, length - 1));
        int e = Math.max(s + 1, Math.min(end, length));
        return new TextRange(s, e);
    }

    static ProblemHighlightType highlight(String level) {
        return switch (level) {
            case "error" -> ProblemHighlightType.GENERIC_ERROR;
            case "warning" -> ProblemHighlightType.WARNING;
            default -> ProblemHighlightType.WEAK_WARNING;
        };
    }

    /** Replaces the flagged text with the suggestion from Plagx Preflight. */
    static final class ReplaceFix implements LocalQuickFix {
        private final TextRange range;
        private final String replacement;

        ReplaceFix(TextRange range, String replacement) {
            this.range = range;
            this.replacement = replacement;
        }

        @Override
        public @NotNull String getName() {
            return replacement.isEmpty() ? "Remove" : "Replace with \"" + replacement + "\"";
        }

        @Override
        public @NotNull String getFamilyName() {
            return "Plagx: apply suggestion";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiFile file = descriptor.getPsiElement().getContainingFile();
            Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            TextRange current = descriptor.getTextRangeInElement();
            TextRange target = current != null ? current : range;
            if (doc != null && target.getEndOffset() <= doc.getTextLength()) {
                doc.replaceString(target.getStartOffset(), target.getEndOffset(), replacement);
            }
        }
    }
}
