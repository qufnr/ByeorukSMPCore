package space.byeoruk.core.system.mining.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import space.byeoruk.core.system.mining.managers.BlockDataManager
import space.byeoruk.core.system.mining.managers.VeinMiningManager
import space.byeoruk.core.utility.BlockUtilities
import space.byeoruk.core.utility.ItemUtilities

class VeinMiningListener(private val blockDataManager: BlockDataManager, private val veinMiningManager: VeinMiningManager): Listener {

    /**
     * 블록 설치 시 블록 마킹
     *
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        event.block.let {
            if(BlockUtilities.isLog(it) || BlockUtilities.isOre(it))
                blockDataManager.markAsPlaced(it)
        }
    }

    /**
     * 블록 파괴 시 마킹 해제
     *
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreakRemoveData(event: BlockBreakEvent) {
        blockDataManager.unmarkPlaced(event.block)
    }

    /**
     * 블록, 아이템 검증 후 베인마이닝 처리
     *
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if(veinMiningManager.isMining(player))
            return

        val block = event.block
        val item = player.inventory.itemInMainHand

        val isLog = BlockUtilities.isLog(block)
        val isAxe = ItemUtilities.isAxe(item)

        val isOre = BlockUtilities.isOre(block)
        val isPickaxe = ItemUtilities.isPickaxe(item)

        if((isLog && isAxe) || (isOre && isPickaxe)) {
            if(blockDataManager.isPlacedByPlayer(block))
                return

            veinMiningManager.executeVeinMining(player, block, isLog, isOre)
        }
    }
}