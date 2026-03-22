package com.yupi.yuaicodemother.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class ScreenshotServiceImplTest {

    @Test
    void generateScreenshotKey_shouldNotStartWithSlash() throws Exception {
        ScreenshotServiceImpl service = new ScreenshotServiceImpl();
        Method method = ScreenshotServiceImpl.class.getDeclaredMethod("generateScreenshotKey", String.class);
        method.setAccessible(true);

        String key = (String) method.invoke(service, "cover.jpg");

        Assertions.assertFalse(key.startsWith("/"));
        Assertions.assertTrue(key.startsWith("screenshots/"));
        Assertions.assertTrue(key.endsWith("/cover.jpg"));
    }
}
