package space.byeoruk.core.system.mining.managers

import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.contains
import kotlin.collections.plus

class BlockDataManager(plugin: JavaPlugin) {
    private val placedKey = NamespacedKey(plugin, "player_placed")

    /**
     * 블록 설치 시 블록 좌표 기록
     *
     * @param block 블록 데이터
     */
    fun markAsPlaced(block: Block) {
        val pdc = block.chunk.persistentDataContainer
        val array = pdc.get(placedKey, PersistentDataType.LONG_ARRAY) ?: LongArray(0)
        block.blockKey.let {
            if(!array.contains(it)) {
                pdc.set(placedKey, PersistentDataType.LONG_ARRAY, array.plus(it))
            }
        }
    }

    /**
     * 블록 파괴 시 저장된 좌표 기록 지우기
     *
     * @param block 블록 데이터
     */
    fun unmarkPlaced(block: Block) {
        val pdc = block.chunk.persistentDataContainer
        val array = pdc.get(placedKey, PersistentDataType.LONG_ARRAY) ?: return
        block.blockKey.let {
            if(array.contains(it)) {
                val newArray = array.filter { item -> item != it }.toLongArray()
                if(newArray.isEmpty())
                    pdc.remove(placedKey)
                else
                    pdc.set(placedKey, PersistentDataType.LONG_ARRAY, newArray)
            }
        }
    }

    /**
     * 플레이어가 설치한 블록인지 확인
     *
     * @param block 블록 데이터
     * @return 플레이어가 설치한 블록이면 true 아니면 false 반환
     */
    fun isPlacedByPlayer(block: Block): Boolean {
        val pdc = block.chunk.persistentDataContainer
        val array = pdc.get(placedKey, PersistentDataType.LONG_ARRAY) ?: LongArray(0)
        return array.contains(block.blockKey)
    }
}