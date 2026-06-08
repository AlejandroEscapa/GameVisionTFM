# Contributing to GameVision

¡Gracias por tu interés en contribuir! 🎮

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Workflow](#development-workflow)
- [Commit Conventions](#commit-conventions)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)

---

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

---

## How Can I Contribute?

### 🐛 Reporting Bugs
1. Search [existing issues](https://github.com/AlejandroEscapa/MatchVisionTFM/issues) to avoid duplicates
2. Use the **Bug Report** template
3. Include:
   - Device model & Android version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots/logs if available

### 💡 Suggesting Features
1. Search [existing feature requests](https://github.com/AlejandroEscapa/MatchVisionTFM/issues)
2. Use the **Feature Request** template
3. Explain the problem and proposed solution
4. Discuss before implementing large features

### 🔧 Code Contributions
1. Pick an open issue or discuss a new idea
2. Fork & create a feature branch
3. Implement, test, and submit a PR

---

## Development Workflow

```bash
# 1. Fork and clone
git clone https://github.com/YOUR_USERNAME/MatchVisionTFM.git
cd MatchVisionTFM

# 2. Add upstream
git remote add upstream https://github.com/AlejandroEscapa/MatchVisionTFM.git

# 3. Create feature branch
git checkout -b feature/your-feature-name

# 4. Make changes, commit, push
git add .
git commit -m "feat: add amazing feature"
git push origin feature/your-feature-name

# 5. Keep branch updated with main
git fetch upstream
git rebase upstream/main

# 6. Open Pull Request via GitHub
```

---

## Commit Conventions

We follow [Conventional Commits 1.0.0](https://www.conventionalcommits.org/):

```text
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types
| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Formatting, linting (no code change) |
| `refactor` | Code restructuring (no behavior change) |
| `perf` | Performance improvement |
| `test` | Adding/updating tests |
| `chore` | Build, CI, dependencies |
| `ci` | CI/CD configuration |

### Examples
```bash
feat(auth): add Google Sign-In via Credential Manager
fix(search): debounce input to prevent API rate limiting
docs(readme): add architecture diagram and badges
refactor(viewmodel): extract common logic to BaseViewModel
test(library): add instrumentation tests for favorites flow
```

---

## Pull Request Process

1. **Branch naming**: `feature/`, `fix/`, `docs/`, `refactor/`
2. **Keep PRs small**: One feature/fix per PR (max ~500 lines changed)
3. **Description template**:
   ```markdown
   ## Summary
   Brief description of changes

   ## Related Issue
   Closes #123

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] UI tests added/updated
   - [ ] Manual testing performed

   ## Screenshots (if UI change)
   | Before | After |
   |--------|-------|
   | img | img |
   ```
4. **CI checks must pass**: build, lint, tests
5. **One review required** before merge
6. **Squash & merge** to `main` (clean history)

---

## Coding Standards

### Kotlin
- Follow [Google Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Max line length: 120 characters
- Use `val` over `var` whenever possible
- Use extension functions for utility logic
- Prefer sealed classes for state management

### Compose
- Follow [Compose API Guidelines](https://developer.android.com/jetpack/compose/api-guidelines)
- State hoisting: lift state to the lowest common ancestor
- Use `remember` + `key` for expensive calculations
- Follow slot API patterns for reusable components

### Architecture
- ViewModels handle UI logic, Repositories handle data
- One ViewModel per screen
- Use `StateFlow` for UI state, `SharedFlow` for events
- No business logic in Composables

### Formatting
```bash
# Auto-format before committing
./gradlew ktlintFormat
```

---

## Testing Requirements

- **New features**: require unit tests
- **Bug fixes**: require regression tests
- **UI changes**: require Compose UI tests
- **Critical paths**: > 80% coverage
- **General**: > 60% coverage

```bash
# Run tests locally before PR
./gradlew test
./gradlew connectedAndroidTest  # if device/emulator available
```

---

## Questions?

Open a [Discussion](https://github.com/AlejandroEscapa/MatchVisionTFM/discussions) or contact the maintainer.

**Happy coding! 🎮**
