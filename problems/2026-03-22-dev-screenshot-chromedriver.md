# 开发环境截图问题排查记录

日期：2026-03-22

## 问题现象

应用部署完成后，系统会异步生成网页封面截图，但开发环境中截图流程出现两类问题：

1. 首次截图时长时间卡住，日志持续显示在下载 `chromedriver`
2. 后续截图报错：

```text
org.openqa.selenium.NoSuchSessionException: invalid session id: session deleted as the browser has closed the connection
from disconnected: not connected to DevTools
```

典型日志包括：

```text
开始生成网页截图，URL：http://localhost/dYlzxJ/
Recoverable I/O exception ... https://raw.githubusercontent.com
Downloading https://storage.googleapis.com/chrome-for-testing-public/...
```

以及：

```text
网页截图失败：http://localhost/dYlzxJ/
org.openqa.selenium.NoSuchSessionException: invalid session id
```

## 根因分析

### 1. 自动下载驱动导致开发环境不稳定

原实现会在初始化截图驱动时执行：

```java
WebDriverManager.chromedriver().setup();
```

这会触发访问外网自动下载 `chromedriver`。如果网络慢、被拦截或访问不稳定，就会导致截图流程长时间阻塞。

### 2. WebDriver 被设计成全局静态单例

原来的 `WebScreenshotUtils` 使用一个全局静态 `WebDriver`：

```java
private static final WebDriver webDriver;
```

截图任务又是异步触发的。如果某次截图过程中 Chrome 进程断开、DevTools 连接失效，后续请求还会继续复用这条已经失效的 session，于是就会抛出 `NoSuchSessionException`。

## 已做修复

本次修复已在项目中落地，核心改动如下：

1. 截图时不再复用全局静态 `WebDriver`
2. 每次调用 `saveWebPageScreenshot` 都会单独创建一个 `ChromeDriver`
3. 截图完成后无论成功还是失败，都会在 `finally` 中释放 driver
4. 初始化驱动时优先读取本地 `chromedriver`，找不到才回退到 `WebDriverManager`
5. COS 上传对象键改为 `screenshots/...`，不再携带前导 `/`
6. 本地 `COS` 配置改为通过环境变量注入，不再在 `application-local.yml` 中硬编码密钥

当前本地驱动优先级如下：

1. JVM 参数 `webdriver.chrome.driver`
2. 环境变量 `CHROMEDRIVER_PATH`
3. 项目内固定路径 `drivers/chromedriver.exe`
4. 项目内固定路径 `tmp/drivers/chromedriver.exe`
5. 系统固定路径 `C:/Program Files/ChromeDriver/chromedriver.exe`
6. 系统固定路径 `C:/Program Files (x86)/ChromeDriver/chromedriver.exe`
7. 最后才使用 `WebDriverManager.chromedriver().setup()`

## 开发环境推荐配置

为了避免每次启动都联网下载驱动，建议手动下载与本机 Chrome 大版本一致的 `chromedriver`。

本机 Chrome 版本示例：

```text
146.0.7680.80
```

则应下载 `ChromeDriver 146.x`。

推荐任选一种本地配置方式：

### 方式一：启动时传 JVM 参数

```text
-Dwebdriver.chrome.driver=D:\tools\chromedriver\chromedriver.exe
```

### 方式二：配置环境变量

```text
CHROMEDRIVER_PATH=D:\tools\chromedriver\chromedriver.exe
```

### 方式三：放到项目固定目录

```text
yu-ai-code-mother/drivers/chromedriver.exe
```

## COS 配置说明

开发环境请通过环境变量提供 COS 配置：

```text
COS_HOST=https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com
COS_SECRET_ID=<your-secret-id>
COS_SECRET_KEY=<your-secret-key>
COS_REGION=ap-shanghai
COS_BUCKET=econgencode-1379443208
```

注意：

1. `SecretId` 和 `SecretKey` 必须来自同一条腾讯云 API 密钥
2. `bucket` 和 `region` 必须与控制台中的对象存储桶一致
3. 上传对象 key 不应以 `/` 开头，否则后续会增加签名和路径排查成本

## 修复后的效果

修复后，开发环境截图流程具备以下特性：

1. 不再因为某次浏览器断开导致后续所有截图都失败
2. 本地有驱动时，不再重复联网下载
3. 单次截图失败不会污染下一次截图请求

## 验证结果

已通过针对截图工具的回归测试：

```text
./mvnw -Dtest=WebScreenshotUtilsTest test
```

验证点包括：

1. 每次截图调用都会创建独立 driver
2. 每次截图结束后都会释放 driver
3. 当本地 `webdriver.chrome.driver` 已配置时，初始化逻辑会优先使用本地驱动

## 后续建议

1. 开发环境固定使用本地 `chromedriver`
2. 生产环境不要依赖运行时在线下载驱动
3. 如果后续还出现截图失败，需要继续排查目标页面本身是否导致 Chrome 进程崩溃
