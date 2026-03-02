package space.byeoruk.core

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.scoreboard.managers.ScoreboardManager
import space.byeoruk.core.system.experience.commands.RewardCommand
import space.byeoruk.core.system.experience.listeners.ExpDropListener
import space.byeoruk.core.system.experience.managers.ExpPlayerDataManager
import space.byeoruk.core.system.experience.tasks.TimeExpRewardTask
import space.byeoruk.core.system.harvest.listeners.HarvestListener
import space.byeoruk.core.system.harvest.managers.HarvestManager
import space.byeoruk.core.system.spawner.listeners.SpawnerListener
import space.byeoruk.core.system.spawner.managers.SpawnerManager
import space.byeoruk.core.system.teleporting.listeners.CompassTeleportListener
import space.byeoruk.core.system.teleporting.listeners.TeleportingListener
import space.byeoruk.core.system.teleporting.managers.CompassTeleportManager
import space.byeoruk.core.system.teleporting.managers.TeleportingPlayerDataManager
import space.byeoruk.core.system.veinMining.listeners.VeinMiningListener
import space.byeoruk.core.system.veinMining.managers.BlockDataManager
import space.byeoruk.core.system.veinMining.managers.VeinMiningManager

class Main: JavaPlugin() {
    private lateinit var configManager: MainConfigManager
    private lateinit var scoreboardManager: ScoreboardManager

    private lateinit var blockDataManager: BlockDataManager
    private lateinit var vienMiningManager: VeinMiningManager

    private lateinit var expPlayerDataManager: ExpPlayerDataManager
    private lateinit var timeExpRewardTask: TimeExpRewardTask

    private lateinit var teleportingPlayerDataManager: TeleportingPlayerDataManager
    private lateinit var compassTeleportManager: CompassTeleportManager

    private lateinit var spawnerManager: SpawnerManager

    private lateinit var harvestManager: HarvestManager

    override fun onEnable() {
        //  전역 Config 매니저 초기화
        configManager = MainConfigManager(this)

        //
        //  Vien Mining
        //

        //  Vein Mining :: 매니저 초기화
        blockDataManager = BlockDataManager(this)
        vienMiningManager = VeinMiningManager(this, configManager, blockDataManager)
        //  Vien Mining :: 리스너 등록
        val vienMiningListener = VeinMiningListener(blockDataManager, vienMiningManager)
        server.pluginManager.registerEvents(vienMiningListener, this)

        //
        //  Experience
        //

        //  Experience :: 매니저 초기화
        expPlayerDataManager = ExpPlayerDataManager(this)
        //  Experience :: 리스너 등록
        server.pluginManager.registerEvents(ExpDropListener(configManager), this)
        //  Experience :: 명령어 등록
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            registrar.register("reward", "출석 경험치 보상을 받아요", RewardCommand(configManager, expPlayerDataManager))
        }
        //  Experience :: 스케줄러 등록
        timeExpRewardTask = TimeExpRewardTask(configManager, expPlayerDataManager)

        //
        //  Teleporting
        //

        //  Teleporting :: 매니저 초기화
        teleportingPlayerDataManager = TeleportingPlayerDataManager()
        compassTeleportManager = CompassTeleportManager(this, configManager, teleportingPlayerDataManager)
        //  Teleporting :: 리스너 등록
        server.pluginManager.registerEvents(TeleportingListener(this, configManager, teleportingPlayerDataManager), this)
        server.pluginManager.registerEvents(CompassTeleportListener(configManager, teleportingPlayerDataManager, compassTeleportManager), this)

        //
        //  Spawner
        //

        //  Spawner :: 매니저 초기화
        spawnerManager = SpawnerManager(this)
        //  Spawner :: 리스너 등록
        server.pluginManager.registerEvents(SpawnerListener(configManager, spawnerManager), this)

        //
        //  Harvest
        //

        //  Harvest :: 매니저 초기화
        harvestManager = HarvestManager(configManager)
        //  Harvest :: 리스너 등록
        server.pluginManager.registerEvents(HarvestListener(this, configManager, harvestManager), this)
        //  Harvest :: 태스크 생성
        harvestManager.startTask(this)

        //  스코어보드 매니저 초기화
        scoreboardManager = ScoreboardManager(configManager, expPlayerDataManager)

        //  1초에 한 번씩 호출하는 스케줄러 생성
        object: BukkitRunnable() {
            override fun run() {
                timeExpRewardTask.run()
                scoreboardManager.run()
            }
        }.runTaskTimer(this, 0, 20L)

        logger.info { "BServer Core가 활성화 되었어요" }
    }

    override fun onDisable() {
        //  Harvest :: 리로드/종료 시 디스플레이 제거
        harvestManager.clearAll()
    }
}
