package space.byeoruk.core.system.harvest.managers

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitRunnable
import space.byeoruk.core.Main
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.utility.NumberUtilities
import java.util.concurrent.ConcurrentHashMap

data class GrowBuff(val location: Location, val endTime: Long, val display: TextDisplay)

class HarvestManager(private val configManager: MainConfigManager) {
    private val activeGrowBuffs = ConcurrentHashMap<Location, GrowBuff>()

    /**
     * 농작물 성장 효과 부여
     *
     * @param location 농작물 위치
     * @param duration 효과 시간 (초 단위)
     */
    fun setGrowBuff(location: Location, duration: Int = 60) {
        //  이미 성장 효과가 적용되고 있다면 무시
        if(activeGrowBuffs.containsKey(location))
            return

        val mm = MiniMessage.miniMessage()

        val endTime = System.currentTimeMillis() + (duration * 1000L)
        val displayLocation = location.clone().add(.5, 1.2, .5)
        val display = location.world.spawnEntity(displayLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        display.billboard = Display.Billboard.CENTER
        display.backgroundColor = Color.fromARGB(0, 0, 0, 0)
        display.isShadowed = true
        display.brightness = Display.Brightness(15, 15)
//        display.transformation = Transformation(
//            Vector3f(0f, 0f, 0f),   //  위치 이동
//            AxisAngle4f(0f ,0f, 0f, 1f),    //  왼쪽 회전
//            Vector3f(0f, 0f, 0f),   //  크기 스케일
//            AxisAngle4f(0f ,0f, 0f, 1f) //  오른쪽 회전
//        )
        display.text(mm.deserialize("<color:#70E346>${NumberUtilities.formatSeconds(duration)}"))

        //  현재 위치 성장 효과 생성
        activeGrowBuffs[location] = GrowBuff(location, endTime, display)
    }

    /**
     * 현재 위치의 농작물이 성장 효과를 받고 있는지 여부
     *
     * @param location 위치
     * @return 위치의 작물이 성장 효과를 받고 있으면 true 아니면 false 반환
     */
    fun isGrowBuffed(location: Location): Boolean = activeGrowBuffs.containsKey(location)

    /**
     * 농작물 파괴 시 성장 효과 제거
     *
     * @param location 농작물 위치
     */
    fun removeGrowBuff(location: Location) = activeGrowBuffs.remove(location)?.display?.remove()

    /**
     * 성장 시작 (.5초 마다 실행하는 태스크)
     *
     * @param plugin Plugin Main
     */
    fun startTask(plugin: Main) {
        object: BukkitRunnable() {
            override fun run() {
                val mm = MiniMessage.miniMessage()
                val now = System.currentTimeMillis()
                val iterator = activeGrowBuffs.entries.iterator()

                while(iterator.hasNext()) {
                    val entry = iterator.next()
                    val growBuff = entry.value
                    val location = growBuff.location

                    //  시간이 다 됐거나, 해당 블록이 농작물이 아니게 된 경우 파괴
                    if(now >= growBuff.endTime || location.block.type.isAir) {
                        growBuff.display.remove()
                        iterator.remove()
                        continue
                    }

                    //  파티클 재생
                    location.world.spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(.5, .5, .5), 3, .3, .3, .3, .0)

                    //  남은 시간 업데이트
                    val remainingDuration = ((growBuff.endTime - now) / 1000).toInt()
                    growBuff.display.text(mm.deserialize("<color:#70E346>${NumberUtilities.formatSeconds(remainingDuration)}"))

                    //  작물 자라는 속도 향상
                    val block = location.block
                    val blockData = block.blockData
                    if(blockData is Ageable) {
                        //  호박, 수박 줄기
                        if(block.type == Material.PUMPKIN_STEM || block.type == Material.MELON_STEM) {
                            if(NumberUtilities.isInChance(configManager.harvestConfig.forceGrowChance)) {
                                val faces = listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
                                val spawnFace = faces.random()
                                //  열매(호박/수박 블록)가 맺히는 위치 (공기여야 함)
                                val targetBlock = block.getRelative(spawnFace)
                                //  열매가 맺혀야할 위치의 아래 블록
                                val belowBlock = targetBlock.getRelative(BlockFace.DOWN)

                                if(targetBlock.type.isAir &&
                                    (belowBlock.type == Material.DIRT || belowBlock.type == Material.GRASS_BLOCK || belowBlock.type == Material.FARMLAND)) {
                                    targetBlock.type = if(block.type == Material.PUMPKIN_STEM) Material.PUMPKIN else Material.MELON

                                    //  열매가 맺히면 연결된 줄기 블록으로 변경
                                    val attachedMaterial = if(block.type == Material.PUMPKIN_STEM) Material.ATTACHED_PUMPKIN_STEM else Material.ATTACHED_MELON_STEM
                                    block.type = attachedMaterial
                                    val attachedData = block.blockData as Directional
                                    attachedData.facing = spawnFace
                                    block.blockData = attachedData

                                    //  열매 맺힐 때 소리/파티클 재생
                                    targetBlock.world.playSound(targetBlock.location, Sound.BLOCK_BONE_BLOCK_BREAK, 1.0f, .9f)
                                    targetBlock.world.spawnParticle(Particle.COMPOSTER, targetBlock.location.clone().add(0.5, 0.5, 0.5), 15, 0.4, 0.4, 0.4, 0.1)

                                    //  연결된 줄기일 경우 다 자란걸로 판단해서 효과 제거
                                    if(block.type == Material.ATTACHED_PUMPKIN_STEM || block.type == Material.ATTACHED_MELON_STEM) {
                                        growBuff.display.remove()
                                        iterator.remove()
                                        continue
                                    }
                                }
                            }
                        }

                        //  작물이 다 자라지 않았다면
                        else if(blockData.age < blockData.maximumAge) {
                            //  강제 성장 확률에 들었을 경우 작물 성장 처리
                            if(NumberUtilities.isInChance(configManager.harvestConfig.forceGrowChance)) {
                                blockData.age += 1
                                block.blockData = blockData

                                //  성장 시 소리/파티클 재생
                                location.world.playSound(location, Sound.BLOCK_CROP_BREAK, 1.0f, 1.5f)
                                location.world.spawnParticle(Particle.COMPOSTER, location.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1)

                                //  최대 성장이면 효과 제거
                                if(blockData.age == blockData.maximumAge) {
                                    growBuff.display.remove()
                                    iterator.remove()
                                    continue
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L)
    }

    /**
     * 모든 효과 초기화
     */
    fun clearAll() {
        activeGrowBuffs.values.forEach { it.display.remove() }
        activeGrowBuffs.clear()
    }
}