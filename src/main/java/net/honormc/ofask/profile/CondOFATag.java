package net.honormc.ofask.profile;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.profile.Profile;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

@Name("Has OFA Tag")
@Description("Whether a player has unlocked a tag. Tags are saved and follow the player between servers.")
@Examples({"if player has ofa tag \"vip\":", "\tsend \"Hello VIP!\" to player"})
public class CondOFATag extends Condition {

    static {
        Skript.registerCondition(CondOFATag.class,
                "%players% (has|have) ofa tag %string%",
                "%players% (doesn't|does not|do not|don't) have ofa tag %string%");
    }

    private Expression<Player> players;
    private Expression<String> tag;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        tag = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event event) {
        String id = tag.getSingle(event);
        if (id == null) return isNegated();
        return players.check(event, player -> {
            Profile profile = ScriptFields.profile(player);
            return profile != null && profile.hasTag(id);
        }, isNegated());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return players.toString(event, debug) + (isNegated() ? " doesn't have" : " has") + " ofa tag " + tag.toString(event, debug);
    }
}
