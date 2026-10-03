package net.honormc.ofask.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;

/** Registers the OFA events that take no argument. Filtered ones have their own classes. */
public final class OFAEvents {

    private OFAEvents() {
    }

    static {
        Skript.registerEvent("OFA Join", SimpleEvent.class, OFAEventWrappers.Join.class, "ofa join")
                .description("Called when a player joins, after their OneForAll profile has loaded.")
                .examples("on ofa join:", "\tsend \"Welcome back!\" to player");
        Skript.registerEvent("OFA First Join", SimpleEvent.class, OFAEventWrappers.FirstJoin.class, "ofa first join")
                .description("Called the first time a player ever joins the network.")
                .examples("on ofa first join:", "\tbroadcast \"Welcome %player% for the first time!\"");
        Skript.registerEvent("OFA Ready", SimpleEvent.class, OFAEventWrappers.Ready.class, "ofa ready")
                .description("Called once a joining player is fully spawned and ready for menus and titles.")
                .examples("on ofa ready:", "\tsend title \"Welcome\" to player");
        Skript.registerEvent("OFA Quit", SimpleEvent.class, OFAEventWrappers.Quit.class, "ofa quit")
                .description("Called when a player leaves.")
                .examples("on ofa quit:");
        Skript.registerEvent("OFA Death", SimpleEvent.class, OFAEventWrappers.Death.class, "ofa death")
                .description("Called when a player dies. 'ofa killer' is the player who killed them, if any.")
                .examples("on ofa death:", "\tif ofa killer is set:", "\t\tsend \"Killed by %ofa killer%\" to player");
        Skript.registerEvent("OFA Chat", SimpleEvent.class, OFAEventWrappers.Chat.class, "ofa chat")
                .description("Called when a player chats. Cancellable. Read 'ofa chat message'; change the line with 'ofa chat format'.")
                .examples("on ofa chat:", "\tset ofa chat format to \"<gray><player>: <white><message>\"");
        Skript.registerEvent("OFA Environmental Damage", SimpleEvent.class, OFAEventWrappers.EnvironmentalDamage.class,
                        "ofa environmental damage")
                .description("Called when a player takes damage with no attacker: falling, the void, fire, drowning and so on.",
                        "Cancellable. 'ofa damage cause' says what hurt them; 'ofa damage' is the amount and can be changed.")
                .examples("on ofa environmental damage:", "\tif ofa damage cause is \"fall\":", "\t\tcancel event");
    }
}
