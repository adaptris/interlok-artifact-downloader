package com.adaptris.downloader.resolvers.ivy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IvyMavenLikePatternMatcherTest {

  @Test
  public void testMatchesAll() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("").matches("com.adaptris:artifact");

    assertTrue(matches);
  }

  @Test
  public void testMatchesWildcardAll() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*").matches("com.adaptris:artifact");

    assertTrue(matches);
  }

  @Test
  public void testMatchesFullMatch() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris:artifact").matches("com.adaptris:artifact");

    assertTrue(matches);
  }

  @Test
  public void testMatchesSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris").matches("com.adaptris");

    assertTrue(matches);
  }

  @Test
  public void testMatchesTrailingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adaptris:artifact");

    assertTrue(matches);
  }

  @Test
  public void testMatchesLeadingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*com.adaptris").matches("com.adaptris");

    assertTrue(matches);
  }

  @Test
  public void testMatchesLeadingAndTrailingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*com.adaptris*").matches("com.adaptris");

    assertTrue(matches);
  }

  @Test
  public void testMatchesWildCardSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adaptris.ui");

    assertTrue(matches);
  }

  @Test
  public void testMatchesTrailingWildCardSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris.ui").matches("com.adaptris.ui");

    assertTrue(matches);
  }

  @Test
  public void testMatchesFalseSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris").matches("com.adaptris.ui");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseTrailingWildcardSimilarGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adapt");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseTrailingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("org.something");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseLeadingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris").matches("org.something");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseLeadingAndTrailingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris*").matches("org.something");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseWithVersionRangeNotSupported() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("(com.adaptris:artifact:1,com.adaptris:artifact:2)").matches("com.adaptris:artifact:1");

    assertFalse(matches);
  }

  @Test
  public void testMatchesFalseWithVersionRangeSquareBracketNotSupported() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("[com.adaptris:artifact:1,com.adaptris:artifact:2]").matches("com.adaptris:artifact:1");

    assertFalse(matches);
  }

  @Test
  public void testMatchesNull() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    assertThrows(NullPointerException.class, () -> {
      patternMatcher.getMatcher("com.adaptris").matches(null);
    });
  }

  @Test
  public void testGetName() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    String name = patternMatcher.getName();

    assertEquals(IvyMavenLikePatternMatcher.IVY_MAVEN_LIKE, name);
  }

  @Test
  public void testIsNotExact() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean exact = patternMatcher.getMatcher("com.adaptris.*").isExact();

    assertFalse(exact);
  }

}
