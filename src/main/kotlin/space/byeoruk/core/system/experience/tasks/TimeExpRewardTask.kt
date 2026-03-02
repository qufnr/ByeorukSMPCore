package space.byeoruk.core.system.experience.tasks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.experience.managers.ExpPlayerDataManager

class TimeExpRewardTask(
    private val configManager: MainConfigManager,
    private val expPlayerDataManager: ExpPlayerDataManager) {
    fun run() {
        //  분 * 60
        val requiredPlayTime = configManager.experienceConfig.intervalMinutes * 60

        for(player in Bukkit.getOnlinePlayers()) {
            var playTime = expPlayerDataManager.getPlayTime(player)
            playTime ++

            //  현재 플레이 타임 카운트
            val currentRewardCount = expPlayerDataManager.getTodayTimeRewardCount(player)
            val expectedReward =
                if(currentRewardCount < configManager.experienceConfig.maxPlayTimeRewardLimit)
                    configManager.experienceConfig.playTimeRewardNormalAmount
                else
                    configManager.experienceConfig.playTimeRewardReducedAmount

            //  플레이어의 플레이 시간이 경험치 보상을 받을 수 있는 플레이 시간 만큼 지났을 경우
            if(playTime >= requiredPlayTime) {
                player.giveExp(expectedReward)
                expPlayerDataManager.addTimeRewardCount(player)
                expPlayerDataManager.setPlayTime(player, 0)

                if(currentRewardCount < configManager.experienceConfig.maxPlayTimeRewardLimit)
                    player.sendActionBar(Component.text("+${configManager.experienceConfig.playTimeRewardNormalAmount} EXP", TextColor.color(170, 211, 74)))
                else
                    player.sendActionBar(Component.text("+${configManager.experienceConfig.playTimeRewardReducedAmount} EXP (일일 획득량 감소)", TextColor.color(170, 211, 74)))
            }
            else
                expPlayerDataManager.setPlayTime(player, playTime)
        }
    }
}