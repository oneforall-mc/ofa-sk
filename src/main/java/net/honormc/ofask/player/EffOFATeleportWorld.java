package net.honormc.ofask.player;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.minestom.world.GameWorld;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Teleport to OFA World")
@Description("Sends players to the spawn point of a world the server loaded under a name.")
@Examples("teleport player to ofa world \"arena\"")
public class EffOFATeleportWorld extends Effect {

    static {
        Skript.registerEffect(EffOFATeleportWorld.class, "teleport %players% to ofa world %string%");
    }

    private Expression<Player> players;
    private Expression<String> name;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        name = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        GameWorld world = ExprOFAWorld.resolve(name.getSingle(event));
        if (world == null) return;
        for (Player player : players.getArray(event)) {
            if (player.getInstance() == world.instance()) {
                player.teleport(world.spawn());
            } else {
                player.setInstance(world.instance(), world.spawn());
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "teleport " + players.toString(event, debug) + " to ofa world " + name.toString(event, debug);
    }
}
