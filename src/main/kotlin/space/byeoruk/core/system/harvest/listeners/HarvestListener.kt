package space.byeoruk.core.system.harvest.listeners

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
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
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import space.byeoruk.core.Main
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.harvest.managers.HarvestManager
import space.byeoruk.core.utility.BlockUtilities.isMaxAge
import space.byeoruk.core.utility.NumberUtilities

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
        Material.PUMPKIN_STEM,
        Material.ATTACHED_PUMPKIN_STEM,
        Material.MELON_STEM,
        Material.ATTACHED_MELON_STEM,
        Material.SWEET_BERRY_BUSH,
        Material.CAVE_VINES,
        Material.CAVE_VINES_PLANT,
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

        if(newState == Material.PUMPKIN || newState == Material.MELON || newState == Material.CAVE_VINES)
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
        val blockData = block.blockData

        //  뼛가루를 들고 작물에 상호작용
        if(item.type == Material.BONE_MEAL && block.type in crops) {
            if(block.isMaxAge() || harvestManager.isGrowBuffed(block.location)) {
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

            return
        }

        //  달콤한 열매 덤불 상호작용으로 열매 못따도록 처리
        //  TODO :: WHY NOT WORKING?!!!
        if(block.type == Material.SWEET_BERRY_BUSH && blockData is Ageable) {
            if(blockData.age >= 2) {
                event.isCancelled = true

                //  바닐라 기준 드롭
                val dropCount = if(blockData.age == 3) NumberUtilities.getRandomInt(2, 4) else NumberUtilities.getRandomInt(1, 3)
                block.world.playSound(block.location, Sound.BLOCK_SWEET_BERRY_BUSH_BREAK, 1.0f, 1.0f)
                block.type = Material.AIR
                val sweetBerries = ItemStack(Material.SWEET_BERRIES, dropCount)
                block.world.dropItemNaturally(block.location.clone().add(.5, .5, .5), sweetBerries)
            }
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
            //  이미 다 자란 작물이거나 성장 효과가 진행 중이면 바닐라처럼 뼛가루 낭비 방지
            if(targetBlock.isMaxAge() || harvestManager.isGrowBuffed(targetBlock.location)) {
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

        //  호박 줄기 파괴 시 씨앗 드롭 설정
        if(block.type == Material.PUMPKIN_STEM || block.type == Material.ATTACHED_PUMPKIN_STEM) {
            if(event.player.gameMode != GameMode.CREATIVE) {
                val seedDropCounts = NumberUtilities.getRangeInt(configManager.harvestConfig.pumpkinStemDropSeedCountRange)
                event.isDropItems = false

                val dropCount = NumberUtilities.getRandomInt(seedDropCounts[0], seedDropCounts[1])
                if(dropCount > 0)
                    block.world.dropItemNaturally(
                        block.location.clone().add(.5, .5, .5),
                        ItemStack(Material.PUMPKIN_SEEDS, dropCount)
                    )
            }
        }

        //  수박 줄기 파괴 시 씨앗 드롭 설정
        if(block.type == Material.MELON_STEM || block.type == Material.ATTACHED_MELON_STEM) {
            if(event.player.gameMode != GameMode.CREATIVE) {
                val seedDropCounts = NumberUtilities.getRangeInt(configManager.harvestConfig.melonStemDropSeedCountRange)
                event.isDropItems = false

                val dropCount = NumberUtilities.getRandomInt(seedDropCounts[0], seedDropCounts[1])
                if(dropCount > 0)
                    block.world.dropItemNaturally(
                        block.location.clone().add(.5, .5, .5),
                        ItemStack(Material.MELON_SEEDS, NumberUtilities.getRandomInt(seedDropCounts[0], seedDropCounts[1]))
                    )
            }
        }

        //  호박이나 수박 열매 파괴 시 연결된 줄기 파괴 처리
        if(block.type == Material.PUMPKIN || block.type == Material.MELON) {
            val faces = listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
            for(face in faces) {
                val adjacent = block.getRelative(face)
                //  연결된 줄기 확인
                if(adjacent.type == Material.ATTACHED_PUMPKIN_STEM || adjacent.type == Material.ATTACHED_MELON_STEM) {
                    //  드롭할 씨앗
                    val seedType = if(adjacent.type == Material.ATTACHED_PUMPKIN_STEM) Material.PUMPKIN_SEEDS else Material.MELON_SEEDS
                    //  씨앗 드롭 개수
                    val dropCounts = NumberUtilities.getRangeInt(
                        if(seedType == Material.PUMPKIN_SEEDS)
                                configManager.harvestConfig.attachedPumpkinStemDropSeedCountRange
                            else
                                configManager.harvestConfig.attachedMelonStemDropSeedCountRange
                    )

                    //  바닐라 드롭 차단을 위해 블록을 공기로 설정
                    adjacent.type = Material.AIR

                    if(event.player.gameMode != GameMode.CREATIVE)
                        adjacent.world.dropItemNaturally(
                            adjacent.location.clone().add(.5, .5, .5),
                            ItemStack(seedType, NumberUtilities.getRandomInt(dropCounts[0], dropCounts[1]))
                        )

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

                val wheatDropCounts = NumberUtilities.getRangeInt(configManager.harvestConfig.hayBlockDropWheatCountRange)
                //  TODO :: 행운 효과는?
                val dropAmount = NumberUtilities.getRandomInt(wheatDropCounts[0], wheatDropCounts[1])
                block.world.dropItemNaturally(
                    block.location.clone().add(.5, .5, .5),
                    ItemStack(Material.WHEAT, dropAmount)
                )
            }
        }
    }

    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        val recipe = event.recipe ?: return

        //  건초 더미 분해 제작 막기
        if(recipe.result.type == Material.WHEAT) {
            //  조합대에 건초 더미가 하나라도 있는지 확인
            val hasHayBlock = event.inventory.matrix.any { it?.type == Material.HAY_BLOCK }
            if(hasHayBlock)
                event.inventory.result = null
        }

        //  호박 분해 제작 막기
        if(recipe.result.type == Material.PUMPKIN_SEEDS) {
            val hasPumpkin = event.inventory.matrix.any { it?.type == Material.PUMPKIN }
            if(hasPumpkin)
                event.inventory.result = null
        }

        //  수박 조각 분해 제작 막기
        if(recipe.result.type == Material.MELON_SEEDS) {
            val hasMelon = event.inventory.matrix.any { it?.type == Material.MELON_SLICE }
            if(hasMelon)
                event.inventory.result = null
        }
    }
}