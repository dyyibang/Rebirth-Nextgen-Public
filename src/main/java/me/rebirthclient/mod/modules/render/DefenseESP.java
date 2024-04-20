/**
 * PlayerESP Module
 */
package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DefenseESP extends Module {

	public DefenseESP() {
		super("DefenseESP", Category.Render);
	}

	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	private final BooleanSetting box = add(new BooleanSetting("Box"));
	private final BooleanSetting outline = add(new BooleanSetting("Outline"));
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow"));
	private final BooleanSetting surround = add(new BooleanSetting("Surround"));
	List<BlockPos> renderList = new ArrayList<>();
    @Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		renderList.clear();
		for (Entity player : CombatUtil.getEnemies(10)) {
			if (burrow.getValue()) {
				float[] offset = new float[]{-0.3f, 0f, 0.3f};
				for (float x : offset) {
					for (float z : offset) {
						BlockPos tempPos;
						if (isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0, z)))) {
							renderList.add(tempPos);
						}
					}
				}
			}

			if (surround.getValue()) {
				BlockPos pos = EntityUtil.getEntityPos(player, true);
				if (!BlockUtil.isHole(pos)) continue;
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (isObsidian(pos.offset(i))) {
						renderList.add(pos.offset(i));
					}
				}
			}
		}
		for (BlockPos pos : renderList) {
			Render3DUtil.draw3DBox(matrixStack, new Box(pos), color.getValue(), outline.getValue(), box.getValue());
		}
	}

	private boolean isObsidian(BlockPos pos) {
		return (BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST) && !renderList.contains(pos);
	}
}
