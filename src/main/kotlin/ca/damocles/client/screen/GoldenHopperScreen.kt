package ca.damocles.client.screen

import ca.damocles.getIdentifier
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text


class GoldenHopperScreen(handler: ScreenHandler, inventory: PlayerInventory, title: Text) : HandledScreen<ScreenHandler>(handler, inventory, title) {

    companion object{
        val TEXTURE = getIdentifier("textures/gui/container/golden_hopper.png")
    }

    override fun drawBackground(matrices: MatrixStack?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        client!!.textureManager.bindTexture(TEXTURE)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    override fun init() {
        super.init()
        playerInventoryTitleY -= 32
    }

}