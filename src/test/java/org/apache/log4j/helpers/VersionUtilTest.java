package org.apache.log4j.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionUtilTest {

    @Test
    public void smoke() {
        assertEquals(4, VersionUtil.getJavaMajorVersion("1.4.xx"));
        assertEquals(5, VersionUtil.getJavaMajorVersion("1.5"));
        assertEquals(5, VersionUtil.getJavaMajorVersion("1.5.xx"));
        assertEquals(5, VersionUtil.getJavaMajorVersion("1.5AA"));
        assertEquals(7, VersionUtil.getJavaMajorVersion("1.7.0_80-b15"));
        assertEquals(8, VersionUtil.getJavaMajorVersion("1.8.0_311"));
        assertEquals(9, VersionUtil.getJavaMajorVersion("9EA"));
        assertEquals(9, VersionUtil.getJavaMajorVersion("9.0.1"));
        assertEquals(18, VersionUtil.getJavaMajorVersion("18.3+xx"));
        assertEquals(19, VersionUtil.getJavaMajorVersion("19"));
        assertEquals(21, VersionUtil.getJavaMajorVersion("21"));
   }
}
