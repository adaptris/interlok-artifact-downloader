package com.adaptris.downloader.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

  private ZipUtils() {}

  private static final int EOF = -1;
  private static final int BUFFER_SIZE = 1024;

  public final static String ZIP = ".zip";

  /**
   * Zip the file list into a ByteArrayOutputStream
   *
   * @param files
   * @throws IOException
   */
  public static ByteArrayOutputStream zipFileList(List<File> files) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ZipUtils.zipFileList(files, outputStream);
      return outputStream;
    }
  }

  /**
   * Zip the file list into the given output stream
   *
   * @param files
   * @param outputstream
   * @throws IOException
   */
  public static void zipFileList(List<File> files, OutputStream outputstream) throws IOException {
    try (ZipOutputStream outputStream = createZip(new BufferedOutputStream(outputstream))) {
      for (File file : files) {
        addFileToZip("", file, outputStream);
      }
    }
  }

  public static ZipOutputStream createZip(OutputStream out) throws IOException {
    return new ZipOutputStream(out);
  }

  public static void addToZip(String name, InputStream inputStream, ZipOutputStream outputStream) throws IOException {
    outputStream.putNextEntry(new ZipEntry(name));
    copy(inputStream, outputStream);
    outputStream.closeEntry();
  }

  private static void addFileToZip(String path, File srcFile, ZipOutputStream outputStream) throws IOException {
    try(FileInputStream inputStream = new FileInputStream(srcFile)) {
      addToZip(path + srcFile.getName(), inputStream, outputStream);
    }
  }

  private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
    byte[] buff = new byte[BUFFER_SIZE];
    int len = 0;
    while ((len = inputStream.read(buff)) > EOF) {
      outputStream.write(buff, 0, len);
    }
  }

}
