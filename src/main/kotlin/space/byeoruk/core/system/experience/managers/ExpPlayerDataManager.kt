package space.byeoruk.core.system.experience.managers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import space.byeoruk.core.Main
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ExpPlayerDataManager(plugin: Main) {
    //  PDC 저장을 위해 NamespacedKey 정의
    private val lastSignKey = NamespacedKey(plugin, "last_signed_at")
    private val timeRewardDateKey = NamespacedKey(plugin, "time_reward_date")
    private val timeRewardCountKey = NamespacedKey(plugin, "time_reward_count")

    private val playTimeKey = NamespacedKey(plugin, "play_time")

    /**
     * 플레이어가 오늘 로그인 보너스를 받았는지 여부
     *
     * @param player 플레이어
     * @return 오늘 로그인 보너스를 받았으면 true 아니면 false
     */
    fun hasSignedToday(player: Player): Boolean {
        val pdc = player.persistentDataContainer
        val lastSign = pdc.get(lastSignKey, PersistentDataType.STRING)
        return lastSign == getTodayString()
    }

    /**
     * 로그인 보너스 처리
     *
     * @param player 플레이어
     */
    fun setSignedToday(player: Player) = player.persistentDataContainer.set(lastSignKey, PersistentDataType.STRING, getTodayString())

    /**
     * 플레이어가 받은 보상 횟수 반환
     *
     * @param player 플레이어
     * @return 보상 횟수
     */
    fun getTodayTimeRewardCount(player: Player): Int {
        val pdc = player.persistentDataContainer
        val lastDate = pdc.get(timeRewardDateKey, PersistentDataType.STRING) ?: ""

        if(lastDate != getTodayString())
            return 0

        return pdc.get(timeRewardCountKey, PersistentDataType.INTEGER) ?: 0
    }

    /**
     * 플레이어 보상 횟수 1 추가
     *
     * @param player 플레이어
     */
    fun addTimeRewardCount(player: Player) {
        val pdc = player.persistentDataContainer
        val count = getTodayTimeRewardCount(player)

        pdc.set(timeRewardDateKey, PersistentDataType.STRING, getTodayString())
        pdc.set(timeRewardCountKey, PersistentDataType.INTEGER, count + 1)
    }

    /**
     * 플레이어 플레이 타입 반환
     *
     * @return 플레이 타임
     */
    fun getPlayTime(player: Player): Int =
        player.persistentDataContainer.get(playTimeKey, PersistentDataType.INTEGER) ?: 0

    /**
     * 플레이어 플레이 타임 설정
     *
     * @param seconds 설정할 플레이 타임 (초 단위)
     */
    fun setPlayTime(player: Player, seconds: Int) =
        player.persistentDataContainer.set(playTimeKey, PersistentDataType.INTEGER, seconds)

    /**
     * 한국 시간 새벽 5시 문자열
     *
     * @return 날짜 문자열
     */
    private fun getTodayString(): String {
        val today: LocalDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(5).toLocalDate()
        return today.toString()
    }
}