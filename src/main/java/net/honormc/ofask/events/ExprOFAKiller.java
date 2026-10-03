package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.event.player.PlayerDeathEvent;
import net.honormc.oneforall.minestom.player.MinestomPlayer;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("OFA Killer")
@Description("The player who killed the victim in an 'on ofa death' event. Not set for deaths with no killer.")
@Examples({"on ofa death:", "\tif ofa killer is set:", "\t\tsend \"You killed %player%\" to ofa killer"})
public class ExprOFAKiller extends SimpleExpression<Player> {

    static {
        Skript.registerExpression(ExprOFAKiller.class, Player.class, ExpressionType.SIMPLE, "[the] ofa killer");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!getParser().isCurrentEvent(OFAEventWrappers.Death.class)) {
            Skript.error("'ofa killer' can only be used in an 'on ofa death' event");
            return false;
        }
        return true;
    }

    @Override
    protected Player @Nullable [] get(Event event) {
        if (event instanceof OFAEventWrappers.Death w
                && w.getEvent().ofa() instanceof PlayerDeathEvent e
                && e.killer() instanceof MinestomPlayer mp) {
            return new Player[]{mp.handle()};
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Player> getReturnType() {
        return Player.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa killer";
    }
}
