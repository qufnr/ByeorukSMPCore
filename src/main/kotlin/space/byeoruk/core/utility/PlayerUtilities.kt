package space.byeoruk.core.utility

import org.bukkit.entity.Player
import kotlin.math.roundToInt

object PlayerUtilities {
    /**
     * 플레이어 총 경험치 반환
     *
     * @param player 플레이어
     * @return 플레이어가 가지고 있는 총 경험치
     */
    fun getTotalExperience(player: Player): Int {
        val level = player.level
        val expProgress = (player.exp * player.expToLevel).roundToInt()

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
     * @param player 플레이어
     * @param amount 경험치 수
     */
    fun setTotalExperience(player: Player, amount: Int) {
        player.level = 0
        player.exp = 0f
        player.totalExperience = 0
        player.giveExp(amount)
    }

    /**
     * 플레이어에게 경험치 만큼 추가 지급
     *
     * @param player 플레이어
     * @param amount 추가할 경험치 수
     */
    fun addTotalExperience(player: Player, amount: Int) =
        setTotalExperience(player, getTotalExperience(player) + amount)

    /**
     * 플레이어에게 경험치 만큼 뺏기
     *
     * @param player 플레이어
     * @param amount 빼앗아갈 경험치 수
     */
    fun subtractTotalExperience(player: Player, amount: Int) =
        setTotalExperience(player, getTotalExperience(player) - amount)
}