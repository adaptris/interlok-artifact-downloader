package com.adaptris.downloader.resolvers.ivy;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IvyMavenLikePatternMatcherTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testMatchesAll() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("").matches("com.adaptris:artifact");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesWildcardAll() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*").matches("com.adaptris:artifact");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesFullMatch() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris:artifact").matches("com.adaptris:artifact");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris").matches("com.adaptris");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesTrailingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adaptris:artifact");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesLeadingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*com.adaptris").matches("com.adaptris");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesLeadingAndTrailingWildCardSameGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*com.adaptris*").matches("com.adaptris");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesWildCardSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adaptris.ui");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesTrailingWildCardSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris.ui").matches("com.adaptris.ui");

    Assert.assertTrue(matches);
  }

  @Test
  public void testMatchesFalseSubGrouptId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris").matches("com.adaptris.ui");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseTrailingWildcardSimilarGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("com.adapt");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseTrailingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("com.adaptris*").matches("org.something");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseLeadingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris").matches("org.something");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseLeadingAndTrailingWildcardDifferentGroupId() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("*adaptris*").matches("org.something");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseWithVersionRangeNotSupported() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("(com.adaptris:artifact:1,com.adaptris:artifact:2)").matches("com.adaptris:artifact:1");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesFalseWithVersionRangeSquareBracketNotSupported() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean matches = patternMatcher.getMatcher("[com.adaptris:artifact:1,com.adaptris:artifact:2]").matches("com.adaptris:artifact:1");

    Assert.assertFalse(matches);
  }

  @Test
  public void testMatchesNull() {
    expectedEx.expect(NullPointerException.class);

    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    patternMatcher.getMatcher("com.adaptris").matches(null);
  }

  @Test
  public void testGetName() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    String name = patternMatcher.getName();

    Assert.assertEquals(IvyMavenLikePatternMatcher.IVY_MAVEN_LIKE, name);
  }

  @Test
  public void testIsNotExact() {
    IvyMavenLikePatternMatcher patternMatcher = new IvyMavenLikePatternMatcher();

    boolean exact = patternMatcher.getMatcher("com.adaptris.*").isExact();

    Assert.assertFalse(exact);
  }

}
