package space.byeoruk.core.utility

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable

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

    /**
     * 블록이 최대 나이인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 최대 나이이면 true 아니면 false
     */
    fun isMaxAge(block: Block): Boolean {
        //  열매랑 붙어있는 꼬다리일 경우에만 Max Age로 판단
        if(block.type == Material.PUMPKIN_STEM || block.type == Material.MELON_STEM)
            return false

        if(block.blockData is Ageable) {
            val ageable = block.blockData as Ageable
            return ageable.age >= ageable.maximumAge
        }

        return false
    }
}