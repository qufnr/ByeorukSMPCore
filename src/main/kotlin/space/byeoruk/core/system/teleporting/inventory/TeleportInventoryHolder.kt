package space.byeoruk.core.system.teleporting.inventory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import space.byeoruk.core.Main

class TeleportInventoryHolder(
    plugin: Main,
    title: String,
    expCost: Int?,
    location: Location
): InventoryHolder {
    private val mm = MiniMessage.miniMessage()
    private val inventory = plugin.server.createInventory(this, 9, mm.deserialize(title))

    init {
        val blank = ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(mm.deserialize("<!i> ")) }
        }

        val yes = ItemStack(Material.GREEN_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply {
                displayName(mm.deserialize("<!i><color:green>이동"))
                lore(listOf(
                    mm.deserialize("<!i><color:white>경험치 비용: <color:#AAD34A>${expCost ?: 0} EXP"),
                    mm.deserialize("<!i><color:white>좌표: <color:green>${location.blockX}, ${location.blockY}, ${location.blockZ}"))
                )
            }
        }

        val no = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(mm.deserialize("<!i><color:red>취소")) }
        }

        inventory.setItem(0, blank)
        inventory.setItem(1, blank)
        inventory.setItem(2, yes)
        inventory.setItem(3, blank)
        inventory.setItem(4, blank)
        inventory.setItem(5, blank)
        inventory.setItem(6, no)
        inventory.setItem(7, blank)
        inventory.setItem(8, blank)
    }

    override fun getInventory(): Inventory = inventory
}