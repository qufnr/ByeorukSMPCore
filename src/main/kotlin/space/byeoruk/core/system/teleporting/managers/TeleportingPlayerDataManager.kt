package space.byeoruk.core.system.teleporting.managers

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class TeleportingPlayerDataManager {
    //  GUI 열렸을 때 목적지를 임시 저장하는 Map
    private val pendingTeleportLocations = mutableMapOf<UUID, Location>()
    //  텔레포트 대기 중인 플레이어와 해당 스케줄러 태스크 ID를 저장하는 Map
    private val teleportWarmupPlayers = mutableMapOf<UUID, Int>()

    /**
     * 텔레포트 대기 중인지 여부
     *
     * @param player 플레이어
     * @return 텔레포트 대기 중일 경우 true 반환
     */
    fun isTeleportReady(player: Player): Boolean = teleportWarmupPlayers.containsKey(player.uniqueId)

    /**
     * 텔레포트 대기 Map에서 플레이어 제거
     *
     * @param player 플레이어
     */
    fun removeTeleportWarmup(player: Player) =
        teleportWarmupPlayers.remove(player.uniqueId)

    /**
     * 텔레포트 대기 Map 에 플레이어와 태스크 ID 설정
     *
     * @param player 플레이어
     * @param taskId 태스크 ID
     */
    fun setTeleportWarmup(player: Player, taskId: Int) {
        teleportWarmupPlayers[player.uniqueId] = taskId
    }

    fun getPendingTeleportLocation(player: Player): Location? = pendingTeleportLocations[player.uniqueId]

    fun setPendingTeleportLocation(player: Player, location: Location) {
        pendingTeleportLocations[player.uniqueId] = location
    }

    fun removePendingTeleportLocation(player: Player) {
        pendingTeleportLocations.remove(player.uniqueId)
    }
}