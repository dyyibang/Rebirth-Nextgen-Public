package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Event {
    public ParticleEvent() {
        super(Stage.Pre);
    }
    public static class AddParticle extends ParticleEvent {

        public Particle particle;
        public AddParticle(Particle particle){
            this.particle = particle;
        }

    }

    public static class AddEmmiter extends ParticleEvent {
        public ParticleEffect emmiter;

        public AddEmmiter(ParticleEffect emmiter){
            this.emmiter = emmiter;
        }

    }
}