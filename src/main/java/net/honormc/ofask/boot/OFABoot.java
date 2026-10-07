package net.honormc.ofask.boot;

import net.honormc.oneforall.OneForAll;
import net.honormc.oneforall.config.ConfigLayout;
import net.honormc.oneforall.config.FeatureConfig;
import net.honormc.oneforall.data.DataServices;
import net.honormc.oneforall.display.persist.PersistentEntities;
import net.honormc.oneforall.expansion.ExpansionManager;
import net.honormc.oneforall.minestom.bootstrap.ServerBoot;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OFABoot {

    public enum Mode { AUTO, ALWAYS, NEVER }

    private static final String DEFAULT_CONFIG = """
            # auto:   use the OneForAll the server already runs, or boot one if there is none
            # always: boot OneForAll; an error if the server already runs one
            # never:  never boot OneForAll; without one, OFA syntax parses but does nothing
            boot: auto
            """;

    private static @Nullable ExpansionManager expansions;

    private OFABoot() {
    }

    public static boolean start(Logger log) {
        Path file = ConfigLayout.file("ofa-sk", "config.yml");
        FeatureConfig.saveDefaultIfAbsent(file, DEFAULT_CONFIG);
        Mode mode = mode(FeatureConfig.load(file).string("boot", "auto"), log);
        if (OneForAll.initialized()) {
            if (mode == Mode.ALWAYS) {
                log.severe("boot: always, but OneForAll is already running and can't boot twice. Using the running one.");
            }
            return true;
        }
        if (mode == Mode.NEVER) {
            log.warning("boot: never and no OneForAll is running. OFA syntax will parse but do nothing.");
            return false;
        }
        boot(log);
        return OneForAll.initialized();
    }

    public static boolean booted() {
        return expansions != null;
    }

    public static void stop(Logger log) {
        ExpansionManager booted = expansions;
        if (booted == null) return;
        expansions = null;
        try {
            booted.unloadAll();
            PersistentEntities.saveAll();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "OneForAll did not shut down cleanly", t);
        } finally {
            DataServices.shutdown();
        }
    }

    private static void boot(Logger log) {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(OFABoot.class.getClassLoader());
        try {
            ConfigLayout.migrateLegacyLayout();
            ServerBoot.installDataLayer();
            expansions = ServerBoot.expansions();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "OneForAll failed to boot", t);
        } finally {
            thread.setContextClassLoader(previous);
        }
        if (OneForAll.initialized()) {
            log.info("Booted OneForAll (" + (DataServices.isLocal() ? "local" : "shared") + " storage).");
        }
    }

    private static Mode mode(String value, Logger log) {
        try {
            return Mode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning("Unknown boot value '" + value + "' in config/ofa-sk/config.yml; using auto.");
            return Mode.AUTO;
        }
    }
}
