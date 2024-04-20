package me.rebirthclient.mod.modules.render;

import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CrystalRenderer extends Module {
    public static CrystalRenderer INSTANCE;
    public static HashMap<EndCrystalEntity, Double> spinMap = new HashMap<>();
    public static HashMap<Vec3d, Double> posSpinMap = new HashMap<>();

    public static HashMap<EndCrystalEntity, Double> floatMap = new HashMap<>();
    public static HashMap<Vec3d, Double> posFloatMap = new HashMap<>();
    public static Random random = new Random();
    public CrystalRenderer() {
        super("CrystalRenderer", Category.Render);
        INSTANCE = this;
    }

    public BooleanSetting sync = add(new BooleanSetting("Sync"));
    public final SliderSetting spinAdd = add(new SliderSetting("SpinNewAdd", 0, 100f, 1, v -> sync.getValue()));
    public final SliderSetting spinValue = add(new SliderSetting("SpinSpeed", 0, 3f, 0.01));
    public final SliderSetting floatValue = add(new SliderSetting("FloatSpeed", 0, 3f, 0.01));
    public final SliderSetting floatOffset = add(new SliderSetting("FloatOffset", -1, 1f, 0.01));
    @Override
    public void onUpdate() {
        if (!sync.getValue()) {
            return;
        }
        List<EndCrystalEntity> noSpinAge = new ArrayList<>();
        List<EndCrystalEntity> noFloatAge = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
                if (spinMap.getOrDefault(crystal, -1D) != -1) {
                    spinMap.put(crystal, spinMap.get(crystal) + 1);
                    posSpinMap.put(crystal.getPos(), spinMap.get(crystal));
                } else {
                    noSpinAge.add(crystal);
                }

                if (floatMap.getOrDefault(crystal, -1D) != -1) {
                    floatMap.put(crystal, floatMap.get(crystal) + 1);
                    posFloatMap.put(crystal.getPos(), floatMap.get(crystal));
                } else {
                    noFloatAge.add(crystal);
                }
            }
        }

        for (EndCrystalEntity crystal : noSpinAge) {
            if (spinMap.getOrDefault(crystal, -1D) == -1) {
                spinMap.put(crystal, posSpinMap.getOrDefault(crystal.getPos(), (double) random.nextInt(10000)) + spinAdd.getValue());
            }
        }

        for (EndCrystalEntity crystal : noFloatAge) {
            if (floatMap.getOrDefault(crystal, -1D) == -1) {
                floatMap.put(crystal, posFloatMap.getOrDefault(crystal.getPos(), (double) random.nextInt(10000)));
            }
        }
    }

    public double getSpinAge(EndCrystalEntity crystal) {
        if (!sync.getValue()) {
            return crystal.endCrystalAge;
        }
        if (spinMap.getOrDefault(crystal, -1D) == -1) {
            spinMap.put(crystal, posSpinMap.getOrDefault(crystal.getPos(), (double) random.nextInt(10000)) + spinAdd.getValue());
        }
        double age = spinMap.getOrDefault(crystal, posSpinMap.getOrDefault(crystal.getPos(), -1d));
        if (age != -1d) {
            return age;
        }
        age = random.nextInt(10000);
        posSpinMap.put(crystal.getPos(), age);
        return age;
    }

    public double getFloatAge(EndCrystalEntity crystal) {
        if (!sync.getValue()) {
            return crystal.endCrystalAge;
        }
        if (floatMap.getOrDefault(crystal, -1D) == -1) {
            floatMap.put(crystal, posFloatMap.getOrDefault(crystal.getPos(), (double) random.nextInt(10000)));
        }
        double age = floatMap.getOrDefault(crystal, posFloatMap.getOrDefault(crystal.getPos(), -1d));
        if (age != -1d) {
            return age;
        }
        age = random.nextInt(10000);
        posFloatMap.put(crystal.getPos(), age);
        return age;
    }
}
