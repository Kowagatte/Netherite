package ca.damocles.client

import ca.damocles.NetheriteRegistry
import ca.damocles.NetheriteRegistry.RICE
import ca.damocles.NetheriteRegistry.TEA_FLOWER_BLOCK
import ca.damocles.NetheriteRegistry.TOMATO_ENTITY
import ca.damocles.client.screen.GoldenHopperScreen
import ca.damocles.client.screen.UpperScreen
import ca.damocles.common.entity.TomatoEntity
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text

@Suppress("unused")
fun init(){
    BlockRenderLayerMap.INSTANCE.putBlock(TEA_FLOWER_BLOCK, RenderLayer.getCutoutMipped())
    BlockRenderLayerMap.INSTANCE.putBlock(RICE, RenderLayer.getCutoutMipped())
    EntityRendererRegistry.INSTANCE.register(TOMATO_ENTITY) { entityRenderDispatcher: EntityRenderDispatcher?, context: EntityRendererRegistry.Context -> FlyingItemEntityRenderer<TomatoEntity?>(entityRenderDispatcher, context.itemRenderer) }
    ScreenRegistry.register(NetheriteRegistry.GOLDEN_HOPPER_BLOCK_SCREEN_HANDLER) { handler: ScreenHandler, inventory: PlayerInventory, title: Text -> GoldenHopperScreen(handler, inventory, title) }
    ScreenRegistry.register(NetheriteRegistry.UPPER_BLOCK_SCREEN_HANDLER) { handler: ScreenHandler, inventory: PlayerInventory, title: Text -> UpperScreen(handler, inventory, title) }
}