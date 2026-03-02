package space.byeoruk.core.system.experience.listeners

import io.papermc.paper.event.player.PlayerTradeEvent
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.MerchantInventory
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.utility.NumberUtilities

class ExpDropListener(
    private val configManager: MainConfigManager): Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.keepLevel = true
        event.droppedExp = 0
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if(event.entity is Player)
            return

        event.droppedExp = 0
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) { event.expToDrop = 0 }

    @EventHandler
    fun onFurnaceExtract(event: FurnaceExtractEvent) { event.expToDrop = 0 }

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) { event.expToDrop = 0 }

    @EventHandler
    fun onEntityBreed(event: EntityBreedEvent) { event.experience = 0 }

    @EventHandler
    fun onPlayerTrade(event: PlayerTradeEvent) {
        event.setRewardExp(false)

        val player = event.player
        val topInventory = player.openInventory.topInventory

        if(topInventory is MerchantInventory) {
            val merchant = topInventory.merchant

            //  떠상이랑 거래할 때만 경험 구슬 드롭
            if(merchant is WanderingTrader) {
                merchant.world.spawn(merchant.location.clone().add(.5, 1.0, .5), ExperienceOrb::class.java) { orb ->
                    val dropExps = configManager.experienceConfig.tradeExpDropWanderingTrader.split(":")
                    orb.experience = NumberUtilities.getRandomInt(dropExps[0].toInt(), dropExps[1].toInt())
                }
            }
        }
    }
}