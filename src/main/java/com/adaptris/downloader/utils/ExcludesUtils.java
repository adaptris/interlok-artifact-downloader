package com.adaptris.downloader.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ExcludesUtils {

  private ExcludesUtils() {}

  public static void addToExcludesList(String excludesToAdd, List<String> excludes) {
    if (StringUtils.isNotBlank(excludesToAdd)) {
      addToExcludesList(Arrays.asList(excludesToAdd.split(",")), excludes);
    }
  }

  public static void addToExcludesList(Collection<String> excludesToAdd, List<String> excludes) {
    for (String exclude : excludesToAdd) {
      excludes.add(exclude);
    }
  }

}
