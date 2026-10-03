package net.honormc.ofask.inventory;

import ch.njol.skript.events.wrapper.EventWrapper;
import ch.njol.skript.events.wrapper.marker.PlayerEventMarker;

/** Bukkit-shim wrapper around {@link OFAMenuCloseEvent}, the shape every skript-minestom event uses. */
public class OFAMenuCloseWrapper extends EventWrapper<OFAMenuCloseEvent> implements PlayerEventMarker {

    public OFAMenuCloseWrapper(OFAMenuCloseEvent event) {
        super(event);
    }
}
