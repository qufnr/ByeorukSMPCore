package space.byeoruk.core.system.teleporting.listeners

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.CompassMeta
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.teleporting.managers.CompassTeleportManager
import space.byeoruk.core.system.teleporting.managers.TeleportingPlayerDataManager

class CompassTeleportListener(
    private val configManager: MainConfigManager,
    private val teleportingPlayerDataManager: TeleportingPlayerDataManager,
    private val compassTeleportManager: CompassTeleportManager
): Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action

        val item = event.item ?: return

        //  허공 또는 블록에 상호작용 중인지 확인
        if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
            return

        //  손에 들고 있는 게 나침반인지 확인
        if(item.type != Material.COMPASS)
            return

        val meta = item.itemMeta as? CompassMeta ?: return

        //  나침반이 자석석과 연결되어 있는지 확인
        if(!meta.hasLodestone() || meta.lodestone == null)
            return

        //  상호작용한 블록이 자석석일 경우 무시
        if(action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock?.type == Material.LODESTONE)
            return

        event.isCancelled = true

        val mm = MiniMessage.miniMessage()

        //  이미 대기 중이면 메시지 띄우기
        if(teleportingPlayerDataManager.isTeleportReady(player)) {
            player.sendMessage(mm.deserialize("${configManager.messagePrefix} 이동 중이에요. 잠시만 기다려 주세요"))
            return
        }

        //  인벤토리 GUI 열기
        compassTeleportManager.openTeleportInventory(player, meta.lodestone!!)
    }
}