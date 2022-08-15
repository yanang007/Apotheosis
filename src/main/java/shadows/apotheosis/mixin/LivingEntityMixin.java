package shadows.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	/**
	 * Enable RESISTANCE logic when either RESISTANCE and SUNDERING is present.
	 * Called from {@link LivingEntity#getDamageAfterMagicAbsorb(DamageSource, float)}
	 */
	@Redirect(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 0))
	public boolean getDamageAfterMagicAbsorb_hasEffect(LivingEntity instance, MobEffect effect) {
		assert MobEffects.DAMAGE_RESISTANCE == effect; // this indicates that code related to RESISTANCE remains unchanged
		return this.hasEffect(effect) || this.hasEffect(Apoth.Effects.SUNDERING);
	}

	/**
	 * Calculate the equivalent RESISTANCE amplifier with SUNDERING considered.
	 * Called from {@link LivingEntity#getDamageAfterMagicAbsorb(DamageSource, float)}
	 */
	@Redirect(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I"))
	public int getDamageAfterMagicAbsorb_getAmplifier(MobEffectInstance instance) {
		int amplifier = 0;
		if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
			amplifier += this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1;
		}
		if (Apoth.Effects.SUNDERING != null && this.hasEffect(Apoth.Effects.SUNDERING)) {
;			amplifier -= this.getEffect(Apoth.Effects.SUNDERING).getAmplifier() + 1;
		}

		return amplifier - 1; // the return value of this method added by 1, so we need to subtract 1 to get the correct value
	}

	@Shadow
	public abstract boolean hasEffect(MobEffect ef);

	@Shadow
	public abstract MobEffectInstance getEffect(MobEffect ef);

	@Inject(method = "createLivingAttributes", at = @At("RETURN"))
	private static void createLivingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		AttributeSupplier.Builder builder = cir.getReturnValue();
		//Formatter::off
		addIfExists(builder, 
				Apoth.Attributes.DRAW_SPEED, 
				Apoth.Attributes.CRIT_CHANCE, 
				Apoth.Attributes.CRIT_DAMAGE, 
				Apoth.Attributes.COLD_DAMAGE, 
				Apoth.Attributes.FIRE_DAMAGE, 
				Apoth.Attributes.LIFE_STEAL, 
				Apoth.Attributes.PIERCING, 
				Apoth.Attributes.CURRENT_HP_DAMAGE, 
				Apoth.Attributes.OVERHEAL,
				Apoth.Attributes.GHOST_HEALTH,
				Apoth.Attributes.BREAK_SPEED,
				Apoth.Attributes.ARROW_DAMAGE,
				Apoth.Attributes.ARROW_VELOCITY);
		//Formatter::on
	}

	private static void addIfExists(AttributeSupplier.Builder builder, Attribute... attribs) {
		for (Attribute attrib : attribs)
			if (attrib != null) builder.add(attrib);
	}

}
