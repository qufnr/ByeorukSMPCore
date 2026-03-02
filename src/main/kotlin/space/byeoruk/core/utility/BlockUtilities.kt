package space.byeoruk.core.utility

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block

object BlockUtilities {
    /**
     * 블록이 원목인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 원목이면 true 아니면 false
     */
    fun isLog(block: Block): Boolean =
        Tag.LOGS.isTagged(block.type)

    /**
     * 블록이 광석인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 광석이면 true 아니면 false
     */
    fun isOre(block: Block): Boolean =
        block.type.name.endsWith("_ORE") || block.type == Material.ANCIENT_DEBRIS

    /**
     * 블록이 원목이거나 광석인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 원목이거나 광석이면 true 아니면 false
     */
    fun isOreOrLog(block: Block): Boolean = isLog(block) || isOre(block)
}