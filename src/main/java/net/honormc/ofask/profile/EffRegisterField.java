package net.honormc.ofask.profile;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Register OFA Field")
@Description({
        "Registers a saved per-player value. Run it in 'on load'. Re-registering an existing field does nothing,",
        "so reloading the script is safe. Field names need a prefix like \"lobby:visits\" so they never clash with",
        "built-in fields. 'seasonal' fields are wiped by a season reset; the rest are kept."
})
@Examples({
        "on load:",
        "\tregister ofa whole number field \"lobby:visits\"",
        "\tregister seasonal ofa number field \"pvp:rating\"",
        "\tregister ofa text field \"lobby:last-server\""
})
public class EffRegisterField extends Effect {

    static {
        Skript.registerEffect(EffRegisterField.class,
                "register [a[n]] [:seasonal] ofa (text:text|number:number|integer:whole number|boolean:(boolean|yes/no)) field %string%");
    }

    private ScriptFields.Kind kind;
    private boolean seasonal;
    private Expression<String> key;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        key = (Expression<String>) exprs[0];
        seasonal = parseResult.hasTag("seasonal");
        if (parseResult.hasTag("number")) kind = ScriptFields.Kind.NUMBER;
        else if (parseResult.hasTag("integer")) kind = ScriptFields.Kind.INTEGER;
        else if (parseResult.hasTag("boolean")) kind = ScriptFields.Kind.BOOLEAN;
        else kind = ScriptFields.Kind.TEXT;
        if (key instanceof Literal<String> literal && !literal.getSingle().contains(":")) {
            Skript.error("OFA field names need a prefix, like \"lobby:" + literal.getSingle() + "\"");
            return false;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        String name = key.getSingle(event);
        if (name == null) return;
        if (!ScriptFields.register(name, kind, seasonal)) {
            Skript.warning("Not registering OFA field \"" + name + "\": field names need a prefix, like \"lobby:" + name + "\"");
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "register " + (seasonal ? "seasonal " : "") + "ofa " + kind.name().toLowerCase() + " field " + key.toString(event, debug);
    }
}
