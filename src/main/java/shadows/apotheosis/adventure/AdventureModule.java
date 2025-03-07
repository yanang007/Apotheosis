package shadows.apotheosis.adventure;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.floats.Float2IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;
import shadows.apotheosis.Apotheosis.ApotheosisReloadEvent;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.affix.AttributeAffix;
import shadows.apotheosis.adventure.affix.effect.CleavingAffix;
import shadows.apotheosis.adventure.affix.effect.ExecutingAffix;
import shadows.apotheosis.adventure.affix.effect.FestiveAffix;
import shadows.apotheosis.adventure.affix.effect.MagicalArrowAffix;
import shadows.apotheosis.adventure.affix.effect.OmneticAffix;
import shadows.apotheosis.adventure.affix.effect.PotionAffix;
import shadows.apotheosis.adventure.affix.effect.PotionAffix.Target;
import shadows.apotheosis.adventure.affix.effect.RadialAffix;
import shadows.apotheosis.adventure.affix.effect.RetreatingAffix;
import shadows.apotheosis.adventure.affix.effect.SpectralShotAffix;
import shadows.apotheosis.adventure.affix.effect.TelepathicAffix;
import shadows.apotheosis.adventure.affix.effect.ThunderstruckAffix;
import shadows.apotheosis.adventure.affix.socket.GemItem;
import shadows.apotheosis.adventure.affix.socket.GemManager;
import shadows.apotheosis.adventure.affix.socket.SocketAffix;
import shadows.apotheosis.adventure.affix.socket.SocketingRecipe;
import shadows.apotheosis.adventure.client.AdventureModuleClient;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.apotheosis.adventure.loot.AffixLootModifier;
import shadows.apotheosis.adventure.loot.GemLootModifier;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.util.NameHelper;
import shadows.placebo.config.Configuration;

public class AdventureModule {

	public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Adventure");

	@SubscribeEvent
	public void preInit(ApotheosisConstruction e) {
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR, 40D, "f_22308_");
		ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS, 30D, "f_22308_");
	}

	@SubscribeEvent
	public void init(FMLCommonSetupEvent e) {
		this.reload(null);
		MinecraftForge.EVENT_BUS.register(new AdventureModuleEvents());
		MinecraftForge.EVENT_BUS.addListener(this::reload);
		GemManager.INSTANCE.registerToBus();
		AffixLootManager.INSTANCE.registerToBus();
		Apotheosis.HELPER.registerProvider(f -> {
			f.addRecipe(new SocketingRecipe());
		});
	}

	@SubscribeEvent
	public void register(Register<Feature<?>> e) {
	}

	@SubscribeEvent
	public void items(Register<Item> e) {
		e.getRegistry().register(new GemItem(new Item.Properties().stacksTo(1)).setRegistryName("gem"));
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
	}

	@SubscribeEvent
	public void tiles(Register<BlockEntityType<?>> e) {
	}

	@SubscribeEvent
	public void serializers(Register<RecipeSerializer<?>> e) {
		e.getRegistry().register(SocketingRecipe.Serializer.INSTANCE.setRegistryName("socketing"));
	}

	@SubscribeEvent
	public void lootSerializers(Register<GlobalLootModifierSerializer<?>> e) {
		e.getRegistry().register(new GemLootModifier.Serializer().setRegistryName("gems"));
		e.getRegistry().register(new AffixLootModifier.Serializer().setRegistryName("affix_loot"));
	}

	@SubscribeEvent
	public void registry(NewRegistryEvent e) {
		RegistryBuilder<Affix> build = new RegistryBuilder<>();
		build.setName(new ResourceLocation(Apotheosis.MODID, "affixes"));
		build.setType(Affix.class);
		build.disableSaving().onBake(AffixHelper::recomputeMaps);
		e.create(build, r -> Affix.REGISTRY = (ForgeRegistry<Affix>) r);
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new RangedAttribute("apotheosis:draw_speed", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("draw_speed"),
				new RangedAttribute("apotheosis:crit_chance", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("crit_chance"),
				new RangedAttribute("apotheosis:crit_damage", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("crit_damage"),
				new RangedAttribute("apotheosis:cold_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("cold_damage"),
				new RangedAttribute("apotheosis:fire_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("fire_damage"),
				new RangedAttribute("apotheosis:life_steal", 1.0D, 1.0D, 1024.0D).setSyncable(true).setRegistryName("life_steal"),
				new RangedAttribute("apotheosis:piercing", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("piercing"),
				new RangedAttribute("apotheosis:current_hp_damage", 1.0D, 1.0D, 2.0D).setSyncable(true).setRegistryName("current_hp_damage"),
				new RangedAttribute("apotheosis:overheal", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("overheal"),
				new RangedAttribute("apotheosis:ghost_health", 0.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("ghost_health"),
				new RangedAttribute("apotheosis:break_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("break_speed"),
				new RangedAttribute("apotheosis:arrow_damage", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("arrow_damage"),
				new RangedAttribute("apotheosis:arrow_velocity", 1.0D, 0.0D, 1024.0D).setSyncable(true).setRegistryName("arrow_velocity")
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void affixes(Register<Affix> e) {
		//Formatter::off
		e.getRegistry().registerAll(
				new AttributeAffix.Builder(() -> Attributes.MAX_HEALTH, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 3, 0.5F))
				.with(LootRarity.UNCOMMON, step(1.5F, 4, 0.5F))
				.with(LootRarity.RARE, step(2F, 6, 0.5F))
				.with(LootRarity.EPIC, step(3F, 7, 0.5F))
				.with(LootRarity.MYTHIC, step(4F, 14, 0.5F))
				.with(LootRarity.ANCIENT, step(8F, 20, 0.5F))
				.types(LootCategory::isDefensive).build("blessed"),

				new AttributeAffix.Builder(() -> Attributes.ARMOR, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.25F, 3, 0.25F))
				.with(LootRarity.UNCOMMON, step(1F, 4, 0.25F))
				.with(LootRarity.RARE, step(2F, 6, 0.25F))
				.with(LootRarity.EPIC, step(3F, 8, 0.25F))
				.with(LootRarity.MYTHIC, step(4F, 10, 0.25F))
				.with(LootRarity.ANCIENT, step(5F, 16, 0.25F))
				.types(LootCategory::isDefensive).build("ironforged"),

				new AttributeAffix.Builder(() -> Attributes.ATTACK_DAMAGE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 4, 0.25F))
				.with(LootRarity.UNCOMMON, step(1F, 6, 0.25F))
				.with(LootRarity.RARE, step(1.5F, 8, 0.25F))
				.with(LootRarity.EPIC, step(2F, 12, 0.25F))
				.with(LootRarity.MYTHIC, step(3.5F, 14, 0.25F))
				.with(LootRarity.ANCIENT, step(5F, 20, 0.25F))
				.types(LootCategory::isWeapon).build("violent"),

				new AttributeAffix.Builder(() -> Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 10, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.08F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.12F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.15F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.18F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.25F, 10, 0.01F))
				.build("windswept"),

				new AttributeAffix.Builder(() -> Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(l -> !l.isRanged()).build("graceful"),

				new AttributeAffix.Builder(() -> Attributes.ATTACK_KNOCKBACK, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.1F, 10, 0.02F))
				.with(LootRarity.UNCOMMON, step(0.2F, 20, 0.02F))
				.with(LootRarity.RARE, step(0.4F, 30, 0.02F))
				.with(LootRarity.EPIC, step(0.6F, 30, 0.02F))
				.with(LootRarity.MYTHIC, step(0.8F, 30, 0.02F))
				.with(LootRarity.ANCIENT, step(1F, 50, 0.02F))
				.types(LootCategory::isWeaponOrShield).build("forceful"),

				new AttributeAffix.Builder(ForgeMod.SWIM_SPEED, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(0.05F, 10, 0.02F))
				.with(LootRarity.UNCOMMON, step(0.08F, 10, 0.02F))
				.with(LootRarity.RARE, step(0.12F, 10, 0.02F))
				.with(LootRarity.EPIC, step(0.15F, 12, 0.02F))
				.with(LootRarity.MYTHIC, step(0.18F, 14, 0.02F))
				.with(LootRarity.ANCIENT, step(0.25F, 10, 0.02F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.FEET).build("aquatic"),

				new AttributeAffix.Builder(ForgeMod.ENTITY_GRAVITY, Operation.MULTIPLY_TOTAL)
				.with(LootRarity.COMMON, step(-0.05F, 2, -0.02F))
				.with(LootRarity.UNCOMMON, step(-0.05F, 4, -0.02F))
				.with(LootRarity.RARE, step(-0.05F, 6, -0.02F))
				.with(LootRarity.EPIC, step(-0.05F, 8, -0.02F))
				.with(LootRarity.MYTHIC, step(-0.05F, 10, -0.02F))
				.with(LootRarity.ANCIENT, step(-0.05F, 10, -0.02F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.CHEST).build("gravitational"),

				new AttributeAffix.Builder(ForgeMod.REACH_DISTANCE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 4, 0.25F))
				.with(LootRarity.UNCOMMON, step(0.75F, 5, 0.25F))
				.with(LootRarity.RARE, step(1F, 6, 0.25F))
				.with(LootRarity.EPIC, step(1.5F, 8, 0.25F))
				.with(LootRarity.MYTHIC, step(2F, 10, 0.25F))
				.with(LootRarity.ANCIENT, step(2.5F, 16, 0.25F))
				.types(l -> l == LootCategory.BREAKER).build("lengthy"),

				new AttributeAffix.Builder(ForgeMod.ATTACK_RANGE, Operation.ADDITION)
				.with(LootRarity.COMMON, step(0.5F, 4, 0.25F))
				.with(LootRarity.UNCOMMON, step(0.75F, 5, 0.25F))
				.with(LootRarity.RARE, step(1F, 6, 0.25F))
				.with(LootRarity.EPIC, step(1.5F, 8, 0.25F))
				.with(LootRarity.MYTHIC, step(2F, 10, 0.25F))
				.with(LootRarity.ANCIENT, step(2.5F, 16, 0.25F))
				.types(LootCategory::isWeapon).build("elongated"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.DRAW_SPEED, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, l -> l > 0.5F ? 0.2F : 0.1F)
				.with(LootRarity.UNCOMMON, l -> l > 0.5F ? 0.33F : 0.25F)
				.with(LootRarity.RARE, l -> l > 0.5F ? 1F : 0.5F)
				.with(LootRarity.EPIC, l -> l > 0.5F ? 1.2F : 1.1F)
				.with(LootRarity.MYTHIC, l -> l > 0.5F ? 1.5F : 1.33F)
				.with(LootRarity.ANCIENT, l -> l > 0.5F ? 2.5F : 2F)
				.types(LootCategory::isRanged).build("agile"),

				new AttributeAffix.Builder(() -> Attributes.ARMOR_TOUGHNESS, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.25F, 2, 0.25F))
				.with(LootRarity.RARE, step(0.5F, 3, 0.25F))
				.with(LootRarity.EPIC, step(1F, 4, 0.25F))
				.with(LootRarity.MYTHIC, step(1.5F, 5, 0.25F))
				.with(LootRarity.ANCIENT, step(3F, 8, 0.25F))
				.types(LootCategory::isDefensive).build("steel_touched"),
				
				new AttributeAffix.Builder(() -> Attributes.KNOCKBACK_RESISTANCE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.04F, 2, 0.02F))
				.with(LootRarity.RARE, step(0.06F, 3, 0.02F))
				.with(LootRarity.EPIC, step(0.1F, 6, 0.02F))
				.with(LootRarity.MYTHIC, step(0.2F, 8, 0.02F))
				.with(LootRarity.ANCIENT, step(0.4F, 12, 0.02F)) 
				.types(LootCategory::isDefensive).build("stalwart"),
				
				new AttributeAffix.Builder(ForgeMod.STEP_HEIGHT_ADDITION, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(0.25F, 1, 0.25F))
				.with(LootRarity.RARE, step(0.25F, 3, 0.25F))
				.with(LootRarity.EPIC, step(0.25F, 4, 0.25F))
				.with(LootRarity.MYTHIC, step(0.25F, 6, 0.25F))
				.with(LootRarity.ANCIENT, step(0.25F, 7, 0.25F))
				.types(l -> l == LootCategory.ARMOR).items(s -> ((ArmorItem) s.getItem()).getSlot() == EquipmentSlot.FEET).build("elastic"),
		
				new AttributeAffix.Builder(() -> Attributes.LUCK, Operation.ADDITION)
				.with(LootRarity.RARE, step(1F, 6, 0.25F))
				.with(LootRarity.EPIC, step(1.25F, 6, 0.25F))
				.with(LootRarity.MYTHIC, step(1.5F, 6, 0.25F))
				.with(LootRarity.ANCIENT, step(2F, 12, 0.25F))
				.types(l -> l == LootCategory.ARMOR).build("fortunate"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_CHANCE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(l -> !l.isRanged()).build("intricate"),
				
				new AttributeAffix.Builder(() -> Apoth.Attributes.CRIT_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(LootCategory::isWeapon).build("lacerating"),
				
				new AttributeAffix.Builder(() -> Apoth.Attributes.COLD_DAMAGE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(1F, 6, 0.25F))
				.with(LootRarity.RARE, step(2F, 8, 0.25F))
				.with(LootRarity.EPIC, step(2F, 10, 0.25F))
				.with(LootRarity.MYTHIC, step(3F, 12, 0.25F))
				.with(LootRarity.ANCIENT, step(4F, 12, 0.25F))
				.types(LootCategory::isWeapon).build("glacial"),
				
				new AttributeAffix.Builder(() -> Apoth.Attributes.FIRE_DAMAGE, Operation.ADDITION)
				.with(LootRarity.UNCOMMON, step(1F, 6, 0.25F))
				.with(LootRarity.RARE, step(2F, 8, 0.25F))
				.with(LootRarity.EPIC, step(2F, 10, 0.25F))
				.with(LootRarity.MYTHIC, step(3F, 12, 0.25F))
				.with(LootRarity.ANCIENT, step(4F, 12, 0.25F))
				.types(LootCategory::isWeapon).build("infernal"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.LIFE_STEAL, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(LootCategory::isWeapon).build("vampiric"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.PIERCING, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 15, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 20, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 25, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 40, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("shredding"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.CURRENT_HP_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.01F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.04F, 6, 0.01F))
				.with(LootRarity.RARE, step(0.09F, 7, 0.01F))
				.with(LootRarity.EPIC, step(0.13F, 8, 0.01F))
				.with(LootRarity.MYTHIC, step(0.17F, 12, 0.01F))
				.with(LootRarity.ANCIENT, step(0.25F, 20, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("giant_slaying"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.OVERHEAL, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(l -> l == LootCategory.HEAVY_WEAPON).build("berserking"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.BREAK_SPEED, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(l -> l == LootCategory.BREAKER).build("destructive"),
				
				new AttributeAffix.Builder(() -> Apoth.Attributes.ARROW_DAMAGE, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.20F, 15, 0.01F))
				.with(LootRarity.RARE, step(0.25F, 15, 0.01F))
				.with(LootRarity.EPIC, step(0.40F, 20, 0.01F))
				.with(LootRarity.MYTHIC, step(0.50F, 20, 0.01F))
				.with(LootRarity.ANCIENT, step(0.70F, 30, 0.01F))
				.types(LootCategory::isRanged).build("elven"),

				new AttributeAffix.Builder(() -> Apoth.Attributes.ARROW_VELOCITY, Operation.MULTIPLY_BASE)
				.with(LootRarity.COMMON, step(0.05F, 5, 0.01F))
				.with(LootRarity.UNCOMMON, step(0.10F, 10, 0.01F))
				.with(LootRarity.RARE, step(0.15F, 10, 0.01F))
				.with(LootRarity.EPIC, step(0.20F, 12, 0.01F))
				.with(LootRarity.MYTHIC, step(0.30F, 14, 0.01F))
				.with(LootRarity.ANCIENT, step(0.40F, 10, 0.01F))
				.types(LootCategory::isRanged).build("streamlined"),

				new SocketAffix().setRegistryName("socket"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SPEED)
				.with(LootRarity.RARE, step(100, 5, 20), l -> 0)
				.with(LootRarity.EPIC, step(140, 8, 20), l -> 0)
				.with(LootRarity.MYTHIC, step(180, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(240, 10, 40), l -> 2)
				.types(LootCategory::isWeapon)
				.build(AffixType.EFFECT, Target.ATTACK_SELF, "elusive"),

				new PotionAffix.Builder(() -> MobEffects.DIG_SPEED)
				.with(LootRarity.RARE, step(100, 5, 20), l -> 0)
				.with(LootRarity.EPIC, step(140, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(180, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(240, 10, 40), l -> 2)
				.types(l -> l == LootCategory.BREAKER)
				.build(AffixType.EFFECT, Target.BREAK_SELF, "swift"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SLOWDOWN)
				.with(LootRarity.RARE, step(20, 2, 20), l -> 0)
				.with(LootRarity.EPIC, step(60, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(80, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(120, 10, 40), l -> 2)
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "ensnaring"),

				new PotionAffix.Builder(() -> MobEffects.POISON)
				.with(LootRarity.RARE, step(60, 5, 20), l -> 0)
				.with(LootRarity.EPIC, step(100, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(120, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(160, 10, 40), l -> 2)
				.types(l -> l == LootCategory.SHIELD)
				.build(AffixType.EFFECT, Target.BLOCK_ATTACKER, "venomous"),

				new PotionAffix.Builder(() -> MobEffects.MOVEMENT_SPEED)
				.with(LootRarity.RARE, step(60, 5, 20), l -> 0)
				.with(LootRarity.EPIC, step(80, 8, 20), l -> 0)
				.with(LootRarity.MYTHIC, step(100, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(120, 10, 40), l -> 2)
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_SELF, "fleeting"),
				
				new PotionAffix.Builder(() -> MobEffects.WEAKNESS)
				.with(LootRarity.RARE, step(40, 5, 20), l -> 0)
				.with(LootRarity.EPIC, step(60, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(80, 10, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(120, 10, 40), l -> 2)
				.types(LootCategory::isWeapon)
				.build(AffixType.EFFECT, Target.ATTACK_TARGET, "weakening"),
				
				new PotionAffix.Builder(() -> MobEffects.DAMAGE_RESISTANCE)
				.with(LootRarity.RARE, step(20, 3, 20), l -> 0)
				.with(LootRarity.EPIC, step(40, 4, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 6, 20), step(0, 1, 1))
				.with(LootRarity.ANCIENT, step(100, 10, 40), step(1, 2, 1))
				.types(l -> l == LootCategory.ARMOR)
				.build(AffixType.EFFECT, Target.HURT_SELF, "bolstering"),

				new PotionAffix.Builder(() -> MobEffects.HEAL)
				.with(LootRarity.EPIC, l -> 1, step(0, 1, 1))
				.with(LootRarity.MYTHIC, l -> 1, step(0, 2, 1))
				.with(LootRarity.ANCIENT, l -> 1, step(1, 3, 1))
				.types(l -> l == LootCategory.ARMOR)
				.build(AffixType.EFFECT, Target.HURT_SELF, "revitalizing"),

				new PotionAffix.Builder(() -> MobEffects.LEVITATION)
				.with(LootRarity.EPIC, step(10, 3, 10), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(10, 4, 10), step(0, 2, 1))
				.with(LootRarity.ANCIENT, step(20, 5, 10), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "shulkers"),
				
				new PotionAffix.Builder(() -> MobEffects.WITHER)
				.with(LootRarity.EPIC, step(40, 8, 20), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(60, 10, 20), step(0, 1, 2))
				.with(LootRarity.ANCIENT, step(100, 10, 40), l -> 2)
				.types(l -> l == LootCategory.SHIELD)
				.build(AffixType.EFFECT, Target.BLOCK_ATTACKER, "withering"),

				new PotionAffix.Builder(() -> MobEffects.POISON)
				.with(LootRarity.EPIC, step(20, 7, 10), step(0, 1, 1))
				.with(LootRarity.MYTHIC, step(20, 9, 10), step(0, 2, 1))
				.with(LootRarity.ANCIENT, step(40, 13, 10), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "ivy_laced"),
				
				new PotionAffix.Builder(() -> MobEffects.WITHER)
				.with(LootRarity.MYTHIC, step(140, 3, 20), step(1, 3, 1))
				.with(LootRarity.ANCIENT, step(180, 5, 20), step(1, 3, 1))
				.types(LootCategory::isRanged)
				.build(AffixType.EFFECT, Target.ARROW_TARGET, "satanic"),
				
				new SpectralShotAffix().setRegistryName("spectral"),
				new MagicalArrowAffix().setRegistryName("magical"),
				new FestiveAffix().setRegistryName("festive"),
				new ThunderstruckAffix().setRegistryName("thunderstruck"),
				new RetreatingAffix().setRegistryName("retreating"),
				new TelepathicAffix().setRegistryName("telepathic"),
				new ExecutingAffix().setRegistryName("executing"),
				new CleavingAffix().setRegistryName("cleaving"),
				new OmneticAffix().setRegistryName("omnetic"),
				new RadialAffix().setRegistryName("radial")
		);
		//Formatter::on
	}

	/**
	 * Level Function that allows for only returning "nice" stepped numbers.
	 * @param min The min value
	 * @param steps The max number of steps
	 * @param step The value per step
	 * @return A level function according to these rules
	 */
	private static Float2FloatFunction step(float min, int steps, float step) {
		return AffixHelper.step(min, steps, step);
	}

	private static Float2IntFunction step(int min, int steps, int step) {
		return AffixHelper.step(min, steps, step);
	}

	@SubscribeEvent
	public void client(FMLClientSetupEvent e) {
		e.enqueueWork(AdventureModuleClient::init);
	}

	/**
	 * Loads all configurable data for the deadly module.
	 */
	public void reload(ApotheosisReloadEvent e) {
		Configuration mainConfig = new Configuration(new File(Apotheosis.configDir, "adventure.cfg"));
		Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
		AdventureConfig.load(mainConfig);
		NameHelper.load(nameConfig);
		if (e == null && mainConfig.hasChanged()) mainConfig.save();
		if (e == null && nameConfig.hasChanged()) nameConfig.save();
	}

	public static final boolean DEBUG = false;

	public static void debugLog(BlockPos pos, String name) {
		if (DEBUG) AdventureModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
	}

}