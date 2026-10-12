package de.twyco.soundboard.util.config;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigMigrationTest {
    @ParameterizedTest
    @CsvSource({"0,1,67,true", "1,1,67,false", "2,1,67,true",
                "0,2,6,true", "1,2,6,true", "2,2,6,false"})
    void followsVersionMatrix(int source, int target, int expected, boolean changed) {
        JsonObject config = config(source, source == 2 ? 6 : 67);
        ConfigMigration.Result result = ConfigMigration.migrate(config, target);
        assertEquals(expected, combo(result.config()).get(0).getAsInt());
        assertEquals(target, result.config().get("configVersion").getAsInt());
        assertEquals(changed, result.changed());
        assertEquals(config(source, source == 2 ? 6 : 67), config, "Input must not be mutated");
        assertFalse(ConfigMigration.migrate(result.config(), target).changed());
    }

    @ParameterizedTest
    @CsvSource({"67,6", "49,30", "79,18", "341,224", "343,227", "347,231",
                "263,80", "313,115", "321,89", "330,99", "331,84", "333,86",
                "348,101", "161,100", "162,50"})
    void preservesRepresentativePhysicalKeysInBothDirections(int glfw, int sdl) {
        assertEquals(sdl, combo(ConfigMigration.migrate(config(1, glfw), 2).config()).get(0).getAsInt());
        assertEquals(glfw, combo(ConfigMigration.migrate(config(2, sdl), 1).config()).get(0).getAsInt());
    }

    @Test
    void allGlfwKeysExceptF25RoundTripWithoutCollisions() {
        // GLFW's published keyboard symbol ranges, independent of the migration table.
        IntStream keys = IntStream.concat(
                IntStream.of(32,39,44,45,46,47,59,61,91,92,93,96,161,162,330,331,332,333,334,335,336),
                IntStream.of(48,65,256,280,290,320,340).flatMap(start -> IntStream.rangeClosed(start,
                        switch (start) { case 48 -> 57; case 65 -> 90; case 256 -> 269;
                            case 280 -> 284; case 290 -> 313; case 320 -> 329; default -> 348; })));
        JsonObject original = config(1);
        keys.forEach(code -> combo(original).add(code));
        assertEquals(119, combo(original).size());
        ConfigMigration.Result forward = ConfigMigration.migrate(original, 2);
        assertTrue(forward.warnings().isEmpty());
        assertEquals(119, combo(forward.config()).size());
        HashSet<Integer> unique = new HashSet<>();
        combo(forward.config()).forEach(code -> assertTrue(unique.add(code.getAsInt())));
        assertEquals(original, ConfigMigration.migrate(forward.config(), 1).config());
    }

    @Test
    void migratesEverySoundAndGlobalComboWithoutChangingOtherSettings() {
        JsonObject original = JsonParser.parseString("""
                {"globalKeyCombos":{"wheel":[341,67],"stop":[]},"defaultAmplifier":125,
                 "globalState":{"soundAmplifier":170,"extra":true},"extra":{"keep":"me"},
                 "sounds":{"first":{"amplifier":90,"favorite":true,"loop":false,"keyCombo":[49]},
                           "second":{"keyCombo":[343,79]},"unbound":{"keyCombo":null}}}
                """).getAsJsonObject();
        JsonObject migrated = ConfigMigration.migrate(original, 2).config();
        assertEquals(JsonParser.parseString("[224,6]"), migrated.getAsJsonObject("globalKeyCombos").get("wheel"));
        assertEquals(JsonParser.parseString("[30]"), migrated.getAsJsonObject("sounds").getAsJsonObject("first").get("keyCombo"));
        assertEquals(JsonParser.parseString("[227,18]"), migrated.getAsJsonObject("sounds").getAsJsonObject("second").get("keyCombo"));
        JsonObject returned = ConfigMigration.migrate(migrated, 1).config();
        returned.remove("configVersion");
        assertEquals(original, returned);
    }

    @ParameterizedTest
    @CsvSource({"1,2,314", "1,2,-1", "1,2,9999", "2,1,220", "2,1,118", "2,1,135"})
    void disablesWholeUnsupportedComboAndKeepsOtherBindings(int source, int target, int unsupported) {
        JsonObject original = config(source, source == 1 ? 341 : 224, unsupported);
        original.getAsJsonObject("globalKeyCombos").add("valid", JsonParser.parseString(source == 1 ? "[67]" : "[6]"));
        ConfigMigration.Result result = ConfigMigration.migrate(original, target);
        assertTrue(combo(result.config()).isEmpty());
        assertEquals(1, result.config().getAsJsonObject("globalKeyCombos").getAsJsonArray("valid").size());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("globalKeyCombos.wheel"));
        assertTrue(result.warnings().get(0).contains(Integer.toString(unsupported)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "3", "-1", "1.5", "null", "\"1\"", "true", "2147483648"})
    void rejectsUnsupportedOrMalformedVersion(String value) {
        JsonObject config = config(1, 67);
        config.add("configVersion", JsonParser.parseString(value));
        assertThrows(IllegalArgumentException.class, () -> ConfigMigration.migrate(config, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "{}", "[1.5]", "[\"67\"]", "[true]", "[2147483648]"})
    void rejectsMalformedCombosEvenWhenVersionAlreadyMatches(String value) {
        JsonObject config = config(2, 6);
        config.getAsJsonObject("globalKeyCombos").add("wheel", JsonParser.parseString(value));
        if (value.equals("null")) {
            assertDoesNotThrow(() -> ConfigMigration.migrate(config, 2));
        } else {
            assertThrows(IllegalArgumentException.class, () -> ConfigMigration.migrate(config, 2));
        }
    }

    static JsonObject config(int version, int... codes) {
        JsonObject root = new JsonObject();
        if (version != 0) root.addProperty("configVersion", version);
        JsonObject combos = new JsonObject();
        JsonArray keys = new JsonArray();
        for (int code : codes) keys.add(code);
        combos.add("wheel", keys);
        root.add("globalKeyCombos", combos);
        root.add("sounds", new JsonObject());
        return root;
    }

    static JsonArray combo(JsonObject config) {
        return config.getAsJsonObject("globalKeyCombos").getAsJsonArray("wheel");
    }
}
