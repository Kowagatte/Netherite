package ca.damocles.common.screenhandler

import ca.damocles.NetheriteRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class GoldenHopperBlockScreenHandler : ScreenHandler {

    private val inventory: Inventory

    constructor(syncId: Int, playerInventory: PlayerInventory): this(syncId, playerInventory, SimpleInventory(6))

    constructor(syncId: Int, playerInventory: PlayerInventory, inventory: Inventory): super(NetheriteRegistry.GOLDEN_HOPPER_BLOCK_SCREEN_HANDLER, syncId){
        checkSize(inventory, 6)
        this.inventory = inventory
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player)

        //This will place the slot in the correct locations. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        var m: Int = 0
        addSlot(object : Slot(inventory, m, 24, 20) {
            override fun canInsert(stack: ItemStack?): Boolean {
                return true
            }

            override fun getMaxItemCount(): Int {
                return 1
            }
        })
        while (m < 5) {
            addSlot(Slot(inventory, m + 1, 64 + m * 18, 20))
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

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index] as Slot

        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (index == 0) {
                if (!insertItem(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
            } else {
                if ((slots[0] as Slot).hasStack() || !(slots[0] as Slot).canInsert(itemStack2)) {
                    return ItemStack.EMPTY
                }
                val itemStack3 = itemStack2.copy()
                itemStack3.count = 1
                itemStack2.decrement(1)
                (slots[0] as Slot).stack = itemStack3
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTakeItem(player, itemStack2)
        }
        return itemStack
    }

    /*override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
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
    }*/

    override fun close(player: PlayerEntity?) {
        super.close(player)
        inventory.onClose(player)
    }
}