package com.plagiarismcheckerx.plagx.jetbrains;

import com.intellij.openapi.vfs.VirtualFile;
import com.plagiarismcheckerx.plagx.cite.CheckResult;
import com.plagiarismcheckerx.plagx.cite.PlagxCite;
import com.plagiarismcheckerx.plagx.preflight.DraftResult;
import com.plagiarismcheckerx.plagx.preflight.PlagxPreflight;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** IDE-independent glue between the plugin settings and the Plagx Cite / Preflight cores. */
public final class PlagxCore {
    private PlagxCore() {
    }

    public static boolean isBib(@Nullable VirtualFile file) {
        return file != null && "bib".equalsIgnoreCase(file.getExtension());
    }

    public static boolean isDraft(@Nullable VirtualFile file, PlagxSettings settings) {
        if (file == null || file.getExtension() == null || isBib(file)) return false;
        String ext = file.getExtension().toLowerCase(Locale.ROOT);
        return settings.extensions.stream().anyMatch(e -> e.trim().replaceFirst("^\\.", "").equalsIgnoreCase(ext));
    }

    public static PlagxPreflight.Options options(PlagxSettings s) {
        PlagxPreflight.Options o = PlagxPreflight.options().style(s.style == null ? "apa" : s.style);
        if (s.minWords > 0) o.minWords(s.minWords);
        if (s.maxWords > 0) o.maxWords(s.maxWords);
        List<String> checks = s.checks.stream().filter(PlagxPreflight.ALL_CHECKS::contains).toList();
        if (!checks.isEmpty()) o.checks(checks);
        List<String> headings = s.requiredHeadings.stream().map(String::trim).filter(h -> !h.isEmpty()).toList();
        if (!headings.isEmpty()) o.requiredHeadings(headings);
        return o;
    }

    public static DraftResult checkDraft(String text, PlagxSettings settings) {
        return PlagxPreflight.checkDraft(text, options(settings));
    }

    /** Readiness summary: ready when there are no errors; warnings are listed but don't block. */
    public record Readiness(boolean ready, String label, int words, int minutes, int errors, int warnings,
                            int infos, long flesch) {
        public String statusText() {
            return String.format(Locale.ROOT, "%s %,d words · %d min", ready ? (warnings == 0 ? "✓" : "ℹ") : "⚠",
                    words, minutes);
        }

        public String summary() {
            return String.format(Locale.ROOT, "%s · %,d words · %d min read · Flesch %d · %d errors, %d warnings",
                    label, words, minutes, flesch, errors, warnings);
        }
    }

    public static Readiness readiness(DraftResult r) {
        boolean ready = r.getErrors() == 0;
        String label = ready ? (r.getWarnings() > 0 ? "Ready with suggestions" : "Ready") : "Not ready";
        return new Readiness(ready, label, r.getStats().getWords(), Math.max(1, r.getStats().getReadingTimeMinutes()),
                r.getErrors(), r.getWarnings(), r.getInfos(), Math.round(r.getStats().getFleschReadingEase()));
    }

    public static List<String> bibliography(String bibtex, String style) {
        List<Map<String, Object>> refs = PlagxCite.parseBibTeX(bibtex);
        return PlagxCite.formatBibliography(refs, style, "text");
    }

    public static CheckResult checkBib(String bibtex, boolean checkDois) {
        List<Map<String, Object>> refs = PlagxCite.parseBibTeX(bibtex);
        return checkDois ? PlagxCite.checkReferences(refs, true, null, null) : PlagxCite.checkReferences(refs);
    }

    /** Offset of the line where a BibTeX entry key is defined, or 0 when not found. */
    public static int bibKeyOffset(String bibtex, String key) {
        if (key == null || key.startsWith("#")) return 0;
        Matcher m = Pattern.compile("(?m)^\\s*@\\w+\\s*\\{\\s*" + Pattern.quote(key) + "\\s*,").matcher(bibtex);
        return m.find() ? m.start() : 0;
    }
}
