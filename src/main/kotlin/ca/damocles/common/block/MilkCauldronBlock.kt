package ca.damocles.common.block

import ca.damocles.NetheriteRegistry
import ca.damocles.NetheriteRegistry.LEVEL_1
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Property
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class MilkCauldronBlock(settings: Settings?): Block(settings) {

    companion object{
        val LEVEL: IntProperty = LEVEL_1
        private val RAY_TRACE_SHAPE = createCuboidShape(2.0, 4.0, 2.0, 14.0, 16.0, 14.0)
        val OUTLINE_SHAPE: VoxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(createCuboidShape(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), *arrayOf<VoxelShape?>(createCuboidShape(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), RAY_TRACE_SHAPE)), BooleanBiFunction.ONLY_FIRST)
    }

    init{
        defaultState = (stateManager.defaultState as BlockState).with(LEVEL, 0) as BlockState
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val i = state.get(LEVEL) as Int
        if(i == 1 && !world.isClient){
            if (!player.abilities.creativeMode) {
                if (!player.inventory.insertStack(ItemStack(NetheriteRegistry.CHEESE))) {
                    player.dropItem(ItemStack(NetheriteRegistry.CHEESE), false)
                }
            }
            player.incrementStat(Stats.USE_CAULDRON)
            world.setBlockState(pos, Blocks.CAULDRON.defaultState, 2);
            world.playSound(null as PlayerEntity?, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f)
            return ActionResult.SUCCESS
        }
        return ActionResult.success(world.isClient)
    }

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape? {
        return OUTLINE_SHAPE
    }

    override fun getRaycastShape(state: BlockState?, world: BlockView?, pos: BlockPos?): VoxelShape? {
        return RAY_TRACE_SHAPE
    }

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        val i = state.get(LEVEL) as Int
        val f = pos.y.toFloat() + (6.0f + (3 * i).toFloat()) / 16.0f
        if (!world.isClient && entity.isOnFire && i > 0 && entity.y <= f.toDouble()) {
            entity.extinguish()
            this.setLevel(world, pos, state, i - 1)
        }
    }

    fun setLevel(world: World, pos: BlockPos?, state: BlockState, level: Int) {
        world.setBlockState(pos, state.with(LEVEL, MathHelper.clamp(level, 0, 1)) as BlockState, 2)
        world.updateComparators(pos, this)
    }


    override fun hasComparatorOutput(state: BlockState?): Boolean {
        return true
    }

    override fun getComparatorOutput(state: BlockState, world: World?, pos: BlockPos?): Int {
        return state.get(LEVEL) as Int
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(*arrayOf<Property<*>>(LEVEL))
    }

    override fun canPathfindThrough(state: BlockState?, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
        return false
    }


}