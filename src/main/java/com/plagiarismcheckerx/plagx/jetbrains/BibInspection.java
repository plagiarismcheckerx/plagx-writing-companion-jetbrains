package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.plagiarismcheckerx.plagx.cite.CheckIssue;
import com.plagiarismcheckerx.plagx.cite.CheckResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Offline BibTeX checks: duplicates, missing authors, titles and years, invalid DOIs. */
public final class BibInspection extends LocalInspectionTool {
    @Override
    public ProblemDescriptor @NotNull [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                                   boolean isOnTheFly) {
        if (!PlagxCore.isBib(file.getVirtualFile())) return ProblemDescriptor.EMPTY_ARRAY;
        String text = file.getText();
        CheckResult result = PlagxCore.checkBib(text, false);
        List<ProblemDescriptor> problems = new ArrayList<>();
        for (CheckIssue issue : result.getIssues()) {
            int start = PlagxCore.bibKeyOffset(text, issue.getRef());
            int lineEnd = text.indexOf('\n', start);
            TextRange range = DraftInspection.range(start, lineEnd < 0 ? text.length() : lineEnd, text.length());
            if (range == null) continue;
            problems.add(manager.createProblemDescriptor(file, range, issue.getRef() + ": " + issue.getMessage(),
                    DraftInspection.highlight(issue.getLevel()), isOnTheFly));
        }
        return problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }
}
