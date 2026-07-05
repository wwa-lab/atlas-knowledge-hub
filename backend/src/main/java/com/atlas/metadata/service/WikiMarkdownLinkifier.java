package com.atlas.metadata.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Deterministic protected-region-aware Wiki link insertion. */
final class WikiMarkdownLinkifier {

  private static final Pattern EXISTING_WIKI_LINK = Pattern.compile("\\[\\[([^\\]]+)]]");
  private static final Pattern MARKDOWN_LINK = Pattern.compile("!?\\[[^\\]]*]\\([^)]*\\)");
  private static final Pattern INLINE_CODE = Pattern.compile("`[^`\\n]+`");

  private WikiMarkdownLinkifier() {}

  /** Inserts Wiki links into eligible Markdown text. */
  static Result linkify(String markdown, String sourceSlug, List<Target> targets) {
    String source = markdown == null ? "" : markdown;
    List<Range> protectedRanges = protectedRanges(source);
    Set<String> existing = existingWikiSlugs(source);
    List<Insertion> insertions = new ArrayList<>();
    Set<String> inserted = new LinkedHashSet<>();
    for (Target target : targets) {
      if (target.slug().equals(sourceSlug) || inserted.contains(target.slug())) {
        continue;
      }
      Insertion insertion = firstEligibleInsertion(source, protectedRanges, target);
      if (insertion != null) {
        insertions.add(insertion);
        inserted.add(target.slug());
      }
    }
    insertions.sort(Comparator.comparingInt(Insertion::start).reversed());
    StringBuilder rewritten = new StringBuilder(source);
    for (Insertion insertion : insertions) {
      rewritten.replace(insertion.start(), insertion.end(), "[[" + insertion.slug() + "]]");
    }
    Set<String> outbound = new LinkedHashSet<>();
    outbound.addAll(existing);
    outbound.addAll(inserted);
    return new Result(
        rewritten.toString(),
        inserted.stream().sorted().toList(),
        outbound.stream().sorted().toList());
  }

  private static Insertion firstEligibleInsertion(
      String markdown, List<Range> protectedRanges, Target target) {
    for (String term : target.terms()) {
      if (term == null || term.isBlank()) {
        continue;
      }
      Pattern pattern =
          Pattern.compile(
              "(?iu)(?<![\\p{Alnum}])" + Pattern.quote(term.trim()) + "(?![\\p{Alnum}])");
      Matcher matcher = pattern.matcher(markdown);
      while (matcher.find()) {
        if (!insideProtectedRange(matcher.start(), matcher.end(), protectedRanges)) {
          return new Insertion(matcher.start(), matcher.end(), target.slug());
        }
      }
    }
    return null;
  }

  private static boolean insideProtectedRange(int start, int end, List<Range> ranges) {
    return ranges.stream().anyMatch(range -> start < range.end() && end > range.start());
  }

  private static Set<String> existingWikiSlugs(String markdown) {
    Set<String> slugs = new LinkedHashSet<>();
    Matcher matcher = EXISTING_WIKI_LINK.matcher(markdown);
    while (matcher.find()) {
      String slug = normalizeSlug(matcher.group(1));
      if (!slug.isBlank()) {
        slugs.add(slug);
      }
    }
    return slugs;
  }

  private static List<Range> protectedRanges(String markdown) {
    List<Range> ranges = new ArrayList<>();
    addFrontmatterRange(markdown, ranges);
    addFenceRanges(markdown, ranges);
    addPatternRanges(markdown, MARKDOWN_LINK, ranges);
    addPatternRanges(markdown, INLINE_CODE, ranges);
    addPatternRanges(markdown, EXISTING_WIKI_LINK, ranges);
    return ranges;
  }

  private static void addFrontmatterRange(String markdown, List<Range> ranges) {
    if (!markdown.startsWith("---")) {
      return;
    }
    int firstLineEnd = markdown.indexOf('\n');
    if (firstLineEnd < 0) {
      return;
    }
    int closing = markdown.indexOf("\n---", firstLineEnd + 1);
    if (closing >= 0) {
      int closingEnd = markdown.indexOf('\n', closing + 1);
      ranges.add(new Range(0, closingEnd < 0 ? markdown.length() : closingEnd + 1));
    }
  }

  private static void addFenceRanges(String markdown, List<Range> ranges) {
    int cursor = 0;
    while (cursor < markdown.length()) {
      int start = markdown.indexOf("```", cursor);
      if (start < 0) {
        return;
      }
      int end = markdown.indexOf("```", start + 3);
      if (end < 0) {
        ranges.add(new Range(start, markdown.length()));
        return;
      }
      ranges.add(new Range(start, end + 3));
      cursor = end + 3;
    }
  }

  private static void addPatternRanges(String markdown, Pattern pattern, List<Range> ranges) {
    Matcher matcher = pattern.matcher(markdown);
    while (matcher.find()) {
      ranges.add(new Range(matcher.start(), matcher.end()));
    }
  }

  static String normalizeSlug(String value) {
    return value == null
        ? ""
        : value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
  }

  record Target(String slug, List<String> terms) {}

  record Result(String markdown, List<String> insertedSlugs, List<String> allOutboundSlugs) {}

  private record Insertion(int start, int end, String slug) {}

  private record Range(int start, int end) {}
}
