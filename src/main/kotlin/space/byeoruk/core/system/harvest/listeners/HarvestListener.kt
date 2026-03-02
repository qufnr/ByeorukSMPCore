package space.byeoruk.core.system.harvest.listeners

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import space.byeoruk.core.Main
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.harvest.managers.HarvestManager
import space.byeoruk.core.utility.NumberUtilities
import kotlin.random.Random

class HarvestListener(
    private val plugin: Main,
    private val configManager: MainConfigManager,
    private val harvestManager: HarvestManager,
): Listener {
    private val crops = setOf(
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.GLOW_BERRIES,
        Material.PUMPKIN_STEM,
        Material.MELON_STEM,
        Material.SWEET_BERRIES
    )

    /**
     * 농장물 성장 처리
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onBlockGrow(event: BlockGrowEvent) {
        val block = event.block
        val newState = event.newState.type

        if(block.type in crops)
            event.isCancelled = true

        if(newState == Material.PUMPKIN || newState == Material.MELON)
            event.isCancelled = true

//        event.block.let {
//            if(it.type in crops) {
//                //  성장 버프를 받지 않았다면 성장 취소 (random_tick_speed 무시)
//                if(!harvestManager.isGrowBuffed(it.location))
//                    event.isCancelled = true
//            }
//        }
    }

    /**
     * 플레이어 농작물에 상호작용 시 성장 효과 부여
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if(event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val block = event.clickedBlock ?: return
        val item = event.item ?: return
        val player = event.player

        if(item.type == Material.BONE_MEAL && block.type in crops) {
            val ageable = block.blockData as Ageable

            //  이미 다 자랐거나 성장 효과가 진행 중인 농작물이면 무시
            if(ageable.age == ageable.maximumAge || harvestManager.isGrowBuffed(block.location)) {
                event.isCancelled = true
                return
            }

            //  바닐라 뼛가루 효과 차단
            event.isCancelled = true

            //  크리에이티브 모드가 아닐 경우 아이템(뼛가루) 1개 차감
            if(player.gameMode != GameMode.CREATIVE)
                item.amount -= 1

            //  성장 효과 부여
            harvestManager.setGrowBuff(block.location, configManager.harvestConfig.growBuffDuration)
        }
    }

    /**
     * 디스펜서가 농작물에 뼛가루를 발사했을 때 성장 효과 부여
     *
     * @param event
     */
    @EventHandler
    fun onBlockDispense(event: BlockDispenseEvent) {
        val item = event.item
        if(item.type != Material.BONE_MEAL)
            return

        val dispenser = event.block
        //  디스펜서가 바라보는 방향
        val directional = dispenser.blockData as? Directional ?: return
        //  디스펜서 타겟 블록
        val targetBlock = dispenser.getRelative(directional.facing)

        //  타겟 블록이 농작물인지 확인
        if(targetBlock.type in crops) {
            val ageable = targetBlock.blockData as? Ageable ?: return

            //  이미 다 자란 작물이거나 성장 효과가 진행 중이면 바닐라처럼 뼛가루 낭비 방지
            if(ageable.age == ageable.maximumAge || harvestManager.isGrowBuffed(targetBlock.location)) {
                event.isCancelled = true
                return
            }

            //  바닐라 시스템 효과 취소
            event.isCancelled = true

            //  스케줄러를 통해 1틱 뒤에 디스펜서 내부 뼛가루 1개 차감
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val dispenserState = dispenser.state as? Dispenser ?: return@Runnable
                val inventory = dispenserState.inventory

                for(i in 0 until inventory.size) {
                    val inventoryItem = inventory.getItem(i)
                    if(inventoryItem != null && inventoryItem.type == Material.BONE_MEAL) {
                        //  뼛가루 하나만 쓰고 종료
                        inventoryItem.amount -= 1
                        break
                    }
                }
            })

            //  성장 효과 부여
            harvestManager.setGrowBuff(targetBlock.location, configManager.harvestConfig.growBuffDuration)
        }
    }

    /**
     * 블록 파괴 처리
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block

        //  성장 효과 부여되어 있으면 제거
        if(harvestManager.isGrowBuffed(block.location))
            harvestManager.removeGrowBuff(block.location)

        //  호박/수박 줄기일 경우 드롭 없음
        if(block.type == Material.PUMPKIN_STEM || block.type == Material.ATTACHED_PUMPKIN_STEM) {
            if(event.player.gameMode != GameMode.CREATIVE) {
                event.isDropItems = false
                block.world.dropItemNaturally(block.location.clone().add(.5, .5, .5), ItemStack(Material.PUMPKIN_SEEDS, 1))
            }
        }
        if(block.type == Material.MELON_STEM || block.type == Material.ATTACHED_MELON_STEM) {
            if(event.player.gameMode != GameMode.CREATIVE) {
                event.isDropItems = false
                block.world.dropItemNaturally(block.location.clone().add(.5, .5, .5), ItemStack(Material.MELON_SEEDS, 1))
            }
        }

        //  호박이나 수박 열매 파괴 시 연결된 줄기 파괴 처리
        if(block.type == Material.PUMPKIN || block.type == Material.MELON) {
            val faces = listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
            for(face in faces) {
                val adjacent = block.getRelative(face)
                //  연결된 줄기 확인
                if(adjacent.type == Material.ATTACHED_PUMPKIN_STEM || adjacent.type == Material.ATTACHED_MELON_STEM) {
                    val seedType = if(adjacent.type == Material.ATTACHED_PUMPKIN_STEM) Material.PUMPKIN_SEEDS else Material.MELON_SEEDS

                    //  바닐라 드롭 차단을 위해 블록을 공기로 설정
                    adjacent.type = Material.AIR

                    if(event.player.gameMode != GameMode.CREATIVE)
                        adjacent.world.dropItemNaturally(adjacent.location.clone().add(.5, .5, .5), ItemStack(seedType, 1))

                    //  줄기에 남아있던 효과 제거
                    if(harvestManager.isGrowBuffed(adjacent.location))
                        harvestManager.removeGrowBuff(adjacent.location)

//                    val directional = adjacent.blockData as? Directional
//                    if(directional != null && directional.facing == face.oppositeFace) {
//                        adjacent.drops.clear()
//                        adjacent.breakNaturally()
//                        //  줄기에 남아있던 효과 제거
//                        if(harvestManager.isGrowBuffed(adjacent.location))
//                            harvestManager.removeGrowBuff(adjacent.location)
//                    }
                }
            }
        }

        //  건초 더미 파괴 시 밀 드롭
        if(block.type == Material.HAY_BLOCK) {
            if(event.player.gameMode != GameMode.CREATIVE) {
                event.isDropItems = false

                val wheatDropCounts = configManager.harvestConfig.hayBlockDropWheatCountRange.split(":").map { it.toInt() }
                //  TODO :: 행운 효과는?
                val dropAmount = NumberUtilities.getRandomInt(wheatDropCounts[0], wheatDropCounts[1])
                block.world.dropItemNaturally(
                    block.location.clone().add(.5, .5, .5),
                    ItemStack(Material.WHEAT, dropAmount)
                )
            }
        }
    }
}