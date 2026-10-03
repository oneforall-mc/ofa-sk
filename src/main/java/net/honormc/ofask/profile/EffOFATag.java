package net.honormc.ofask.profile;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.profile.Profile;
import net.honormc.oneforall.profile.ProfileFields;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Name("Add/Remove OFA Tag")
@Description("Unlocks or removes a tag for a player. Tags are saved and follow the player between servers.")
@Examples({"add ofa tag \"vip\" to player", "remove ofa tag \"vip\" from player"})
public class EffOFATag extends Effect {

    static {
        Skript.registerEffect(EffOFATag.class,
                "add ofa tag %string% to %players%",
                "remove ofa tag %string% from %players%");
    }

    private boolean add;
    private Expression<String> tag;
    private Expression<Player> players;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        add = matchedPattern == 0;
        tag = (Expression<String>) exprs[0];
        players = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String id = tag.getSingle(event);
        if (id == null || id.isBlank() || id.contains(",")) return; // tags are stored comma-joined
        for (Player player : players.getArray(event)) {
            Profile profile = ScriptFields.profile(player);
            if (profile == null) continue;
            if (add) {
                profile.addTag(id);
            } else {
                Set<String> tags = profile.tags();
                if (tags.remove(id)) profile.set(ProfileFields.TAGS, String.join(",", tags));
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (add ? "add ofa tag " : "remove ofa tag ") + tag.toString(event, debug)
                + (add ? " to " : " from ") + players.toString(event, debug);
    }
}
