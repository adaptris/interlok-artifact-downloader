package com.adaptris.downloader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
      File dir = new File(exclude);
      if (dir.isDirectory()) {
        excludes.addAll(getExcludesFromDir(dir));
      } else {
        excludes.add(exclude);
      }
    }
  }

  private static List<String> getExcludesFromDir(File dir) {
    List<String> excludes = new ArrayList<>();
    Collection<File> listFiles = FileUtils.listFiles(dir, new String[] {"jar"}, false);
    listFiles.forEach(file -> excludes.add(filenameToExclude(file.getName())));
    return excludes;
  }

  private static String filenameToExclude(String filename) {
    return "*:" + FilenameUtils.removeExtension(filename);
  }

}
