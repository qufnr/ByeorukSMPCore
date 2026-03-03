package space.byeoruk.core.global.configs

import org.bukkit.plugin.java.JavaPlugin
import space.byeoruk.core.system.experience.configs.ExpConfigManager
import space.byeoruk.core.system.harvest.configs.HarvestConfigManager
import space.byeoruk.core.system.mining.config.MiningConfigManager
import space.byeoruk.core.system.spawner.configs.SpawnerConfigManager
import space.byeoruk.core.system.teleporting.configs.TeleportingConfigManager

class MainConfigManager(private val plugin: JavaPlugin) {
    val messagePrefix: String = "<color:grey><i>[*] "

    lateinit var miningConfig: MiningConfigManager
        private set

    //  Experience Configuration
    lateinit var experienceConfig: ExpConfigManager
        private set

    //  Teleporting Configuration
    lateinit var teleportingConfig: TeleportingConfigManager
        private set

    //  Spawner Configuration
    lateinit var spawnerConfig: SpawnerConfigManager

    //  Harvest Configuration
    lateinit var harvestConfig: HarvestConfigManager

    init { load() }

    fun load() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        val config = plugin.config

        //  Mining
        miningConfig = MiningConfigManager(
            maxBreakingBlocks = config.getInt("vein-mining.max-breaking-blocks", 64),
            maxBreakingLeaveBlocks = config.getInt("vein-mining.max-breaking-leave-blocks", 300),
            blockBreakingPerTick = config.getInt("vein-mining.block-breaking-per-tick", 2),
            blockBreakingDelayTicks = config.getLong("vein-mining.block-breaking-delay-ticks", 1L)
        )

        //  EXP / Time Rewards
        experienceConfig = ExpConfigManager(
            signReward = config.getInt("exp.sign-reward-amount", 300),
            intervalMinutes = config.getInt("exp.time-reward.interval-minutes", 10),
            maxPlayTimeRewardLimit = config.getInt("exp.time-reward.limit", 6),
            playTimeRewardNormalAmount = config.getInt("exp.time-reward.normal-amount", 20),
            playTimeRewardReducedAmount = config.getInt("exp.time-reward.reduced-amount", 10),
            tradeExpDropWanderingTrader = config.getString("exp.drop.trade.wandering-trader", "1:4") ?: "1:4"
        )

        //  Compass Teleport / TPA etc.
        teleportingConfig = TeleportingConfigManager(
            compassTeleportExpCost = config.getInt("teleporting.compass-teleport.exp-cost", 30),
            teleportDelay = config.getInt("teleporting.delay", 3)
        )

        //  Spawner Mob Modifier
        spawnerConfig = SpawnerConfigManager(
            healthMultiplier = config.getString("spawner.mob.health-multiplier", "1.2:1.5") ?: "1.2:1.5",
            equipmentChance =  config.getDouble("spawner.mob.equipment-chance", 30.0)
        )

        //  Harvest System Modifier
        harvestConfig = HarvestConfigManager(
            growBuffDuration = config.getInt("harvest.grow-buff.duration", 60),
            forceGrowChance = config.getDouble("harvest.grow-buff.force-grow-chance", 5.0),
            hayBlockDropWheatCountRange = config.getString("harvest.hay-block.wheat-drop-count", "1:2") ?: "1:2"
        )
    }
}