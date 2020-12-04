package com.adaptris.downloader.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CollectionUtilsTest {

  @Test
  public void testListIsEmptyTrue() throws Exception {
    assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
  }

  @Test
  public void testListIsEmptyNull() throws Exception {
    assertTrue(CollectionUtils.isEmpty((List<String>) null));
  }

  @Test
  public void testListIsEmptyNotEmpty() throws Exception {
    assertFalse(CollectionUtils.isEmpty(Collections.singletonList("value")));
  }

  @Test
  public void testMapIsEmptyTrue() throws Exception {
    assertTrue(CollectionUtils.isEmpty(Collections.emptyMap()));
  }

  @Test
  public void testMapIsEmptyNull() throws Exception {
    assertTrue(CollectionUtils.isEmpty((Map<String, String>) null));
  }

  @Test
  public void testMapIsEmptyNotEmpty() throws Exception {
    assertFalse(CollectionUtils.isEmpty(Collections.singletonMap("key", "value")));
  }

}
