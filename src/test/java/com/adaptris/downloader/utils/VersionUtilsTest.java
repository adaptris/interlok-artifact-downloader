package com.adaptris.downloader.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
