## Overview

Build an IntelliJ Platform (JetBrains) plugin that enhances developer workflows for **Cortex** service catalogs (on‑prem):

* Provide rich editing support for `cortex.yaml` (schema validation, completions, navigation, quick fixes).
* Offer convenient links and actions to open Cortex entities (services, scorecards, runbooks) from code.
* Allow configuration of the **Cortex API base URL** (on‑prem) and **access token** securely.
* Integrate a **Juni-powered planning** action that reads `docs/requirements.md` and generates `docs/plan.md`.

Primary IDEs: IntelliJ IDEA (Ultimate/Community). Secondary (nice-to-have): WebStorm, PyCharm.

## Users & Use Cases

* **Developers** authoring/maintaining `cortex.yaml` in service repos.
* **Tech Leads/SREs** validating scorecards and ownership metadata.
* **Platform/IDP team** enforcing standards via inspections and quick-fixes.

Key scenarios:

1. Create/edit `cortex.yaml` with real-time validation and guided completion.
2. Jump to Cortex UI pages from within the IDE (gutter icons / tool window shortcuts).
3. Validate repo contents against organization rules (e.g., required fields, tag formats, scorecard IDs).
4. Configure on‑prem endpoint + token once, used across inspections and API calls.
5. Use the **Juni** action to generate/update `docs/plan.md` from requirements.

## Functional Requirements

1. **cortex.yaml language support**

    * YAML schema validation against a configurable JSON Schema (bundled default + org override).
    * Inspections (LocalInspectionTool): missing required keys, invalid enum values, bad references (e.g., scorecard IDs), URL format, owners syntax.
    * Annotator/References: make entity IDs clickable; resolve to Cortex UI URL; hover info fetched from API (with caching).
    * CompletionContributor: keys, enums, known entity IDs (services, teams, scorecards) via background fetch + index.
    * Quick Fixes (LocalQuickFix): insert required blocks, normalize key casing, add missing tags.
    * Intentions/Templates: live templates for skeleton `cortex.yaml`.

2. **Cortex integration (on‑prem)**

    * Settings page: base URL (e.g., `https://cortex.mycorp.local`), organization slug (if needed), token storage in **PasswordSafe**.
    * Health check + version fetch; show status in settings.
    * Background sync to index known IDs for completion (persisted via `PersistentStateComponent`).

3. **Navigation & UX**

    * Gutter icons next to `id:` or `service:` entries to open in browser.
    * Tool Window: search Cortex entities, view metadata, open links, quick copy IDs.
    * Context menu actions on `cortex.yaml`: "Validate now", "Open in Cortex", "Insert template".

4. **Juni Planning Action**

    * Action: **“Generate Improvement Plan (Juni)”**.
    * Reads `docs/requirements.md`, runs configured Juni command/prompt, writes `docs/plan.md`, opens it in editor.
    * If `docs/requirements.md` missing, offer to create a stub.

5. **Project detection & onboarding**

    * If repository contains `cortex.yaml`, show one-time tip to configure Cortex endpoint/token.
    * Provide sample templates via *New File → Cortex → cortex.yaml*.

## Non-Functional Requirements

* **Security**: tokens stored via `com.intellij.credentialStore.PasswordSafe`; never written to logs.
* **Performance**: inspections must be fast; API calls off EDT with caching; index refresh throttled/debounced.
* **Resilience**: plugin features degrade gracefully when offline; clear error messaging.
* **Compatibility**: build against IntelliJ Platform 2024.3 LTS (adjust if org standard differs).
* **Configurability**: allow org to supply custom JSON Schema URL or bundled schema override file.

## API & Data

* Endpoints (illustrative; adapt to Cortex API):

    * `GET /api/services` (list ids/names)
    * `GET /api/scorecards`
    * `GET /api/teams`
    * `GET /api/version`
    * `GET /api/services/{id}` (hover details)
* Auth: Bearer token.
* Rate limiting/backoff + ETag/If‑None‑Match caching when possible.

## IDE Integration Details

* Modules: single Gradle module based on `intellij-platform-plugin-template`.
* Key IntelliJ extension points:

    * `yaml.schemaProviderFactory` (map JSON Schema to `cortex.yaml`).
    * `localInspection` (multiple inspections).
    * `completion.contributor` (YAML).
    * `annotator` (clickable references & tooltips).
    * `applicationService` / `projectService` (settings, API client, cache).
    * `toolWindow` (Cortex browser/search).
    * `action` (Generate Plan with Juni; Open in Cortex).
    * `fileTypeFactory` (if needed for custom icon for `cortex.yaml`).

## Configuration & Secrets

* Settings UI (Configurable): Base URL, optional org slug, token (PasswordSafe), schema source (Bundled/URL/Path).
* Export/import settings without secrets; token re‑entry required.

## Testing & QA

* Unit tests for schema mapping, inspections, quick fixes.
* UI tests with `LightPlatformCodeInsightFixtureTestCase`.
* Offline/invalid token scenarios.
* Large monorepo performance test.

## Packaging & Release

* Gradle CI: build, verify, run tests.
* Sign plugin; publish to JetBrains Marketplace (private channel initially).
* Versioning: semantic; changelog; compatibility range.

## Risks & Mitigations

* **Schema drift**: allow org override schema + fast updates.
* **API instability**: feature-flag remote calls; cache aggressively; fail softly.
* **Token handling**: PasswordSafe only; redact logs; prompt on 401.
