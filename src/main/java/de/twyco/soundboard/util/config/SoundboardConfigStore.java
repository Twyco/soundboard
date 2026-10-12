package de.twyco.soundboard.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.function.Consumer;

/** Owns config activation and prevents failed loads from being overwritten by automatic saves. */
final class SoundboardConfigStore {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setStrictness(Strictness.STRICT)
            .create();

    @FunctionalInterface
    interface AtomicReplace {
        void replace(Path temporary, Path destination) throws IOException;
    }

    @FunctionalInterface
    interface Backup {
        void copy(Path original, Path destination) throws IOException;
    }

    private final AtomicReplace atomicReplace;
    private final Backup backup;
    private final Path file;
    private final int targetVersion;
    private final Consumer<String> warning;
    private SoundboardConfigData current;
    private JsonObject document;
    private boolean writable;
    private String persistedContent;

    SoundboardConfigStore(Path file, int targetVersion, Consumer<String> warning) {
        this(file, targetVersion, warning, (temporary, destination) ->
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    SoundboardConfigStore(Path file, int targetVersion, Consumer<String> warning, AtomicReplace atomicReplace) {
        this(file, targetVersion, warning, atomicReplace, (original, destination) -> Files.copy(original, destination));
    }

    SoundboardConfigStore(Path file, int targetVersion, Consumer<String> warning,
                          AtomicReplace atomicReplace, Backup backup) {
        this.atomicReplace = atomicReplace;
        this.backup = backup;
        this.file = file;
        this.targetVersion = targetVersion;
        this.warning = warning;
    }

    SoundboardConfigData get() {
        if (current == null) {
            throw new IllegalStateException("Soundboard configuration has not been loaded");
        }
        return current;
    }

    void load() throws IOException {
        writable = false;
        SoundboardConfigData candidate;
        JsonObject candidateDocument;
        String candidateContent;
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            candidate = SoundboardConfigData.createDefault();
            candidate.configVersion = targetVersion;
            candidateDocument = GSON.toJsonTree(candidate).getAsJsonObject();
            // Also validate the requested target version for new files.
            ConfigMigration.migrate(candidateDocument, targetVersion);
            candidateContent = writeAtomically(candidateDocument);
        } else {
            candidateContent = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject original = GSON.fromJson(candidateContent, JsonObject.class);
            if (original == null) {
                throw new IllegalArgumentException("Soundboard configuration must be a JSON object");
            }
            ConfigMigration.Result migration = ConfigMigration.migrate(original, targetVersion);
            candidateDocument = migration.config();
            candidate = GSON.fromJson(candidateDocument, SoundboardConfigData.class);
            if (candidate.globalState == null) {
                throw new IllegalArgumentException("globalState must be an object");
            }
            if (migration.changed()) {
                Path backup = file.resolveSibling(file.getFileName() + ".before-v" + migration.sourceVersion()
                        + "-to-v" + targetVersion + "-" + UUID.randomUUID() + ".bak");
                this.backup.copy(file, backup);
                candidateContent = writeAtomically(candidateDocument);
                migration.warnings().forEach(warning);
            }
        }
        current = candidate;
        document = candidateDocument;
        persistedContent = candidateContent;
        writable = true;
    }

    boolean save() throws IOException {
        if (!writable) {
            warning.accept("Config save blocked after a failed load; fix the file and reload it first");
            return false;
        }
        try {
            // The reload UI saves its draft before reading the file. Never overwrite an external edit
            // or a config migrated by another Minecraft instance with those stale active settings.
            if (!Files.readString(file, StandardCharsets.UTF_8).equals(persistedContent)) {
                writable = false;
                warning.accept("Config save blocked because the file changed externally; reload it first");
                return false;
            }
            JsonObject serialized = GSON.toJsonTree(current).getAsJsonObject();
            // Keep additional JSON fields intact while updating the known settings.
            JsonObject updated = merge(document, serialized);
            persistedContent = writeAtomically(updated);
            document = updated;
            return true;
        } catch (IOException exception) {
            writable = false;
            throw exception;
        }
    }

    private static JsonObject merge(JsonObject existing, JsonObject replacement) {
        JsonObject result = existing.deepCopy();
        for (String key : replacement.keySet()) {
            if (replacement.get(key).isJsonObject() && result.has(key) && result.get(key).isJsonObject()) {
                JsonObject oldObject = result.getAsJsonObject(key);
                JsonObject newObject = replacement.getAsJsonObject(key);
                if (key.equals("sounds") || key.equals("globalKeyCombos")) {
                    // Removed bindings/sounds must not be resurrected by preservation of extra fields.
                    oldObject.keySet().retainAll(newObject.keySet());
                }
                result.add(key, merge(oldObject, newObject));
            } else {
                result.add(key, replacement.get(key).deepCopy());
            }
        }
        return result;
    }

    private String writeAtomically(JsonObject config) throws IOException {
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "soundboard-", ".tmp");
        try {
            String content = GSON.toJson(config) + System.lineSeparator();
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            atomicReplace.replace(temporary, file);
            return content;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
