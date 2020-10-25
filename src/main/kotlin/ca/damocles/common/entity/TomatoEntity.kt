package ca.damocles.common.entity

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.particle.ItemStackParticleEffect
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class TomatoEntity: ThrownItemEntity {

    constructor(entityType: EntityType<out TomatoEntity?>?, world: World?): super(entityType, world) {}

    constructor(world: World?, owner: LivingEntity?): super(EntityType.SNOWBALL, owner, world) {}

    constructor(world: World?, x: Double, y: Double, z: Double): super(EntityType.SNOWBALL, x, y, z, world) {}

    override fun getDefaultItem(): Item? {
        return Items.SNOWBALL
    }

    @Environment(EnvType.CLIENT)
    private fun getParticleParameters(): ParticleEffect? {
        val itemStack = this.item
        return (if (itemStack.isEmpty) ParticleTypes.ITEM_SNOWBALL else ItemStackParticleEffect(ParticleTypes.ITEM, itemStack)) as ParticleEffect
    }

    @Environment(EnvType.CLIENT)
    override fun handleStatus(status: Byte) {
        if (status.toInt() == 3) {
            val particleEffect = getParticleParameters()
            for (i in 0..7) {
                world.addParticle(particleEffect, this.x, this.y, this.z, 0.0, 0.0, 0.0)
            }
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        val entity = entityHitResult.entity
        val i = if (entity is BlazeEntity) 3 else 0
        entity.damage(DamageSource.thrownProjectile(this, this.owner), i.toFloat())
    }

    override fun onCollision(hitResult: HitResult?) {
        super.onCollision(hitResult)
        if (!world.isClient) {
            world.sendEntityStatus(this, 3.toByte())
            this.remove()
        }
    }

}