package ca.damocles

import io.github.prospector.modmenu.api.ConfigScreenFactory
import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry
import net.minecraft.client.gui.screen.Screen


@Config(name = "netherite")
class ModConfig: ConfigData{
    var toggleA = true
    var toggleB = false

    @ConfigEntry.Gui.CollapsibleObject
    var stuff = InnerStuff()

    @ConfigEntry.Gui.Excluded
    var invisibleStuff = InnerStuff()

    class InnerStuff {
        var a = 0
        var b = 1
    }
}

class NetheriteModMenuIntegration: ModMenuApi{
    override fun getModId(): String? {
        return "netherite"
    }

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*>? {
        return ConfigScreenFactory { parent: Screen? ->
            AutoConfig.getConfigScreen(
                ModConfig::class.java, parent
            ).get()
        }
    }
}