package net.honormc.ofask.player;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.minestom.world.GameWorld;
import net.honormc.oneforall.minestom.world.NamedWorlds;
import net.minestom.server.instance.Instance;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("OFA World")
@Description("A world the server loaded under a name, like \"spawn\" or \"arena\". Works anywhere an instance does.")
@Examples({"if instance of player is ofa world \"arena\":", "\tsend \"You're in the arena\" to player"})
public class ExprOFAWorld extends SimpleExpression<Instance> {

    static {
        Skript.registerExpression(ExprOFAWorld.class, Instance.class, ExpressionType.COMBINED, "ofa world %string%");
    }

    private Expression<String> name;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        name = (Expression<String>) exprs[0];
        return true;
    }

    static @Nullable GameWorld resolve(@Nullable String name) {
        if (name == null) return null;
        GameWorld world = NamedWorlds.resolve(name).orElse(null);
        if (world == null) Skript.warning("No OFA world called \"" + name + "\"");
        return world;
    }

    @Override
    protected Instance @Nullable [] get(Event event) {
        GameWorld world = resolve(name.getSingle(event));
        return world == null ? null : new Instance[]{world.instance()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Instance> getReturnType() {
        return Instance.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa world " + name.toString(event, debug);
    }
}
