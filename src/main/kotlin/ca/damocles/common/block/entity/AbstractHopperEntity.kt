package ca.damocles.common.block.entity

import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.*
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
import net.minecraft.screen.ScreenHandler
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.text.Text
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

abstract class AbstractHopperEntity<A : BlockEntity?>(entityType: BlockEntityType<A>, private val FACING: DirectionProperty, private val ENABLED: BooleanProperty, private val defaultDirection: Direction, size: Int): LootableContainerBlockEntity(entityType), Hopper, Tickable{

    private var inventory: DefaultedList<ItemStack>
    private var transferCooldown: Int
    private var lastTickTime: Long

    init{
        inventory = DefaultedList.ofSize(size, ItemStack.EMPTY)
        transferCooldown = -1
        lastTickTime = 0L
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
        if (!deserializeLootTable(tag)) {
            Inventories.fromTag(tag, inventory)
        }
        transferCooldown = tag.getInt("TransferCooldown")
    }

    override fun toTag(tag: CompoundTag): CompoundTag? {
        super.toTag(tag)
        if (!serializeLootTable(tag)) {
            Inventories.toTag(tag, inventory)
        }
        tag.putInt("TransferCooldown", transferCooldown)
        return tag
    }

    override fun size(): Int {
        return inventory.size
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        checkLootInteraction(null as PlayerEntity?)
        return Inventories.splitStack(this.invStackList, slot, amount)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        checkLootInteraction(null as PlayerEntity?)
        this.invStackList[slot] = stack
        if (stack.count > this.maxCountPerStack) {
            stack.count = this.maxCountPerStack
        }
    }

    abstract override fun getContainerName(): Text?

    override fun tick() {
        if (world != null && !world!!.isClient) {
            --transferCooldown
            lastTickTime = world!!.time
            if (!needsCooldown()) {
                setCooldown(0)
                insertAndExtract(Supplier { extract(this) })
            }
        }
    }

    private fun insertAndExtract(extractMethod: Supplier<Boolean>): Boolean {
        return if (world != null && !world!!.isClient) {
            if (!needsCooldown() && cachedState.get(ENABLED) as Boolean) {
                var bl = false
                if (!this.isEmpty) {
                    bl = insert()
                }
                if (!isFull()) {
                    bl = bl or extractMethod.get()
                }
                if (bl) {
                    setCooldown(8)
                    markDirty()
                    return true
                }
            }
            false
        } else {
            false
        }
    }

    private fun isFull(): Boolean {
        val var1: Iterator<*> = inventory.iterator()
        var itemStack: ItemStack
        do {
            if (!var1.hasNext()) {
                return true
            }
            itemStack = var1.next() as ItemStack
        } while (!itemStack.isEmpty && itemStack.count == itemStack.maxCount)
        return false
    }

    private fun insert(): Boolean {
        val inventory = getOutputInventory()
        return if (inventory == null) {
            false
        } else {
            val direction = (cachedState.get(FACING) as Direction).opposite
            if (isInventoryFull(inventory, direction)) {
                false
            } else {
                for (i in 0 until size()) {
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

    private fun getAvailableSlots(inventory: Inventory, side: Direction): IntStream {
        return if (inventory is SidedInventory) IntStream.of(*inventory.getAvailableSlots(side)) else IntStream.range(0, inventory.size())
    }

    private fun isInventoryFull(inv: Inventory, direction: Direction): Boolean {
        return getAvailableSlots(inv, direction).allMatch { i: Int ->
            val itemStack = inv.getStack(i)
            itemStack.count >= itemStack.maxCount
        }
    }

    private fun isInventoryEmpty(inv: Inventory, facing: Direction): Boolean {
        return getAvailableSlots(inv, facing).allMatch { i: Int -> inv.getStack(i).isEmpty }
    }

    fun extract(hopper: Hopper): Boolean {
        val inventory = getInputInventory(hopper)
        return if (inventory != null) {
            val direction = defaultDirection
            if (isInventoryEmpty(inventory, direction)){
                false
            }else{
                getAvailableSlots(inventory, direction).anyMatch { i: Int -> extract(hopper, inventory, i, direction) }
            }
        } else {
            val var2: Iterator<*> = getInputItemEntities(hopper).iterator()
            var itemEntity: ItemEntity
            do {
                if (!var2.hasNext()) {
                    return false
                }
                itemEntity = var2.next() as ItemEntity
            } while (!extract(hopper, itemEntity))
            true
        }
    }

    private fun extract(hopper: Hopper, inventory: Inventory, slot: Int, side: Direction): Boolean {
        val itemStack = inventory.getStack(slot)
        if (!itemStack.isEmpty && canExtract(inventory, itemStack, slot, side)) {
            val itemStack2 = itemStack.copy()
            val itemStack3 = transfer(inventory, hopper, inventory.removeStack(slot, 1), null as Direction?)
            if (itemStack3.isEmpty) {
                inventory.markDirty()
                return true
            }
            inventory.setStack(slot, itemStack2)
        }
        return false
    }

    fun extract(inventory: Inventory, itemEntity: ItemEntity): Boolean {
        var bl = false
        val itemStack = itemEntity.stack.copy()
        val itemStack2 = transfer(null as Inventory?, inventory, itemStack, null as Direction?)
        if (itemStack2.isEmpty) {
            bl = true
            itemEntity.remove()
        } else {
            itemEntity.stack = itemStack2
        }
        return bl
    }

    fun transfer(from: Inventory?, to: Inventory, stack: ItemStack, side: Direction?): ItemStack {
        var stack = stack
        if (to is SidedInventory) {
            val `is` = to.getAvailableSlots(side)
            var i = 0
            while (i < `is`.size && !stack.isEmpty) {
                stack = transfer(from, to, stack, `is`[i], side)
                ++i
            }
        } else {
            val j = to.size()
            var k = 0
            while (k < j && !stack.isEmpty) {
                stack = transfer(from, to, stack, k, side)
                ++k
            }
        }
        return stack
    }

    private fun canInsert(inventory: Inventory, stack: ItemStack, slot: Int, side: Direction?): Boolean {
        return if (!inventory.isValid(slot, stack)) {
            false
        } else {
            inventory !is SidedInventory || inventory.canInsert(slot, stack, side)
        }
    }

    private fun canExtract(inv: Inventory, stack: ItemStack, slot: Int, facing: Direction): Boolean {
        return inv !is SidedInventory || inv.canExtract(slot, stack, facing)
    }

    private fun transfer(from: Inventory?, to: Inventory, stack: ItemStack, slot: Int, direction: Direction?): ItemStack {
        var stack = stack
        val itemStack = to.getStack(slot)
        if (canInsert(to, stack, slot, direction)) {
            var bl = false
            val bl2 = to.isEmpty
            if (itemStack.isEmpty) {
                to.setStack(slot, stack)
                stack = ItemStack.EMPTY
                bl = true
            } else if (canMergeItems(itemStack, stack)) {
                val i = stack.maxCount - itemStack.count
                val j = Math.min(stack.count, i)
                stack.decrement(j)
                itemStack.increment(j)
                bl = j > 0
            }
            if (bl) {
                if (bl2 && to is AbstractHopperEntity<*>) {
                    val hopperBlockEntity = to
                    if (!hopperBlockEntity.isDisabled()) {
                        var k = 0
                        if (from is AbstractHopperEntity<*>) {
                            if (hopperBlockEntity.lastTickTime >= from.lastTickTime) {
                                k = 1
                            }
                        }
                        hopperBlockEntity.setCooldown(8 - k)
                    }
                }
                to.markDirty()
            }
        }
        return stack
    }

    private fun getOutputInventory(): Inventory? {
        val direction = cachedState.get(FACING) as Direction
        return getInventoryAt(getWorld(), pos.offset(direction))
    }

    abstract fun getInputInventory(hopper: Hopper): Inventory?

    fun getInputItemEntities(hopper: Hopper): List<ItemEntity?> {
        return hopper.inputAreaShape.boundingBoxes.stream().flatMap { box: Box -> hopper.world!!.getEntitiesByClass(ItemEntity::class.java, box.offset(hopper.hopperX - 0.5, hopper.hopperY - 0.5, hopper.hopperZ - 0.5), EntityPredicates.VALID_ENTITY).stream() }.collect(Collectors.toList())
    }

    fun getInventoryAt(world: World?, blockPos: BlockPos): Inventory? {
        return getInventoryAt(world, blockPos.x.toDouble() + 0.5, blockPos.y.toDouble() + 0.5, blockPos.z.toDouble() + 0.5)
    }

    fun getInventoryAt(world: World?, x: Double, y: Double, z: Double): Inventory? {
        var inventory: Inventory? = null
        val blockPos = BlockPos(x, y, z)
        val blockState = world!!.getBlockState(blockPos)
        val block = blockState.block
        if (block is InventoryProvider) {
            inventory = (block as InventoryProvider).getInventory(blockState, world, blockPos)
        } else if (block.hasBlockEntity()) {
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is Inventory) {
                inventory = blockEntity
                if (inventory is ChestBlockEntity && block is ChestBlock) {
                    inventory = ChestBlock.getInventory(block, blockState, world, blockPos, true)
                }
            }
        }
        if (inventory == null) {
            val list = world.getOtherEntities(null as Entity?, Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES)
            if (!list.isEmpty()) {
                inventory = list[world.random.nextInt(list.size)] as Inventory
            }
        }
        return inventory
    }

    private fun canMergeItems(first: ItemStack, second: ItemStack): Boolean {
        return if (first.item !== second.item) {
            false
        } else if (first.damage != second.damage) {
            false
        } else if (first.count > first.maxCount) {
            false
        } else {
            ItemStack.areTagsEqual(first, second)
        }
    }

    override fun getHopperX(): Double {
        return pos.x.toDouble() + 0.5
    }

    override fun getHopperY(): Double {
        return pos.y.toDouble() + 0.5
    }

    override fun getHopperZ(): Double {
        return pos.z.toDouble() + 0.5
    }

    private fun setCooldown(cooldown: Int) {
        transferCooldown = cooldown
    }

    private fun needsCooldown(): Boolean {
        return transferCooldown > 0
    }

    private fun isDisabled(): Boolean {
        return transferCooldown > 8
    }

    override fun getInvStackList(): DefaultedList<ItemStack> {
        return inventory
    }

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventory = list
    }

    fun onEntityCollided(entity: Entity) {
        if (entity is ItemEntity) {
            val blockPos = getPos()
            if (VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset((-blockPos.x).toDouble(), (-blockPos.y).toDouble(), (-blockPos.z).toDouble())), this.inputAreaShape, BooleanBiFunction.AND)) {
                insertAndExtract(Supplier { extract(this, entity) })
            }
        }
    }

    abstract override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler?

}