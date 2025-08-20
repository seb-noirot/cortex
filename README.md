# cortex

![Build](https://github.com/seb-noirot/cortex/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/28241.svg)](https://plugins.jetbrains.com/plugin/28241)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/28241.svg)](https://plugins.jetbrains.com/plugin/28241)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [x] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [x] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [x] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
Cortex IntelliJ Plugin adds smart navigation and links for `cortex.yaml` files in JetBrains IDEs.

What you can do with it:
- Click on IDs and tags in `cortex.yaml` to open the corresponding entity in Cortex (services, teams/groups, scorecards, and dependencies).
- See helpful hovers (e.g., resolve a service name for a known ID) fetched in the background and cached to stay fast and resilient.
- Configure your Cortex domain and an access token in Settings › Tools › Cortex. A built‑in “Test connection” validates connectivity. You can also open the Personal Access Tokens page directly from Settings.
- Get an editor banner guiding you to configure Cortex when a `cortex.yaml` is open and no configuration is set yet.

Design notes:
- Security first: the token is stored securely in PasswordSafe and never logged; network calls run off the EDT.
- Works with IntelliJ Platform 2024.3 and compatible IDEs.

This section is used to generate the plugin description in plugin.xml during build. Keep the `<!-- ... -->` markers intact.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "cortex"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28241) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/28241/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/seb-noirot/cortex/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation


## Features (Early Beta)
- YAML gutter icon on `service.id` in `cortex.yaml` files to open the corresponding Cortex page. [BETA-2]
- Tooltip/hover shows the service name when available, fetched in the background and cached (network off EDT). Secrets are never logged.
