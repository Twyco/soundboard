# Changelog policy

This directory contains user-facing release notes for Simple Voice Chat
Soundboard. Each file is intended to be pasted directly into the corresponding
Modrinth release.

## File convention

- Use one Markdown file per **mod version**, named `<version>.md`, for example
  `0.3.0.md`.
- Do not create separate changelogs for each Minecraft version. Builds for 26.2,
  26.1.x, and 1.21.11 share the same mod-version changelog.
- List releases below in newest-first order and mark each as `Draft` or
  `Released`.
- A released entry includes its release date in ISO format: `YYYY-MM-DD`.

## Writing workflow

1. Create the next version file when development for that mod release begins.
2. Mark it `Draft` and describe the previous mod version it builds upon.
3. Update the draft in the same commit as every user-visible feature, behavior
   change, fix, removal, or compatibility change.
4. Write for players and server or modpack users. Avoid commit hashes, class
   names, implementation details, and development-only refactors.
5. Use only relevant headings from `Added`, `Changed`, `Fixed`, `Removed`,
   `Compatibility`, and `Known issues`.
6. Before publishing, verify that the file matches every supported Minecraft
   build, remove empty headings, change the status to `Released`, and add the
   date.
7. Treat published files as immutable release history. Correct factual mistakes
   when necessary, but record new changes in the next version file.
8. Create a new draft file as soon as work starts on the following release.

Keep entries short, concrete, and focused on effects users can observe. The
release file should make sense without the rest of the repository documentation.

## Releases

| Version | Status | Notes |
| --- | --- | --- |
| [0.3.0](0.3.0.md) | Draft | Custom config UI and sound wheel |
