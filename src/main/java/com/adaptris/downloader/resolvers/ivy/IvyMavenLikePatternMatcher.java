package com.adaptris.downloader.resolvers.ivy;

import org.apache.ivy.plugins.matcher.AbstractPatternMatcher;
import org.apache.ivy.plugins.matcher.Matcher;

/**
 *
 * Matcher that work like the aether maven exclusion dependency filter
 * (org.eclipse.aether.util.filter.PatternExclusionsDependencyFilter) so we get the same results
 * with ivy and maven with the user excludes
 *
 */
public class IvyMavenLikePatternMatcher extends AbstractPatternMatcher {

  public static final String IVY_MAVEN_LIKE = "ivyMavenLike";

  public IvyMavenLikePatternMatcher() {
    super(IVY_MAVEN_LIKE);
  }

  @Override
  protected Matcher newMatcher(String expression) {
    return new IvyMavenLikeMatcher(expression);
  }

  private static class IvyMavenLikeMatcher implements Matcher {
    private final String  pattern;

    public IvyMavenLikeMatcher(String expression) {
      pattern = expression;
    }

    @Override
    public boolean matches(String input) {
      if (input == null) {
        throw new NullPointerException();
      }
      // return new Perl5Matcher().matches(input, pattern);

      boolean matches;

      // support full wildcard and implied wildcard
      if ("*".equals(pattern) || pattern.length() == 0) {
        matches = true;
      }
      // support contains wildcard
      else if (pattern.startsWith("*") && pattern.endsWith("*")) {
        final String contains = pattern.substring(1, pattern.length() - 1);

        matches = input.contains(contains);
      }
      // support leading wildcard
      else if (pattern.startsWith("*")) {
        final String suffix = pattern.substring(1, pattern.length());

        matches = input.endsWith(suffix);
      }
      // support trailing wildcard
      else if (pattern.endsWith("*")) {
        final String prefix = pattern.substring(0, pattern.length() - 1);

        matches = input.startsWith(prefix);
      }
      // support versions range
      else if (pattern.startsWith("[") || pattern.startsWith("(")) {
        matches = isVersionIncludedInRange(input, pattern);
      }
      // support exact match
      else {
        matches = input.equals(pattern);
      }

      return matches;
    }

    private boolean isVersionIncludedInRange(final String version, final String range) {
      // We don't support that
      return false;
    }

    @Override
    public boolean isExact() {
      return false;
    }

  }

}
