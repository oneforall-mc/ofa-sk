package net.honormc.ofask.profile;

import ch.njol.skript.registrations.Classes;
import net.honormc.oneforall.profile.Profile;
import net.honormc.oneforall.profile.ProfileField;
import net.honormc.oneforall.profile.ProfileFields;
import net.honormc.oneforall.profile.Profiles;
import net.minestom.server.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Glue between Skript values and {@link ProfileFields}. Script-registered fields hold one of four
 * scalar types; the core's built-in fields (coins, kills, team…) are reachable too, read and
 * written through the same string encode/decode the core uses.
 */
public final class ScriptFields {

    public enum Kind {
        TEXT("", raw -> raw),
        NUMBER(0.0, Double::valueOf),
        INTEGER(0L, Long::valueOf),
        BOOLEAN(false, Boolean::valueOf);

        final Object defaultValue;
        final Function<String, Object> decode;

        Kind(Object defaultValue, Function<String, Object> decode) {
            this.defaultValue = defaultValue;
            this.decode = decode;
        }
    }

    private ScriptFields() {
    }

    /**
     * Registers a persistent field for scripts. A key that already exists is left alone, so
     * {@code /sk reload} re-running {@code on load} is harmless (fields can't be unregistered).
     *
     * @return false if the key is missing the {@code namespace:} prefix scripts must use
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean register(String key, Kind kind, boolean seasonal) {
        if (!key.contains(":")) return false;
        if (ProfileFields.byKey(key) != null) return true;
        Function decode = kind.decode;
        Function<Object, String> encode = value -> value == null ? null : value.toString();
        ProfileFields.register(key, kind.defaultValue, ProfileField.Storage.PERSISTENT, decode, (Function) encode, seasonal);
        return true;
    }

    public static @Nullable Profile profile(Player player) {
        return Profiles.of(player.getUuid());
    }

    public static @Nullable ProfileField<?> field(String key) {
        return ProfileFields.byKey(key);
    }

    /** Converts a Skript value into the field's own type, or null if it can't be. */
    public static @Nullable Object coerce(ProfileField<?> field, Object value) {
        Object type = field.defaultValue();
        String raw;
        if (value instanceof Number n) {
            raw = type instanceof Long || type instanceof Integer
                    ? String.valueOf(Math.round(n.doubleValue()))
                    : String.valueOf(n.doubleValue());
        } else if (value instanceof String s) {
            raw = s;
        } else {
            raw = Classes.toString(value);
        }
        try {
            return field.decode(raw);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** {@code true} for fields whose value is a whole number, the only kind Redis can add to atomically. */
    public static boolean isWholeNumber(ProfileField<?> field) {
        Object type = field.defaultValue();
        return type instanceof Long || type instanceof Integer;
    }
}
