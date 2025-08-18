# Cortex IntelliJ Plugin – Improvement Plan (derived from docs/requirements.md)

## 0. Summary

Build and ship an IntelliJ Platform plugin that provides first‑class editing for cortex.yaml, seamless navigation to Cortex (on‑prem), secure configuration of API access, and a Juni‑powered planning action that generates docs/plan.md from docs/requirements.md. Deliver iteratively (MVP → Beta → 1.0), with strong attention to security, performance, and graceful degradation offline.

## 1. Product Goals (extracted)

- Rich editing for cortex.yaml: schema validation, completions, navigation, quick fixes. Rationale: Developers need correctness and guidance inline to maintain service metadata efficiently.
- IDE shortcuts into Cortex: open services/scorecards/runbooks from code. Rationale: Reduce context switching; faster validation and discovery.
- Secure, configurable on‑prem integration: base URL, token in PasswordSafe, health/version. Rationale: Works with enterprise deployments; protects secrets.
- Juni planning action: read requirements, generate plan.md. Rationale: Keeps planning artifacts in‑repo and reproducible from requirements.
- Broad IDE compatibility (primary IntelliJ; nice‑to‑have WebStorm/PyCharm). Rationale: Support common JVM polyglot repos; future expansion path.

## 2. Non‑Functional Constraints (extracted)

- Security: tokens only in PasswordSafe; redact logs. Rationale: Prevent secret leakage.
- Performance: inspections fast; API calls off EDT; caching and throttled indexing. Rationale: Maintain editor responsiveness in large repos.
- Resilience: degrade gracefully offline with clear errors. Rationale: Developer productivity shouldn’t depend on network.
- Compatibility: target IntelliJ Platform 2024.3 LTS. Rationale: Stable baseline for orgs.
- Configurability: allow org schema override (URL/file). Rationale: Adapt to schema drift across orgs.

## 3. Architecture & Modules

- Single Gradle module to start (align with intellij-platform-plugin-template). Rationale: Simpler build and distribution early.
- Internal package boundaries (can become modules later):
  - core: settings, credential access, HTTP client, cache/index store. Rationale: Reusable services and headless tests.
  - yaml: schema provider, inspections, completions, annotators, references, quick-fixes. Rationale: Language features isolated.
  - ui: settings Configurable, tool window, actions, notifications. Rationale: Clear UX separation.
  - juni: action/runner to generate docs/plan.md. Rationale: Encapsulate external integration and IO.

## 4. cortex.yaml Language Support

- JSON Schema mapping to cortex.yaml (bundled default + org override). Rationale: Enforce structure consistently while supporting org variance.
- Inspections: missing required keys, invalid enums, bad references (scorecard/team/service IDs), URL format, owners syntax. Rationale: Catch common issues early.
- Annotator/References: clickable entity IDs; hover info from API (cached). Rationale: Inline discovery and navigation.
- Completions: keys/enums and known entity IDs fetched in background. Rationale: Reduce memorization; prevent typos.
- Quick Fixes: insert required blocks; normalize key casing; add missing tags. Rationale: Improve fix velocity.
- Live Templates: skeleton cortex.yaml creation. Rationale: Faster onboarding for new files.

Implementation notes:
- yaml.schemaProviderFactory to bind schema to cortex.yaml and globs.
- completion.contributor for YAML paths (e.g., $.service.id).
- localInspection + annotator + PSI references for navigation.

## 5. Cortex Integration (on‑prem)

- Settings UI: base URL, optional org slug, token stored in PasswordSafe; health check + version display. Rationale: Secure and transparent connectivity.
- Background sync/index: fetch services/teams/scorecards; persist via PersistentStateComponent with TTL. Rationale: Power completions without blocking editor.
- HTTP client with retry/backoff; all calls off EDT; optional ETag/If‑None‑Match. Rationale: Network stability and performance.

## 6. Navigation & UX

- Gutter icons near IDs/service blocks to open in Cortex. Rationale: One‑click navigation.
- Tool Window: search entities, view metadata snippet, open links, copy IDs. Rationale: Discoverability and context.
- Context menu actions on cortex.yaml: Validate now, Open in Cortex, Insert template. Rationale: Make common tasks obvious.

## 7. Juni Planning Action

- Action: Generate Improvement Plan (Juni). Reads docs/requirements.md, runs configured Juni command, writes docs/plan.md, then opens it. Rationale: Codifies planning loop in IDE.
- If requirements.md missing, offer to create a stub. Rationale: Smooth first‑run experience.
- Configurable Juni binary/path and prompt preset. Rationale: Works across environments.

## 8. Project Detection & Onboarding

- Detect cortex.yaml; show one‑time tip to configure endpoint/token. Rationale: Drive initial setup and awareness.
- New File templates: Cortex → cortex.yaml skeleton. Rationale: Standardize starting point.

## 9. Security & Privacy

- Use com.intellij.credentialStore.PasswordSafe for tokens; never persist tokens in state or logs; redact from errors. Rationale: Meet enterprise security expectations.
- Validate URLs and refuse plaintext token echo in UI. Rationale: Prevent accidental leakage.

## 10. Performance & Resilience

- All network work off EDT; use BackgroundTaskUtil/ProgressManager.
- Cache entity lists in memory + persisted index with TTL; throttle refresh and debounce rapid changes.
- Fail soft: when offline/401, keep inspections that don’t require remote; show actionable notification.
Rationale: Maintain responsiveness and reliability in monorepos and unstable networks.

## 11. Configurability

- Schema source: Bundled/URL/Path; per‑project settings. Rationale: Adapt to local policies.
- Optional org slug and custom globs for cortex.yaml detection. Rationale: Support diverse layouts.

## 12. API & Data

- Illustrative endpoints: GET /api/services, /api/scorecards, /api/teams, /api/version, /api/services/{id}.
- Auth: Bearer token; support rate limiting/backoff and ETag.
Rationale: Minimize load and stay within org limits while keeping data fresh.

## 13. IDE Integration Details

- Extension points: yaml.schemaProviderFactory, localInspection, completion.contributor, annotator, applicationService/projectService, toolWindow, action, fileType icon if desired.
Rationale: Use standard platform mechanisms for maintainability.

## 14. Testing & QA

- Unit tests: schema mapping, inspections, quick fixes.
- LightPlatform UI tests for editor interactions and actions.
- Scenarios: offline, invalid token, slow API, large monorepo (>10k files), multiple cortex.yaml files.
Rationale: Guard regressions and validate non‑functional constraints.

## 15. Packaging & Release

- Gradle CI: build, verify, run tests; sign plugin; publish to Marketplace private channel initially.
- Versioning: semantic; changelog; compatibility range targeting 2024.3.
Rationale: Enable internal distribution and controlled rollout.

## 16. Risks & Mitigations

- Schema drift: allow org override schema + quick updates.
- API instability: feature‑flag remote calls; cache aggressively; fail softly.
- Token handling: PasswordSafe only; prompt on 401; redact logs.
Rationale: Reduce operational risk while keeping UX stable.

## 17. Metrics/Telemetry (optional)

- Prefer Marketplace statistics; if in‑plugin telemetry, make it opt‑in and anonymized.
Rationale: Respect privacy while gathering usefulness signals.

## 18. Open Questions

- Exact Cortex API endpoints and payload shapes in the on‑prem instance.
- Whether teams and scorecards are namespaced by org.
- Preferred auth beyond token (SAML/OIDC service tokens?).

## 19. Roadmap & Milestones

MVP (Weeks 1–2)
- Detect cortex.yaml; onboarding tip.
- Settings page (base URL + token via PasswordSafe) with connectivity check.
- JSON Schema mapping (bundled starter schema).
- Minimal inspections (required keys, enum validation, URL format).
- Juni action to generate docs/plan.md from docs/requirements.md.
Rationale: Unlock core value quickly and establish secure connectivity.

Beta (Weeks 3–4)
- Completions for keys/enums and background‑indexed entity IDs.
- Annotator with clickable links and hovers.
- Quick fixes for block insertion, key normalization, missing tags.
- Tool window for entity search and navigation.
Rationale: Improve speed, discoverability, and reduce context switching.

1.0 (Weeks 5–6)
- Org schema override (URL/file) + per‑project settings.
- Index persistence with PersistentStateComponent and scheduled refresh.
- Optional telemetry; Marketplace private channel release; internal docs.
Rationale: Harden for real‑world variance and large repos; prepare release.

## 20. Implementation Notes

- HTTP: java.net.http.HttpClient or OkHttp; retries/backoff; all calls off EDT.
- Caching: in‑memory + serialized lightweight cache with TTL.
- References: PSI references for IDs → quick navigation (Open in Cortex).
- Templates: FileTemplateDescriptor for cortex.yaml skeleton.
- Juni integration: external process path configurable; capture stdout to docs/plan.md.

## 21. Testing Matrix

- IDEs: IntelliJ Community/Ultimate 2024.3 on macOS/Linux/Windows.
- Scenarios: offline, invalid token, slow API, large monorepo (>10k files), multiple cortex.yaml files.

## 22. Ownership & Next Steps

- Ownership: Platform team; code owners for core/yaml/ui/juni packages.
- Next Steps:
  1) Scaffold from intellij-platform-plugin-template.
  2) Implement settings + credential storage + health check.
  3) Add schema provider + minimal inspections.
  4) Wire Juni action; open docs/plan.md after run.
  5) Implement completions/annotator; add tool window; persist index.
