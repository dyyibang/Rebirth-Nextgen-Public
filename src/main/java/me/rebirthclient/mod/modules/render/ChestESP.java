/**
 * ChestESP Module
 */
package me.rebirthclient.mod.modules.render;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.Render3DUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;

public class ChestESP extends Module {

	public final ColorSetting color = add(new ColorSetting("Color",new Color(255, 255, 255, 100)));
	
	public ChestESP() {
		super("ChestESP", Category.Render);
		this.setDescription("Allows the player to see Chests with an ESP.");
	}


	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
		for(BlockEntity blockEntity : blockEntities) {
			if(blockEntity instanceof ChestBlockEntity) {
				Box box = new Box(blockEntity.getPos());
				Render3DUtil.draw3DBox(matrixStack, box, color.getValue());
			}
		}
	}

}
