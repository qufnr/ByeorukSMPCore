package space.byeoruk.core.utility

import org.bukkit.entity.Player
import kotlin.math.roundToInt

object PlayerUtilities {
    /**
     * 플레이어 총 경험치 반환
     *
     * @return 플레이어가 가지고 있는 총 경험치
     */
    fun Player.getTotalExp(): Int {
        val level = this.level
        val expProgress = (this.exp * this.expToLevel).roundToInt()

        val totalExpFromLevels = when {
            level <= 16 -> level * level + 6 * level
            level <= 31 -> (2.5 * level * level - 40.5 * level + 360).toInt()
            else -> (4.5 * level * level - 162.5 * level + 2220).toInt()
        }

        return totalExpFromLevels + expProgress
    }

    /**
     * 플레이어에게 경험치 만큼 지급
     *
     * @param amount 경험치 수
     */
    fun Player.setTotalExp(amount: Int) {
        this.level = 0
        this.exp = 0f
        this.totalExperience = 0
        this.giveExp(amount)
    }

    /**
     * 플레이어에게 경험치 만큼 추가 지급
     *
     * @param amount 추가할 경험치 수
     */
    fun Player.addTotalExp(amount: Int) {
        this.setTotalExp(this.getTotalExp() + amount)
    }

    /**
     * 플레이어에게 경험치 만큼 뺏기
     *
     * @param amount 빼앗아갈 경험치 수
     */
    fun Player.subtractTotalExp(amount: Int) {
        var value = this.getTotalExp() - amount
        if(value < 0)
            value = 0
        this.setTotalExp(value)
    }
}