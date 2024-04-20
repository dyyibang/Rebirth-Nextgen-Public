package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.item.BowItem;

public class Quiver extends Module {
	public static Quiver INSTANCE;

	public Quiver() {
		super("Quiver", Category.Combat);
		INSTANCE = this;
	}

	@EventHandler(priority = -101)
	public void onRotate(RotateEvent event) {
		if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
			event.setPitch(-90);
		}
	}
}