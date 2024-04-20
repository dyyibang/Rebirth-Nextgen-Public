/**
 * Anti-Invis Module
 */
package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import static me.rebirthclient.api.util.BlockUtil.getBlock;

public class AutoCity extends Module {
	public static AutoCity INSTANCE;
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 0.0, 8.0, 0.1));
	public AutoCity() {
		super("AutoCity", Category.Combat);
		INSTANCE = this;
	}
	@Override
	public void onUpdate() {
		PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
		if (player == null) return;
		BlockPos pos = EntityUtil.getEntityPos(player, true);
		BlockPos pos1 = new BlockPos(MathHelper.floor(player.getX() + 0.2), MathHelper.floor(player.getY() + 0.5), MathHelper.floor(player.getZ() + 0.2));
		BlockPos pos2 = new BlockPos(MathHelper.floor(player.getX() - 0.2), MathHelper.floor(player.getY() + 0.5), MathHelper.floor(player.getZ() + 0.2));
		BlockPos pos3 = new BlockPos(MathHelper.floor(player.getX() + 0.2), MathHelper.floor(player.getY() + 0.5), MathHelper.floor(player.getZ() - 0.2));
		BlockPos pos4 = new BlockPos(MathHelper.floor(player.getX() - 0.2), MathHelper.floor(player.getY() + 0.5), MathHelper.floor(player.getZ() - 0.2));
		if (isObsidian(pos)) {
			PacketMine.INSTANCE.mine(pos);
			return;
		}
		if (isObsidian(pos1)) {
			PacketMine.INSTANCE.mine(pos1);
			return;
		}
		if (isObsidian(pos2)) {
			PacketMine.INSTANCE.mine(pos2);
			return;
		}
		if (isObsidian(pos3)) {
			PacketMine.INSTANCE.mine(pos3);
			return;
		}
		if (isObsidian(pos4)) {
			PacketMine.INSTANCE.mine(pos4);
			return;
		}
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if (BlockUtil.isAir(pos.offset(i))) {
				return;
			}
		}
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if (!PacketMine.godBlocks.contains(getBlock(pos.offset(i)))) {
				PacketMine.INSTANCE.mine(pos.offset(i));
				return;
			}
		}
	}

	private boolean isObsidian(BlockPos pos) {
		return (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST) && BlockUtil.getClickSideStrict(pos) != null && (!pos.equals(PacketMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem));
	}
}