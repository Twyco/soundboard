package de.twyco.soundboard.util.config;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SoundboardConfigStoreTest {
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(ints = {1,2})
    void createsNewConfigInNativeFormat(int target) throws IOException {
        Path file = directory.resolve("nested/soundboard.json");
        SoundboardConfigStore store = store(file, target);
        store.load();
        assertEquals(target, store.get().configVersion);
        assertEquals(target, read(file).get("configVersion").getAsInt());
        assertTrue(store.save());
        assertTrue(backups(file.getParent()).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0,1", "1,1", "2,1", "0,2", "1,2", "2,2"})
    void persistsVersionMatrixOnceAndBacksUpExactOriginal(int source, int target) throws IOException {
        Path file = directory.resolve("soundboard.json");
        String original = ConfigMigrationTest.config(source, source == 2 ? 6 : 67).toString();
        Files.writeString(file, original);
        SoundboardConfigStore store = store(file, target);
        store.load();
        assertEquals(target, store.get().configVersion);
        assertEquals(target == 1 ? 67 : 6, store.get().globalKeyCombos.get("wheel").iterator().next());
        int expectedBackups = source != target ? 1 : 0;
        assertEquals(expectedBackups, backups(directory).size());
        if (expectedBackups == 1) assertEquals(original, Files.readString(backups(directory).getFirst()));
        String firstLoad = Files.readString(file);
        store.load();
        assertEquals(firstLoad, Files.readString(file));
        assertEquals(expectedBackups, backups(directory).size());
    }

    @Test
    void switchingVersionsRepeatedlyKeepsKeysAndEveryBackup() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, ConfigMigrationTest.config(1, 341, 67).toString());
        for (int target : new int[]{2,1,2,1}) {
            SoundboardConfigStore store = store(file, target);
            store.load();
            assertEquals(target == 1 ? List.of(341,67) : List.of(224,6), new ArrayList<>(store.get().globalKeyCombos.get("wheel")));
        }
        assertEquals(4, backups(directory).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "[]", "{\"configVersion\":3}", "{\"configVersion\":0}",
            "{\"configVersion\":2,\"globalState\":null}", "{\"sounds\":{\"bad\":null}}", "{} trailing",
            "{configVersion:1}", "{\"globalKeyCombos\":null}", "{\"defaultAmplifier\":\"bad\"}"})
    void failedInitialLoadNeverOverwritesOriginal(String original) throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, original);
        SoundboardConfigStore store = store(file, 2);
        assertThrows(Exception.class, store::load);
        assertThrows(IllegalStateException.class, store::get);
        assertFalse(store.save());
        assertEquals(original, Files.readString(file));
        assertTrue(backups(directory).isEmpty());
    }

    @Test
    void failedReloadRetainsActiveConfigAndBlocksSavesUntilSuccessfulReload() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, ConfigMigrationTest.config(2, 6).toString());
        SoundboardConfigStore store = store(file, 2);
        store.load();
        SoundboardConfigData active = store.get();
        Files.writeString(file, "{\"configVersion\":3}");
        assertThrows(IllegalArgumentException.class, store::load);
        assertSame(active, store.get());
        assertFalse(store.save());
        assertEquals("{\"configVersion\":3}", Files.readString(file));
        Files.writeString(file, ConfigMigrationTest.config(2, 18).toString());
        store.load();
        assertNotSame(active, store.get());
        assertTrue(store.save());
    }

    @Test
    void danglingLinkIsNotReplacedWithDefaults() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Path original = directory.resolve("original.json");
        String content = ConfigMigrationTest.config(1, 67).toString();
        Files.writeString(original, content);
        Files.createSymbolicLink(file, original.getFileName());
        Files.delete(original);
        SoundboardConfigStore store = store(file, 2);
        // A dangling link must not be treated as a missing config and replaced with defaults.
        assertThrows(IOException.class, store::load);
        assertTrue(Files.isSymbolicLink(file));
        assertFalse(store.save());
    }

    @Test
    void backupFailureDoesNotActivateOrOverwriteConfig() throws IOException {
        Path file = directory.resolve("soundboard.json");
        String original = ConfigMigrationTest.config(1, 67).toString();
        Files.writeString(file, original);
        SoundboardConfigStore store = new SoundboardConfigStore(file, 2, ignored -> {},
                (temporary, destination) -> fail("Must not replace config when backup fails"),
                (source, destination) -> { throw new IOException("simulated backup failure"); });
        assertThrows(IOException.class, store::load);
        assertThrows(IllegalStateException.class, store::get);
        assertEquals(original, Files.readString(file));
        assertFalse(store.save());
        assertNoTemporaryFiles();
    }

    @Test
    void externalEditsCannotBeOverwrittenByDraftSaveBeforeReload() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, ConfigMigrationTest.config(1, 67).toString());
        SoundboardConfigStore store = store(file, 1);
        store.load();
        String external = ConfigMigrationTest.config(2, 18).toString();
        Files.writeString(file, external);
        assertFalse(store.save());
        assertEquals(external, Files.readString(file));
        store.load();
        assertEquals(79, store.get().globalKeyCombos.get("wheel").iterator().next());
        assertTrue(store.save());
    }

    @Test
    void atomicReplaceFailureRetainsOriginalBackupAndBlocksSaves() throws IOException {
        Path file = directory.resolve("soundboard.json");
        String original = ConfigMigrationTest.config(1, 67).toString();
        Files.writeString(file, original);
        // Inject the failure at the filesystem boundary, without permission-dependent tests.
        SoundboardConfigStore store = new SoundboardConfigStore(file, 2, ignored -> {},
                (temporary, destination) -> { throw new IOException("simulated atomic replace failure"); });
        assertThrows(IOException.class, store::load);
        assertEquals(original, Files.readString(file));
        assertEquals(original, Files.readString(backups(directory).getFirst()));
        assertFalse(store.save());
        assertNoTemporaryFiles();
    }

    @Test
    void failedOrdinarySaveBlocksFurtherSavesUntilReload() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, ConfigMigrationTest.config(2, 6).toString());
        SoundboardConfigStore store = new SoundboardConfigStore(file, 2, ignored -> {},
                (temporary, destination) -> { throw new IOException("simulated write failure"); });
        store.load();
        store.get().defaultAmplifier = 200;
        String original = Files.readString(file);
        assertThrows(IOException.class, store::save);
        assertFalse(store.save());
        assertEquals(original, Files.readString(file));
        assertNoTemporaryFiles();
    }

    @Test
    void unsupportedComboWarningIsEmittedAfterSuccessfulPersistence() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, ConfigMigrationTest.config(1, 341, 314).toString());
        List<String> warnings = new ArrayList<>();
        SoundboardConfigStore store = new SoundboardConfigStore(file, 2, warnings::add);
        store.load();
        assertTrue(store.get().globalKeyCombos.get("wheel").isEmpty());
        assertTrue(ConfigMigrationTest.combo(read(file)).isEmpty());
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("314"));
    }

    @Test
    void savingPreservesExtraFieldsButDoesNotResurrectRemovedSounds() throws IOException {
        Path file = directory.resolve("soundboard.json");
        Files.writeString(file, """
                {"configVersion":1,"extra":{"keep":true},"globalState":{"extra":123},
                 "sounds":{"keep":{"keyCombo":[67],"extra":"yes"},"remove":{"keyCombo":[49]}}}
                """);
        SoundboardConfigStore store = store(file, 2);
        store.load();
        store.get().sounds.remove("remove");
        store.get().sounds.get("keep").amplifier = 180;
        assertTrue(store.save());
        JsonObject saved = read(file);
        assertTrue(saved.getAsJsonObject("extra").get("keep").getAsBoolean());
        assertEquals(123, saved.getAsJsonObject("globalState").get("extra").getAsInt());
        assertEquals("yes", saved.getAsJsonObject("sounds").getAsJsonObject("keep").get("extra").getAsString());
        assertFalse(saved.getAsJsonObject("sounds").has("remove"));
        assertEquals(180, saved.getAsJsonObject("sounds").getAsJsonObject("keep").get("amplifier").getAsInt());
    }

    private SoundboardConfigStore store(Path file, int target) {
        return new SoundboardConfigStore(file, target, ignored -> {});
    }

    private JsonObject read(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    private List<Path> backups(Path parent) throws IOException {
        try (var files = Files.list(parent)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".bak")).toList();
        }
    }

    private void assertNoTemporaryFiles() throws IOException {
        try (var files = Files.list(directory)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }
}
