package net.honormc.ofask.events;

import ch.njol.skript.events.wrapper.EventWrapper;
import ch.njol.skript.events.wrapper.marker.PlayerEventMarker;

/**
 * One Bukkit-shim wrapper per Skript event. Skript routes triggers by wrapper class, so every
 * event needs its own. The type argument is what {@code cancel event} reads to decide whether
 * an event can be cancelled, so only the cancellable OFA events use the cancellable carrier.
 */
public final class OFAEventWrappers {

    private OFAEventWrappers() {
    }

    public static final class Join extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public Join(OFABridgedEvent event) { super(event); }
    }

    public static final class FirstJoin extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public FirstJoin(OFABridgedEvent event) { super(event); }
    }

    public static final class Ready extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public Ready(OFABridgedEvent event) { super(event); }
    }

    public static final class Quit extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public Quit(OFABridgedEvent event) { super(event); }
    }

    public static final class Death extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public Death(OFABridgedEvent event) { super(event); }
    }

    public static final class Chat extends EventWrapper<OFACancellableBridgedEvent> implements PlayerEventMarker {
        public Chat(OFACancellableBridgedEvent event) { super(event); }
    }

    public static final class RegionEnter extends EventWrapper<OFACancellableBridgedEvent> implements PlayerEventMarker {
        public RegionEnter(OFACancellableBridgedEvent event) { super(event); }
    }

    public static final class RegionExit extends EventWrapper<OFACancellableBridgedEvent> implements PlayerEventMarker {
        public RegionExit(OFACancellableBridgedEvent event) { super(event); }
    }

    public static final class StatusEffectApply extends EventWrapper<OFACancellableBridgedEvent> implements PlayerEventMarker {
        public StatusEffectApply(OFACancellableBridgedEvent event) { super(event); }
    }

    public static final class EnvironmentalDamage extends EventWrapper<OFACancellableBridgedEvent> implements PlayerEventMarker {
        public EnvironmentalDamage(OFACancellableBridgedEvent event) { super(event); }
    }

    public static final class StatusEffectRemove extends EventWrapper<OFABridgedEvent> implements PlayerEventMarker {
        public StatusEffectRemove(OFABridgedEvent event) { super(event); }
    }
}
