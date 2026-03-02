package space.byeoruk.core.system.spawner.managers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import space.byeoruk.core.Main

class SpawnerManager(plugin: Main) {
    private val mobSpawnedFromSpawnerKey = NamespacedKey(plugin, "is_spawned_from_spawner")

    /**
     * 스포너에 의해 스폰된 몹을 키에 저장
     *
     * @param entity 엔티티
     */
    fun setSpawnedFromSpawner(entity: Entity) {
        entity.persistentDataContainer.set(mobSpawnedFromSpawnerKey, PersistentDataType.BYTE, 1)
    }

    /**
     * 이 엔티티가 스포너에 의해 스폰했는지 여부 반환
     *
     * @param entity 엔티티
     * @return 스포너에 의해 스폰했다면 true 아니면 false 반환
     */
    fun isSpawnedFromSpawner(entity: Entity): Boolean =
        entity.persistentDataContainer.has(mobSpawnedFromSpawnerKey, PersistentDataType.BYTE)
}