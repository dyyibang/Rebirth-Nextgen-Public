/**
 * A class to represent a system that manages all of the Modules.
 */
package me.rebirthclient.api.managers;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.screens.ClickGuiScreen;
import me.rebirthclient.mod.modules.ExtraModule;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.client.ArrayListModule;
import me.rebirthclient.mod.modules.client.Chat;
import me.rebirthclient.mod.modules.client.ClickGui;
import me.rebirthclient.mod.modules.combat.*;
import me.rebirthclient.mod.modules.combat.autotrap.AutoTrap;
import me.rebirthclient.mod.modules.combat.autotrap.ExtraAutoTrap;
import me.rebirthclient.mod.modules.combat.surround.ExtraSurround;
import me.rebirthclient.mod.modules.combat.surround.Surround;
import me.rebirthclient.mod.modules.miscellaneous.*;
import me.rebirthclient.mod.modules.movement.*;
import me.rebirthclient.mod.modules.player.*;
import me.rebirthclient.mod.modules.render.*;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.BindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleManager implements Wrapper {
	public ArrayList<Module> modules = new ArrayList<>();
	public ArrayList<ExtraModule> extraModules = new ArrayList<>();
	public HashMap<Module.Category, Integer> categoryModules = new HashMap<>();
	public static Mod lastLoadMod;
	public ModuleManager() {
		addExtraModule(new ExtraAutoTrap());
		addExtraModule(new ExtraSurround());

		addModule(new AntiCactus());
		addModule(new AntiRegear());
		addModule(new AntiWeak());
		addModule(new ArrayListModule());
		addModule(new Aura());
		addModule(new AutoAnchor());
		addModule(new AutoArmor());
		addModule(new AutoCenter());
		addModule(new AutoCrystal());
		addModule(new AutoEXP());
		addModule(new AutoCity());
		addModule(new AutoPush());
		addModule(new AutoPeek());
		addModule(new AutoRegear());
		addModule(new AutoRespawn());
		addModule(new AutoTotem());
		addModule(new AutoTrap());
		addModule(new AutoWalk());
		addModule(new AutoWeb());
		addModule(new Blink());
		addModule(new BlockStrafe());
		addModule(new BreakESP());
		addModule(new Burrow());
		addModule(new CameraClip());
		addModule(new Chat());
		addModule(new ChatAppend());
		addModule(new ChestESP());
		addModule(new ClickGui());
		addModule(new Criticals());
		addModule(new CrystalRenderer());
		addModule(new ElytraFly());
		addModule(new FakePlayer());
		addModule(new FastPlace());
		addModule(new Flight());
		addModule(new Fullbright());
		addModule(new GameSpeed());
		addModule(new GhostHand());
		addModule(new GuiMove());
		addModule(new AntiCev());
		addModule(new HitboxDesync());
		addModule(new HitLog());
		addModule(new HoleSnap());
		addModule(new ItemESP());
		addModule(new LogoutSpots());
		addModule(new MCP());
		addModule(new MultiTask());
		addModule(new NameTags());
		addModule(new NoFall());
		addModule(new NoRender());
		addModule(new NoRotate());
		addModule(new NoSlowdown());
		addModule(new NoTrace());
		addModule(new OBSClip());
		addModule(new PacketEat());
		addModule(new PacketMine());
		addModule(new PlayerESP());
		addModule(new PopCounter());
		addModule(new Quiver());
		addModule(new Reach());
		addModule(new Replenish());
		addModule(new Scaffold());
		addModule(new ShaderChams());
		addModule(new ShulkerViewer());
		addModule(new SilentDisconnect());
		addModule(new Speed());
		addModule(new SpinBot());
		addModule(new Sprint());
		addModule(new Step());
		addModule(new Surround());
		addModule(new TwoDItem());
		addModule(new Velocity());
		addModule(new ViewModel());
		addModule(new DefenseESP());
		addModule(new XCarry());
		addModule(new BedAura());
	}

	public boolean setBind(int eventKey) {
		if (eventKey == -1 || eventKey == 0) {
			return false;
		}
		AtomicBoolean set = new AtomicBoolean(false);
		modules.forEach(module -> {
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BindSetting bindSetting) {
					if (bindSetting.isListening()) {
						bindSetting.setKey(eventKey);
						bindSetting.setListening(false);
						if (bindSetting.getBind().equals("DELETE")) {
							bindSetting.setKey(-1);
						}
						set.set(true);
					}
				}
			}
		});
		return set.get();
	}
	public void onKeyReleased(int eventKey) {
		if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGuiScreen) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey) {
				//module.disable();
			}
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BindSetting bindSetting) {
					if (bindSetting.getKey() == eventKey) {
						bindSetting.setDown(false);
					}
				}
			}
		});
	}
	public void onKeyPressed(int eventKey) {
		if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGuiScreen) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey) {
				if (mc.currentScreen == null)
					module.toggle();
			}
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BindSetting bindSetting) {
					if (bindSetting.getKey() == eventKey) {
						bindSetting.setDown(true);
					}
				}
			}
		});
	}
	
	public void onUpdate() {
		for (Module module : modules) {
			if (module.isOn()) {
				module.onUpdate();
			}
		}
	}

	public void onLogin() {
		for (Module module : modules) {
			if (module.isOn()) {
				module.onLogin();
			}
		}
	}

	public void render3D(MatrixStack matrixStack) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		matrixStack.push();
		Vec3d camPos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos();
		matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
		for(Module module : modules) {
			if(module.isOn()) {
				module.onRender3D(matrixStack, MinecraftClient.getInstance().getTickDelta());
			}
		}
		for (ExtraModule extraModule : extraModules) {
			extraModule.onRender3D(matrixStack, MinecraftClient.getInstance().getTickDelta());
		}
		matrixStack.pop();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	public void render2D(DrawContext drawContext) {
		for(Module module : modules) {
			if(module.isOn()) {
				module.onRender2D(drawContext, MinecraftClient.getInstance().getTickDelta());
			}
		}
	}

	public void addExtraModule(ExtraModule module) {
		extraModules.add(module);
	}
	public void addModule(Module module) {
		modules.add(module);
		categoryModules.put(module.getCategory(), categoryModules.getOrDefault(module.getCategory(), 0) + 1);
	}
	
	public void disableAll() {
		for(Module module : modules) {
			module.disable();
		}
	}
	
	public Module getModuleByName(String string) {
		for(Module module : modules) {
			if(module.getName().equalsIgnoreCase(string)) {
				return module;
			}
		}
		return null;
	}
}
