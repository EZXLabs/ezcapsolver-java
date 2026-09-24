<div align="center">
  <img src="./assets/ez-captcha-logo.svg" alt="EZCaptchaSolver by EZXLabs" height="88">
  &nbsp;&nbsp;
  <img src="./assets/java.svg" alt="Java" height="88">
  <h1>EZCaptchaSolver Java SDK</h1>
  <p>
    <a href="https://github.com/EZXLabs/ezcapsolver-java/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/EZXLabs/ezcapsolver-java/actions/workflows/ci.yml/badge.svg"></a>
    <a href="https://central.sonatype.com/artifact/com.burstlinker.ezcapsolver/ezcapsolver-java"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.burstlinker.ezcapsolver/ezcapsolver-java.svg?logo=apachemaven"></a>
    <a href="https://javadoc.io/doc/com.burstlinker.ezcapsolver/ezcapsolver-java"><img alt="javadoc" src="https://javadoc.io/badge2/com.burstlinker.ezcapsolver/ezcapsolver-java/javadoc.svg"></a>
    <a href="./LICENSE"><img alt="License: Apache-2.0" src="https://img.shields.io/badge/license-Apache--2.0-blue.svg"></a>
    <a href="https://www.oracle.com/java/"><img alt="Java 8+" src="https://img.shields.io/badge/java-8%2B-orange.svg?logo=openjdk&logoColor=white"></a>
    <a href="https://ezxlabs.com"><img alt="EZXLabs 官网" src="https://img.shields.io/badge/website-ezxlabs.com-FFDB29?logoColor=black"></a>
  </p>
  <p>
    <a href="https://ezxlabs.com">🌐 官网</a> &nbsp;·&nbsp;
    <a href="https://docs.ezxlabs.com/zh/docs/captcha/api">📚 EZCaptchaSolver API 文档</a> &nbsp;·&nbsp;
    <a href="./examples">🧪 示例</a> &nbsp;·&nbsp;
    <a href="#-支持的验证码类型">🧩 验证码类型</a>
  </p>
  <p><a href="./README.md">English</a> &nbsp;·&nbsp; <b>简体中文</b></p>
</div>

---

EZCaptchaSolver Java SDK 是 [EZXLabs](https://ezxlabs.com) 维护的开源 Java 客户端，用于接入其 CAPTCHA 识别任务 API。它为下列受支持的任务类型提供类型化请求和线程安全的客户端；该库还包含不用于解验证码的 TLS 转发任务。了解 SDK 产品系列请查看 [EZCaptchaSolver SDK 产品页](https://ezxlabs.com/zh/products/sdk)，查看 HTTP 请求与响应字段请访问 [EZCaptchaSolver API 文档](https://docs.ezxlabs.com/zh/docs/captcha/api)，Java 调用代码请以[本仓库示例](./examples/README.zh-CN.md)为准。

## 🧩 支持的验证码类型

验证码任务类型分同步和异步两种形态:

- 同步:创建任务后请求阻塞,任务完成时返回。
- 异步:创建任务返回一个 task ID,之后靠轮询这个 ID 取结果。适合耗时较长的验证码类型。

一种验证码类型可以同时支持两种形态,绝大多数类型都支持同步形态,少数类型只支持异步。

### reCAPTCHA v2

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `ReCaptchaV2TaskProxyless` | all | [示例](#ReCaptchaV2TaskProxyless) | reCAPTCHA v2 |
| `ReCaptchaV2TaskProxylessS9` | all | [示例](#ReCaptchaV2TaskProxylessS9) | reCAPTCHA v2,返回评分 ≥ 0.9 的 token |
| `ReCaptchaV2STaskProxyless` | all | [示例](#ReCaptchaV2STaskProxyless) | reCAPTCHA v2,带挑战绑定的 `s` 参数 |
| `ReCaptchaV2EnterpriseTaskProxyless` | all | [示例](#ReCaptchaV2EnterpriseTaskProxyless) | reCAPTCHA v2 Enterprise |
| `ReCaptchaV2SEnterpriseTaskProxyless` | all | [示例](#ReCaptchaV2SEnterpriseTaskProxyless) | reCAPTCHA v2 Enterprise,带 `s` 参数 |
| `ReCaptchaV2Classification` | sync | [示例](#ReCaptchaV2Classification) | reCAPTCHA v2 图像识别 |

### reCAPTCHA v3

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `ReCaptchaV3TaskProxyless` | all | [示例](#ReCaptchaV3TaskProxyless) | reCAPTCHA v3 |
| `ReCaptchaV3TaskProxylessS9` | all | [示例](#ReCaptchaV3TaskProxylessS9) | reCAPTCHA v3,返回评分 ≥ 0.9 的 token |
| `ReCaptchaV3EnterpriseTaskProxyless` | all | [示例](#ReCaptchaV3EnterpriseTaskProxyless) | reCAPTCHA v3 Enterprise |
| `ReCaptchaV3EnterpriseTaskProxylessS9` | all | [示例](#ReCaptchaV3EnterpriseTaskProxylessS9) | reCAPTCHA v3 Enterprise,返回评分 ≥ 0.9 的 token |

### FunCaptcha / Arkose Labs

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `FuncaptchaTaskProxyless` | async | [示例](#FuncaptchaTaskProxyless) | FunCaptcha / Arkose Labs |
| `FunCaptchaClassification` | sync | [示例](#FunCaptchaClassification) | FunCaptcha 图像识别 |

### hCaptcha

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `HCaptcha` | async | [示例](#HCaptcha) | hCaptcha |
| `HCaptchaClassification` | sync | [示例](#HCaptchaClassification) | hCaptcha 图像识别,单图或多图 |

### Cloudflare

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `CloudFlare5STask` | async | [示例](#CloudFlare5STask) | CF 五秒盾,**必须**提供 `proxy` |
| `CloudFlareTurnstileTask` | async | [示例](#CloudFlareTurnstileTask) | Turnstile,返回 token |

### Akamai

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `AkamaiWEBTaskProxyless` | sync | [示例](#AkamaiWEBTaskProxyless) | Akamai Web |
| `AkamaiSBSDTaskProxyless` | sync | [示例](#AkamaiSBSDTaskProxyless) | Akamai SBSD |

> Akamai Web 是多轮流程:把上一轮返回的 `encodedata` 作为下一轮的 `encodeData` 传回去。这两个拼写在线格式上确实不一样,SDK 保留服务端的定义原样,不去「纠正」它。

### DataDome

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `DataDomeTaskProxyless` | sync | [示例](#DataDomeTaskProxyless) | 被拦截后的挑战,分两步,由 `step` 选择 |
| `DataDomeTagsTaskProxyless` | sync | [示例](#DataDomeTagsTaskProxyless) | 在正常浏览路径上上报指纹 |

### 其它

| 任务类型 | 形态 | 示例 | 说明 |
| :-: | :---: | :-: | --- |
| `PerimeterX` | async | [示例](#PerimeterX) | PerimeterX 放行 cookie |
| `IncapsulaTaskProxyless` | sync | [示例](#IncapsulaTaskProxyless) | Incapsula Reese84 载荷 |
| `TlsTask` | sync | [示例](#TlsTask) | 经 TLS 转发一次 HTTP 请求,返回上游响应 |

## 📦 安装

**Maven**

```xml
<dependency>
  <groupId>com.burstlinker.ezcapsolver</groupId>
  <artifactId>ezcapsolver-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

**Gradle**

```kotlin
implementation("com.burstlinker.ezcapsolver:ezcapsolver-java:0.1.0")
```

### 环境要求

**Java 8 及以上。** 产物以 `--release 8` 编译,CI 还会在真实的 JDK 8 上跑完整测试套件 —— 这条下限是被验证过的,不只是声明出来的。

## 🚀 快速开始

配置里没有显式提供 key 时,客户端会从环境变量 `EZCAPTCHA_API_KEY` 读取。

```java
import com.burstlinker.ezcapsolver.EzCapSolverClient;
import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.ReCaptchaSolution;
import com.burstlinker.ezcapsolver.model.task.ReCaptchaV2TaskParams;

EzCapSolverClient client = new EzCapSolverClient();

ReCaptchaV2TaskParams params = ReCaptchaV2TaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("6Lc_your_site_key")
        .build();

Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxyless(params);

System.out.println(solved.getTaskId());
System.out.println(solved.getSolution().getToken());
```

> **创建任务会扣费,且无论 worker 是否成功都扣。** SDK 绝不自行重试创建任务:超时无法告诉你服务端是否已经受理,盲目重试等于付两次钱。该怎么办见 [错误处理](#️-错误处理)。

## 📖 用法

### 同步 / 异步

**每种任务类型都有两个方法**,接收同样的参数模型、返回同样的类型,区别只在端点:

```java
// 创建任务,然后轮询结果
Solved<ReCaptchaSolution> a = client.solveReCaptchaV2TaskProxyless(params);

// 同步端点,一次请求出结果
Solved<ReCaptchaSolution> b = client.syncSolveReCaptchaV2TaskProxyless(params);
```

### reCAPTCHA v2

[reCAPTCHA v2 API 文档](https://docs.ezxlabs.com/zh/docs/captcha/api/recaptcha-v2)

前五种共用 `ReCaptchaV2TaskParams` 和 `ReCaptchaSolution`,只有方法名不同。

<a id="ReCaptchaV2TaskProxyless"></a>

#### ReCaptchaV2TaskProxyless

```java
ReCaptchaV2TaskParams params = ReCaptchaV2TaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("6Lc_your_site_key")
        .invisible(false)
        .build();

Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxyless(params);
System.out.println(solved.getSolution().getToken());
```

`invisible` 是原始 `boolean`,因此**恒发送**。这正是关键:`false` 是一个真实答案,意思是「控件可见」;省略这个字段会让服务端套用它自己的默认值,而不是你的选择。

<a id="ReCaptchaV2TaskProxylessS9"></a>

#### ReCaptchaV2TaskProxylessS9

参数与普通 v2 相同,走高分队列,返回评分 0.9 及以上的 token。

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxylessS9(params);
System.out.println(solved.getSolution().getToken());
```

<a id="ReCaptchaV2STaskProxyless"></a>

#### ReCaptchaV2STaskProxyless

带挑战绑定的 `s` 参数。它其实不是必填的,不填就等同于普通 v2。

```java
ReCaptchaV2TaskParams params = ReCaptchaV2TaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("6Lc_your_site_key")
        .s("从页面上读到的值")
        .build();

Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2STaskProxyless(params);
```

<a id="ReCaptchaV2EnterpriseTaskProxyless"></a>

#### ReCaptchaV2EnterpriseTaskProxyless

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2EnterpriseTaskProxyless(params);
```

<a id="ReCaptchaV2SEnterpriseTaskProxyless"></a>

#### ReCaptchaV2SEnterpriseTaskProxyless

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2SEnterpriseTaskProxyless(params);
```

<a id="ReCaptchaV2Classification"></a>

#### ReCaptchaV2Classification

识别图像网格,而不是解控件。同步。

```java
byte[] image = Files.readAllBytes(Paths.get("grid.jpg"));

ReCaptchaV2ClassificationTaskParams params = ReCaptchaV2ClassificationTaskParams.builder()
        .image(Base64.getEncoder().encodeToString(image))
        .question("/m/014xcs")
        .size(3)
        .build();

Solved<ReClassificationSolution> solved =
        client.syncSolveReCaptchaV2Classification(params);

System.out.println(solved.getSolution().getObjects());   // 例如 [0, 4, 7]
System.out.println(solved.getSolution().isMulti());
```

`size` 是装箱的 `Integer`,不设置就不发送,服务端套用自己的默认值 4。只有当网格不是 4×4 时才需要设。

### reCAPTCHA v3

[reCAPTCHA v3 API 文档](https://docs.ezxlabs.com/zh/docs/captcha/api/recaptcha-v3)

四种共用 `ReCaptchaV3TaskParams` 和 `ReCaptchaSolution`。

<a id="ReCaptchaV3TaskProxyless"></a>

#### ReCaptchaV3TaskProxyless

```java
ReCaptchaV3TaskParams params = ReCaptchaV3TaskParams.builder()
        .websiteUrl("https://example.com/checkout")
        .websiteKey("6Lc_your_site_key")
        .pageAction("checkout")
        .build();

Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3TaskProxyless(params);
System.out.println(solved.getSolution().getToken());
```

<a id="ReCaptchaV3TaskProxylessS9"></a>

#### ReCaptchaV3TaskProxylessS9

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3TaskProxylessS9(params);
```

<a id="ReCaptchaV3EnterpriseTaskProxyless"></a>

#### ReCaptchaV3EnterpriseTaskProxyless

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3EnterpriseTaskProxyless(params);
```

<a id="ReCaptchaV3EnterpriseTaskProxylessS9"></a>

#### ReCaptchaV3EnterpriseTaskProxylessS9

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV3EnterpriseTaskProxylessS9(params);
```

### FunCaptcha / Arkose Labs

[FunCaptcha API 文档](https://docs.ezxlabs.com/zh/docs/captcha/api/funcaptcha)

<a id="FuncaptchaTaskProxyless"></a>

#### FuncaptchaTaskProxyless

```java
FunCaptchaTaskParams params = FunCaptchaTaskParams.builder()
        .websiteUrl("https://example.com/signup")
        .websiteKey("YOUR_PUBLIC_KEY")
        .apiJsSubdomain("client-api.arkoselabs.com")
        .data("页面产生的 blob 值")
        .build();

Solved<FunCaptchaSolution> solved = client.solveFuncaptchaTaskProxyless(params);
System.out.println(solved.getSolution().getToken());
```

<a id="FunCaptchaClassification"></a>

#### FunCaptchaClassification

```java
FunCaptchaClassificationTaskParams params = FunCaptchaClassificationTaskParams.builder()
        .image(Base64.getEncoder().encodeToString(image))
        .question("Pick the image that is the right way up")
        .build();

Solved<FunCaptchaClassificationSolution> solved =
        client.syncSolveFunCaptchaClassification(params);

System.out.println(solved.getRaw());
```

### hCaptcha

[hCaptcha API 文档](https://docs.ezxlabs.com/zh/docs/captcha/api/hcaptcha)

<a id="HCaptcha"></a>

#### HCaptcha

```java
HCaptchaTaskParams params = HCaptchaTaskParams.builder()
        .websiteUrl("https://accounts.hcaptcha.com/demo")
        .websiteKey("338af34c-7bcb-4c7c-900b-acbec73d7d43")
        .lang("en-US")
        .invisible(false)
        .build();

Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
System.out.println(solved.getSolution().getGeneratedPassUuid());
System.out.println(solved.getSolution().getUa());
```

<a id="HCaptchaClassification"></a>

#### HCaptchaClassification

```java
HCaptchaClassificationTaskParams params = HCaptchaClassificationTaskParams.builder()
        .images(Arrays.asList(imageBase64))
        .question("Please click each image containing a crosswalk")
        .build();

Solved<HCaptchaClassificationSolution> solved =
        client.syncSolveHCaptchaClassification(params);

System.out.println(solved.getRaw());
```

### Cloudflare

<a id="CloudFlare5STask"></a>

#### CloudFlare5STask

```java
Map<String, Object> rqData = new LinkedHashMap<>();
rqData.put("chlPageData", "...");

CloudFlare5sTaskParams params = CloudFlare5sTaskParams.builder()
        .websiteUrl("https://example.com")
        .proxy("http://user:pass@host:8080")
        .rqData(rqData)
        .build();

Solved<CloudFlare5sSolution> solved = client.solveCloudFlare5STask(params);

System.out.println(solved.getSolution().getCookies());
System.out.println(solved.getSolution().getHeader());
System.out.println(solved.getSolution().getTlsVersion());
```

**这个类型的代理是必填的**,不像大多数类型。

<a id="CloudFlareTurnstileTask"></a>

#### CloudFlareTurnstileTask

```java
CloudFlareTurnstileTaskParams params = CloudFlareTurnstileTaskParams.builder()
        .websiteUrl("https://example.com/login")
        .websiteKey("0x4AAAAAAA...")
        .build();

Solved<CloudFlareTurnstileSolution> solved = client.solveCloudFlareTurnstileTask(params);
System.out.println(solved.getSolution().getToken());
```

### Akamai

<a id="AkamaiWEBTaskProxyless"></a>

#### AkamaiWEBTaskProxyless

多轮流程。**每一轮都是一个单独扣费的任务。**

```java
String encodeData = "";

for (int index = 0; index < 3; index++) {
    AkamaiWebTaskParams params = AkamaiWebTaskParams.builder()
            .pageUrl("https://example.com")
            .v3Url("https://example.com/akam/13/abcdef12")
            .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .lang("en-US")
            .index(index)
            .encodeData(encodeData)
            .build();

    Solved<AkamaiWebSolution> solved = client.syncSolveAkamaiWEBTaskProxyless(params);

    // 出来是 encodedata,进去是 encodeData —— 服务端两个方向的拼写就是不一样
    encodeData = solved.getSolution().getEncodedata();
}
```

<a id="AkamaiSBSDTaskProxyless"></a>

#### AkamaiSBSDTaskProxyless

```java
AkamaiSbsdTaskParams params = AkamaiSbsdTaskParams.builder()
        .pageUrl("https://example.com")
        .sbsdUrl("https://example.com/.well-known/sbsd")
        .bmSo("已有的 bm_so cookie")
        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .lang("en-US")
        .scriptBase64(Base64.getEncoder().encodeToString(script))
        .build();

Solved<AkamaiSbsdSolution> solved = client.syncSolveAkamaiSBSDTaskProxyless(params);
System.out.println(solved.getSolution().getPayload());
```

### DataDome

<a id="DataDomeTaskProxyless"></a>

#### DataDomeTaskProxyless

```java
DataDomeTaskParams params = DataDomeTaskParams.builder()
        .htmlB64(Base64.getEncoder().encodeToString(challengeHtml))
        .step(DataDomeTaskParams.STEP_ONE)
        .referer("https://example.com/search")
        .build();

Solved<DataDomeSolution> solved = client.syncSolveDataDomeTaskProxyless(params);
System.out.println(solved.getSolution().getUrl());
```

<a id="DataDomeTagsTaskProxyless"></a>

#### DataDomeTagsTaskProxyless

```java
Map<String, Object> fields = new LinkedHashMap<>();
fields.put("tags_url", "https://example.com/js/tags.js");

DataDomeTagsTaskParams params = DataDomeTagsTaskParams.builder()
        .ddk("YOUR_DDJSKEY")                              // 页面上的 window.ddjskey
        .jsType(DataDomeTagsTaskParams.JS_TYPE_CH)
        .bpc(DataDomeTagsTaskParams.MIN_PACKET_COUNTER)
        .referer("https://example.com/search")
        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .fields(fields)
        .build();

Solved<DataDomeSolution> solved = client.syncSolveDataDomeTagsTaskProxyless(params);
```

### 其它

<a id="PerimeterX"></a>

#### PerimeterX

```java
PerimeterXTaskParams params = PerimeterXTaskParams.builder()
        .websiteKey("YOUR_PX_APP_ID")
        .invisible(false)
        .build();

Solved<PerimeterXSolution> solved = client.solvePerimeterX(params);

System.out.println(solved.getSolution().getPx3());
System.out.println(solved.getSolution().getPxVid());
System.out.println(solved.getSolution().getPxde());
```

<a id="IncapsulaTaskProxyless"></a>

#### IncapsulaTaskProxyless

```java
IncapsulaTaskParams params = IncapsulaTaskParams.builder()
        .script("(function(){...})()")            // 脚本源码本身,不只是 URL
        .scriptUrl("https://example.com/_Incapsula_Resource?SWJIYLWA=...")
        .pageUrl("https://example.com")
        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .acceptLanguage("en-US,en;q=0.9")
        .build();

Solved<IncapsulaSolution> solved = client.syncSolveIncapsulaTaskProxyless(params);
System.out.println(solved.getSolution().getData());
```

<a id="TlsTask"></a>

#### TlsTask

这个压根不是验证码:由 worker 用它自己的 TLS 指纹发一次 HTTP 请求,把上游响应交回来。

```java
Map<String, Object> headers = new LinkedHashMap<>();
headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
headers.put("Accept", "application/json");

TlsForwardTaskParams params = TlsForwardTaskParams.builder()
        .tlsType("chrome")
        .proxy("http://user:pass@host:8080")
        .method(TlsForwardTaskParams.METHOD_GET)
        .url("https://example.com/api/profile")
        .headers(headers)
        .headersOrder("User-Agent,Accept")
        .build();

Solved<TlsForwardSolution> solved = client.syncSolveTlsTask(params);

System.out.println(solved.getSolution().getStatus());
System.out.println(solved.getSolution().getBody());
```

### 透传字段

服务端加参数的速度比任何 SDK 发版都快。**你永远不需要等。**

模型没声明的参数走 `put`,在线格式上和已声明字段落在同一层:

```java
HCaptchaTaskParams params = HCaptchaTaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("...")
        .build();

params.put("someParameterAddedLater", 42);
```

反方向同理。worker 返回的、solution 模型没声明的内容会被保留而不是丢弃:

```java
Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);

Object extra = solved.getSolution().get("someNewField");
JsonNode raw = solved.getRaw();                // 整个响应,原封不动
```

### 自定义任务类型

本次发版之后服务端新增的任务类型照样能用 —— 把线格式名字当字符串传进去:

```java
Map<String, Object> params = new LinkedHashMap<>();
params.put("websiteURL", "https://example.com");
params.put("websiteKey", "...");

Solved<JsonNode> solved = client.solveRaw("SomeBrandNewTaskType", params);
Solved<JsonNode> sync   = client.syncSolveRaw("SomeBrandNewTaskType", params);
```

`TaskType` 用常量保存已知的名字,但它是一组 `String` 而不是枚举,正是为了让未知类型只是一次普通调用,而不是一个编译错误。`TaskType.isKnown(name)` 可以告诉你本次发版是否建模了某个类型。

## ⚙️ 配置

由简到繁:

```java
new EzCapSolverClient();                       // 全默认,key 从 EZCAPTCHA_API_KEY 读
new EzCapSolverClient("your-key");             // 全默认,key 在这里给
new EzCapSolverClient(someClientConfig);       // 用一份已经建好的配置
EzCapSolverClient.builder()./* ... */.build(); // 就地配置
```

`EzCapSolverClient.of("your-key")` 和 `EzCapSolverClient.of(someClientConfig)` 就是上面那两个构造的另一个名字。它们殊途同归,在调用处哪种读起来顺就用哪种。

```java
EzCapSolverClient client = EzCapSolverClient.builder()
        .clientKey("your-key")
        .timeout(Duration.ofSeconds(30))
        .syncTimeout(Duration.ofSeconds(240))
        .pollInterval(Duration.ofSeconds(3))
        .maxPollAttempts(50)
        .userAgent("my-app/1.0")
        .build();
```

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `clientKey` | `$EZCAPTCHA_API_KEY` | 必需,这里不设就从环境变量读 |
| `timeout` | 30s | 异步端点和余额查询 |
| `syncTimeout` | 240s | 仅 `/createSyncTask` |
| `pollInterval` | 3s | 两次查询结果之间的等待 |
| `maxPollAttempts` | 50 | `pollInterval × maxPollAttempts` 是 `solve*` 最长的等待时间 |
| `appId` | — | 可选的开发者/推广标识 |
| `proxy` | — | **你的进程**怎么连到 EZCaptchaSolver,不是 worker 的代理 |
| `userAgent` | `ezcapsolver-java/<版本>` | |
| `asyncBaseUrl` / `syncBaseUrl` | 公网端点 | 指向 mock 或私有部署时才改 |
| `okHttpClient` | 新建一个 | 传自己的进来可以共享连接池 |

**两条超时预算是刻意分开的。** `timeout` 覆盖立即响应的端点;`syncTimeout` 覆盖 `/createSyncTask`,它会阻塞到 worker 出结果,服务端给部分类型三分钟。合成一个值的结果是:要么掐断一个本来就要成功的同步任务(钱已经付了),要么为一个本该几秒内失败的余额查询干等几分钟。

`ClientConfig.toString()` 会掩码 client key 和代理 URL(代理 URL 自带凭据),可以放心打日志。

### 复用客户端

一个应用一个 client 就够。它构造后不可变,持有单个 OkHttp 连接池;每次请求新建一个等于把连接池扔掉、重新建连。它可以安全地在多线程间共享。

## ⚠️ 错误处理

所有失败都是 `EzCaptchaException` 之下的 unchecked 异常,所以只捕获这一个类型就是完整的兜底。

| 异常 | 含义 |
| --- | --- |
| `EzCaptchaException` | 基类。配置错误直接抛它 |
| `TransportException` | 没到达服务端,或者没等到回应 |
| `ApiException` | 服务端回应了,并且说不行 |
| `PollingExhaustedException` | 轮询预算用尽。**任务还在跑** |
| `UnexpectedResponseException` | 格式合法但违反契约的响应 |
| `SolutionDecodeException` | 任务成功了,但 solution 解不动 |
| `WaitInterruptedException` | 等待过程中线程被中断 |

`ApiException` 上的三个判定,覆盖了你实际需要做的决策:

```java
try {
    Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
} catch (ApiException e) {
    if (e.isAuthenticationError()) {
        // 停手。这几个码会触发服务端封禁计数,重试只会越陷越深。
    } else if (e.isRateLimited()) {
        // 在创建任何东西之前就被限流了。等一会儿重试是安全的。
    } else if (e.isTerminal()) {
        // 同样的输入重试会同样失败。要改的是请求本身。
    }
    log.error("{} ({}) request={}", e.getErrorCode(), e.getHttpStatus(), e.getRequestId());
}
```

### 找回已经付过钱的任务

这才是这套异常分层真正的用途。等待失败时,任务通常还在 —— 而且服务端会保留结果 **5 分钟**。

```java
try {
    solved = client.solveHCaptcha(params);
} catch (EzCaptchaException e) {
    String taskId = EzCaptchaException.taskIdOf(e);   // 对任意一种都有效
    if (taskId != null) {
        solved = client.waitForResult(taskId, HCaptchaSolution.class);
    }
}
```

`taskIdOf` 做成统一入口,正是为了让这段代码不必关心是哪一层失败的。改为创建第二个任务,等于为同一份工作付两次钱。

## 📝 日志

SDK 通过 slf4j 输出,并且**只带门面**。在你把一个绑定放上 classpath 之前它是静默的 —— 用你的应用已经在用的那个就行:

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.3.15</version>
</dependency>
```

> Logback 1.3.x 是 Java 8 的版本线,1.4+ 需要 Java 11。

Logger 按类命名,所以 `com.burstlinker.ezcapsolver` 就能框住整个 SDK。它打出来的任何内容都不含你的 API key。

## 🧪 可运行示例

一种任务类型一个文件,见 [`examples`](./examples) —— 索引在那里,代码在 `com.burstlinker.ezcapsolver.examples` 包下。

```bash
export EZCAPTCHA_API_KEY=your-key

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

java -cp "target/classes:$(cat target/cp.txt)" \
     com.burstlinker.ezcapsolver.examples.hcaptcha.HCaptcha
```

示例放在源码树里,这样每次构建编译器都会检查它们;同时它们被排除在发布的 jar、sources jar 和 javadoc 之外。

## 🛠️ 开发

```bash
mvn clean test                           # 测试套件
JAVA_HOME=/path/to/jdk8 mvn clean test   # 在 Java 8 上再跑一遍
mvn verify                               # 加上 animal-sniffer 和 japicmp
mvn javadoc:javadoc
typos
```

任何测试都不允许打真实服务 —— 创建任务会扣费。所有 HTTP 测试都走 MockWebServer。

新增一种任务类型要动哪五处、以及两个容易踩的模型陷阱,见 [`CONTRIBUTING.md`](./CONTRIBUTING.md)。

## 📄 许可证

[Apache-2.0](./LICENSE)
