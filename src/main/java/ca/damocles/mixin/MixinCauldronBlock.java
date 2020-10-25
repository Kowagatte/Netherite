package ca.damocles.mixin;

import ca.damocles.NetheriteRegistry;
import ca.damocles.common.block.entity.GoldenHopperBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBlock.class)
public class MixinCauldronBlock {
    @Inject(method = "onUse", at = @At(value = "HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        ItemStack itemStack = player.getStackInHand(hand);
        if(!itemStack.isEmpty()){
            Item item = itemStack.getItem();
            int i = (Integer)state.get(CauldronBlock.LEVEL);
            if(item == Items.MILK_BUCKET){
                if (i == 0 && !world.isClient) {
                    if (!player.abilities.creativeMode) {
                        player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                    }

                    player.incrementStat(Stats.FILL_CAULDRON);
                    world.setBlockState(pos, NetheriteRegistry.INSTANCE.getMILK_CAULDRON().getDefaultState(), 2);
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    info.setReturnValue(ActionResult.SUCCESS);
                }
                info.setReturnValue(ActionResult.success(world.isClient));
            }else if(item == NetheriteRegistry.INSTANCE.getMILK_BOTTLE()){
                if (i == 0 && !world.isClient) {
                    if (!player.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }

                    player.incrementStat(Stats.USE_CAULDRON);
                    world.setBlockState(pos, NetheriteRegistry.INSTANCE.getMILK_CAULDRON().getDefaultState(), 2);
                    world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    info.setReturnValue(ActionResult.SUCCESS);
                }
                info.setReturnValue(ActionResult.success(world.isClient));
            }
        }
    }
}
