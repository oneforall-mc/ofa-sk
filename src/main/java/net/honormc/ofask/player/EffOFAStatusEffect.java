package net.honormc.ofask.player;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.player.OFAPlayer;
import net.honormc.oneforall.statuseffect.StatusEffectRegistry;
import net.honormc.oneforall.statuseffect.StatusEffects;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Apply/Remove OFA Status Effect")
@Description({
        "Applies a OneForAll status effect to players, or removes it. Without 'for', the effect's own",
        "default duration is used. Applying an effect a player already has restarts its timer."
})
@Examples({
        "apply ofa status effect \"ofa:combat_tag\" to player for 15 seconds",
        "remove ofa status effect \"ofa:combat_tag\" from player"
})
public class EffOFAStatusEffect extends Effect {

    static {
        Skript.registerEffect(EffOFAStatusEffect.class,
                "apply ofa status effect %string% to %players% [for %-timespan%]",
                "remove ofa status effect %string% from %players%");
    }

    private boolean apply;
    private Expression<String> effect;
    private Expression<Player> players;
    private @Nullable Expression<Timespan> duration;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        apply = matchedPattern == 0;
        effect = (Expression<String>) exprs[0];
        players = (Expression<Player>) exprs[1];
        duration = apply ? (Expression<Timespan>) exprs[2] : null;
        return true;
    }

    @Override
    protected void execute(Event event) {
        String id = effect.getSingle(event);
        if (id == null) return;
        if (StatusEffectRegistry.get(id) == null) {
            Skript.warning("No OFA status effect called \"" + id + "\"");
            return;
        }
        int ticks = -1;
        if (duration != null) {
            Timespan span = duration.getSingle(event);
            if (span != null) ticks = (int) Math.min(Integer.MAX_VALUE, span.getAs(Timespan.TimePeriod.TICK));
        }
        for (Player player : players.getArray(event)) {
            OFAPlayer ofa = OFAPlayers.of(player);
            if (ofa == null) continue;
            if (apply) StatusEffects.apply(ofa, id, ticks);
            else StatusEffects.remove(ofa, id);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return apply
                ? "apply ofa status effect " + effect.toString(event, debug) + " to " + players.toString(event, debug)
                    + (duration == null ? "" : " for " + duration.toString(event, debug))
                : "remove ofa status effect " + effect.toString(event, debug) + " from " + players.toString(event, debug);
    }
}
