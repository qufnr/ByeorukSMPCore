package space.byeoruk.core.utility

import org.bukkit.inventory.ItemStack

object ItemUtilities {
    fun isAxe(item: ItemStack): Boolean =
        item.type.name.endsWith("_AXE")

    fun isPickaxe(item: ItemStack): Boolean =
        item.type.name.endsWith("_PICKAXE")
}