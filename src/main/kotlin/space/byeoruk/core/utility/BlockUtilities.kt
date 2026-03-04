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
    fun Block.isLog(): Boolean =
        Tag.LOGS.isTagged(this.type)

    /**
     * 블록이 광석인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 광석이면 true 아니면 false
     */
    fun Block.isOre(): Boolean =
        this.type.name.endsWith("_ORE") || this.type == Material.ANCIENT_DEBRIS

    /**
     * 블록이 원목이거나 광석인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 원목이거나 광석이면 true 아니면 false
     */
    fun Block.isOreOrLog(): Boolean = this.isLog() || this.isOre()

    /**
     * 블록이 최대 나이인지 유무
     *
     * @param block 블록 데이터
     * @return 블록이 최대 나이이면 true 아니면 false
     */
    fun Block.isMaxAge(): Boolean {
        //  열매랑 붙어있는 꼬다리일 경우에만 Max Age로 판단
        if(this.type == Material.PUMPKIN_STEM || this.type == Material.MELON_STEM)
            return false

        if(this.blockData is Ageable) {
            val ageable = this.blockData as Ageable
            return ageable.age >= ageable.maximumAge
        }

        return false
    }
}