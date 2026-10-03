package net.honormc.ofask.variables;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.VariablesStorage;
import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 * Skript's side of {@code {net::*}}: a {@link VariablesStorage} that hands every load and save to
 * {@link NetworkVariables}. Skript constructs it reflectively from the database section
 * {@link NetworkVariables#injectDatabase()} adds, so users never edit {@code config.sk}.
 */
public final class NetworkVariableStorage extends VariablesStorage {

    public NetworkVariableStorage(String type) {
        super(type);
    }

    @Override
    protected boolean load_i(SectionNode n) {
        return NetworkVariables.get().loadRows();
    }

    @Override
    protected void allLoaded() {
        NetworkVariables.get().applyLoadedRows();
    }

    @Override
    protected boolean requiresFile() {
        return false;
    }

    @Override
    protected @Nullable File getFile(String fileName) {
        return null;
    }

    @Override
    protected boolean connect() {
        return true;
    }

    @Override
    protected void disconnect() {
    }

    @Override
    protected boolean save(String name, @Nullable String type, byte @Nullable [] value) {
        NetworkVariables.get().onSave(name, type, value);
        return true;
    }

    @Override
    public void close() {
        super.close(); // drains Skript's queue into onSave first
        NetworkVariables.get().shutdown();
    }
}
