package com.yupi.yuaicodemother.utils;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class WebScreenshotUtilsTest {

    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7Z0e0AAAAASUVORK5CYII=");

    @AfterEach
    void tearDown() throws Exception {
        invokeStatic("resetWebDriverFactoryForTest");
        invokeStatic("resetChromeDriverPathResolverForTest");
        System.clearProperty("webdriver.chrome.driver");
    }

    @Test
    void saveWebPageScreenshot_createsAndClosesDriverForEachCall() throws Exception {
        FakeDriverSession firstSession = new FakeDriverSession();
        FakeDriverSession secondSession = new FakeDriverSession();
        Deque<FakeDriverSession> sessions = new ArrayDeque<>();
        sessions.add(firstSession);
        sessions.add(secondSession);
        AtomicInteger createCount = new AtomicInteger();

        invokeStatic("setWebDriverFactoryForTest", (Supplier<WebDriver>) () -> {
            createCount.incrementAndGet();
            return sessions.removeFirst().driver();
        });

        String firstPath = WebScreenshotUtils.saveWebPageScreenshot("http://localhost/app-1/");
        String secondPath = WebScreenshotUtils.saveWebPageScreenshot("http://localhost/app-2/");

        Assertions.assertNotNull(firstPath);
        Assertions.assertNotNull(secondPath);
        Assertions.assertEquals(2, createCount.get());
        Assertions.assertTrue(Files.exists(Path.of(firstPath)));
        Assertions.assertTrue(Files.exists(Path.of(secondPath)));
        Assertions.assertNotEquals(firstPath, secondPath);
        Assertions.assertTrue(firstSession.quitCalled);
        Assertions.assertTrue(secondSession.quitCalled);
        FileUtil.del(firstPath);
        FileUtil.del(secondPath);
    }

    @Test
    void configureChromeDriver_usesExistingSystemPropertyPath() throws Exception {
        Path driverPath = Files.createTempFile("chromedriver", ".exe");
        System.setProperty("webdriver.chrome.driver", driverPath.toString());

        invokeStatic("setChromeDriverPathResolverForTest", (Supplier<String>) () -> {
            throw new AssertionError("should not query fallback path resolver when system property is set");
        });

        Method method = WebScreenshotUtils.class.getDeclaredMethod("configureChromeDriver");
        method.setAccessible(true);
        method.invoke(null);

        Assertions.assertEquals(driverPath.toString(), System.getProperty("webdriver.chrome.driver"));
        FileUtil.del(driverPath.toFile());
    }

    private static Object invokeStatic(String methodName, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Supplier) {
                parameterTypes[i] = Supplier.class;
            } else {
                parameterTypes[i] = args[i].getClass();
            }
        }
        Method method = WebScreenshotUtils.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    private static final class FakeDriverSession implements InvocationHandler {

        private final WebDriver driver;
        private boolean quitCalled;

        private FakeDriverSession() {
            this.driver = (WebDriver) Proxy.newProxyInstance(
                    WebDriver.class.getClassLoader(),
                    new Class[]{WebDriver.class, JavascriptExecutor.class, TakesScreenshot.class},
                    this
            );
        }

        private WebDriver driver() {
            return driver;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            return switch (name) {
                case "get" -> null;
                case "quit", "close" -> {
                    quitCalled = true;
                    yield null;
                }
                case "getScreenshotAs" -> ONE_PIXEL_PNG;
                case "executeScript" -> "complete";
                case "manage" -> createOptionsProxy();
                case "toString" -> "FakeDriverSession";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object createOptionsProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("timeouts".equals(method.getName())) {
                    return createTimeoutsProxy();
                }
                return defaultValue(method.getReturnType());
            };
            return Proxy.newProxyInstance(
                    WebDriver.Options.class.getClassLoader(),
                    new Class[]{WebDriver.Options.class},
                    handler
            );
        }

        private Object createTimeoutsProxy() {
            InvocationHandler handler = (proxy, method, args) -> proxy;
            return Proxy.newProxyInstance(
                    WebDriver.Timeouts.class.getClassLoader(),
                    new Class[]{WebDriver.Timeouts.class},
                    handler
            );
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class.equals(returnType)) {
            return false;
        }
        if (char.class.equals(returnType)) {
            return '\0';
        }
        if (byte.class.equals(returnType)) {
            return (byte) 0;
        }
        if (short.class.equals(returnType)) {
            return (short) 0;
        }
        if (int.class.equals(returnType)) {
            return 0;
        }
        if (long.class.equals(returnType)) {
            return 0L;
        }
        if (float.class.equals(returnType)) {
            return 0F;
        }
        if (double.class.equals(returnType)) {
            return 0D;
        }
        return null;
    }
}
