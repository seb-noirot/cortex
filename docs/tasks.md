# Cortex IntelliJ Plugin – Task List

Generated: 2025-08-18 08:18 local

This checklist is the source of truth for iterative improvements. Implement tasks in order (MVP → Beta → 1.0), making small, focused changes. After completing a task, change its checkbox from [ ] to [x] and reference the task ID in the commit message. Follow .junie/guidelines.md and keep docs/plan.md alignment.

References:
- Plan: docs/plan.md
- Requirements: docs/requirements.md
- Guidelines: .junie/guidelines.md

Conventions:
- Commit messages: Conventional Commits with task ID, e.g., `feat(settings): add PasswordSafe-backed token [MVP-2]`.
- Definition of Done: see .junie/guidelines.md (tests, docs, security, resilience, no EDT blocking).

## MVP (Weeks 1–2)

- [x] [MVP-1] Detect `cortex.yaml` and show onboarding tip
  - Acceptance criteria:
    - Project service detects presence of `cortex.yaml` (configurable globs later) and shows a one-time notification guiding user to configure Cortex endpoint/token.
    - Notification does not block UI and never repeats unnecessarily (persist flag).
    - Threading: detection off EDT for IO; UI updates on EDT.

- [x] [MVP-2] Settings UI with secure credentials and health check
  - Acceptance criteria:
    - Configurable: Base URL (validated), optional org slug, access token stored/retrieved via PasswordSafe.
    - "Test connection" performs health/version check (off EDT) and displays status (on EDT) without printing token.
    - No secrets logged; errors redact token with "***".

- [x] [MVP-3] Bundle default JSON Schema and map to `cortex.yaml`
  - Acceptance criteria:
    - Provide a bundled starter JSON Schema resource.
    - Implement `yaml.schemaProviderFactory` binding to files named `cortex.yaml`.
    - Validation runs and highlights schema violations.

- [ ] [MVP-4] Minimal inspections (local) (skipped per request)
  - Acceptance criteria:
    - Implement inspections for: required keys, enum validation, URL format.
    - Tests cover positive/negative cases using LightPlatform fixtures where applicable.
    - Inspections fail softly (no crashes) and are performant.

- [ ] [MVP-5] Juni action: Generate Improvement Plan (skipped per request)
  - Acceptance criteria:
    - IDE action "Generate Improvement Plan (Juni)" reads docs/requirements.md, runs configured Juni command, writes docs/plan.md, and opens it.
    - If docs/requirements.md is missing, offers to create a stub.
    - Configurable Juni binary/path and prompt preset; all IO off EDT.

## Beta (Weeks 3–4)

- [ ] [BETA-1] Completion contributor with background index (skipped per request)
  - Acceptance criteria:
    - YAML completions for keys/enums and known entity IDs (services, teams, scorecards).
    - Background fetch to index entities; cached with TTL; off EDT with throttling/debounce.

- [x] [BETA-2] Annotator + clickable references + hovers
  - Acceptance criteria:
    - Clickable IDs resolve to Cortex UI URLs; gutter or inline links.
    - Hover info fetched via API and cached; offline degrades gracefully.

- [x] [BETA-2a] Derive/store API URL and validate connectivity
  - Acceptance criteria:
    - When a Cortex domain is saved (e.g., https://cortex.example.com), derive and persist API base (https://api.cortex.example.com).
    - Test connection uses API endpoint GET /api/v1/catalog/definitions and reports success on HTTP 200.
    - All network calls off EDT; token never logged; clear error messages.

- [ ] [BETA-3] Quick fixes for common issues
  - Acceptance criteria:
    - Quick fixes to insert required blocks, normalize key casing, and add missing tags.
    - Tests verifying application and resulting YAML.

- [ ] [BETA-4] Tool Window for entity search and navigation
  - Acceptance criteria:
    - Tool window lists/searches entities; open in browser; quick copy IDs.
    - Uses cached/indexed data; off EDT for network/IO.

## 1.0 (Weeks 5–6)

- [ ] [GA-1] Org schema override + per-project settings
  - Acceptance criteria:
    - Schema source selectable: Bundled / URL / Local Path.
    - Validation uses selected schema; URL fetched with ETag/backoff.

- [ ] [GA-2] Persistent index with scheduled refresh
  - Acceptance criteria:
    - Index persisted via PersistentStateComponent; TTL enforced; scheduled refresh with jitter.
    - Offline/401 scenarios handled with clear notifications; no token leakage.

- [ ] [GA-3] Packaging & release preparation
  - Acceptance criteria:
    - CI builds/tests; plugin signed; publish to Marketplace private channel.
    - CHANGELOG updated; compatibility targets IntelliJ 2024.3.

## Cross-Cutting Infrastructure

- [ ] [CORE-HTTP] HTTP client wrapper with resilience
  - Acceptance criteria:
    - java.net.http or OkHttp with timeouts, retries with exponential backoff + jitter, User-Agent, optional ETag/If-None-Match.
    - All network work off EDT; errors actionable and redacted.

- [ ] [CORE-SEC] Security hardening
  - Acceptance criteria:
    - Tokens only via PasswordSafe; URL validation/normalization; never echo tokens in UI/logs.
    - Prompt for re-authentication on 401; do not auto-log sensitive context.

- [ ] [CORE-CACHE] Caching and indexing scaffolding
  - Acceptance criteria:
    - In-memory cache + persisted store with TTL; throttled refresh.
    - Clear APIs usable by inspections/completions/tool window.

- [ ] [CORE-TEST] Testing matrix and scenarios
  - Acceptance criteria:
    - Unit tests for schema mapping, inspections, quick fixes.
    - LightPlatform tests for editor interactions and actions.
    - Negative tests: offline, invalid token, slow API; large repo scenario where feasible.

- [ ] [CORE-TEMPLATES] New File template for `cortex.yaml`
  - Acceptance criteria:
    - File template and new-file action to create a skeleton `cortex.yaml` with live templates.

- [ ] [DOCS] Documentation alignment
  - Acceptance criteria:
    - README updated with setup, settings, and usage; docs/plan.md kept in sync when scope changes; CHANGELOG entries for user-facing changes.

---
How to use this list:
1. Pick the next unchecked task in priority order.
2. Implement with minimal, focused changes following .junie/guidelines.md.
3. Add/update tests and docs.
4. Mark the item as [x] and reference its ID in the commit message.
5. Proceed to the next task.
