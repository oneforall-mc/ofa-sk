package net.honormc.ofask.player;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.player.OFAPlayer;
import net.honormc.oneforall.statuseffect.StatusEffects;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Has OFA Status Effect")
@Description("Whether a player currently has a OneForAll status effect.")
@Examples({"if player has ofa status effect \"ofa:combat_tag\":", "\tsend \"You can't do that in combat!\" to player"})
public class CondOFAStatusEffect extends Condition {

    static {
        Skript.registerCondition(CondOFAStatusEffect.class,
                "%players% (has|have) ofa status effect %string%",
                "%players% (doesn't|does not|do not|don't) have ofa status effect %string%");
    }

    private Expression<Player> players;
    private Expression<String> effect;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        effect = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event event) {
        String id = effect.getSingle(event);
        if (id == null) return isNegated();
        return players.check(event, player -> {
            OFAPlayer ofa = OFAPlayers.of(player);
            return ofa != null && StatusEffects.isActive(ofa, id);
        }, isNegated());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return players.toString(event, debug) + (isNegated() ? " doesn't have" : " has")
                + " ofa status effect " + effect.toString(event, debug);
    }
}
