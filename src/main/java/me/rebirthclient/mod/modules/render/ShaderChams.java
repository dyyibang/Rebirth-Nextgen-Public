package me.rebirthclient.mod.modules.render;

import me.rebirthclient.Rebirth;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.Setting;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.lang.reflect.Field;

import static me.rebirthclient.api.managers.ShaderManager.Shader;
public class ShaderChams extends Module {

    public static ShaderChams INSTANCE;

    public ShaderChams() {
        super("ShaderChams", Category.Render);
        try {
            for (Field field : ShaderChams.class.getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType()))
                    continue;
                Setting setting = (Setting) field.get(this);
                addSetting(setting);
            }
        } catch (Exception e) {
        }
        INSTANCE = this;
    }
    private enum Page {
        Shader,
        Target
    }
    private final EnumSetting page = new EnumSetting("Page", Page.Shader);

    public EnumSetting mode = new EnumSetting("Mode", Shader.Default, v -> page.getValue() == Page.Shader);
    public EnumSetting handsMode = new EnumSetting("HandsMode", Shader.Default, v -> page.getValue() == Page.Shader);
    public EnumSetting skyMode = new EnumSetting("SkyMode", Shader.Default, v -> page.getValue() == Page.Shader);
    public final SliderSetting speed = new SliderSetting("Speed", 0, 20, 0.1, v -> page.getValue() == Page.Shader);
    public final BooleanSetting glow = new BooleanSetting("Glow", v -> page.getValue() == Page.Shader);
    public final SliderSetting quality = new SliderSetting("Quality", 0, 20, v -> glow.getValue() && page.getValue() == Page.Shader);
    public final SliderSetting lineWidth = new SliderSetting("LineWidth", 0, 20, v -> page.getValue() == Page.Shader);
    public final SliderSetting factor = new SliderSetting("GradientFactor", 0f, 20f, v -> page.getValue() == Page.Shader);
    public final SliderSetting gradient = new SliderSetting("Gradient", 0f, 20f, v -> page.getValue() == Page.Shader);
    public final SliderSetting alpha2 = new SliderSetting("GradientAlpha", 0, 255, v -> page.getValue() == Page.Shader);
    public final SliderSetting octaves = new SliderSetting("Octaves", 5, 30, v -> page.getValue() == Page.Shader);
    public final SliderSetting fillAlpha = new SliderSetting("Alpha", 0, 255, v -> page.getValue() == Page.Shader);
    public final ColorSetting outlineColor = new ColorSetting("Outline", new Color(255,255,255), v -> page.getValue() == Page.Shader);
    public final ColorSetting smokeGlow = new ColorSetting("SmokeGlow", new Color(255,255,255), v -> page.getValue() == Page.Shader);
    public final ColorSetting smokeGlow1 = new ColorSetting("SmokeGlow", new Color(255,255,255), v -> page.getValue() == Page.Shader);
    public final ColorSetting fillColor2 = new ColorSetting("SmokeFill", new Color(255,255,255), v -> page.getValue() == Page.Shader);
    public final ColorSetting fillColor3 = new ColorSetting("SmokeFil2", new Color(255,255,255), v -> page.getValue() == Page.Shader);
    public final ColorSetting fill = new ColorSetting("Fill", new Color(255,255,255), v -> page.getValue() == Page.Shader);

    public final BooleanSetting sky = new BooleanSetting("Sky[!]", v -> page.getValue() == Page.Target);
    private final BooleanSetting hands = new BooleanSetting("Hands", v -> page.getValue() == Page.Target);
    public final SliderSetting maxRange = new SliderSetting("MaxRange", 16, 512, v -> page.getValue() == Page.Target);
    private final BooleanSetting self = new BooleanSetting("Self", v -> page.getValue() == Page.Target);
    private final BooleanSetting players = new BooleanSetting("Players", v -> page.getValue() == Page.Target);
    private final BooleanSetting friends = new BooleanSetting("Friends", v -> page.getValue() == Page.Target);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", v -> page.getValue() == Page.Target);
    private final BooleanSetting creatures = new BooleanSetting("Creatures", v -> page.getValue() == Page.Target);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", v -> page.getValue() == Page.Target);
    private final BooleanSetting ambients = new BooleanSetting("Ambients", v -> page.getValue() == Page.Target);
    private final BooleanSetting items = new BooleanSetting("Items", v -> page.getValue() == Page.Target);
    private final BooleanSetting others = new BooleanSetting("Others", v -> page.getValue() == Page.Target);

    public boolean shouldRender(Entity entity) {
        if (entity == null)
            return false;

        if (mc.player == null)
            return false;

        if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(entity.getPos())) > maxRange.getValue())
            return false;

        if (entity instanceof PlayerEntity) {
            if (entity == mc.player)
                return self.getValue();
            if (Rebirth.FRIEND.isFriend((PlayerEntity) entity))
                return friends.getValue();
            return players.getValue();
        }

        if (entity instanceof EndCrystalEntity)
            return crystals.getValue();
        if (entity instanceof ItemEntity)
            return items.getValue();
        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        if (hands.getValue())
            Rebirth.SHADER.renderShader(()-> mc.gameRenderer.renderHand(matrixStack, mc.gameRenderer.getCamera(), mc.getTickDelta()), (Shader) handsMode.getValue());
    }

    @Override
    public void onDisable() {
        Rebirth.SHADER.reloadShaders();
    }
}
