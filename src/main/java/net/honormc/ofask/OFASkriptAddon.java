package net.honormc.ofask;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import net.honormc.ofask.boot.OFABoot;
import net.honormc.ofask.events.OFAEventBridge;
import net.honormc.ofask.inventory.OFAInventoryClasses;
import net.honormc.ofask.variables.NetworkVariables;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Entry point for the {@code ofa-sk} Skript addon — a real addon jar (same shape as
 * {@code skript-reflect}) that adds custom {@code .sk} syntax backed directly by
 * oneforall-api's Java systems. Dropped into {@code Skript/addons/}, Skript loads and
 * enables this class itself, synchronously, before it parses any script.
 */
public class OFASkriptAddon extends JavaPlugin {

    private static OFASkriptAddon instance;
    private static SkriptAddon addonInstance;

    public OFASkriptAddon() {
        instance = this;
    }

    @Override
    public void onEnable() {
        boolean ofa = OFABoot.start(getLogger());
        OFAInventoryClasses.register();
        try {
            getAddonInstance().loadClasses("net.honormc.ofask", "inventory", "events", "profile", "player");
        } catch (IOException e) {
            getLogger().severe("Failed to load ofa-sk syntax classes: " + e.getMessage());
        }
        if (ofa) {
            OFAEventBridge.install();
        }
        // Must run here: Skript loads variables right after addons enable.
        NetworkVariables.install();
    }

    @Override
    public void onDisable() {
        OFAEventBridge.uninstall();
        OFABoot.stop(getLogger());
    }

    public static OFASkriptAddon instance() {
        return instance;
    }

    public static SkriptAddon getAddonInstance() {
        if (addonInstance == null) {
            addonInstance = Skript.registerAddon(instance);
        }
        return addonInstance;
    }
}
