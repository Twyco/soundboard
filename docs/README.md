# Soundboard-Dokumentation

Diese Dokumentation beschreibt den aktuellen Stand des Fabric-Mods **Simple Voice
Chat Soundboard**. Der Mod ist eine reine Client-Erweiterung fuer Minecraft und
spielt benutzerdefinierte Sounds ueber die API von Simple Voice Chat ab.

## Dokumente

- [Projektstruktur](project-structure.md): Verzeichnisse, Pakete und Zustaendigkeiten
- [Architektur](architecture.md): Initialisierung, Audiofluss, Eingaben und GUI
- [Konfiguration](configuration.md): Laufzeitdateien, JSON-Schema und Bedienverhalten
- [Entwicklung](development.md): Build, Abhaengigkeiten und Projektkonventionen

## Festgelegter Projektumfang

- **Plattform:** Fabric, clientseitig
- **Voice-Chat-Integration:** Simple Voice Chat ist eine feste Voraussetzung.
- **Aktive Minecraft-Versionen:** 26.2 und 26.1.x werden in getrennten Branches
  gepflegt.
- **Primaerer Entwicklungsbranch:** `26.2` (gleichzeitig der Default-Branch des
  Repositorys)
- **Audioformate:** Derzeit wird ausschliesslich MP3 geladen. Weitere Formate sind
  eine spaetere Erweiterung.
- **Wiedergabe:** Mehrere verschiedene Sounds koennen gleichzeitig laufen. Ein
  erneuter Druck auf die Tastenkombination eines laufenden Sounds stoppt diesen.
- **Eingaben:** Ausgeloeste Sound-Tastenkombinationen konsumieren das zugehoerige
  Minecraft-Tastaturereignis.
- **Qualitaetssicherung:** Eine GitHub-Actions-Builddatei ist vorhanden. Eigene
  automatisierte Tests existieren derzeit nicht.

## Begriffe

| Begriff | Bedeutung |
| --- | --- |
| Key Binding | Normale, von Minecraft verwaltete einzelne Tastenbelegung |
| Key Combo | Mod-eigene Kombination aus einer oder mehreren GLFW-Tasten |
| Sound Entry | Persistierte Einstellungen fuer genau eine Audiodatei |
| Playing Sound | Dekodierte PCM-Daten mit aktueller Abspielposition |
| Merge Event | Simple-Voice-Chat-Ereignis, in das der Mod Audiobloecke mischt |
