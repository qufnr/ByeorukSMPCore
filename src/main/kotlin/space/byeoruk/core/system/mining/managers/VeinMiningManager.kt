package space.byeoruk.core.system.mining.managers

import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.utility.ItemUtilities
import java.util.LinkedList
import java.util.UUID
import kotlin.random.Random

class VeinMiningManager(
    private val plugin: JavaPlugin,
    private val configManager: MainConfigManager,
    private val blockDataManager: BlockDataManager,
) {
    private val activePlayers = mutableSetOf<UUID>()

    fun isMining(player: Player): Boolean = activePlayers.contains(player.uniqueId)

    fun executeVeinMining(player: Player, startBlock: Block, isLog: Boolean, isOre: Boolean) {
        activePlayers.add(player.uniqueId)

        val connectedBlocks = getConnectedBlocks(startBlock, configManager.miningConfig.maxBreakingBlocks)
        val connectedLeaveBlocks = if(isLog) getConnectedLeaveBlocks(connectedBlocks, configManager.miningConfig.maxBreakingLeaveBlocks) else emptySet()

        val breakBlockQueue = LinkedList<Block>()
        breakBlockQueue.addAll(connectedBlocks.filter { it != startBlock })
        breakBlockQueue.addAll(connectedLeaveBlocks)

        object : BukkitRunnable() {
            override fun run() {
                for(i in 0 until configManager.miningConfig.blockBreakingPerTick) {
                    //  대기열이 비었거나, 플레이어가 없으면 작업 종료
                    if(breakBlockQueue.isEmpty() || !player.isOnline) {
                        activePlayers.remove(player.uniqueId)
                        cancel()
                        return
                    }

                    //  파괴할 블록 타겟 큐에서 꺼내기
                    val target = breakBlockQueue.poll()

                    //  블록이 사라졌으면 다음 블록으로 넘어감
                    if(target.type.isAir)
                        continue

                    val targetLocation = target.location.add(.5, .5, .5)

                    if(Tag.LEAVES.isTagged(target.type)) {
                        //  파티클 생성
                        target.world.spawnParticle(Particle.BLOCK, targetLocation, 10, .3, .3, .3, .0, target.blockData)
                        //  소리 재생
                        val pitch = .8f + Random.nextFloat() * .4f
                        target.world.playSound(targetLocation, target.blockData.soundGroup.breakSound, SoundCategory.BLOCKS, 1f, pitch)

                        //  블록 파괴 처리
                        target.breakNaturally()
                    }
                    else {
                        //  원목, 광석 파괴 시 도구 검사
                        val currentItem = player.inventory.itemInMainHand
                        if(currentItem.type.isAir ||
                            (isLog && !ItemUtilities.isAxe(currentItem)) ||
                            (isOre && !ItemUtilities.isPickaxe(currentItem))) {
                            activePlayers.remove(player.uniqueId)
                            cancel()
                            return
                        }

                        //  파티클 생성
                        target.world.spawnParticle(Particle.BLOCK, targetLocation, 20, .3, .3, .3, .0, target.blockData)
                        //  소리 재생
                        val pitch = .8f + Random.nextFloat() * .4f
                        target.world.playSound(targetLocation, target.blockData.soundGroup.breakSound, SoundCategory.BLOCKS, 1f, pitch)

                        //  블록 파괴 처리
                        player.breakBlock(target)
                    }
                }
            }
        }.runTaskTimer(plugin, configManager.miningConfig.blockBreakingDelayTicks, configManager.miningConfig.blockBreakingDelayTicks)
    }

    /**
     * 연결된 블록 찾기
     *
     * @param startBlock 시작 블록 데이터
     * @param maxBlocks 최대 파괴 가능 블록 수
     *
     * @return 연결된 블록 목록
     */
    private fun getConnectedBlocks(startBlock: Block, maxBlocks: Int): Set<Block> {
        val connected = mutableSetOf<Block>()
        val queue = LinkedList<Block>()
        val material = startBlock.type

        queue.add(startBlock)
        connected.add(startBlock)

        val offsets = getSearchOffsets()

        while(queue.isNotEmpty() && connected.size < maxBlocks) {
            val current = queue.poll()
            for(offset in offsets) {
                val next = current.getRelative(offset.blockX, offset.blockY, offset.blockZ)
                //  동일한 물질이면서 아직 찾지 않은 블록
                if(next.type == material && !connected.contains(next)) {
                    if(!blockDataManager.isPlacedByPlayer(next)) {
                        connected.add(next)
                        queue.add(next)
                        if(connected.size >= maxBlocks)
                            break
                    }
                }
            }
        }

        return connected
    }

    /**
     * 원목에 연결된 나뭇잎 블록 찾기
     *
     * @param logs 원목 블록들
     * @param maxBlocks 최대 파괴 가능 블록 수
     *
     * @return 연결된 나뭇잎 블록 목록
     */
    private fun getConnectedLeaveBlocks(logs: Set<Block>, maxBlocks: Int): Set<Block> {
        val blocks = mutableSetOf<Block>()
        val queue = LinkedList<Pair<Block, Int>>()
        //  최대 5 블록 떨어진 나뭇잎까지만 탐색
        val maxDepth = 5

        val offsets = getSearchOffsets()

        //  원목과 맞닿아 있는 나뭇잎 찾고 큐에 넣기
        for(log in logs) {
            for(offset in offsets) {
                val adj = log.getRelative(offset.blockX, offset.blockY, offset.blockZ)
                if(Tag.LEAVES.isTagged(adj.type) && !blocks.contains(adj)) {
                    blocks.add(adj)
                    queue.add(Pair(adj, 1))
                }
            }
        }

        //  찾아낸 나뭇잎에 이어져 있는 다른 나뭇잎들을 너비 우선 탐색으로 확장
        while(queue.isNotEmpty() && blocks.size < maxBlocks) {
            val (current, depth) = queue.poll()
            if(depth >= maxDepth)
                continue

            for(offset in offsets) {
                val next = current.getRelative(offset.blockX, offset.blockY, offset.blockZ)
                if(Tag.LEAVES.isTagged(next.type) && !blocks.contains(next)) {
                    queue.add(Pair(next, depth + 1))
                }
            }
        }

        return blocks
    }

    /**
     * 3 x 3 x 3 범위 백터 배열
     *
     * @return 백터 배열
     */
    private fun getSearchOffsets(): List<Vector> {
        val offsets = mutableListOf<Vector>()
        for(x in -1..1) {
            for(y in -1..1) {
                for(z in -1..1) {
                    if(x == 0 && y == 0 && z == 0)
                        continue

                    offsets.add(Vector(x, y, z))
                }
            }
        }
        return offsets
    }
}