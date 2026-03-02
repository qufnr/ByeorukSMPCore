package space.byeoruk.core.system.teleporting.managers

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import space.byeoruk.core.Main
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.teleporting.inventory.TeleportInventoryHolder

class CompassTeleportManager(
    private val plugin: Main,
    private val configManager: MainConfigManager,
    private val teleportingPlayerDataManager: TeleportingPlayerDataManager,
) {
    /**
     * 자석석 이동 텔레포트 GUI 열기
     *
     * @param player 플레이어
     * @param location 자석석 위치
     */
    fun openTeleportInventory(player: Player, location: Location) {
        val inventoryHolder = TeleportInventoryHolder(plugin, "자석석으로 이동할까요?", configManager.teleportingConfig.compassTeleportExpCost, location)

        teleportingPlayerDataManager.setPendingTeleportLocation(player, location)
        player.openInventory(inventoryHolder.inventory)
    }
}