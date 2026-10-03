package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.honormc.oneforall.event.player.PlayerChatEvent;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * How the chat line renders. MiniMessage (legacy {@code &} codes work too) with two
 * placeholders filled in as plain text, never parsed as tags, so a player typing {@code <red>}
 * can't style their own message: {@code <player>} and {@code <message>}.
 */
@Name("OFA Chat Format")
@Description({
        "How the chat line looks, in an 'on ofa chat' event. MiniMessage or & colour codes.",
        "<player> and <message> are replaced with the player's name and what they typed, as plain text."
})
@Examples({
        "on ofa chat:",
        "\tif player has permission \"chat.vip\":",
        "\t\tset ofa chat format to \"<gold>[VIP] <player><gray>: <white><message>\""
})
public class ExprOFAChatFormat extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprOFAChatFormat.class, String.class, ExpressionType.SIMPLE, "[the] ofa chat format");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!getParser().isCurrentEvent(OFAEventWrappers.Chat.class)) {
            Skript.error("'ofa chat format' can only be used in an 'on ofa chat' event");
            return false;
        }
        return true;
    }

    private static @Nullable PlayerChatEvent chat(Event event) {
        return event instanceof OFAEventWrappers.Chat w && w.getEvent().ofa() instanceof PlayerChatEvent e ? e : null;
    }

    @Override
    protected String @Nullable [] get(Event event) {
        PlayerChatEvent chat = chat(event);
        return chat == null || chat.template() == null ? null : new String[]{chat.template()};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET || mode == ChangeMode.RESET || mode == ChangeMode.DELETE
                ? new Class[]{String.class} : null;
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        PlayerChatEvent chat = chat(event);
        if (chat == null) return;
        if (mode != ChangeMode.SET || delta == null || delta.length == 0) {
            // Back to the server's plain default line.
            chat.render(null, Map.of());
            return;
        }
        chat.render((String) delta[0], Map.of(
                "player", chat.player().name(),
                "message", chat.message()));
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ofa chat format";
    }
}
