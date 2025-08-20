<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# cortex Changelog

## [Unreleased]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Detect cortex.yaml and show a one-time onboarding tip [MVP-1]
- YAML gutter navigation for service.id and x-cortex-tag with hover info (cached) [BETA-2]
- Settings: derive and persist API base URL from Cortex domain; test connection calls /api/v1/catalog/definitions [BETA-2a]
- Propose opening Cortex project via x-cortex-tag lookup; one-click "Open in Cortex" notification [BETA-2b]
- YAML gutter link for x-cortex-owners (provider=CORTEX) resolving to Teams admin URL [BETA-2c]
- YAML gutter link for x-cortex-groups resolving to Teams admin URL [BETA-2d]
- YAML gutter links for x-cortex-dependencies (tag) resolving via Catalog to Cortex admin URL [BETA-2e]
- Settings: "Create token…" link opens Personal Access Tokens page at {baseUrl}/admin/settings/personal-access-tokens [BETA-2f]
- Editor banner on cortex.yaml when Cortex isn’t configured, with a link to open Settings [BETA-2h]
- Dynamic refresh of cortex.yaml banners and links after applying/deleting settings; no need to reopen files [BETA-2i]
### Changed
 - Settings UI: form now anchors to the top of the page and text fields have constrained preferred widths to avoid horizontal scrollbars in the Settings dialog.
 - Settings UI: eliminated horizontal scrollbar after adding the "Create token…" button by making the token row responsive.
 - Removed vendor-specific examples; replaced all references to sportradar with neutral example.com in tests, samples, and comments.
### Added
- Settings: "Delete configuration…" action clears Base URL, API URL, Org Slug, and stored token [BETA-2g]
