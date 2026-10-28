# Plagx Writing Companion for JetBrains IDEs

**Free academic writing checks in IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, CLion, GoLand, Rider, Android Studio and other JetBrains IDEs.** Plagx Writing Companion checks essays, theses and papers written in Markdown, plain text or LaTeX: citations and reference lists, BibTeX files, word limits, basic grammar and punctuation, and overall writing readiness.

By Plagiarism Checker X. For full plagiarism checks and AI-generated text detection, visit [Text Similarity Detector](https://plagiarismcheckerx.com/?utm_source=plagx-jetbrains-github). This free plugin does not detect plagiarism or AI-generated text.

---

## Installation

**Settings | Plugins | Marketplace** → search for **Plagx Writing Companion** → **Install**.

Requires a JetBrains IDE 2024.2 or newer.

## Features

### Draft checks as you type
Open any `.md`, `.txt` or `.tex` file and suggestions are highlighted like code inspections:

- **Citations and references:** in-text citations with no matching reference entry, references that are never cited, a missing reference section, and *et al.* formatting (APA or MLA)
- **Word limits:** warnings when the draft goes over or under your minimum and maximum word counts (the reference list isn't counted)
- **Punctuation:** double spaces, spaces before punctuation, missing spaces after punctuation, repeated punctuation, unmatched brackets and quotes, mixed quote styles
- **Basic grammar:** repeated words, *a/an* mistakes, lowercase *i*, sentence capitals, commonly confused words
- **Structure:** long sentences, long or single-sentence paragraphs, and required headings
- **Passive voice** (optional)

Many suggestions have a quick fix: press `Alt+Enter` (`⌥⏎` on macOS).

### Writing readiness
The status bar shows the word count and reading time of the open draft, with ✓ (ready), ℹ (ready with suggestions) or ⚠ (not ready). Click it, or use **Tools | Plagx | Show Writing Readiness**, for the full summary with the Flesch Reading Ease score.

### Citations and bibliography
- **BibTeX checks:** duplicate entries, missing authors, titles and years, and invalid DOIs are highlighted in `.bib` files
- **Format BibTeX as Reference List:** opens an APA reference list or MLA Works Cited page for the current `.bib` file
- **Check BibTeX References Online:** also verifies every DOI on Crossref
- **Insert Reference from DOI:** enter a DOI and insert the formatted reference, an in-text citation or a narrative citation

## Commands

All commands are in **Tools | Plagx** and in **Find Action** (`Ctrl+Shift+A` / `⇧⌘A`):

| Command | What it does |
|---|---|
| Check Draft | Re-runs the checks on the current file |
| Show Writing Readiness | Readiness summary, word count, reading time and readability |
| Format BibTeX as Reference List | Opens a formatted APA or MLA list for a `.bib` file |
| Check BibTeX References Online | Checks a `.bib` file and verifies DOIs on Crossref |
| Insert Reference from DOI | Looks up a DOI and inserts a reference or citation |

## Settings

**Settings | Tools | Plagx Writing Companion**

| Setting | Default | Description |
|---|---|---|
| Citation style | APA | APA or MLA |
| Minimum / maximum words | 0 (no limit) | Word limits, reference list excluded |
| Required headings | *(none)* | Comma separated, e.g. `Introduction, Conclusion` |
| File extensions checked | `md, markdown, txt, tex` | Files treated as drafts |
| Checks | all except passive voice | `length`, `references`, `punctuation`, `grammar`, `passive-voice` |

The checks can also be switched off or have their severity changed in **Settings | Editor | Inspections | Plagx Writing Companion**.

## Privacy

All draft and BibTeX checks run locally inside the IDE. Your text is never uploaded. The plugin connects to the internet only when you ask it to: **Insert Reference from DOI** and **Check BibTeX References Online** send DOIs to the public [Crossref API](https://www.crossref.org/). No accounts, API keys or telemetry are used.

## How it works

The plugin is a thin IntelliJ Platform layer over two open-source Plagx libraries from Maven Central:

- [plagx-preflight](https://central.sonatype.com/artifact/io.github.plagiarismcheckerx/plagx-preflight): draft checks, word counts, citation matching, grammar and punctuation
- [plagx-cite](https://central.sonatype.com/artifact/io.github.plagiarismcheckerx/plagx-cite): APA and MLA formatting, BibTeX, DOI lookup and duplicate detection

The checks match the other Plagx tools exactly. Suggestions are rule-based, so always check them against your institution's style guide.

## What this plugin does not do

Plagx Writing Companion gives writing suggestions only. It does **not** check for plagiarism or detect AI-generated text. For full document checks, use [Text Similarity Detector](https://plagiarismcheckerx.com/?utm_source=plagx-jetbrains-github) or [download Plagiarism Checker X](https://plagiarismcheckerx.com/download?utm_source=plagx-jetbrains-github) for desktop.

## Development

```bash
./gradlew test          # headless IDE tests
./gradlew buildPlugin   # build/distributions/plagx-writing-companion-jetbrains-<version>.zip
./gradlew runIde        # try it in a sandbox IDE
./gradlew verifyPlugin  # JetBrains Plugin Verifier
```

## License

[MIT](LICENSE) © 2026 Plagiarism Checker X
