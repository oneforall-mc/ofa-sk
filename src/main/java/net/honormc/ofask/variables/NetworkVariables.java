package net.honormc.ofask.variables;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import net.honormc.ofask.OFASkriptAddon;
import net.honormc.oneforall.config.ServerNamespace;
import net.honormc.oneforall.data.DataServices;
import net.honormc.oneforall.data.redis.RedisSubscription;
import net.honormc.oneforall.data.sql.SqlDatabase;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code {net::*}}: Skript variables kept in Postgres and synced to every server over Redis.
 *
 * <ul>
 *   <li><b>Reads</b> never touch the database. Skript keeps every variable in memory; this only
 *       persists and syncs.</li>
 *   <li><b>Writes</b> arrive on Skript's own save thread ({@link #onSave}), are coalesced for
 *       {@value #FLUSH_MS} ms, committed in one transaction, then published.</li>
 *   <li><b>Numbers merge by adding.</b> A numeric write is sent as the change since the last value
 *       this server knew, and Postgres adds it to whatever is stored. {@code add 1 to {net::x}} on
 *       two servers at once therefore counts twice, without scripts needing a special counter.</li>
 *   <li><b>Everything else</b> is last-commit-wins. Each row carries a version; a server only
 *       applies a remote change newer than what it has.</li>
 *   <li><b>Remote changes</b> are applied on the primary thread through Skript's public
 *       {@code setVariable}. That re-queues a save of the same value, which {@link #onSave}
 *       recognises from {@link #expected} and drops, so nothing echoes back to the database.</li>
 *   <li><b>Redis is only the fast path.</b> A reconcile query every {@value #RECONCILE_SECONDS}s
 *       picks up anything pub/sub missed.</li>
 * </ul>
 *
 * Only runs under {@code storage: shared}. With OneForAll's default local storage there is one
 * server, so {@code {net::*}} is left to Skript's normal file storage.
 */
public final class NetworkVariables {

    public static final String PREFIX = "net::";
    static final String STORAGE_TYPE = "ofa-network";

    private static final Logger LOG = Logger.getLogger("OFA-SK");
    private static final long FLUSH_MS = 50;
    private static final long RECONCILE_SECONDS = 30;
    private static final int MAX_VALUE_BYTES = 64 * 1024;
    private static final int MAX_INLINE_BYTES = 8 * 1024;
    private static final Set<String> NUMBER_TYPES = Set.of("number", "long", "integer", "double", "float", "short", "byte");
    private static final Object TOMBSTONE = new Object();

    private static final String DDL = """
            CREATE TABLE IF NOT EXISTS skript_net_vars (
                namespace  TEXT        NOT NULL,
                name       TEXT        NOT NULL,
                type       TEXT,
                value      BYTEA,
                num        NUMERIC,
                version    BIGINT      NOT NULL,
                origin     TEXT        NOT NULL,
                updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                PRIMARY KEY (namespace, name)
            )""";
    private static final String DDL_INDEX =
            "CREATE INDEX IF NOT EXISTS skript_net_vars_updated ON skript_net_vars (namespace, updated_at)";

    private static final String SELECT_LIVE =
            "SELECT name, type, value, num, version, updated_at FROM skript_net_vars "
                    + "WHERE namespace = ? AND (value IS NOT NULL OR num IS NOT NULL)";
    private static final String SELECT_CHANGED =
            "SELECT name, type, value, num, version, updated_at FROM skript_net_vars "
                    + "WHERE namespace = ? AND updated_at > ? ORDER BY updated_at";
    private static final String SELECT_ONE =
            "SELECT name, type, value, num, version, updated_at FROM skript_net_vars WHERE namespace = ? AND name = ?";
    // ?7-?9 are the numeric delta. With a delta: add it to a stored number; if the row was deleted
    // (a tombstone), count from zero, so an add racing a delete can't resurrect the old total.
    // Otherwise (no delta, or the row holds a non-number) take ?5 as-is.
    private static final String UPSERT = """
            INSERT INTO skript_net_vars AS t (namespace, name, type, value, num, version, origin, updated_at)
            VALUES (?, ?, ?, ?, ?, 1, ?, now())
            ON CONFLICT (namespace, name) DO UPDATE SET
                type = EXCLUDED.type,
                value = EXCLUDED.value,
                num = CASE WHEN CAST(? AS NUMERIC) IS NULL THEN EXCLUDED.num
                           WHEN t.num IS NOT NULL THEN t.num + CAST(? AS NUMERIC)
                           WHEN t.value IS NULL THEN CAST(? AS NUMERIC)
                           ELSE EXCLUDED.num END,
                version = t.version + 1,
                origin = EXCLUDED.origin,
                updated_at = now()
            RETURNING version, num, updated_at""";
    private static final String GC_TOMBSTONES =
            "DELETE FROM skript_net_vars WHERE namespace = ? AND value IS NULL AND num IS NULL "
                    + "AND updated_at < now() - INTERVAL '7 days'";

    private static volatile @Nullable NetworkVariables instance;

    private final String origin = UUID.randomUUID().toString();
    private final String namespace = ServerNamespace.get();
    private final String channel = "ofa:skvar:" + namespace;
    private final SqlDatabase sql = DataServices.sql();

    private final ConcurrentHashMap<String, Long> versions = new ConcurrentHashMap<>();
    /** The last number each name held as far as the save thread knows. Save thread only. */
    private final Map<String, BigDecimal> lastKnown = new ConcurrentHashMap<>();
    /** Values a remote apply is about to make Skript save; {@link #onSave} drops these. */
    private final Map<String, Deque<Object>> expected = new ConcurrentHashMap<>();
    private final Map<String, Change> pending = new LinkedHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "OFA-SK network variables");
        t.setDaemon(true);
        return t;
    });

    private List<Row> loaded = List.of();
    private volatile @Nullable Timestamp lastSeen;
    private @Nullable RedisSubscription subscription;
    private long lastErrorLog;
    private long lastGc;

    private NetworkVariables() {
    }

    static NetworkVariables get() {
        NetworkVariables engine = instance;
        if (engine == null) throw new IllegalStateException("network variables were not set up");
        return engine;
    }

    /**
     * Called from the addon's {@code onEnable}, before Skript loads variables. Under shared
     * storage, registers the storage type and adds its database section to the in-memory
     * {@code config.sk} at the top, so {@code net::} names land here and the user's own
     * {@code default} database is left exactly as it was.
     */
    public static void install() {
        if (!DataServices.isInstalled() || !DataServices.isNetworked()) {
            LOG.info("[OFA-SK] Single-server storage: {net::*} variables are saved like any other variable.");
            return;
        }
        instance = new NetworkVariables();
        Variables.registerStorage(NetworkVariableStorage.class, STORAGE_TYPE);
        injectDatabase();
    }

    private static void injectDatabase() {
        Config config = SkriptConfig.getConfig();
        if (config == null) return;
        if (!(config.getMainNode().get("databases") instanceof SectionNode databases)) return;
        for (Node node : databases) {
            if (node instanceof SectionNode section && STORAGE_TYPE.equalsIgnoreCase(section.getValue("type"))) {
                return; // configured by hand already
            }
        }
        SectionNode section = new SectionNode("ofa network", "", databases, 0);
        section.add(new EntryNode("type", STORAGE_TYPE, section));
        section.add(new EntryNode("pattern", "(?i)" + java.util.regex.Pattern.quote(PREFIX) + ".*", section));
        section.add(new EntryNode("backup interval", "0", section));
        databases.add(0, section);
    }

    // ── Boot ────────────────────────────────────────────────────────────────────────────────────

    boolean loadRows() {
        try {
            sql.update(DDL, ps -> {
            });
            sql.update(DDL_INDEX, ps -> {
            });
            loaded = sql.query(SELECT_LIVE, ps -> ps.setString(1, namespace), NetworkVariables::readRows);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "[OFA-SK] Couldn't load {net::*} variables from Postgres", e);
            return false;
        }
        if (loaded.size() > 50_000) {
            LOG.warning("[OFA-SK] " + loaded.size() + " {net::*} variables loaded. Every server keeps all of them in memory;"
                    + " per-player data belongs in 'ofa field' instead.");
        }
        return true;
    }

    void applyLoadedRows() {
        for (Row row : loaded) {
            apply(row);
            Timestamp seen = lastSeen;
            if (seen == null || row.updatedAt.after(seen)) lastSeen = row.updatedAt;
        }
        loaded = List.of();
        subscription = DataServices.redis().subscribe(channel, this::onMessage);
        executor.scheduleWithFixedDelay(this::flushQuietly, FLUSH_MS, FLUSH_MS, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(this::reconcileQuietly, RECONCILE_SECONDS, RECONCILE_SECONDS, TimeUnit.SECONDS);
    }

    // ── Local writes (Skript's save thread) ─────────────────────────────────────────────────────

    void onSave(String name, @Nullable String type, byte @Nullable [] value) {
        Deque<Object> echoes = expected.get(name);
        if (echoes != null) {
            synchronized (echoes) {
                Object next = echoes.peekFirst();
                if (next != null && matches(next, type, value)) {
                    echoes.pollFirst();
                    if (next instanceof BigDecimal number) lastKnown.put(name, number);
                    else lastKnown.remove(name);
                    return;
                }
            }
        }

        Change change;
        if (value == null) {
            lastKnown.remove(name);
            change = Change.delete();
        } else if (value.length > MAX_VALUE_BYTES) {
            LOG.warning("[OFA-SK] {" + name + "} is " + value.length / 1024 + " KB; network variables are limited to 64 KB. Not saved.");
            return;
        } else if (type != null && NUMBER_TYPES.contains(type) && Classes.deserialize(type, value) instanceof Number n) {
            BigDecimal now = new BigDecimal(n.toString());
            BigDecimal before = lastKnown.put(name, now);
            change = Change.number(now, before == null ? null : now.subtract(before));
        } else {
            lastKnown.remove(name);
            change = Change.blob(type, value);
        }
        synchronized (pending) {
            Change previous = pending.get(name);
            pending.put(name, previous == null ? change : previous.then(change));
        }
    }

    private static boolean matches(Object expected, @Nullable String type, byte @Nullable [] value) {
        if (expected == TOMBSTONE) return value == null;
        if (value == null || type == null) return false;
        if (expected instanceof BigDecimal number) {
            return NUMBER_TYPES.contains(type)
                    && Classes.deserialize(type, value) instanceof Number n
                    && new BigDecimal(n.toString()).compareTo(number) == 0;
        }
        return expected instanceof Blob blob && blob.type.equals(type) && Arrays.equals(blob.value, value);
    }

    // ── Flush to Postgres + publish (network thread) ────────────────────────────────────────────

    private void flushQuietly() {
        try {
            flush();
        } catch (Throwable t) {
            logError("Couldn't save {net::*} variables; retrying", t);
        }
    }

    private void flush() throws SQLException {
        Map<String, Change> batch;
        synchronized (pending) {
            if (pending.isEmpty()) return;
            batch = new LinkedHashMap<>(pending);
            pending.clear();
        }
        List<String> lines = new ArrayList<>();
        try (Connection connection = sql.connection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(UPSERT)) {
                for (Map.Entry<String, Change> entry : batch.entrySet()) {
                    String name = entry.getKey();
                    Change change = entry.getValue();
                    bind(ps, name, change);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        long version = rs.getLong(1);
                        BigDecimal stored = rs.getBigDecimal(2);
                        versions.merge(name, version, Math::max);
                        lines.add(line(name, version, change, stored));
                        if (change.number != null && stored != null && stored.compareTo(change.number) != 0) {
                            // Another server added to it too: show the merged total here as well.
                            Row merged = new Row(name, "number", null, stored, version, rs.getTimestamp(3));
                            Bukkit.getScheduler().runTask(OFASkriptAddon.instance(), () -> apply(merged));
                        }
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                synchronized (pending) { // put the batch back in front of anything newer
                    Map<String, Change> newer = new LinkedHashMap<>(pending);
                    pending.clear();
                    pending.putAll(batch);
                    newer.forEach((name, change) -> pending.merge(name, change, Change::then));
                }
                throw e;
            }
        }
        DataServices.redis().publish(channel, origin + "\n" + String.join("\n", lines));
    }

    private void bind(PreparedStatement ps, String name, Change change) throws SQLException {
        ps.setString(1, namespace);
        ps.setString(2, name);
        if (change.number != null) {
            ps.setString(3, "number");
            ps.setNull(4, Types.BINARY);
            ps.setBigDecimal(5, change.number);
        } else {
            ps.setString(3, change.type);
            ps.setBytes(4, change.value);
            ps.setNull(5, Types.NUMERIC);
        }
        ps.setString(6, origin);
        ps.setBigDecimal(7, change.delta);
        ps.setBigDecimal(8, change.delta);
        ps.setBigDecimal(9, change.delta);
    }

    /** {@code base64(name) TAB version TAB kind TAB payload}: d = deleted, n = number, b = type:base64, f = fetch it. */
    private static String line(String name, long version, Change change, @Nullable BigDecimal stored) {
        String head = b64(name.getBytes(StandardCharsets.UTF_8)) + "\t" + version + "\t";
        if (change.number != null) return head + "n\t" + (stored == null ? change.number : stored).toPlainString();
        if (change.value == null) return head + "d\t-";
        if (change.value.length > MAX_INLINE_BYTES) return head + "f\t-";
        return head + "b\t" + change.type + ":" + b64(change.value);
    }

    // ── Remote changes ──────────────────────────────────────────────────────────────────────────

    private void onMessage(String message) {
        String[] lines = message.split("\n");
        if (lines.length < 2 || lines[0].equals(origin)) return;
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split("\t", 4);
            if (parts.length < 4) continue;
            String name = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            long version = Long.parseLong(parts[1]);
            if (!isNewer(name, version)) continue;
            switch (parts[2]) {
                case "n" -> schedule(new Row(name, "number", null, new BigDecimal(parts[3]), version, null));
                case "d" -> schedule(new Row(name, null, null, null, version, null));
                case "b" -> {
                    int colon = parts[3].indexOf(':');
                    schedule(new Row(name, parts[3].substring(0, colon), Base64.getDecoder().decode(parts[3].substring(colon + 1)), null, version, null));
                }
                case "f" -> executor.execute(() -> fetch(name));
                default -> {
                }
            }
        }
    }

    private boolean isNewer(String name, long version) {
        boolean[] newer = {false};
        versions.compute(name, (key, known) -> {
            if (known != null && known >= version) return known;
            newer[0] = true;
            return version;
        });
        return newer[0];
    }

    private void fetch(String name) {
        try {
            List<Row> rows = sql.query(SELECT_ONE, ps -> {
                ps.setString(1, namespace);
                ps.setString(2, name);
            }, NetworkVariables::readRows);
            rows.forEach(this::schedule);
        } catch (SQLException e) {
            logError("Couldn't fetch {" + name + "}", e);
        }
    }

    private void reconcileQuietly() {
        try {
            Timestamp since = lastSeen;
            Timestamp from = since == null ? new Timestamp(0) : new Timestamp(since.getTime() - 5_000);
            List<Row> rows = sql.query(SELECT_CHANGED, ps -> {
                ps.setString(1, namespace);
                ps.setTimestamp(2, from);
            }, NetworkVariables::readRows);
            for (Row row : rows) {
                if (since == null || row.updatedAt.after(since)) lastSeen = row.updatedAt;
                if (isNewer(row.name, row.version)) schedule(row);
            }
            long now = System.currentTimeMillis();
            if (now - lastGc > TimeUnit.HOURS.toMillis(1)) {
                lastGc = now;
                sql.update(GC_TOMBSTONES, ps -> ps.setString(1, namespace));
            }
        } catch (Throwable t) {
            logError("Couldn't check for missed {net::*} changes", t);
        }
    }

    private void schedule(Row row) {
        Bukkit.getScheduler().runTask(OFASkriptAddon.instance(), () -> apply(row));
    }

    /** Puts a stored value into Skript's memory. Primary thread (or boot, before scripts run). */
    private void apply(Row row) {
        Object value;
        Object echo;
        if (row.num != null) {
            value = row.num.stripTrailingZeros().scale() <= 0 ? (Object) row.num.longValue() : (Object) row.num.doubleValue();
            echo = row.num;
        } else if (row.value != null && row.type != null) {
            value = Classes.deserialize(row.type, row.value);
            if (value == null) {
                LOG.warning("[OFA-SK] Couldn't read {" + row.name + "} (type " + row.type + "); is the addon that defines it installed?");
                return;
            }
            echo = new Blob(row.type, row.value);
        } else {
            value = null;
            echo = TOMBSTONE;
        }
        Deque<Object> echoes = expected.computeIfAbsent(row.name, key -> new ArrayDeque<>());
        synchronized (echoes) {
            echoes.addLast(echo);
        }
        Variables.setVariable(row.name, value, null, false);
    }

    // ── Shutdown ────────────────────────────────────────────────────────────────────────────────

    void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            flush();
        } catch (Exception e) {
            synchronized (pending) {
                LOG.log(Level.SEVERE, "[OFA-SK] {net::*} changes not saved on shutdown: " + pending.keySet(), e);
            }
        }
        if (subscription != null) subscription.close();
        instance = null;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────────────────

    private void logError(String message, Throwable t) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLog < 30_000) return;
        lastErrorLog = now;
        LOG.log(Level.WARNING, "[OFA-SK] " + message, t);
    }

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static List<Row> readRows(ResultSet rs) throws SQLException {
        List<Row> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new Row(rs.getString(1), rs.getString(2), rs.getBytes(3), rs.getBigDecimal(4), rs.getLong(5), rs.getTimestamp(6)));
        }
        return rows;
    }

    private record Row(String name, @Nullable String type, byte @Nullable [] value, @Nullable BigDecimal num,
                       long version, @Nullable Timestamp updatedAt) {
    }

    private record Blob(String type, byte[] value) {
    }

    /** A pending write. {@code delta} non-null means "add this to the stored number". */
    private record Change(@Nullable String type, byte @Nullable [] value,
                          @Nullable BigDecimal number, @Nullable BigDecimal delta) {

        static Change delete() {
            return new Change(null, null, null, null);
        }

        static Change blob(@Nullable String type, byte[] value) {
            return new Change(type, value, null, null);
        }

        static Change number(BigDecimal value, @Nullable BigDecimal delta) {
            return new Change(null, null, value, delta);
        }

        /** This change followed by {@code next}, as one write. */
        Change then(Change next) {
            if (next.delta != null && delta != null) return number(next.number, delta.add(next.delta));
            if (next.delta != null && number != null) return number(next.number, null); // absolute set, then adds
            return next;
        }
    }
}
