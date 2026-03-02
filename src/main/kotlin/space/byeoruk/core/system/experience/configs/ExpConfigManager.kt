package space.byeoruk.core.system.experience.configs

class ExpConfigManager {
    var signReward: Int = 300
        private set
    var intervalMinutes: Int = 10
        private set
    var maxPlayTimeRewardLimit: Int = 6
        private set
    var playTimeRewardNormalAmount: Int = 50
        private set
    var playTimeRewardReducedAmount: Int = 10
        private set

    var tradeExpDropWanderingTrader: String = "1:4"
        private set

    constructor(
        signReward: Int,
        intervalMinutes: Int,
        maxPlayTimeRewardLimit: Int,
        playTimeRewardNormalAmount: Int,
        playTimeRewardReducedAmount: Int,
        tradeExpDropWanderingTrader: String) {
        this.signReward = signReward
        this.intervalMinutes = intervalMinutes
        this.maxPlayTimeRewardLimit = maxPlayTimeRewardLimit
        this.playTimeRewardNormalAmount = playTimeRewardNormalAmount
        this.playTimeRewardReducedAmount = playTimeRewardReducedAmount
        this.tradeExpDropWanderingTrader = tradeExpDropWanderingTrader
    }
}