package com.adaptris.downloader.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

  @Test
  public void testListIsEmptyTrue() throws Exception {
    Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
  }

  @Test
  public void testListIsEmptyNull() throws Exception {
    Assert.assertTrue(CollectionUtils.isEmpty((List<String>) null));
  }

  @Test
  public void testListIsEmptyNotEmpty() throws Exception {
    Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonList("value")));
  }

  @Test
  public void testMapIsEmptyTrue() throws Exception {
    Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyMap()));
  }

  @Test
  public void testMapIsEmptyNull() throws Exception {
    Assert.assertTrue(CollectionUtils.isEmpty((Map<String, String>) null));
  }

  @Test
  public void testMapIsEmptyNotEmpty() throws Exception {
    Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonMap("key", "value")));
  }

}
