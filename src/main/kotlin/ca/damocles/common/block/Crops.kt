package ca.damocles.common.block

import ca.damocles.NetheriteRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.CropBlock
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemConvertible
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

class RiceBlock(settings: Settings?) : CropBlock(settings) {
    private val AGE_TO_SHAPE = arrayOf(createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0))

    @Environment(EnvType.CLIENT)
    override fun getSeedsItem(): ItemConvertible? {
        return NetheriteRegistry.RICE_SEEDS
    }

    override fun getOutlineShape(state: BlockState, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape? {
        return AGE_TO_SHAPE[(state.get(this.ageProperty) as Int)]
    }
}