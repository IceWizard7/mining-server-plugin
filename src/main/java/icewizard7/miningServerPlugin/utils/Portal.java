package icewizard7.miningServerPlugin.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class Portal {
    public Location fromCenter;
    public double radius;
    public Location to;
    public Particle particle;
    public int particleCount;
    public Sound sound;

    public Portal(Location fromCenter, double radius, Location to,
                  Particle particle, int particleCount, Sound sound) {
        this.fromCenter = fromCenter;
        this.radius = radius;
        this.to = to;
        this.particle = particle;
        this.particleCount = particleCount;
        this.sound = sound;
    }
}
