package ca.damocles.common.block

import ca.damocles.NetheriteRegistry
import ca.damocles.common.block.entity.GoldenHopperBlockEntity
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.Hopper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.util.*
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class GoldenHopperBlock(settings: Settings?): BlockWithEntity(settings) {

    companion object{
        val FACING = NetheriteRegistry.HOPPER_FACING
        val ENABLED = NetheriteRegistry.ENABLED
        val TOP_SHAPE = createCuboidShape(0.0, 10.0, 0.0, 16.0, 16.0, 16.0)
        val MIDDLE_SHAPE = createCuboidShape(4.0, 4.0, 4.0, 12.0, 10.0, 12.0)
        val OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE)
        val DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, Hopper.INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST)
        val DOWN_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, createCuboidShape(6.0, 0.0, 6.0, 10.0, 4.0, 10.0))
        val EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, createCuboidShape(12.0, 4.0, 6.0, 16.0, 8.0, 10.0))
        val NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, createCuboidShape(6.0, 4.0, 0.0, 10.0, 8.0, 4.0))
        val SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, createCuboidShape(6.0, 4.0, 12.0, 10.0, 8.0, 16.0))
        val WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, createCuboidShape(0.0, 4.0, 6.0, 4.0, 8.0, 10.0))
        val DOWN_RAY_TRACE_SHAPE = Hopper.INSIDE_SHAPE
        val EAST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, createCuboidShape(12.0, 8.0, 6.0, 16.0, 10.0, 10.0))
        val NORTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, createCuboidShape(6.0, 8.0, 0.0, 10.0, 10.0, 4.0))
        val SOUTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, createCuboidShape(6.0, 8.0, 12.0, 10.0, 10.0, 16.0))
        val WEST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, createCuboidShape(0.0, 8.0, 6.0, 4.0, 10.0, 10.0))
    }

    init{
        defaultState = ((stateManager.defaultState as BlockState).with(FACING, Direction.DOWN) as BlockState).with(ENABLED, true) as BlockState
    }

    override fun getOutlineShape(state: BlockState, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape? {
        return when (state.get(FACING) as Direction) {
            Direction.DOWN -> DOWN_SHAPE
            Direction.NORTH -> NORTH_SHAPE
            Direction.SOUTH -> SOUTH_SHAPE
            Direction.WEST -> WEST_SHAPE
            Direction.EAST -> EAST_SHAPE
            else -> DEFAULT_SHAPE
        }
    }

    override fun getRaycastShape(state: BlockState, world: BlockView?, pos: BlockPos?): VoxelShape? {
        return when (state.get(FACING) as Direction) {
            Direction.DOWN -> DOWN_RAY_TRACE_SHAPE
            Direction.NORTH -> NORTH_RAY_TRACE_SHAPE
            Direction.SOUTH -> SOUTH_RAY_TRACE_SHAPE
            Direction.WEST -> WEST_RAY_TRACE_SHAPE
            Direction.EAST -> EAST_RAY_TRACE_SHAPE
            else -> Hopper.INSIDE_SHAPE
        }
    }

    override fun onPlaced(world: World, pos: BlockPos?, state: BlockState?, placer: LivingEntity?, itemStack: ItemStack) {
        if (itemStack.hasCustomName()) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is GoldenHopperBlockEntity) {
                blockEntity.customName = itemStack.name
            }
        }
    }

    override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
        if (!oldState.isOf(state.block)) {
            updateEnabled(world, pos, state)
        }
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        return if (world.isClient) {
            ActionResult.SUCCESS
        } else {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is GoldenHopperBlockEntity) {
                player.openHandledScreen(blockEntity)
                player.incrementStat(Stats.INSPECT_HOPPER)
            }
            ActionResult.CONSUME
        }
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        updateEnabled(world, pos, state)
    }

    private fun updateEnabled(world: World, pos: BlockPos, state: BlockState) {
        val bl = !world.isReceivingRedstonePower(pos)
        if (bl != state.get(ENABLED) as Boolean) {
            world.setBlockState(pos, state.with(ENABLED, bl) as BlockState, 4)
        }
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is GoldenHopperBlockEntity) {
                ItemScatterer.spawn(world, pos, blockEntity)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val direction = ctx.side.opposite
        return (defaultState.with(FACING, if (direction.axis === Direction.Axis.Y) Direction.DOWN else direction) as BlockState).with(ENABLED, true) as BlockState
    }

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.MODEL
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean {
        return true
    }

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos?): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState? {
        return state.with(HopperBlock.FACING, rotation.rotate(state.get(HopperBlock.FACING) as Direction)) as BlockState
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState? {
        return state.rotate(mirror.getRotation(state.get(HopperBlock.FACING) as Direction))
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(*arrayOf(FACING, ENABLED))
    }

    override fun onEntityCollision(state: BlockState?, world: World, pos: BlockPos?, entity: Entity) {
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is GoldenHopperBlockEntity) {
            blockEntity.onEntityCollided(entity)
        }
    }

    override fun canPathfindThrough(state: BlockState?, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
        return false
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? {
        return GoldenHopperBlockEntity()
    }

}