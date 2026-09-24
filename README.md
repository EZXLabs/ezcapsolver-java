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
    <a href="https://ezxlabs.com"><img alt="EZXLabs website" src="https://img.shields.io/badge/website-ezxlabs.com-FFDB29?logoColor=black"></a>
  </p>
  <p>
    <a href="https://ezxlabs.com">🌐 Official website</a> &nbsp;·&nbsp;
    <a href="https://docs.ezxlabs.com/docs/captcha/api">📚 EZCaptchaSolver API reference</a> &nbsp;·&nbsp;
    <a href="./examples">🧪 Examples</a> &nbsp;·&nbsp;
    <a href="#-supported-captcha-types">🧩 Captcha Types</a>
  </p>
  <p><b>English</b> &nbsp;·&nbsp; <a href="./README.zh-CN.md">简体中文</a></p>
</div>

---

The EZCaptchaSolver Java SDK is an open-source Java client maintained by [EZXLabs](https://ezxlabs.com) for its CAPTCHA recognition task API. It provides typed requests and a thread-safe client for the supported task types below; the library also includes a TLS forwarding task that does not solve CAPTCHAs. For the wider SDK family, see the [EZCaptchaSolver SDK product page](https://ezxlabs.com/products/sdk); for HTTP request and response fields, see the [EZCaptchaSolver API reference](https://docs.ezxlabs.com/docs/captcha/api); for Java usage, see the [examples in this repository](./examples/README.md).

## 🧩 Supported Captcha Types

Captcha task types come in a synchronous and an asynchronous form:

- Synchronous: the request blocks after the task is created and returns once the task is done.
- Asynchronous: creating the task returns a task ID, and the result is fetched later by polling that ID. This suits captcha types that take a while to solve.

A captcha type can support both forms at once, and almost every type supports the synchronous one. A few types are asynchronous only.

### reCAPTCHA v2

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `ReCaptchaV2TaskProxyless` | all | [Example](#ReCaptchaV2TaskProxyless) | reCAPTCHA v2 |
| `ReCaptchaV2TaskProxylessS9` | all | [Example](#ReCaptchaV2TaskProxylessS9) | reCAPTCHA v2, returns a token scored ≥ 0.9 |
| `ReCaptchaV2STaskProxyless` | all | [Example](#ReCaptchaV2STaskProxyless) | reCAPTCHA v2 carrying the challenge-bound `s` parameter |
| `ReCaptchaV2EnterpriseTaskProxyless` | all | [Example](#ReCaptchaV2EnterpriseTaskProxyless) | reCAPTCHA v2 Enterprise |
| `ReCaptchaV2SEnterpriseTaskProxyless` | all | [Example](#ReCaptchaV2SEnterpriseTaskProxyless) | reCAPTCHA v2 Enterprise, carrying the `s` parameter |
| `ReCaptchaV2Classification` | sync | [Example](#ReCaptchaV2Classification) | reCAPTCHA v2 image recognition |

### reCAPTCHA v3

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `ReCaptchaV3TaskProxyless` | all | [Example](#ReCaptchaV3TaskProxyless) | reCAPTCHA v3 |
| `ReCaptchaV3TaskProxylessS9` | all | [Example](#ReCaptchaV3TaskProxylessS9) | reCAPTCHA v3, returns a token scored ≥ 0.9 |
| `ReCaptchaV3EnterpriseTaskProxyless` | all | [Example](#ReCaptchaV3EnterpriseTaskProxyless) | reCAPTCHA v3 Enterprise |
| `ReCaptchaV3EnterpriseTaskProxylessS9` | all | [Example](#ReCaptchaV3EnterpriseTaskProxylessS9) | reCAPTCHA v3 Enterprise, returns a token scored ≥ 0.9 |

### FunCaptcha / Arkose Labs

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `FuncaptchaTaskProxyless` | async | [Example](#FuncaptchaTaskProxyless) | FunCaptcha / Arkose Labs |
| `FunCaptchaClassification` | sync | [Example](#FunCaptchaClassification) | FunCaptcha image recognition |

### hCaptcha

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `HCaptcha` | async | [Example](#HCaptcha) | hCaptcha |
| `HCaptchaClassification` | sync | [Example](#HCaptchaClassification) | hCaptcha image recognition, single or multiple images |

### Cloudflare

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `CloudFlare5STask` | async | [Example](#CloudFlare5STask) | CF five-second interstitial, **requires** a `proxy` |
| `CloudFlareTurnstileTask` | async | [Example](#CloudFlareTurnstileTask) | Turnstile, returns a token |

### Akamai

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `AkamaiWEBTaskProxyless` | sync | [Example](#AkamaiWEBTaskProxyless) | Akamai Web |
| `AkamaiSBSDTaskProxyless` | sync | [Example](#AkamaiSBSDTaskProxyless) | Akamai SBSD |

> Akamai Web is a multi-round flow: feed the `encodedata` of one round back as the `encodeData` of the next. The two spellings genuinely differ on the wire; the SDK keeps the service's definitions as they are rather than "fixing" them.

### DataDome

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `DataDomeTaskProxyless` | sync | [Example](#DataDomeTaskProxyless) | The challenge after an interception, in two steps selected by `step` |
| `DataDomeTagsTaskProxyless` | sync | [Example](#DataDomeTagsTaskProxyless) | Reports a fingerprint on the normal browsing path |

### Other

| Task type | Modes | Example | Description |
| :-: | :---: | :-: | --- |
| `PerimeterX` | async | [Example](#PerimeterX) | PerimeterX clearance cookies |
| `IncapsulaTaskProxyless` | sync | [Example](#IncapsulaTaskProxyless) | Incapsula Reese84 payload |
| `TlsTask` | sync | [Example](#TlsTask) | HTTP request forwarded over TLS, returns the upstream response |

## 📦 Installation

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

### Requirements

**Java 8 or newer.** The artifact is compiled with `--release 8` and CI runs the whole test suite on a real JDK 8 as well, so the floor is tested rather than merely declared.

## 🚀 Quick Start

The client reads `EZCAPTCHA_API_KEY` from the environment when the configuration carries no explicit key.

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

> **Creating a task is billed, and it is billed whether or not the worker succeeds.** The SDK never retries task creation on its own: a timeout cannot tell you whether the service already accepted the task, so a blind retry pays twice. See [Errors](#️-errors) for what to do instead.

## 📖 Usage

### Sync / async

**Every task type has two methods**, taking the same parameter model and returning the same type. Only the endpoint differs:

```java
// Create, then poll for the result
Solved<ReCaptchaSolution> a = client.solveReCaptchaV2TaskProxyless(params);

// The synchronous endpoint, answered in one request
Solved<ReCaptchaSolution> b = client.syncSolveReCaptchaV2TaskProxyless(params);
```

### reCAPTCHA v2

[reCAPTCHA v2 API reference](https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2)

The first five types share `ReCaptchaV2TaskParams` and `ReCaptchaSolution`; only the method name differs.

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

`invisible` is a primitive `boolean`, so it is **always sent**. That is the point: `false` is a real answer meaning "the widget is visible", and omitting the field would let the service apply its own default instead of your choice.

<a id="ReCaptchaV2TaskProxylessS9"></a>

#### ReCaptchaV2TaskProxylessS9

Same parameters as plain v2, on the high-score queue, returning a token scored 0.9 or above.

```java
Solved<ReCaptchaSolution> solved = client.solveReCaptchaV2TaskProxylessS9(params);
System.out.println(solved.getSolution().getToken());
```

<a id="ReCaptchaV2STaskProxyless"></a>

#### ReCaptchaV2STaskProxyless

Carries the challenge-bound `s` parameter. It is not actually mandatory; leaving it out behaves like plain v2.

```java
ReCaptchaV2TaskParams params = ReCaptchaV2TaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("6Lc_your_site_key")
        .s("value-read-from-the-page")
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

Reads an image grid rather than solving the widget. Synchronous.

```java
byte[] image = Files.readAllBytes(Paths.get("grid.jpg"));

ReCaptchaV2ClassificationTaskParams params = ReCaptchaV2ClassificationTaskParams.builder()
        .image(Base64.getEncoder().encodeToString(image))
        .question("/m/014xcs")
        .size(3)
        .build();

Solved<ReClassificationSolution> solved =
        client.syncSolveReCaptchaV2Classification(params);

System.out.println(solved.getSolution().getObjects());   // e.g. [0, 4, 7]
System.out.println(solved.getSolution().isMulti());
```

`size` is a boxed `Integer`, so leaving it unset omits it and the service applies its own default of 4. Set it only when the grid is not 4×4.

### reCAPTCHA v3

[reCAPTCHA v3 API reference](https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3)

All four share `ReCaptchaV3TaskParams` and `ReCaptchaSolution`.

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

[FunCaptcha API reference](https://docs.ezxlabs.com/docs/captcha/api/funcaptcha)

<a id="FuncaptchaTaskProxyless"></a>

#### FuncaptchaTaskProxyless

```java
FunCaptchaTaskParams params = FunCaptchaTaskParams.builder()
        .websiteUrl("https://example.com/signup")
        .websiteKey("YOUR_PUBLIC_KEY")
        .apiJsSubdomain("client-api.arkoselabs.com")
        .data("blob-value-from-the-page")
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

[hCaptcha API reference](https://docs.ezxlabs.com/docs/captcha/api/hcaptcha)

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

**The proxy is required for this type**, unlike most others.

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

A multi-round flow. Each round is a separate billed task.

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

    // encodedata out, encodeData in — the service spells it differently per direction
    encodeData = solved.getSolution().getEncodedata();
}
```

<a id="AkamaiSBSDTaskProxyless"></a>

#### AkamaiSBSDTaskProxyless

```java
AkamaiSbsdTaskParams params = AkamaiSbsdTaskParams.builder()
        .pageUrl("https://example.com")
        .sbsdUrl("https://example.com/.well-known/sbsd")
        .bmSo("existing-bm_so-cookie")
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
        .ddk("YOUR_DDJSKEY")                              // window.ddjskey on the page
        .jsType(DataDomeTagsTaskParams.JS_TYPE_CH)
        .bpc(DataDomeTagsTaskParams.MIN_PACKET_COUNTER)
        .referer("https://example.com/search")
        .ua("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .fields(fields)
        .build();

Solved<DataDomeSolution> solved = client.syncSolveDataDomeTagsTaskProxyless(params);
```

### Other

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
        .script("(function(){...})()")            // the script source, not just its URL
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

Not a captcha at all: a worker performs one HTTP request with its own TLS fingerprint and hands back the upstream response.

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

### Pass-through fields

The service adds parameters faster than any SDK releases. **You never have to wait for one.**

Any parameter a model does not declare goes through `put`, and lands at the same level as the declared fields on the wire:

```java
HCaptchaTaskParams params = HCaptchaTaskParams.builder()
        .websiteUrl("https://example.com")
        .websiteKey("...")
        .build();

params.put("someParameterAddedLater", 42);
```

The same applies in reverse. Anything a worker returns that the solution model does not declare is kept rather than discarded:

```java
Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);

Object extra = solved.getSolution().get("someNewField");
JsonNode raw = solved.getRaw();                // the whole response, untouched
```

### Custom task types

A task type the service added after this release still works — pass the wire name as a string:

```java
Map<String, Object> params = new LinkedHashMap<>();
params.put("websiteURL", "https://example.com");
params.put("websiteKey", "...");

Solved<JsonNode> solved = client.solveRaw("SomeBrandNewTaskType", params);
Solved<JsonNode> sync   = client.syncSolveRaw("SomeBrandNewTaskType", params);
```

`TaskType` holds the known names as constants, but it is a holder of `String`s rather than an enum precisely so that an unknown type is a normal call, not a compile error. `TaskType.isKnown(name)` tells you whether this release models one.

## ⚙️ Configuration

Shortest first:

```java
new EzCapSolverClient();                       // defaults, key from EZCAPTCHA_API_KEY
new EzCapSolverClient("your-key");             // defaults, key given here
new EzCapSolverClient(someClientConfig);       // a configuration you already built
EzCapSolverClient.builder()./* ... */.build(); // configured inline
```

`EzCapSolverClient.of("your-key")` and `EzCapSolverClient.of(someClientConfig)` are the same two constructors under another name. They all end up in the same place; pick whichever reads best at the call site.

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

| Option | Default | Notes |
| --- | --- | --- |
| `clientKey` | `$EZCAPTCHA_API_KEY` | Required, from the environment if not set here |
| `timeout` | 30s | Asynchronous endpoints and the balance query |
| `syncTimeout` | 240s | `/createSyncTask` only |
| `pollInterval` | 3s | Wait between result queries |
| `maxPollAttempts` | 50 | `pollInterval × maxPollAttempts` is the longest `solve*` waits |
| `appId` | — | Optional developer/affiliate identifier |
| `proxy` | — | How **your process** reaches EZCaptchaSolver, not the worker's proxy |
| `userAgent` | `ezcapsolver-java/<version>` | |
| `asyncBaseUrl` / `syncBaseUrl` | the public endpoints | Override for a mock or a private deployment |
| `okHttpClient` | a fresh one | Supply your own to share a connection pool |

**The two timeout budgets are separate on purpose.** `timeout` covers endpoints that answer immediately; `syncTimeout` covers `/createSyncTask`, which blocks until a worker finishes — the service allows some types three minutes. One shared value means either cutting off a synchronous task that was about to succeed (already paid for), or waiting minutes for a balance query that should have failed in seconds.

`ClientConfig.toString()` masks the client key and the proxy URL, a proxy URL carrying credentials of its own. It is safe to log.

### Reusing the client

One client is enough for an entire application. It is immutable after construction and holds a single OkHttp connection pool; building one per request throws that pool away and opens fresh connections every time. It is safe to share across threads.

## ⚠️ Errors

Every failure is an unchecked exception under `EzCaptchaException`, so catching that one type is a complete safety net.

| Exception | Meaning |
| --- | --- |
| `EzCaptchaException` | The base class. Configuration errors are thrown as this directly |
| `TransportException` | Never reached the service, or no answer came back |
| `ApiException` | The service answered, and said no |
| `PollingExhaustedException` | The polling budget ran out. **The task is still running** |
| `UnexpectedResponseException` | A well-formed response that broke the contract |
| `SolutionDecodeException` | The task succeeded; the solution would not decode |
| `WaitInterruptedException` | The thread was interrupted mid-wait |

On `ApiException`, three predicates cover what you actually need to decide:

```java
try {
    Solved<HCaptchaSolution> solved = client.solveHCaptcha(params);
} catch (ApiException e) {
    if (e.isAuthenticationError()) {
        // Stop. These codes trip a server-side ban counter; retrying digs the hole deeper.
    } else if (e.isRateLimited()) {
        // Throttled before anything was created. Safe to retry after a wait.
    } else if (e.isTerminal()) {
        // Retrying with the same input fails the same way. Change the request.
    }
    log.error("{} ({}) request={}", e.getErrorCode(), e.getHttpStatus(), e.getRequestId());
}
```

### Recovering a task you have already paid for

This is what the hierarchy is really for. When a wait fails, the task usually still exists — and the service holds its result for **five minutes**.

```java
try {
    solved = client.solveHCaptcha(params);
} catch (EzCaptchaException e) {
    String taskId = EzCaptchaException.taskIdOf(e);   // works on any of them
    if (taskId != null) {
        solved = client.waitForResult(taskId, HCaptchaSolution.class);
    }
}
```

`taskIdOf` is a single entry point precisely so this works without knowing which layer failed. Creating a second task instead means paying twice for the same work.

## 📝 Logging

The SDK logs through slf4j and ships **only the API**. It stays silent until you put a binding on the classpath — whichever your application already uses:

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.3.15</version>
</dependency>
```

> Logback 1.3.x is the Java 8 line; 1.4+ requires Java 11.

Loggers are named after their classes, so `com.burstlinker.ezcapsolver` scopes the whole SDK. Nothing it logs contains your API key.

## 🧪 Runnable Examples

One file per task type, in [`examples`](./examples) — indexed there, with the code under `com.burstlinker.ezcapsolver.examples`.

```bash
export EZCAPTCHA_API_KEY=your-key

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

java -cp "target/classes:$(cat target/cp.txt)" \
     com.burstlinker.ezcapsolver.examples.hcaptcha.HCaptcha
```

The examples live in the source tree so the compiler checks them on every build, and are excluded from the published jar, sources jar and javadoc.

## 🛠️ Development

```bash
mvn clean test                           # the suite
JAVA_HOME=/path/to/jdk8 mvn clean test   # again on Java 8
mvn verify                               # adds animal-sniffer and japicmp
mvn javadoc:javadoc
typos
```

No test is allowed to reach the real service — creating a task is billed. Every HTTP test goes through MockWebServer.

See [`CONTRIBUTING.md`](./CONTRIBUTING.md) for the five places a new task type touches, and for the two model traps that are easy to hit.

## 📄 License

[Apache-2.0](./LICENSE)
