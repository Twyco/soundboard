package de.twyco.soundboard.util.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Translates persisted keyboard codes without loading Minecraft or native libraries. */
final class ConfigMigration {
    private static final Map<Integer, Integer> GLFW_TO_SDL;
    private static final Map<Integer, Integer> SDL_TO_GLFW;

    static {
        // Minecraft InputConstants: GLFW key symbols (through 26.2) and SDL scancodes (26.3).
        // F25 has no SDL equivalent. Super keys are named GUI keys in SDL.
        int[][] pairs = {
            {48, 39}, // 0
            {49, 30}, // 1
            {50, 31}, // 2
            {51, 32}, // 3
            {52, 33}, // 4
            {53, 34}, // 5
            {54, 35}, // 6
            {55, 36}, // 7
            {56, 37}, // 8
            {57, 38}, // 9
            {65, 4}, // A
            {66, 5}, // B
            {67, 6}, // C
            {68, 7}, // D
            {69, 8}, // E
            {70, 9}, // F
            {71, 10}, // G
            {72, 11}, // H
            {73, 12}, // I
            {74, 13}, // J
            {75, 14}, // K
            {76, 15}, // L
            {77, 16}, // M
            {78, 17}, // N
            {79, 18}, // O
            {80, 19}, // P
            {81, 20}, // Q
            {82, 21}, // R
            {83, 22}, // S
            {84, 23}, // T
            {85, 24}, // U
            {86, 25}, // V
            {87, 26}, // W
            {88, 27}, // X
            {89, 28}, // Y
            {90, 29}, // Z
            {290, 58}, // F1
            {291, 59}, // F2
            {292, 60}, // F3
            {293, 61}, // F4
            {294, 62}, // F5
            {295, 63}, // F6
            {296, 64}, // F7
            {297, 65}, // F8
            {298, 66}, // F9
            {299, 67}, // F10
            {300, 68}, // F11
            {301, 69}, // F12
            {302, 104}, // F13
            {303, 105}, // F14
            {304, 106}, // F15
            {305, 107}, // F16
            {306, 108}, // F17
            {307, 109}, // F18
            {308, 110}, // F19
            {309, 111}, // F20
            {310, 112}, // F21
            {311, 113}, // F22
            {312, 114}, // F23
            {313, 115}, // F24
            {282, 83}, // NUMLOCK
            {320, 98}, // NUMPAD0
            {321, 89}, // NUMPAD1
            {322, 90}, // NUMPAD2
            {323, 91}, // NUMPAD3
            {324, 92}, // NUMPAD4
            {325, 93}, // NUMPAD5
            {326, 94}, // NUMPAD6
            {327, 95}, // NUMPAD7
            {328, 96}, // NUMPAD8
            {329, 97}, // NUMPAD9
            {330, 99}, // KEYPAD DECIMAL: SDL KP_PERIOD is the physical decimal key
            {335, 88}, // NUMPADENTER
            {336, 103}, // NUMPADEQUALS
            {264, 81}, // DOWN
            {263, 80}, // LEFT
            {262, 79}, // RIGHT
            {265, 82}, // UP
            {334, 87}, // ADD
            {39, 52}, // APOSTROPHE
            {92, 49}, // BACKSLASH
            {44, 54}, // COMMA
            {61, 46}, // EQUALS
            {96, 53}, // GRAVE
            {91, 47}, // LBRACKET
            {45, 45}, // MINUS
            {332, 85}, // MULTIPLY
            {331, 84}, // KEYPAD DIVIDE
            {333, 86}, // KEYPAD SUBTRACT
            {46, 55}, // PERIOD
            {93, 48}, // RBRACKET
            {59, 51}, // SEMICOLON
            {47, 56}, // SLASH
            {32, 44}, // SPACE
            {258, 43}, // TAB
            {342, 226}, // LALT
            {341, 224}, // LCONTROL
            {340, 225}, // LSHIFT
            {343, 227}, // LSUPER
            {346, 230}, // RALT
            {345, 228}, // RCONTROL
            {344, 229}, // RSHIFT
            {347, 231}, // RSUPER
            {257, 40}, // RETURN
            {256, 41}, // ESCAPE
            {259, 42}, // BACKSPACE
            {261, 76}, // DELETE
            {269, 77}, // END
            {268, 74}, // HOME
            {260, 73}, // INSERT
            {267, 78}, // PAGEDOWN
            {266, 75}, // PAGEUP
            {280, 57}, // CAPSLOCK
            {284, 72}, // PAUSE
            {281, 71}, // SCROLLLOCK
            {283, 70}, // PRINTSCREEN
            {161, 100}, // WORLD_1 (non-US backslash)
            {162, 50}, // WORLD_2 (non-US hash)
            {348, 101}, // MENU: SDL APPLICATION is the physical context-menu key
        };
        Map<Integer, Integer> forward = new HashMap<>();
        Map<Integer, Integer> reverse = new HashMap<>();
        for (int[] pair : pairs) {
            if (forward.put(pair[0], pair[1]) != null || reverse.put(pair[1], pair[0]) != null) {
                throw new IllegalStateException("Ambiguous keyboard code mapping");
            }
        }
        GLFW_TO_SDL = Map.copyOf(forward);
        SDL_TO_GLFW = Map.copyOf(reverse);
    }

    private ConfigMigration() {}

    record Result(JsonObject config, int sourceVersion, boolean changed, List<String> warnings) {}

    static Result migrate(JsonObject original, int targetVersion) {
        requireSupportedVersion(targetVersion);
        boolean versionMissing = !original.has("configVersion");
        int sourceVersion = versionMissing ? 1 : integer(original.get("configVersion"), "configVersion");
        requireSupportedVersion(sourceVersion);
        JsonObject config = original.deepCopy();
        List<String> warnings = new ArrayList<>();
        boolean convert = sourceVersion != targetVersion;
        Map<Integer, Integer> mapping = sourceVersion == 1 ? GLFW_TO_SDL : SDL_TO_GLFW;

        if (config.has("globalKeyCombos")) {
            JsonObject combos = object(config.get("globalKeyCombos"), "globalKeyCombos");
            for (String id : new ArrayList<>(combos.keySet())) {
                migrateCombo(combos, id, "globalKeyCombos." + id, convert, mapping, warnings);
            }
        }
        if (config.has("sounds")) {
            JsonObject sounds = object(config.get("sounds"), "sounds");
            for (String id : sounds.keySet()) {
                JsonObject sound = object(sounds.get(id), "sounds." + id);
                if (sound.has("keyCombo")) {
                    migrateCombo(sound, "keyCombo", "sounds." + id + ".keyCombo", convert, mapping, warnings);
                }
            }
        }
        config.addProperty("configVersion", targetVersion);
        return new Result(config, sourceVersion, versionMissing || convert, List.copyOf(warnings));
    }

    private static void migrateCombo(JsonObject owner, String field, String path, boolean convert,
                                     Map<Integer, Integer> mapping, List<String> warnings) {
        JsonElement value = owner.get(field);
        if (value.isJsonNull()) {
            return; // Existing unbound combinations may be null.
        }
        if (!value.isJsonArray()) {
            throw new IllegalArgumentException(path + " must be an array of integer keyboard codes");
        }
        JsonArray converted = new JsonArray();
        List<Integer> unsupported = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            int code = integer(element, path);
            if (convert) {
                Integer translated = mapping.get(code);
                if (translated == null) {
                    unsupported.add(code);
                } else {
                    converted.add(translated);
                }
            }
        }
        if (convert) {
            if (!unsupported.isEmpty()) {
                owner.add(field, new JsonArray());
                warnings.add("Disabled key combination " + path + ": unsupported keyboard codes " + unsupported);
            } else {
                owner.add(field, converted);
            }
        }
    }

    private static JsonObject object(JsonElement element, String path) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException(path + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private static int integer(JsonElement element, String path) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(path + " must contain an integer");
        }
        try {
            return element.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException(path + " must contain an integer", exception);
        }
    }

    private static void requireSupportedVersion(int version) {
        if (version != 1 && version != 2) {
            throw new IllegalArgumentException("Unsupported soundboard configVersion: " + version);
        }
    }
}
