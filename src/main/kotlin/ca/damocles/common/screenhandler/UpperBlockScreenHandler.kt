package ca.damocles.common.screenhandler

import ca.damocles.NetheriteRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class UpperBlockScreenHandler : ScreenHandler {

    private val inventory: Inventory

    constructor(syncId: Int, playerInventory: PlayerInventory): this(syncId, playerInventory, SimpleInventory(5))

    constructor(syncId: Int, playerInventory: PlayerInventory, inventory: Inventory): super(NetheriteRegistry.UPPER_BLOCK_SCREEN_HANDLER, syncId){
        checkSize(inventory, 5)
        this.inventory = inventory
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player)

        //This will place the slot in the correct locations. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        var m: Int = 0
        while (m < 5) {
            addSlot(Slot(inventory, m, 44 + m * 18, 20))
            ++m
        }

        m = 0
        while (m < 3) {
            for (l in 0..8) {
                addSlot(Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, m * 18 + 51))
            }
            ++m
        }

        m = 0
        while (m < 9) {
            addSlot(Slot(playerInventory, m, 8 + m * 18, 109))
            ++m
        }
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return this.inventory.canPlayerUse(player)
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot != null && slot.hasStack()) {
            val originalStack = slot.stack
            newStack = originalStack.copy()
            if (index < inventory.size()) {
                if (!insertItem(originalStack, inventory.size(), slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(originalStack, 0, inventory.size(), false)) {
                return ItemStack.EMPTY
            }
            if (originalStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return newStack!!
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        inventory.onClose(player)
    }
}