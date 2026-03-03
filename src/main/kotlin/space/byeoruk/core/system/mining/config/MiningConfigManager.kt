package space.byeoruk.core.system.mining.config

class MiningConfigManager {
    var maxBreakingBlocks: Int = 64         //  원목/광석 파괴 가능한 개수
        private set
    var maxBreakingLeaveBlocks: Int = 300   //  나뭇잎 블록 파괴 가능한 개수
        private set
    var blockBreakingPerTick: Int = 3       //  블록 파괴 개수
        private set
    var blockBreakingDelayTicks: Long = 1L  //  블록 파괴 딜레이
        private set

    constructor(maxBreakingBlocks: Int, maxBreakingLeaveBlocks: Int, blockBreakingPerTick: Int, blockBreakingDelayTicks: Long) {
        this.maxBreakingBlocks = maxBreakingBlocks
        this.maxBreakingLeaveBlocks = maxBreakingLeaveBlocks
        this.blockBreakingPerTick = blockBreakingPerTick
        this.blockBreakingDelayTicks = blockBreakingDelayTicks
    }
}