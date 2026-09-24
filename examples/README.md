# Examples

[English](./README.md) · [简体中文](./README.zh-CN.md)

One runnable file per task type, named after the wire task type so it lines up with the Rust, Go, Python and JavaScript SDKs file for file.

## Where They Live

The example **code** sits in the normal source tree, under `com.burstlinker.ezcapsolver.examples`:

```
src/main/java/com/burstlinker/ezcapsolver/examples/
├── ClientSetup.java · Concurrency.java · RawUsage.java · LoggingAndErrors.java
├── akamai/ · cloudflare/ · datadome/ · funcaptcha/ · hcaptcha/
├── incapsula/ · perimeterx/ · recaptchav2/ · recaptchav3/ · tlsforward/
examples/
├── README.md · README.zh-CN.md   ← you are here
└── fixtures/                     ← images the classification examples read
```

Keeping the code in the source tree means **the compiler checks it on every build** — an example that stops matching the API breaks CI instead of quietly rotting.

It also means the build has to keep them out of the release, which three plugins are configured to do: they are excluded from the jar, the sources jar and the javadoc. `animal-sniffer` deliberately still checks them, so the examples are proven to work on Java 8 too.

> Because the examples are excluded from the jar, run them from `target/classes` rather than from the artifact. The commands below already do.

> **Every run creates a real task and is billed, whether or not the worker succeeds.** The only exception is [`ClientSetup.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/ClientSetup.java), which calls `getBalance` — the one free endpoint.
>
> The reCAPTCHA v2 and hCaptcha examples point at the vendors' own demo pages and work as written. The rest carry placeholder site keys for you to fill in.

## Running One

```bash
export EZCAPTCHA_API_KEY=your-key

# Once: compile everything and write out the dependency classpath.
mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

# Then any example, by its fully qualified name.
java -cp "target/classes:$(cat target/cp.txt)" \
     com.burstlinker.ezcapsolver.examples.hcaptcha.HCaptcha
```

Run them **from the repository root** — the classification examples read their images from `examples/fixtures/`.

No example hardcodes a key. The ones that need a proxy read `EZCAPTCHA_PROXY`; a few read other values from the environment rather than inventing plausible-looking fakes.

## The SDK Itself

| Example | What it covers |
| --- | --- |
| [`ClientSetup.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/ClientSetup.java) | Every option, and why the two timeout budgets are separate |
| [`Concurrency.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/Concurrency.java) | Sharing one client across threads, with a concurrency cap |
| [`RawUsage.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/RawUsage.java) | Polling by hand, and reaching a task type the SDK does not model |
| [`LoggingAndErrors.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/LoggingAndErrors.java) | Injecting a logger, and telling the failure layers apart |

## reCAPTCHA v2

[Docs](https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v2)

| Example | Task type |
| --- | --- |
| [`recaptchav2/ReCaptchaV2TaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2TaskProxyless.java) | `ReCaptchaV2TaskProxyless` |
| [`recaptchav2/ReCaptchaV2TaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2TaskProxylessS9.java) | `ReCaptchaV2TaskProxylessS9` |
| [`recaptchav2/ReCaptchaV2STaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2STaskProxyless.java) | `ReCaptchaV2STaskProxyless` |
| [`recaptchav2/ReCaptchaV2EnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2EnterpriseTaskProxyless.java) | `ReCaptchaV2EnterpriseTaskProxyless` |
| [`recaptchav2/ReCaptchaV2SEnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2SEnterpriseTaskProxyless.java) | `ReCaptchaV2SEnterpriseTaskProxyless` |
| [`recaptchav2/ReCaptchaV2Classification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav2/ReCaptchaV2Classification.java) | `ReCaptchaV2Classification` |

## reCAPTCHA v3

[Docs](https://docs.ezxlabs.com/docs/captcha/api/recaptcha-v3)

| Example | Task type |
| --- | --- |
| [`recaptchav3/ReCaptchaV3TaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3TaskProxyless.java) | `ReCaptchaV3TaskProxyless` |
| [`recaptchav3/ReCaptchaV3TaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3TaskProxylessS9.java) | `ReCaptchaV3TaskProxylessS9` |
| [`recaptchav3/ReCaptchaV3EnterpriseTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3EnterpriseTaskProxyless.java) | `ReCaptchaV3EnterpriseTaskProxyless` |
| [`recaptchav3/ReCaptchaV3EnterpriseTaskProxylessS9.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/recaptchav3/ReCaptchaV3EnterpriseTaskProxylessS9.java) | `ReCaptchaV3EnterpriseTaskProxylessS9` |

## FunCaptcha / Arkose Labs

[Docs](https://docs.ezxlabs.com/docs/captcha/api/funcaptcha)

| Example | Task type |
| --- | --- |
| [`funcaptcha/FuncaptchaTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/funcaptcha/FuncaptchaTaskProxyless.java) | `FuncaptchaTaskProxyless` |
| [`funcaptcha/FunCaptchaClassification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/funcaptcha/FunCaptchaClassification.java) | `FunCaptchaClassification` |

## hCaptcha

[Docs](https://docs.ezxlabs.com/docs/captcha/api/hcaptcha)

| Example | Task type |
| --- | --- |
| [`hcaptcha/HCaptcha.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/hcaptcha/HCaptcha.java) | `HCaptcha` |
| [`hcaptcha/HCaptchaClassification.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/hcaptcha/HCaptchaClassification.java) | `HCaptchaClassification` |

## Cloudflare

| Example | Task type |
| --- | --- |
| [`cloudflare/CloudFlare5STask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/cloudflare/CloudFlare5STask.java) | `CloudFlare5STask` |
| [`cloudflare/CloudFlareTurnstileTask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/cloudflare/CloudFlareTurnstileTask.java) | `CloudFlareTurnstileTask` |

## Akamai

| Example | Task type |
| --- | --- |
| [`akamai/AkamaiWEBTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/akamai/AkamaiWEBTaskProxyless.java) | `AkamaiWEBTaskProxyless` |
| [`akamai/AkamaiSBSDTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/akamai/AkamaiSBSDTaskProxyless.java) | `AkamaiSBSDTaskProxyless` |

## DataDome

| Example | Task type |
| --- | --- |
| [`datadome/DataDomeTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/datadome/DataDomeTaskProxyless.java) | `DataDomeTaskProxyless` |
| [`datadome/DataDomeTagsTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/datadome/DataDomeTagsTaskProxyless.java) | `DataDomeTagsTaskProxyless` |

## Other Protection Systems

| Example | Task type |
| --- | --- |
| [`perimeterx/PerimeterX.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/perimeterx/PerimeterX.java) | `PerimeterX` |
| [`incapsula/IncapsulaTaskProxyless.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/incapsula/IncapsulaTaskProxyless.java) | `IncapsulaTaskProxyless` |
| [`tlsforward/TlsTask.java`](../src/main/java/com/burstlinker/ezcapsolver/examples/tlsforward/TlsTask.java) | `TlsTask` |

## Fixtures

`fixtures/` holds the images the classification examples read. They are the same three files the other language SDKs use, so the sets are comparable across languages.

## Adding One

A new task type needs an example here and a row in the table above, in **both** this file and `README.zh-CN.md`. Nothing tests that; it stays correct by review. See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for the other four places a task type touches.
