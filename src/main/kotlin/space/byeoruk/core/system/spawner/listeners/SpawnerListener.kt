package space.byeoruk.core.system.spawner.listeners

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import space.byeoruk.core.global.configs.MainConfigManager
import space.byeoruk.core.system.spawner.managers.SpawnerManager
import space.byeoruk.core.utility.NumberUtilities
import kotlin.random.Random

class SpawnerListener(
    private val configManager: MainConfigManager,
    private val spawnerManager: SpawnerManager): Listener {
    private enum class ArmorType { HELMET, CHESTPLATE, LEGGINGS, BOOTS }

    /**
     * 크리처 스폰 시 스폰 사유 확인해서 스포너에 의해 스폰했을 경우 키값 부여
     *
     * @param event 이벤트
     */
    @EventHandler
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        if(event.spawnReason == SpawnReason.SPAWNER) {
            val entity = event.entity

            //  몬스터 스포너에서 생성된 몹이라고 낙인 찍기
            spawnerManager.setSpawnedFromSpawner(entity)

            //  최대 체력 설정
            val healthAttribute = entity.getAttribute(Attribute.MAX_HEALTH)
            if(healthAttribute != null) {
                val multiplierValues = configManager.spawnerConfig.healthMultiplier.split(":")
                if(multiplierValues.isNotEmpty()) {
                    //  체력 증가 배율
                    val multiplier =
                        if(multiplierValues.size == 1)
                            multiplierValues[0].toDouble()
                        else
                            Random.nextDouble(multiplierValues[0].toDouble(), multiplierValues[1].toDouble())

                    healthAttribute.baseValue = healthAttribute.baseValue * multiplier
                    entity.health = healthAttribute.baseValue
                }
            }

            //  무작위 장비 착용 (빈 슬롯 만)
            val equipment = entity.equipment
            if(equipment != null) {
                //  헬멧
                if(equipment.helmet == null || equipment.helmet!!.type.isAir)
                    getRandomArmor(ArmorType.HELMET)?.let { equipment.helmet = it }

                //  흉갑
                if(equipment.chestplate == null || equipment.chestplate!!.type.isAir)
                    getRandomArmor(ArmorType.CHESTPLATE)?.let { equipment.chestplate = it }

                //  레깅스
                if(equipment.leggings == null || equipment.leggings!!.type.isAir)
                    getRandomArmor(ArmorType.LEGGINGS)?.let { equipment.leggings = it }

                //  부츠
                if(equipment.boots == null || equipment.boots!!.type.isAir)
                    getRandomArmor(ArmorType.BOOTS)?.let { equipment.boots = it }
            }
        }
    }

    /**
     * 스포너에 의해 생성된 몹 죽었을 때 전리품 삭제
     *
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        if(spawnerManager.isSpawnedFromSpawner(entity)) {
            event.droppedExp = 0
            event.drops.clear()
        }
    }

    /**
     * 부위별로 일정 확률로 가죽, 금, 철 장비 중 하나 반환
     *
     * @param type 방어구 유형
     * @return 아이템
     */
    private fun getRandomArmor(type: ArmorType): ItemStack? {
        if(!NumberUtilities.isInChance(configManager.spawnerConfig.equipmentChance))
            return null

        val materials = when(type) {
            ArmorType.HELMET -> listOf(Material.LEATHER_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET)
            ArmorType.CHESTPLATE -> listOf(Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE)
            ArmorType.LEGGINGS -> listOf(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS)
            ArmorType.BOOTS -> listOf(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS)
        }

        return ItemStack(materials.random())
    }
}