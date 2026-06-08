# Support

## Getting Help

### 📖 Documentation
- [README.md](README.md) — Project overview, setup, and architecture
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development guidelines
- [CHANGELOG.md](CHANGELOG.md) — Version history
- [SECURITY.md](SECURITY.md) — Vulnerability reporting

### 🐛 Bug Reports & Feature Requests
- [GitHub Issues](https://github.com/AlejandroEscapa/MatchVisionTFM/issues)
  - Search existing issues before opening a new one
  - Use templates (Bug Report / Feature Request)
  - Include:
    - Device model & Android version
    - Steps to reproduce
    - Screenshots/recordings if applicable
    - Logcat output for crashes

### 💬 Questions & Discussion
- [GitHub Discussions](https://github.com/AlejandroEscapa/MatchVisionTFM/discussions) — Q&A, ideas, polls

### 🔒 Security Vulnerabilities
See [SECURITY.md](SECURITY.md) for private reporting guidelines.

---

## Common Issues

### Build Error: `google-services.json not found`
```bash
# Download from Firebase Console → Project Settings → Your apps
# Place in: app/google-services.json
```

### API Rate Limiting (RAWG)
The RAWG free tier has rate limits. To avoid 429 errors:
- Implement caching (Room local storage)
- Use debounced search input (already in codebase)
- Consider upgrading to RAWG paid tier for production

### Firebase Emulator for Testing
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Start emulators
firebase emulators:start --only auth,firestore
```

### Gradle Sync Issues
```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# In Android Studio: File → Invalidate Caches → Restart
```

---

## Contact

- **Author**: Alejandro Olivares Escapa
- **Email**: alejandro.oliesc97@gmail.com
- **GitHub**: [@AlejandroEscapa](https://github.com/AlejandroEscapa)

---

## Status

| Service | Status |
|---------|--------|
| Repository | ✅ Active |
| Documentation | ✅ Available |
| Issue Tracker | ✅ Open |
| CI/CD | ⚠️ Pending setup |

*Last updated: 2025*
