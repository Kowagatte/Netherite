package ca.damocles

import ca.damocles.common.block.GoldenHopperBlock
import ca.damocles.common.block.MilkCauldronBlock
import ca.damocles.common.block.RiceBlock
import ca.damocles.common.block.entity.GoldenHopperBlockEntity
import ca.damocles.common.entity.TomatoEntity
import ca.damocles.common.item.MilkBottle
import ca.damocles.common.item.Tomato
import ca.damocles.common.screenhandler.GoldenHopperBlockScreenHandler
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.SimpleClientHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.*
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.util.Rarity
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

@Suppress("unused")
object NetheriteRegistry{
    val registerObjects = Object()

    /** Screen Handlers */
    val GOLDEN_HOPPER_BLOCK_SCREEN_HANDLER: ScreenHandlerType<GoldenHopperBlockScreenHandler> = ScreenHandlerRegistry.registerSimple(getIdentifier("golden_hopper")) { int: Int, inventory: PlayerInventory ->
        GoldenHopperBlockScreenHandler(int, inventory)
    }

    /** Properties */
    val LEVEL_1: IntProperty = IntProperty.of("level", 0, 1)
    val HOPPER_FACING: DirectionProperty = DirectionProperty.of("facing") { facing: Direction -> facing != Direction.UP }
    val ENABLED: BooleanProperty = BooleanProperty.of("enabled")

    private fun registerFood(nameSpace: String, hunger: Int, saturation: Float, rarity: Rarity = Rarity.COMMON, statusEffect: Pair<StatusEffectInstance, Float>? = null, alwaysEdible: Boolean = false): Item{
        var foodBuilder = FoodComponent.Builder().hunger(hunger).saturationModifier(saturation)
        if(alwaysEdible)
            foodBuilder = foodBuilder.alwaysEdible()
        if(statusEffect != null)
            foodBuilder = foodBuilder.statusEffect(statusEffect.first, statusEffect.second)
        return Registry.register(Registry.ITEM, getIdentifier(nameSpace), Item(FabricItemSettings().rarity(rarity).group(ItemGroup.FOOD).food(foodBuilder.build())))
    }

    private fun registerItem(nameSpace: String, group: ItemGroup, rarity: Rarity = Rarity.COMMON): Item{
        return Registry.register(Registry.ITEM, getIdentifier(nameSpace), Item(
                FabricItemSettings().rarity(rarity).group(group)))
    }

    /** ENTITIES */
    val TOMATO_ENTITY: EntityType<TomatoEntity> = Registry.register(
            Registry.ENTITY_TYPE,
            getIdentifier("tomato"),
            FabricEntityTypeBuilder.create<TomatoEntity>().dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build())

    /** BLOCKS */
    val GOLDEN_HOPPER: Block = Registry.register(
            Registry.BLOCK, getIdentifier("golden_hopper"), GoldenHopperBlock(
            AbstractBlock.Settings.copy(Blocks.HOPPER)))

    val TEA_FLOWER_BLOCK: Block = Registry.register(
            Registry.BLOCK, getIdentifier("tea_flower"), FlowerBlock(
            StatusEffects.NIGHT_VISION,
            5,
            AbstractBlock.Settings.of(Material.PLANT).noCollision().breakInstantly().sounds(
                    BlockSoundGroup.GRASS)))

    val RICE: Block = Registry.register(
            Registry.BLOCK, getIdentifier("rice"), RiceBlock(
            AbstractBlock.Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(
                    BlockSoundGroup.CROP)))

    val MILK_CAULDRON: Block = Registry.register(
            Registry.BLOCK, getIdentifier("milk_cauldron"),
            MilkCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON)))

    /** BLOCK ENTITY */
    var GOLDEN_HOPPER_BLOCK_ENTITY: BlockEntityType<GoldenHopperBlockEntity> = Registry.register(
            Registry.BLOCK_ENTITY_TYPE, getIdentifier("golden_hopper"),
            BlockEntityType.Builder.create(Supplier { GoldenHopperBlockEntity() }, GOLDEN_HOPPER).build(null))

    /** ITEMS */
    val GOLDEN_HOPPER_ITEM: Item = Registry.register(
            Registry.ITEM, getIdentifier("golden_hopper"), BlockItem(
            GOLDEN_HOPPER, Item.Settings().group(ItemGroup.REDSTONE)))

    /** SEEDS */
    val TEA_FLOWER_ITEM: Item = Registry.register(
            Registry.ITEM, getIdentifier("tea_flower"), BlockItem(
            TEA_FLOWER_BLOCK,
            Item.Settings().group(ItemGroup.DECORATIONS)))

    val RICE_SEEDS: Item = Registry.register(
            Registry.ITEM, getIdentifier("rice_seeds"), AliasedBlockItem(
            RICE, Item.Settings().group(
            ItemGroup.MATERIALS)))

    /** FOODS */
    val TEA_LEAF_ITEM: Item = registerItem("tea_leaves", ItemGroup.FOOD, Rarity.RARE)
    val RICE_ITEM: Item = registerFood("rice", 1, 1f)
    val ONIGIRI: Item = registerFood("onigiri", 3, 0.5f)
    val SHRIMP: Item = registerItem("shrimp", ItemGroup.FOOD)
    val NIGIRI: Item = registerFood("nigiri", 2, 1f)
    val RAW_CHICKEN_LEG: Item = registerFood("raw_chicken_leg", 1, 1f, statusEffect = Pair(StatusEffectInstance(StatusEffects.HUNGER, 600), 0.3f))
    val CUP: Item = registerItem("cup", ItemGroup.MISC)
    val WATER_CUP: Item = registerFood("water_cup", 0, 1f, alwaysEdible = true)
    val TEA: Item = registerFood("tea", 4, 0.75f, Rarity.RARE, Pair(StatusEffectInstance(StatusEffects.LUCK, 600), 1.0f), true)
    val BANANA: Item = registerFood("banana", 2, 0.5f)
    val LETTUCE: Item = registerFood("lettuce", 1, 0f)
    val CHEESE: Item = registerFood("cheese", 1, 0f)
    val TOMATO: Item = Registry.register(
            Registry.ITEM, getIdentifier("tomato"), Tomato(
            FabricItemSettings().group(ItemGroup.FOOD).rarity(Rarity.COMMON)))
    val MILK_BOTTLE: Item = Registry.register(
            Registry.ITEM, getIdentifier("milk_bottle"), MilkBottle(
            FabricItemSettings().group(ItemGroup.FOOD).rarity(Rarity.COMMON).maxCount(16)))
    val CHOCOLATE_BAR: Item = registerFood("chocolate_bar", 2, 1.5f)
    val DOUGH: Item = registerItem("dough", ItemGroup.FOOD)
    val CHOCOLATE_DONUT: Item = registerFood("chocolate_donut", 2, 0.5f)
    val CHOCOLATE_BANANA: Item = registerFood("chocolate_banana", 1, 3f)
    val GARLIC: Item = registerItem("garlic", ItemGroup.FOOD)
    val SAUSAGE: Item = registerFood("sausage", 1, 1.5f)
    val HOT_DOG: Item = registerFood("hot_dog", 3, 0.5f)
    val PEPPERONI: Item = registerFood("pepperoni", 2, 1.5f)
    val TOMATO_SAUCE: Item = registerItem("tomato_sauce", ItemGroup.FOOD)
    val PEPPERONI_PIZZA: Item = registerFood("pepperoni_pizza", 2, 1.5f)
    val PIZZA_SLICE: Item = registerFood("pizza_slice", 2, 1.5f)
    val SEED_OIL: Item = registerItem("seed_oil", ItemGroup.FOOD)
}