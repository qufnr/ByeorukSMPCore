package space.byeoruk.core.system.spawner.configs

class SpawnerConfigManager {
    var healthMultiplier: String = "1.2:1.5"
        private set

    var equipmentChance: Double = 40.0
        private set

    constructor(healthMultiplier: String, equipmentChance: Double) {
        this.healthMultiplier = healthMultiplier
        this.equipmentChance = equipmentChance
    }
}