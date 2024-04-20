package me.rebirthclient.mod.modules.player;

import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class MCP extends Module {
	public static MCP INSTANCE;

	public MCP() {
		super("MCP", Category.Player);
		INSTANCE = this;
	}

	private final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap"));
	boolean click = false;
	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (mc.mouse.wasMiddleButtonClicked()) {
			if (!click) {
				int pearl;
				if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
					EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
				} else if (inventory.getValue() && (pearl = InventoryUtil.findItemInventorySlot(Items.ENDER_PEARL)) != -1) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, pearl, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
					EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, pearl, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
					EntityUtil.sync();
				} else if ((pearl = InventoryUtil.findItem(Items.ENDER_PEARL)) != -1) {
					int old = mc.player.getInventory().selectedSlot;
					InventoryUtil.doSwap(pearl);
					EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
					mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
					InventoryUtil.doSwap(old);
				}
				click = true;
			}
		} else click = false;
	}
}