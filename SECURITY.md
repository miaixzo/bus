# Security Policy

## Supported Versions

Security fixes are provided only for the latest stable release of the `8.x` series. Earlier releases are not actively maintained.

| Release                       | Status        |
|-------------------------------|---------------|
| Latest stable `8.x` release   | Supported     |
| Earlier releases              | Not supported |

Users should upgrade to the latest available release before reporting a vulnerability.

## Reporting a Vulnerability

Please do not report security vulnerabilities through public GitHub issues, discussions, or pull requests.

Report suspected vulnerabilities privately by email to `service@miaixz.org` with the subject:

```text
[Security] Brief description of the vulnerability
```

Please include the following information when available:

- Affected module and version
- Vulnerability description and potential impact
- Steps required to reproduce the issue
- Proof-of-concept code or relevant logs
- Affected environment or configuration
- Suggested remediation
- Preferred contact information and disclosure credit

Do not include sensitive credentials, personal data, or production secrets in the report.

## Response Process

We aim to acknowledge reports within three business days. After initial review, we will confirm whether the issue is accepted, request additional information if necessary, and provide status updates as remediation progresses.

Resolution time depends on the severity and complexity of the issue.

## Coordinated Disclosure

Please allow reasonable time for investigation and remediation before publicly disclosing a vulnerability.

Once a fix is available, we may publish a security advisory describing the affected versions, impact, remediation, and reporter acknowledgement. Reporter credit will be provided when requested.

## Third-Party Dependencies

Reports concerning third-party dependencies should normally be submitted to the corresponding upstream project. However, reports are welcome when the way this project integrates or configures a dependency creates a security impact specific to this project.
