package net.honormc.ofask.events;

import net.honormc.oneforall.OneForAll;
import net.honormc.oneforall.event.EventPriority;
import net.honormc.oneforall.event.Subscription;
import net.honormc.oneforall.event.player.CancellablePlayerEvent;
import net.honormc.oneforall.event.player.PlayerChatEvent;
import net.honormc.oneforall.event.player.PlayerDeathEvent;
import net.honormc.oneforall.event.player.PlayerEnvironmentalDamageEvent;
import net.honormc.oneforall.event.player.PlayerEvent;
import net.honormc.oneforall.event.player.PlayerFirstJoinEvent;
import net.honormc.oneforall.event.player.PlayerJoinEvent;
import net.honormc.oneforall.event.player.PlayerQuitEvent;
import net.honormc.oneforall.event.player.PlayerReadyEvent;
import net.honormc.oneforall.event.region.BoundEnterEvent;
import net.honormc.oneforall.event.region.BoundExitEvent;
import net.honormc.oneforall.event.statuseffect.StatusEffectApplyEvent;
import net.honormc.oneforall.event.statuseffect.StatusEffectRemoveEvent;
import net.honormc.oneforall.minestom.player.MinestomPlayer;
import net.minestom.server.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Forwards OFA's core events into Skript. Listens at {@link EventPriority#HIGH} so a script
 * sees what the NORMAL listeners decided (the chat module's rendered format, an earlier
 * cancel) and gets the last say over it; MONITOR listeners still observe the result.
 *
 * <p>Triggers run on the thread OFA fired the event on — the tick thread for every event here,
 * which skript-minestom treats as primary, so {@code cancel event} lands before OFA reads it back.
 */
public final class OFAEventBridge {

    private static final List<Subscription> SUBSCRIPTIONS = new ArrayList<>();

    private OFAEventBridge() {
    }

    public static synchronized void install() {
        if (!SUBSCRIPTIONS.isEmpty()) return;
        forward(PlayerJoinEvent.class, OFAEventWrappers.Join::new);
        forward(PlayerFirstJoinEvent.class, OFAEventWrappers.FirstJoin::new);
        forward(PlayerReadyEvent.class, OFAEventWrappers.Ready::new);
        forward(PlayerQuitEvent.class, OFAEventWrappers.Quit::new);
        forward(PlayerDeathEvent.class, OFAEventWrappers.Death::new);
        forward(StatusEffectRemoveEvent.class, OFAEventWrappers.StatusEffectRemove::new);
        forwardCancellable(PlayerChatEvent.class, OFAEventWrappers.Chat::new);
        forwardCancellable(BoundEnterEvent.class, OFAEventWrappers.RegionEnter::new);
        forwardCancellable(BoundExitEvent.class, OFAEventWrappers.RegionExit::new);
        forwardCancellable(StatusEffectApplyEvent.class, OFAEventWrappers.StatusEffectApply::new);
        forwardCancellable(PlayerEnvironmentalDamageEvent.class, OFAEventWrappers.EnvironmentalDamage::new);
    }

    public static synchronized void uninstall() {
        SUBSCRIPTIONS.forEach(Subscription::unregister);
        SUBSCRIPTIONS.clear();
    }

    private static <E extends PlayerEvent> void forward(Class<E> type, Function<OFABridgedEvent, ? extends Event> wrapper) {
        SUBSCRIPTIONS.add(OneForAll.events().listen(type, EventPriority.HIGH, event -> {
            Player player = handle(event);
            if (player != null) Bukkit.getPluginManager().callEvent(wrapper.apply(new OFABridgedEvent(event, player)));
        }));
    }

    private static <E extends CancellablePlayerEvent> void forwardCancellable(
            Class<E> type, Function<OFACancellableBridgedEvent, ? extends Event> wrapper) {
        SUBSCRIPTIONS.add(OneForAll.events().listen(type, EventPriority.HIGH, event -> {
            Player player = handle(event);
            if (player != null) Bukkit.getPluginManager().callEvent(wrapper.apply(new OFACancellableBridgedEvent(event, player)));
        }));
    }

    private static Player handle(PlayerEvent event) {
        return event.player() instanceof MinestomPlayer mp ? mp.handle() : null;
    }
}
