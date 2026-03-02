package space.byeoruk.core.system.teleporting.configs

class TeleportingConfigManager {
    var compassTeleportExpCost: Int = 100
        private set
    var teleportDelay: Int = 3
        private set

    constructor(compassTeleportExpCost: Int, teleportDelay: Int) {
        this.compassTeleportExpCost = compassTeleportExpCost
        this.teleportDelay = teleportDelay
    }
}