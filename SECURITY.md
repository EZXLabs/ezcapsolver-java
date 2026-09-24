# Security Policy

## Supported Versions

Security updates are provided only for the latest released version.

| Version | Supported |
|---------|-----------|
| Latest release | Yes |
| Historical versions | No |

## Reporting A Vulnerability

If you discover a security vulnerability, please do **not** report it through a public issue, because doing so may expose the vulnerability before a fix is available.

Please report it privately by email: **zadmin@ezxlabs.com**

When reporting, include as much of the following information as possible:

- Vulnerability type and impact scope
- Reproduction steps or proof of concept
- Affected versions

We will confirm the report as soon as possible and disclose it publicly after a fix has been released.

## A Note On Credentials

Your EzCaptchaSolver API key is a credential. The SDK never logs it: `ClientConfig.toString()` masks both the key and the proxy URL, since a proxy URL carries its own username and password.

If you believe a key has leaked, rotate it at [ezxlabs.com](https://ezxlabs.com) — that is a service matter rather than an SDK one, and this repository cannot revoke anything.
