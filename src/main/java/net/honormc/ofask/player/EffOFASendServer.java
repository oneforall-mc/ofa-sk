package net.honormc.ofask.player;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.player.OFAPlayer;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Send to OFA Server")
@Description({
        "Moves players to another server on your network, by the name your proxy (Velocity) knows it by.",
        "Does nothing on a server without a proxy."
})
@Examples("send player to ofa server \"survival\"")
public class EffOFASendServer extends Effect {

    static {
        Skript.registerEffect(EffOFASendServer.class, "send %players% to ofa server %string%");
    }

    private Expression<Player> players;
    private Expression<String> server;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        server = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String target = server.getSingle(event);
        if (target == null || target.isBlank()) return;
        for (Player player : players.getArray(event)) {
            OFAPlayer ofa = OFAPlayers.of(player);
            if (ofa != null) ofa.transferTo(target);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "send " + players.toString(event, debug) + " to ofa server " + server.toString(event, debug);
    }
}
