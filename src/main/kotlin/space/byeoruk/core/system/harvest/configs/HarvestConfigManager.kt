package space.byeoruk.core.system.harvest.configs

class HarvestConfigManager {
    var growBuffDuration: Int = 60
        private set
    var forceGrowChance: Double = 5.0
        private set
    var hayBlockDropWheatCountRange: String = "1:2"
        private set
    var pumpkinStemDropSeedCountRange: String = "0:1"
        private set
    var melonStemDropSeedCountRange: String = "0:1"
        private set
    var attachedPumpkinStemDropSeedCountRange: String = "1:3"
        private set
    var attachedMelonStemDropSeedCountRange: String = "1:3"
        private set

    constructor(
        growBuffDuration: Int,
        forceGrowChance: Double,
        hayBlockDropWheatCountRange: String,
        pumpkinStemDropSeedCountRange: String,
        melonStemDropSeedCountRange: String,
        attachedPumpkinStemDropSeedCountRange: String,
        attachedMelonStemDropSeedCountRange: String,
    ) {
        this.growBuffDuration = growBuffDuration
        this.forceGrowChance = forceGrowChance
        this.hayBlockDropWheatCountRange = hayBlockDropWheatCountRange
        this.pumpkinStemDropSeedCountRange = pumpkinStemDropSeedCountRange
        this.melonStemDropSeedCountRange = melonStemDropSeedCountRange
        this.attachedPumpkinStemDropSeedCountRange = attachedPumpkinStemDropSeedCountRange
        this.attachedMelonStemDropSeedCountRange = attachedMelonStemDropSeedCountRange
    }
}