package com.adaptris.downloader.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionUtilsTest {

  @Test
  public void testIsSnapshotTrue() {
    assertTrue(VersionUtils.isSnapshot("3.10-SNAPSHOT"));
  }

  @Test
  public void testIsSnapshotFalse() {
    assertFalse(VersionUtils.isSnapshot("3.10.0-RELEASE"));
  }

  @Test
  public void testIsSnapshotNull() {
    assertFalse(VersionUtils.isSnapshot(null));
  }

}
