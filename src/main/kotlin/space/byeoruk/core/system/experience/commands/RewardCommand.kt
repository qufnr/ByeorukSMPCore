package space.byeoruk.core.system.experience.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.experience.managers.ExpPlayerDataManager
import space.byeoruk.core.utility.PlayerUtilities.addTotalExp

class RewardCommand(
    private val configManager: MainConfigManager,
    private val expPlayerDataManager: ExpPlayerDataManager): BasicCommand {
    /**
     * /reward 명령어 Execute
     *
     * @param commandSourceStack 명령어 소스 스택
     * @param args 매개변수
     */
    override fun execute(commandSourceStack: CommandSourceStack, args: Array<out String>) {
        val sender = commandSourceStack.sender

        if(sender !is Player)
            return

        val mm = MiniMessage.miniMessage()

        if(expPlayerDataManager.hasSignedToday(sender)) {
            sender.sendMessage(mm.deserialize("${configManager.messagePrefix} 이미 보상을 받았어요"))
            return
        }

        val reward = configManager.experienceConfig.signReward

        sender.addTotalExp(reward)
        expPlayerDataManager.setSignedToday(sender)

        sender.sendMessage(mm.deserialize("${configManager.messagePrefix} 출석 보상으로 경험치를 받았어요"))
    }
}