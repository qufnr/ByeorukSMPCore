package space.byeoruk.core.utility

import org.bukkit.inventory.ItemStack

object ItemUtilities {
    /**
     * 아이템이 도끼인지 유무
     *
     * @return 도끼일 경우 true 아니면 false 반환
     */
    fun ItemStack.isAxe(): Boolean =
        this.type.name.endsWith("_AXE")

    /**
     * 아이템이 곡괭이인지 유무
     *
     * @return 곡괭이일 경우 true 아니면 false 반환
     */
    fun ItemStack.isPickaxe(): Boolean =
        this.type.name.endsWith("_PICKAXE")
}