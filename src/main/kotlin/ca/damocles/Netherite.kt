package ca.damocles

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.serializer.ConfigSerializer
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.FlowerBlock
import net.minecraft.block.Material
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

fun getIdentifier(id: String): Identifier {
    return Identifier("netherite", id);
}

@Suppress("unused")
fun init() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.
    AutoConfig.register(ModConfig::class.java,
        ConfigSerializer.Factory { definition: Config?, configClass: Class<ModConfig?>? ->
            GsonConfigSerializer(definition, configClass)
        })

    //Loads the registry Object class into memory, doesnt if this is not here. *shrug*
    NetheriteRegistry.registerObjects
}

