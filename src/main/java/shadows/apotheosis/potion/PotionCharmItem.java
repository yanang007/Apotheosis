package shadows.apotheosis.potion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;

public class PotionCharmItem extends Item {

	public static final Set<ResourceLocation> EXTENDED_POTIONS = new HashSet<>();

	public PotionCharmItem() {
		super(new Item.Properties().stacksTo(1).durability(192).tab(Apotheosis.APOTH_GROUP).setNoRepair());
	}

	@Override
	public ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.LONG_INVISIBILITY);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
		if (!hasPotion(stack)) return;
		if (stack.getOrCreateTag().getBoolean("charm_enabled") && entity instanceof ServerPlayer) {
			Potion p = PotionUtils.getPotion(stack);
			MobEffectInstance contained = p.getEffects().get(0);
			MobEffectInstance active = ((ServerPlayer) entity).getEffect(contained.getEffect());
			if (active == null || active.getDuration() < getCriticalDuration(active.getEffect())) {
				int durationOffset = getCriticalDuration(contained.getEffect());
				if (contained.getEffect() == MobEffects.REGENERATION) durationOffset += 50 >> contained.getAmplifier();
				MobEffectInstance newEffect = new MobEffectInstance(contained.getEffect(), (int) Math.ceil(contained.getDuration() / 24D) + durationOffset, contained.getAmplifier(), false, false);
				((ServerPlayer) entity).addEffect(newEffect);
				if (stack.hurt(contained.getEffect() == MobEffects.REGENERATION ? 2 : 1, world.random, (ServerPlayer) entity)) stack.shrink(1);
			}
		}
	}

	private static int getCriticalDuration(MobEffect effect) {
		return EXTENDED_POTIONS.contains(effect.getRegistryName()) ? 210 : 5;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean("charm_enabled");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide) {
			stack.getOrCreateTag().putBoolean("charm_enabled", !stack.getTag().getBoolean("charm_enabled"));
		} else if (!stack.getTag().getBoolean("charm_enabled")) world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1, 0.3F);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		if (hasPotion(stack)) {
			Potion p = PotionUtils.getPotion(stack);
			MobEffectInstance effect = p.getEffects().get(0);
			TranslatableComponent potionCmp = new TranslatableComponent(effect.getDescriptionId());
			if (effect.getAmplifier() > 0) {
				potionCmp = new TranslatableComponent("potion.withAmplifier", potionCmp, new TranslatableComponent("potion.potency." + effect.getAmplifier()));
			}
			potionCmp.withStyle(effect.getEffect().getCategory().getTooltipFormatting());
			tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".desc", potionCmp).withStyle(ChatFormatting.GRAY));
			boolean enabled = stack.getOrCreateTag().getBoolean("charm_enabled");
			TranslatableComponent enabledCmp = new TranslatableComponent(this.getDescriptionId() + (enabled ? ".enabled" : ".disabled"));
			enabledCmp.withStyle(enabled ? ChatFormatting.BLUE : ChatFormatting.RED);
			if (effect.getDuration() > 20) {
				potionCmp = new TranslatableComponent("potion.withDuration", potionCmp, MobEffectUtil.formatDuration(effect, 1));
			}
			potionCmp.withStyle(effect.getEffect().getCategory().getTooltipFormatting());
			tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".desc3", potionCmp).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		if (!hasPotion(stack)) return 1;
		return 192;
	}

	@Override
	public Component getName(ItemStack stack) {
		if (!hasPotion(stack)) return new TranslatableComponent("item.apotheosis.potion_charm_broke");
		Potion p = PotionUtils.getPotion(stack);
		MobEffectInstance effect = p.getEffects().get(0);
		TranslatableComponent potionCmp = new TranslatableComponent(effect.getDescriptionId());
		if (effect.getAmplifier() > 0) {
			potionCmp = new TranslatableComponent("potion.withAmplifier", potionCmp, new TranslatableComponent("potion.potency." + effect.getAmplifier()));
		}
		return new TranslatableComponent("item.apotheosis.potion_charm", potionCmp);
	}

	public static boolean hasPotion(ItemStack stack) {
		return PotionUtils.getPotion(stack) != Potions.EMPTY;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			for (Potion potion : ForgeRegistries.POTIONS) {
				if (potion.getEffects().size() == 1 && !potion.getEffects().get(0).getEffect().isInstantenous()) {
					items.add(PotionUtils.setPotion(new ItemStack(this), potion));
				}
			}
		}

	}

}