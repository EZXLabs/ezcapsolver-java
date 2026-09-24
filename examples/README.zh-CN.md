# 示例

[English](./README.md) · [简体中文](./README.zh-CN.md)

一种任务类型一个可运行文件,**文件名取自线格式的任务类型名**,与 Rust、Go、Python、JavaScript 四版逐个对应。

## 文件在哪

示例**代码**在正常的源码树里,包名 `com.burstlinker.ezcapsolver.examples`:

```
src/main/java/com/burstlinker/ezcapsolver/examples/
├── ClientSetup.java · Concurrency.java · RawUsage.java · LoggingAndErrors.java
├── akamai/ · cloudflare/ · datadome/ · funcaptcha/ · hcaptcha/
├── incapsula/ · perimeterx/ · recaptchav2/ · recaptchav3/ · tlsforward/
examples/
├── README.md · README.zh-CN.md   ← 你在这里
└── fixtures/                     ← 分类类示例读取的图片
```

代码放在源码树里意味着**每次构建编译器都会检查它** —— 示例一旦跟不上 API 变化,CI 直接红,而不是悄悄烂掉。

代价是构建必须把它们挡在发布物之外,这由三个插件负责:jar、sources jar、javadoc 各排除一次。`animal-sniffer` **故意不排除**,顺带证明示例本身也是 Java 8 兼容的。

> 因为示例被排除在 jar 之外,运行时要用 `target/classes` 而不是打好的 jar。下面的命令已经是这样写的。

> **每次运行都会创建真实任务并扣费,无论 worker 是否成功。** 唯一的例外是 [`ClientSetup.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/ClientSetup.java),它调用的 `getBalance` 是唯一免费的端点。
>
> reCAPTCHA v2 和 hCaptcha 的示例指向厂商自己的 demo 页面,照着就能跑。其余的站点密钥都是占位符,需要你自己填。

## 怎么跑

```bash
export EZCAPTCHA_API_KEY=your-key

# 只需一次:编译并导出依赖 classpath。
mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

# 之后用全限定类名跑任意示例。
java -cp "target/classes:$(cat target/cp.txt)" \
     com.burstlinker.ezcapsolver.examples.hcaptcha.HCaptcha
```

**在仓库根目录运行** —— 分类类示例会从 `examples/fixtures/` 读图片。

没有任何示例硬编码密钥。需要代理的读 `EZCAPTCHA_PROXY`;另有几个从环境变量读取参数,而不是编造一个看着像真的假值。

## SDK 本身

| 示例 | 讲什么 |
| --- | --- |
| [`ClientSetup.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/ClientSetup.java) | 全部配置项,以及两条超时预算为什么分开 |
| [`Concurrency.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/Concurrency.java) | 多线程共享一个 client,并限制并发数 |
| [`RawUsage.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/RawUsage.java) | 手动轮询,以及访问 SDK 尚未建模的任务类型 |
| [`LoggingAndErrors.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/LoggingAndErrors.java) | 接入日志,以及区分各层失败 |

## reCAPTCHA v2

[文档](https://docs.ezxlabs.com/zh/docs/captcha/api/recaptcha-v2)

| 示例 | 任务类型 |
| --- | --- |
| [`recaptchav2/ReCaptchaV2TaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2TaskProxyless.java) | `ReCaptchaV2TaskProxyless` |
| [`recaptchav2/ReCaptchaV2TaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2TaskProxylessS9.java) | `ReCaptchaV2TaskProxylessS9` |
| [`recaptchav2/ReCaptchaV2STaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2STaskProxyless.java) | `ReCaptchaV2STaskProxyless` |
| [`recaptchav2/ReCaptchaV2EnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2EnterpriseTaskProxyless.java) | `ReCaptchaV2EnterpriseTaskProxyless` |
| [`recaptchav2/ReCaptchaV2SEnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2SEnterpriseTaskProxyless.java) | `ReCaptchaV2SEnterpriseTaskProxyless` |
| [`recaptchav2/ReCaptchaV2Classification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2Classification.java) | `ReCaptchaV2Classification` |

## reCAPTCHA v3

[文档](https://docs.ezxlabs.com/zh/docs/captcha/api/recaptcha-v3)

| 示例 | 任务类型 |
| --- | --- |
| [`recaptchav3/ReCaptchaV3TaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3TaskProxyless.java) | `ReCaptchaV3TaskProxyless` |
| [`recaptchav3/ReCaptchaV3TaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3TaskProxylessS9.java) | `ReCaptchaV3TaskProxylessS9` |
| [`recaptchav3/ReCaptchaV3EnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3EnterpriseTaskProxyless.java) | `ReCaptchaV3EnterpriseTaskProxyless` |
| [`recaptchav3/ReCaptchaV3EnterpriseTaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3EnterpriseTaskProxylessS9.java) | `ReCaptchaV3EnterpriseTaskProxylessS9` |

## FunCaptcha / Arkose Labs

[文档](https://docs.ezxlabs.com/zh/docs/captcha/api/funcaptcha)

| 示例 | 任务类型 |
| --- | --- |
| [`funcaptcha/FuncaptchaTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/funcaptcha/FuncaptchaTaskProxyless.java) | `FuncaptchaTaskProxyless` |
| [`funcaptcha/FunCaptchaClassification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/funcaptcha/FunCaptchaClassification.java) | `FunCaptchaClassification` |

## hCaptcha

[文档](https://docs.ezxlabs.com/zh/docs/captcha/api/hcaptcha)

| 示例 | 任务类型 |
| --- | --- |
| [`hcaptcha/HCaptcha.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/hcaptcha/HCaptcha.java) | `HCaptcha` |
| [`hcaptcha/HCaptchaClassification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/hcaptcha/HCaptchaClassification.java) | `HCaptchaClassification` |

## Cloudflare

| 示例 | 任务类型 |
| --- | --- |
| [`cloudflare/CloudFlare5STask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/cloudflare/CloudFlare5STask.java) | `CloudFlare5STask` |
| [`cloudflare/CloudFlareTurnstileTask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/cloudflare/CloudFlareTurnstileTask.java) | `CloudFlareTurnstileTask` |

## Akamai

| 示例 | 任务类型 |
| --- | --- |
| [`akamai/AkamaiWEBTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/akamai/AkamaiWEBTaskProxyless.java) | `AkamaiWEBTaskProxyless` |
| [`akamai/AkamaiSBSDTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/akamai/AkamaiSBSDTaskProxyless.java) | `AkamaiSBSDTaskProxyless` |

## DataDome

| 示例 | 任务类型 |
| --- | --- |
| [`datadome/DataDomeTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/datadome/DataDomeTaskProxyless.java) | `DataDomeTaskProxyless` |
| [`datadome/DataDomeTagsTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/datadome/DataDomeTagsTaskProxyless.java) | `DataDomeTagsTaskProxyless` |

## 其它防护系统

| 示例 | 任务类型 |
| --- | --- |
| [`perimeterx/PerimeterX.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/perimeterx/PerimeterX.java) | `PerimeterX` |
| [`incapsula/IncapsulaTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/incapsula/IncapsulaTaskProxyless.java) | `IncapsulaTaskProxyless` |
| [`tlsforward/TlsTask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/tlsforward/TlsTask.java) | `TlsTask` |

## Fixtures

`fixtures/` 放分类类示例读取的图片。和其它几版 SDK 用的是同样的三个文件,方便跨语言对照。

## 新增示例

新增一种任务类型,需要在这里加一个示例文件,并在上面的表格里补一行 —— **两份 README 都要改**。没有任何测试守这条,靠 review 保证。任务类型还要动的另外四处见 [`../CONTRIBUTING.md`](../CONTRIBUTING.md)。
