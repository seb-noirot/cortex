# Juni Coding & Contribution Guidelines

These guidelines define how Juni should implement tasks in this repository. All contributions must follow these rules. When completing a task from docs/tasks.md, ensure you also update the checklist and relevant docs.

## Principles
- Security first: never persist or log secrets (tokens). Redact sensitive values in logs and messages.
- Responsiveness: never block the EDT (Event Dispatch Thread). Run IO and heavy work in background tasks.
- Minimal, incremental changes: make small, focused PRs tied to a single checklist item.
- Tests and reproducibility: add/keep tests; ensure changes are verifiable and deterministic.
- Clarity: prefer readable, idiomatic Kotlin and clear UX in the IDE.

## Code Style (Kotlin, IntelliJ Platform)
- Kotlin style: idiomatic Kotlin, JetBrains conventions. Prefer val, immutability, and data classes where appropriate.
- Null-safety: explicit nullability, avoid platform types where possible.
- Visibility: keep types/members as private/internal unless externally needed. Final by default.
- Structure packages by concern (per docs/plan.md):
  - core: settings, credentials (PasswordSafe), HTTP client, caching/index store
  - yaml: schema provider, inspections, annotations, references, completions, quick-fixes
  - ui: settings Configurable, tool window, actions/notifications
  - juni: plan generation action, configuration, process runner
- Logging: use com.intellij.openapi.diagnostic.Logger. Do not log tokens or raw responses that may contain secrets. Provide actionable messages.
- Threading: all network calls and filesystem IO off EDT. Use BackgroundTaskUtil/ProgressManager or AppExecutorUtil. UI updates must go through the EDT.
- Error handling: fail softly with clear notifications. Provide retry/backoff for network. Do not crash inspections.
- HTTP client: OkHttp or java.net.http with timeouts, retry/backoff, and optional ETag. Always set User-Agent and honor rate limits.
- Credentials: store tokens only in com.intellij.credentialStore.PasswordSafe. Never persist or print raw tokens.
- Caching/Indexing: in-memory cache + PersistentStateComponent for persisted data with TTL. Throttle refresh and debounce rapid changes.
- YAML/PSI: use standard extension points (yaml.schemaProviderFactory, localInspection, completion.contributor, annotator) and PSI references for navigation.
- Compatibility: target IntelliJ Platform 2024.3 LTS. Guard optional APIs with reflection or version checks when necessary.

## Testing
- Write unit tests for business logic (schema mapping, inspections, quick fixes).
- For editor interactions, use LightPlatform tests (e.g., LightPlatformCodeInsightFixtureTestCase or LightJavaCodeInsightFixtureTestCase).
- Include negative tests: offline, invalid token (401), slow API (timeouts), and large repo scenarios where feasible.
- Keep tests fast and deterministic; mock network and clock when possible.

## Commits & PRs
- Use Conventional Commits: feat:, fix:, perf:, refactor:, docs:, test:, chore:.
- Keep changes scoped to a single task from docs/tasks.md and reference it in the commit message (e.g., "feat(settings): add PasswordSafe-backed token [MVP-2]").
- Update CHANGELOG.md when user-facing behavior changes or when reaching milestones.

## Documentation
- Always update docs/tasks.md by changing the checkbox from [ ] to [x] upon completion of a task.
- Keep docs/plan.md alignment: if scope changes, update plan.md accordingly and note rationale.
- Add usage notes to README.md when introducing new user-visible features or settings.

## Security & Privacy
- Never echo tokens or secrets in UI, logs, or exceptions. Redact with "***".
- Validate and normalize URLs; refuse to show plaintext tokens back to the user.
- Handle 401s by prompting for re-authentication; do not auto-log sensitive context.

## Performance & Resilience
- All network and blocking work off EDT.
- Cache server data and respect TTL to avoid redundant calls; implement exponential backoff and jitter.
- Degrade gracefully when offline: keep local inspections functional and show actionable notifications.

## Definition of Done (for each task)
- Code follows these guidelines and compiles against 2024.3 target.
- Relevant tests updated/added and pass locally.
- No secrets in logs; basic error handling in place.
- Documentation updated (README, docs/tasks.md checkbox toggled, CHANGELOG entry when applicable).
- Manual smoke test if feature is user-facing (document steps briefly in the PR description).
