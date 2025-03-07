package shadows.apotheosis.adventure.affix;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;

public final record AffixInstance(Affix affix, ItemStack stack, LootRarity rarity, float level) {

	/**
	 * Retrieve the modifiers from this affix to be applied to the itemstack.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param type The slot type for modifiers being gathered.
	 * @param map The destination for generated attribute modifiers.
	 */
	public void addModifiers(EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		affix.addModifiers(stack, rarity, level, type, map);
	}

	/**
	 * Adds all tooltip data from this affix to the given stack's tooltip list.
	 * This consumer will insert tooltips immediately after enchantment tooltips, or after the name if none are present.
	 * @param stack The stack the affix is on.
	 * @param level The level of this affix.
	 * @param tooltips The destination for tooltips.
	 */
	public void addInformation(Consumer<Component> list) {
		affix.addInformation(stack, rarity, level, list);
	}

	/**
	 * Get a component representing an addition of this affix to the item's name.
	 * @return The name part, prefix or suffix, as requested.
	 */
	public Component getName(boolean prefix) {
		return affix.getName(stack, rarity, level, prefix);
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param level The level of this affix, if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	public int getDamageProtection(DamageSource source) {
		return affix.getDamageProtection(stack, rarity, level, source);
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getDamageBonus(MobType creatureType) {
		return affix.getDamageBonus(stack, rarity, level, creatureType);
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param level The level of this affix, if applicable.
	 */
	public void doPostAttack(LivingEntity user, @Nullable Entity target) {
		affix.doPostAttack(stack, rarity, level, user, target);
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void doPostHurt(LivingEntity user, @Nullable Entity attacker) {
		affix.doPostHurt(stack, rarity, level, user, attacker);
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
		affix.onArrowFired(stack, rarity, level, user, arrow);
	}

	/**
	 * Called when {@link Item#onItemUse(ItemUseContext)} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(UseOnContext ctx) {
		return affix.onItemUse(stack, rarity, level, ctx);
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param stack  The shield itemstack the affix is on .
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param level  The level of this affix.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
		return affix.onShieldBlock(stack, rarity, level, entity, source, amount);
	}

	public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
		affix.onBlockBreak(stack, rarity, level, player, world, pos, state);
	}

}
