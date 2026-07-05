package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for deterministic Wiki Markdown link insertion. */
class WikiMarkdownLinkifierTest {

  @Test
  void insertsOneWikiLinkPerTargetAndSkipsProtectedMarkdownRegions() {
    String markdown =
        """
        ---
        title: Application Inventory
        ---

        `Application Inventory` should not change.

        ```text
        Application Inventory should not change.
        ```

        [Application Inventory](https://example.invalid) should not change.
        ![Application Inventory](image.png) should not change.
        [[application-inventory]] should not change.

        Application Inventory should link once. Application Inventory repeats.
        Migration Scope should not self-link.
        """;

    var result =
        WikiMarkdownLinkifier.linkify(
            markdown,
            "migration-scope",
            List.of(
                new WikiMarkdownLinkifier.Target("application-inventory", List.of("application inventory")),
                new WikiMarkdownLinkifier.Target("migration-scope", List.of("migration scope"))));

    assertThat(result.markdown()).contains("[[application-inventory]] should link once");
    assertThat(result.markdown()).contains("Application Inventory repeats.");
    assertThat(result.markdown()).contains("`Application Inventory` should not change.");
    assertThat(result.markdown()).contains("Application Inventory should not change.");
    assertThat(result.markdown()).contains("[Application Inventory](https://example.invalid)");
    assertThat(result.markdown()).contains("![Application Inventory](image.png)");
    assertThat(result.insertedSlugs()).containsExactly("application-inventory");
    assertThat(result.allOutboundSlugs()).containsExactly("application-inventory");
  }

  @Test
  void reportsExistingBrokenWikiLinksWithoutDuplicatingValidOutboundLinks() {
    var result =
        WikiMarkdownLinkifier.linkify(
            "A [[missing-page]] link and Application Inventory mention.",
            "source-page",
            List.of(new WikiMarkdownLinkifier.Target("application-inventory", List.of("application inventory"))));

    assertThat(result.markdown()).contains("[[application-inventory]] mention");
    assertThat(result.allOutboundSlugs()).containsExactly("application-inventory", "missing-page");
    assertThat(result.insertedSlugs()).containsExactly("application-inventory");
  }
}
