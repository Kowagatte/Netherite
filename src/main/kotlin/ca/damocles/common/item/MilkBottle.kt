package ca.damocles.common.item

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class MilkBottle(settings: FabricItemSettings) : Item(settings){

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        if (user is ServerPlayerEntity) {
            val serverPlayerEntity = user
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack)
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this))
        }
        if (user is PlayerEntity && !user.abilities.creativeMode) {
            stack.decrement(1)
        }
        if (!world.isClient) {
            user.clearStatusEffects()
        }
        return if (stack.isEmpty) ItemStack(Items.GLASS_BOTTLE) else stack
    }

    override fun getMaxUseTime(stack: ItemStack?): Int {
        return 32
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.DRINK
    }

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack?>? {
        return ItemUsage.consumeHeldItem(world, user, hand)
    }
}