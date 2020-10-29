package ca.damocles.common.block.entity

import ca.damocles.NetheriteRegistry
import ca.damocles.common.block.GoldenHopperBlock
import ca.damocles.common.block.UpperBlock
import ca.damocles.common.screenhandler.GoldenHopperBlockScreenHandler
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.Hopper
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.screen.HopperScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.IntStream

class GoldenHopperBlockEntity(): AbstractHopperEntity<GoldenHopperBlockEntity>(NetheriteRegistry.GOLDEN_HOPPER_BLOCK_ENTITY, GoldenHopperBlock.FACING, GoldenHopperBlock.ENABLED, Direction.DOWN, 6), SidedInventory{

    override fun getAvailableSlots(side: Direction?): IntArray {
        return IntStream.range(1, inventory.size).toArray()
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return (inventory[0].item == stack.item) || inventory[0] == ItemStack.EMPTY
    }

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
        return true
    }

    override fun insert(): Boolean {
        val inventory = getOutputInventory()
        return if (inventory == null) {
            false
        } else {
            val direction = (cachedState.get(GoldenHopperBlock.FACING) as Direction).opposite
            if (isInventoryFull(inventory, direction)) {
                false
            } else {
                // This ignores the first slot in the GoldenHopper
                for (i in 1 until size()) {
                    if (!getStack(i).isEmpty) {
                        val itemStack = getStack(i).copy()
                        val itemStack2 = transfer(this, inventory, this.removeStack(i, 1), direction)
                        if (itemStack2.isEmpty) {
                            inventory.markDirty()
                            return true
                        }
                        setStack(i, itemStack)
                    }
                }
                false
            }
        }
    }

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler? {
        return GoldenHopperBlockScreenHandler(syncId, playerInventory, this)
    }

    override fun getContainerName(): Text? = TranslatableText("container.golden_hopper")

    override fun getInputInventory(hopper: Hopper): Inventory? {
        return getInventoryAt(hopper.world, hopper.hopperX, hopper.hopperY + 1.0, hopper.hopperZ)
    }
}