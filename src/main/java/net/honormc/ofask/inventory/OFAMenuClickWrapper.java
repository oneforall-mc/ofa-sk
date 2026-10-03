package net.honormc.ofask.inventory;

import ch.njol.skript.events.wrapper.EventWrapper;
import ch.njol.skript.events.wrapper.marker.PlayerEventMarker;

/** Bukkit-shim wrapper around {@link OFAMenuClickEvent}, the shape every skript-minestom event uses. */
public class OFAMenuClickWrapper extends EventWrapper<OFAMenuClickEvent> implements PlayerEventMarker {

    public OFAMenuClickWrapper(OFAMenuClickEvent event) {
        super(event);
    }
}
