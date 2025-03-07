package shadows.apotheosis.spawn.compat;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.apotheosis.spawn.modifiers.SpawnerStats;
import shadows.apotheosis.spawn.spawner.ApothSpawnerBlock;
import shadows.apotheosis.spawn.spawner.ApothSpawnerTile;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin, IComponentProvider, IServerDataProvider<BlockEntity> {

	public static final String STATS = "spw_stats";

	@Override
	public void register(IWailaCommonRegistration reg) {
		reg.registerBlockDataProvider(this, ApothSpawnerTile.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration reg) {
		reg.registerComponentProvider(this, TooltipPosition.BODY, ApothSpawnerBlock.class);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (Screen.hasControlDown()) {
			int[] stats = accessor.getServerData().getIntArray(STATS);
			if (stats.length != 12) return;
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MIN_DELAY.name(), stats[0]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MAX_DELAY.name(), stats[1]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_COUNT.name(), stats[2]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.MAX_NEARBY_ENTITIES.name(), stats[3]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.REQ_PLAYER_RANGE.name(), stats[4]));
			tooltip.add(ApothSpawnerBlock.concat(SpawnerStats.SPAWN_RANGE.name(), stats[5]));
			if (stats[6] == 1) tooltip.add(SpawnerStats.IGNORE_PLAYERS.name().withStyle(ChatFormatting.DARK_GREEN));
			if (stats[7] == 1) tooltip.add(SpawnerStats.IGNORE_CONDITIONS.name().withStyle(ChatFormatting.DARK_GREEN));
			if (stats[8] == 1) tooltip.add(SpawnerStats.REDSTONE_CONTROL.name().withStyle(ChatFormatting.DARK_GREEN));
			if (stats[9] == 1) tooltip.add(SpawnerStats.IGNORE_LIGHT.name().withStyle(ChatFormatting.DARK_GREEN));
			if (stats[10] == 1) tooltip.add(SpawnerStats.NO_AI.name().withStyle(ChatFormatting.DARK_GREEN));
			if (stats[11] == 1) tooltip.add(SpawnerStats.SILENT.name().withStyle(ChatFormatting.DARK_GREEN));
		} else tooltip.add(new TranslatableComponent("misc.apotheosis.ctrl_stats"));
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, BlockEntity te, boolean arg4) {
		if (te instanceof ApothSpawnerTile) {
			ApothSpawnerTile spw = (ApothSpawnerTile) te;
			BaseSpawner logic = spw.getSpawner();
			//Formatter::off
			tag.putIntArray(STATS, 
				new int[] { 
					logic.minSpawnDelay, 
					logic.maxSpawnDelay, 
					logic.spawnCount, 
					logic.maxNearbyEntities, 
					logic.requiredPlayerRange, 
					logic.spawnRange, 
					spw.ignoresPlayers ? 1 : 0, 
					spw.ignoresConditions ? 1 : 0, 
					spw.redstoneControl ? 1 : 0,
					spw.ignoresLight ? 1 : 0, 
					spw.hasNoAI ? 1 : 0,
					spw.silent ? 1 : 0
				});
			//Formatter::on
		}
	}

}