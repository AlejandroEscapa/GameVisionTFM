# Security Policy

## Supported Versions

Only the latest released version receives security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | ✅ Active support  |

---

## Reporting a Vulnerability

**DO NOT report security vulnerabilities through public GitHub issues.**

### Private Disclosure

Please report vulnerabilities privately via:

1. **GitHub Security Advisories**: Go to [Security → Advisories → Report a vulnerability](https://github.com/AlejandroEscapa/GameVisionTFM/security/advisories/new)
2. **Email**: alejandro.oliesc97@gmail.com *(encrypted with PGP if sensitive)*

### What to Include

- **Description** of the vulnerability
- **Steps to reproduce** (proof-of-concept if possible)
- **Potential impact** (data exposure, privilege escalation, etc.)
- **Android version** and device model affected
- **Code location** (file, function, line)

### Response Timeline

| Phase | Expected Time |
|-------|---------------|
| Acknowledgment | Within 48 hours |
| Initial assessment | Within 5 business days |
| Patch development | Depends on severity |
| Coordinated disclosure | After patch is released |

### Severity Classification

| Level | Criteria | Response |
|-------|----------|----------|
| **Critical** | Auth bypass, data breach, remote code execution | Immediate |
| **High** | Privilege escalation, sensitive data exposure | 48h |
| **Medium** | Input validation bypass, information disclosure | 1 release cycle |
| **Low** | Minor security hardening | Next feature release |

---

## Security Best Practices for Contributors

### API Keys & Secrets
- **NEVER** commit API keys, tokens, or credentials
- Store secrets in `local.properties` (gitignored)
- If you accidentally commit a secret:
  1. Rotate the key IMMEDIATELY on the provider dashboard
  2. `git filter-branch` or `BFG Repo-Cleaner` to scrub history
  3. Contact maintainer

### Code
- Validate all user input (especially in search, auth flows)
- Use parameterized queries with Room (prevents SQL injection)
- Sanitize data before displaying (XSS prevention in WebViews)
- Follow [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)

### Dependencies
- Keep dependencies updated (Dependabot alerts)
- Review new dependencies before adding
- Run vulnerability scan periodically:
  ```bash
  ./gradlew dependencyCheckAnalyze
  ```

### Data Privacy
- This app uses Firebase (Google infrastructure)
- User data stored in Firestore must comply with:
  - **GDPR** (EU users — right to access/delete data)
  - **Spanish LOPDGDD**
- Implement data export/deletion features
- Minimize data collection (only what's necessary)

---

## Disclosure Policy

We follow **coordinated vulnerability disclosure**:

1. Reporter privately discloses vulnerability
2. Maintainer acknowledges and triages
3. Fix is developed and tested
4. Patch is released
5. Public disclosure with credit to reporter (optional)

**We do not take legal action against researchers who act in good faith.**

---

## Hall of Fame

We appreciate and recognize security researchers. With permission, reporters will be listed here.

*No reports yet — be the first!*
