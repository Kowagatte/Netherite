package ca.damocles.common.block.entity

import ca.damocles.NetheriteRegistry
import ca.damocles.common.block.UpperBlock
import ca.damocles.common.screenhandler.UpperBlockScreenHandler
import net.minecraft.block.entity.Hopper
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.Direction

class UpperBlockEntity(): AbstractHopperEntity<UpperBlockEntity>(NetheriteRegistry.UPPER_BLOCK_ENTITY, UpperBlock.FACING, UpperBlock.ENABLED, Direction.UP, 5){

    override fun getContainerName(): Text? = TranslatableText("container.upper")

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler? {
        return UpperBlockScreenHandler(syncId, playerInventory, this)
    }

    override fun getInputInventory(hopper: Hopper): Inventory? {
        return getInventoryAt(hopper.world, hopper.hopperX, hopper.hopperY - 1.0, hopper.hopperZ)
    }

}