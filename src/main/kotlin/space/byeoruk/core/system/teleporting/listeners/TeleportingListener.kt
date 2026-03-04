package space.byeoruk.core.system.teleporting.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import space.byeoruk.core.Main
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.teleporting.inventory.TeleportInventoryHolder
import space.byeoruk.core.system.teleporting.managers.TeleportingPlayerDataManager
import space.byeoruk.core.utility.PlayerUtilities
import space.byeoruk.core.utility.PlayerUtilities.getTotalExp
import space.byeoruk.core.utility.PlayerUtilities.subtractTotalExp

class TeleportingListener(
    private val plugin: Main,
    private val configManager: MainConfigManager,
    private val teleportingPlayerDataManager: TeleportingPlayerDataManager): Listener {
    /**
     * 인벤토리 클릭 이벤트 (GUI에서 아이템 클릭 처리)
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if(event.inventory.holder !is TeleportInventoryHolder)
            return

        event.isCancelled = true

        val player = event.whoClicked as? Player ?: return
        val clickedItem = event.currentItem ?: return
        val teleportLocation = teleportingPlayerDataManager.getPendingTeleportLocation(player) ?: return

        //  선택한 아이템이 초록색 색유리 판일 때 텔레포트 처리
        if(clickedItem.type == Material.GREEN_STAINED_GLASS_PANE) {
            player.closeInventory()
            teleportingPlayerDataManager.removePendingTeleportLocation(player)
            startTeleportWarmup(player, teleportLocation)
        }

        //  선택한 아이템이 빨간색 색유리 판일 때 닫기
        else if(clickedItem.type == Material.RED_STAINED_GLASS_PANE) {
            player.closeInventory()
        }
    }

    /**
     * 인벤토리 닫기 이벤트
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        if(event.inventory.holder is TeleportInventoryHolder)
            teleportingPlayerDataManager.removePendingTeleportLocation(player)
    }

    /**
     * 플레이어 이동 이벤트 (텔레포트 대기 중일 때 움직일 경우 취소 처리)
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        //  텔레포트 대기 중인 플레이어만 검사
        if(!teleportingPlayerDataManager.isTeleportReady(player))
            return

        val from = event.from
        val to = event.to

        val mm = MiniMessage.miniMessage()

        //  좌표만 이동했을 경우 텔레포트 취소
        if(from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ)
            cancelTeleport(player, mm.deserialize("${configManager.messagePrefix} 이동이 취소 되었습니다"))
    }

    /**
     * 플레이어 퇴장 이벤트 (텔레포트 대기 중일 때 나가면 대기 텔레포트 플레이어에서 제거)
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        teleportingPlayerDataManager.removePendingTeleportLocation(player)
        cancelTeleport(player, null)
    }

    /**
     * 텔레포트 시작
     *
     * @param player 플레이어
     * @param teleportLocation 텔레포트 위치
     */
    private fun startTeleportWarmup(player: Player, teleportLocation: Location) {
        val mm = MiniMessage.miniMessage()
        val expCost = configManager.teleportingConfig.compassTeleportExpCost
        val teleportDelay = configManager.teleportingConfig.teleportDelay

        if(!hasTeleportCost(player, expCost))
            return

        player.sendMessage(mm.deserialize("${configManager.messagePrefix} $teleportDelay 초 후 이동해요. 움직이지 마세요"))

        //  텔레포트 딜레이 태스크 생성
        val task = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            teleportingPlayerDataManager.removeTeleportWarmup(player)

            if(!hasTeleportCost(player, expCost))
                return@scheduleSyncDelayedTask

            player.subtractTotalExp(expCost)

            val endRodParticleLocation = teleportLocation.clone().add(.5, 1.0, .5)
            val electricSparkParticleLocation = teleportLocation.clone().add(.5, 1.2, .5)
            val safeLocation = teleportLocation.clone().add(.5, 1.0, .5)
            safeLocation.yaw = player.location.yaw
            safeLocation.pitch = player.location.pitch

            player.teleport(safeLocation)
            //  사운드 재생
            player.playSound(safeLocation, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f)
            player.playSound(safeLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)

            //  파티클 생성
            safeLocation.world?.let { world ->
                val center = safeLocation.clone().add(0.0, 1.0, 0.0)
                world.spawnParticle(Particle.END_ROD, center, 80, 0.3, 1.0, 0.3, 0.1)
                world.spawnParticle(Particle.DRAGON_BREATH, safeLocation.clone().add(0.0, 0.5, 0.0), 50, 0.5, 0.2, 0.5, 0.1, 1.0f)
            }

        }, 60L)

        teleportingPlayerDataManager.setTeleportWarmup(player, task)
    }

    /**
     * 텔레포트 취소 처리
     *
     * @param player 플레이어
     * @param message 메시지
     */
    private fun cancelTeleport(player: Player, message: Component?) {
        teleportingPlayerDataManager.removeTeleportWarmup(player).let {
            if(it != null) {
                Bukkit.getScheduler().cancelTask(it)
                if(message != null)
                    player.sendMessage(message)
            }
        }
    }

    /**
     * 플레이어가 텔레포트할 수 있는 경험치를 충분히 가지고 있는지 확인
     *
     * @param player 플레이어
     * @param expCost 경험치 비용
     * @return 플레이어가 텔레포트할 수 있는 경험치를 충분히 가지고 있다면 true 반환
     */
    private fun hasTeleportCost(player: Player, expCost: Int): Boolean {
        val mm = MiniMessage.miniMessage()
        if(player.getTotalExp() < expCost) {
            player.sendMessage(mm.deserialize("${configManager.messagePrefix} 경험치가 부족해요. (필요 경험치: $expCost EXP)"))
            return false
        }

        return true
    }
}