package space.byeoruk.core.system.harvest.configs

class HarvestConfigManager {
    var growBuffDuration: Int = 60
        private set

    var forceGrowChance: Double = 5.0
        private set

    var hayBlockDropWheatCountRange: String = "1:2"
        private set

    constructor(growBuffDuration: Int, forceGrowChance: Double, hayBlockDropWheatCountRange: String) {
        this.growBuffDuration = growBuffDuration
        this.forceGrowChance = forceGrowChance
        this.hayBlockDropWheatCountRange = hayBlockDropWheatCountRange
    }
}