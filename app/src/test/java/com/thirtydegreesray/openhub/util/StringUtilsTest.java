package com.thirtydegreesray.openhub.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testListToStringWithNullList() {
        // This should not crash and should return empty string
        String result = StringUtils.listToString(null, ",");
        assertEquals("", result);
    }

    @Test
    public void testListToStringWithEmptyList() {
        List<String> emptyList = new ArrayList<>();
        String result = StringUtils.listToString(emptyList, ",");
        assertEquals("", result);
    }

    @Test
    public void testListToStringWithValidList() {
        List<String> testList = Arrays.asList("user", "repo", "gist");
        String result = StringUtils.listToString(testList, ",");
        assertEquals("user,repo,gist", result);
    }

    @Test
    public void testListToStringWithNullSeparator() {
        List<String> testList = Arrays.asList("user", "repo");
        String result = StringUtils.listToString(testList, null);
        assertEquals("", result);
    }

    @Test
    public void testListToStringWithEmptySeparator() {
        List<String> testList = Arrays.asList("user", "repo");
        String result = StringUtils.listToString(testList, "");
        assertEquals("", result);
    }
}
