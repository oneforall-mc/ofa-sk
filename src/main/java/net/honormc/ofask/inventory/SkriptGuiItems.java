package net.honormc.ofask.inventory;

import net.honormc.oneforall.inventory.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a Minestom {@link ItemStack} (the type behind Skript's own {@code Item}) into a
 * platform-free {@link GuiItem} — the inverse of {@code MinestomGuiItems.toItemStack} in
 * oneforall-minestom, which this pack doesn't depend on directly.
 */
final class SkriptGuiItems {

    private SkriptGuiItems() {
    }

    static GuiItem toGuiItem(ItemStack stack) {
        if (stack == null || stack.isAir()) {
            return GuiItem.EMPTY;
        }
        GuiItem.Builder builder = GuiItem.of(stack.material().key().asString()).amount(stack.amount());

        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            builder.name(LegacyComponentSerializer.legacyAmpersand().serialize(name));
        }

        List<Component> lore = stack.get(DataComponents.LORE);
        if (lore != null && !lore.isEmpty()) {
            List<String> lines = new ArrayList<>(lore.size());
            for (Component line : lore) {
                lines.add(LegacyComponentSerializer.legacyAmpersand().serialize(line));
            }
            builder.lore(lines);
        }

        Boolean glowing = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (glowing != null) {
            builder.glowing(glowing);
        }

        return builder.build();
    }
}
