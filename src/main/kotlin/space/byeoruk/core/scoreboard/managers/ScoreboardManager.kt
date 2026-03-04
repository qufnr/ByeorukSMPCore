package space.byeoruk.core.scoreboard.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.experience.managers.ExpPlayerDataManager
import space.byeoruk.core.utility.NumberUtilities
import space.byeoruk.core.utility.PlayerUtilities.getTotalExp

class ScoreboardManager(
    val configManager: MainConfigManager,
    val expPlayerDataManager: ExpPlayerDataManager
) {
    fun run() {
        for(player in Bukkit.getOnlinePlayers()) {
            var board = player.scoreboard

            //  플레이어 개인 스코어보드 생성 (메인 스코어보드일 경우 겹침 방지)
            if(board == Bukkit.getScoreboardManager().mainScoreboard) {
                board = Bukkit.getScoreboardManager().newScoreboard
                player.scoreboard = board
            }

            val objectiveName = "bserver_core_board"
            var objective = board.getObjective(objectiveName)

            val mm = MiniMessage.miniMessage()

            if(objective == null) {
                val title = mm.deserialize("<gradient:#ec77ab:#7873f5>ByeorukSMP</gradient>")

                objective = board.registerNewObjective(objectiveName, Criteria.DUMMY, title)
                objective.displaySlot = DisplaySlot.SIDEBAR

                objective.getScore("§f ").score = 8
                objective.getScore("§e§l상태").score = 7
                objective.getScore("§1").score = 6
                objective.getScore("§f§f ").score = 5
                objective.getScore("§e§lEXP 보상").score = 4
                objective.getScore("§2").score = 3
                objective.getScore("§3").score = 2
                objective.getScore("§4").score = 1
                objective.getScore("§f§f§f ").score = 0

                registerTeam(board, "exp_value", "§1", mm.deserialize("<color:#C8C8C8>|</color> <reset>EXP: "))
                registerTeam(board, "exp_play_time", "§2", mm.deserialize("<color:#C8C8C8>|</color> <reset>다음 보상 까지: "))
                registerTeam(board, "exp_reward", "§3", mm.deserialize("<color:#C8C8C8>|</color> <reset>보상 EXP: "))
                registerTeam(board, "exp_reward_status", "§4", mm.deserialize("<color:#C8C8C8>|</color> <reset>보상 현황: "))
            }

            //  플레이어의 플레이 타임
            val playTime = expPlayerDataManager.getPlayTime(player)
            val totalPlayTime = (configManager.experienceConfig.intervalMinutes * 60) - playTime
            //  플레이어의 받은 경험치 보상 횟수
            val currentPlayTimeRewardCount = expPlayerDataManager.getTodayTimeRewardCount(player)
            //  최대 받을 수 있는 경험치 보상 횟수
            val maxPlayTimeRewardCount = configManager.experienceConfig.maxPlayTimeRewardLimit
            //  플레이어의 다음 경험치 보상
            val nextReward =
                if(currentPlayTimeRewardCount < maxPlayTimeRewardCount)
                    configManager.experienceConfig.playTimeRewardNormalAmount
                else
                    configManager.experienceConfig.playTimeRewardReducedAmount

            updateTeam(board, "exp_value", mm.deserialize("<color:#AAD34A>${player.getTotalExp()} (${player.level})"))
            updateTeam(board, "exp_play_time", mm.deserialize("<color:#AAD34A>${NumberUtilities.formatSeconds(totalPlayTime)}"))
            updateTeam(board, "exp_reward", mm.deserialize("<color:#AAD34A>+$nextReward EXP"))
            updateTeam(board, "exp_reward_status", mm.deserialize("<color:#AAD34A>$currentPlayTimeRewardCount / $maxPlayTimeRewardCount"))
        }
    }

    /**
     * 스코어보드 팀 등록
     *
     * @param board 스코어보드
     * @param teamName 팀 이름
     * @param entry 엔트리 문자열
     * @param defaultPrefix Prefix
     */
    private fun registerTeam(board: Scoreboard, teamName: String, entry: String, defaultPrefix: Component) {
        var team = board.getTeam(teamName)
        if(team == null) {
            team = board.registerNewTeam(teamName)
            team.addEntry(entry)
        }
        team.prefix(defaultPrefix)
        team.suffix(Component.empty())
    }

    /**
     * 스코어보드 팀 업데이트
     *
     * @param board 스코어보드
     * @param teamName 팀 이름
     * @param suffix suffix
     */
    private fun updateTeam(board: Scoreboard, teamName: String, suffix: Component)
        = board.getTeam(teamName)?.suffix(suffix)
}