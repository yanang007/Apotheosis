package shadows.apotheosis.adventure.affix.socket;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.affix.AffixHelper;

public class SocketHelper {

	public static final String GEMS = "gems";

	public static List<ItemStack> getGems(ItemStack stack) {
		return getGems(stack, getSockets(stack));
	}

	public static List<ItemStack> getGems(ItemStack stack, int size) {
		List<ItemStack> gems = NonNullList.withSize(size, ItemStack.EMPTY);
		int i = 0;
		CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
		if (afxData != null && afxData.contains(GEMS)) {
			ListTag gemData = afxData.getList(GEMS, Tag.TAG_COMPOUND);
			for (Tag tag : gemData) {
				gems.set(i++, ItemStack.of((CompoundTag) tag));
			}
		}
		return gems;
	}

	public static void setGems(ItemStack stack, List<ItemStack> gems) {
		CompoundTag afxData = stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
		ListTag gemData = new ListTag();
		for (ItemStack s : gems) {
			gemData.add(s.save(new CompoundTag()));
		}
		afxData.put(GEMS, gemData);
	}

	public static int getSockets(ItemStack stack) {
		var inst = AffixHelper.getAffixes(stack).get(Apoth.Affixes.SOCKET);
		if (inst == null) return 0;
		return (int) inst.level();
	}

	public static int getEmptySockets(ItemStack stack) {
		return (int) getGems(stack).stream().filter(ItemStack::isEmpty).count();
	}

}
