package net.honormc.ofask.inventory;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import net.honormc.oneforall.inventory.OFAInventory;

/**
 * Registers the Skript {@link ClassInfo} for {@link OFAInventory} — a live, in-memory
 * handle only, the same shape skript-minestom's own {@code Player}/{@code Sidebar}
 * classes use for objects that only ever come from another expression, never from parsed
 * text: no {@link ch.njol.skript.classes.Serializer}, so {@code {_menu}} holds the real
 * object for the lifetime of the running script (menus don't need to survive a restart).
 */
public final class OFAInventoryClasses {

    private OFAInventoryClasses() {
    }

    public static void register() {
        Classes.registerClass(new ClassInfo<>(OFAInventory.class, "ofainventory")
                .user("ofa ?(inventor(y|ies)|menus?)")
                .name("OFA Inventory")
                .description("A oneforall-api menu created with 'a [bordered] <n> row ofa menu named <text>'.")
                .examples("set {_menu} to a bordered 6 row ofa menu named \"&8Bounties\"")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(OFAInventory o, int flags) {
                        return toVariableNameString(o);
                    }

                    @Override
                    public String toVariableNameString(OFAInventory o) {
                        return "ofa inventory titled \"" + o.title() + "\"";
                    }
                }));
    }
}
