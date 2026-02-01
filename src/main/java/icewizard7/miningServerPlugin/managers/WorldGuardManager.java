package icewizard7.miningServerPlugin.managers;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardManager {
    private final WorldGuard worldGuard;

    public WorldGuardManager(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    public boolean isInRegion(Player player, String regionId) {
        var container = worldGuard.getPlatform().getRegionContainer();
        var query = container.createQuery();

        ApplicableRegionSet regions = query.getApplicableRegions(
                BukkitAdapter.adapt(player.getLocation())
        );

        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionId)) {
                return true;
            }
        }
        return false;
    }

    public boolean inPvPAllowedRegion(Entity victim) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(victim.getLocation()));

        return set.testState(null, Flags.PVP);
    }
}
