package me.juneylove.shakedown.mechanics.worlds;

import me.juneylove.shakedown.mechanics.CapturePoint;
import me.juneylove.shakedown.mechanics.InvulnerableRegion;
import me.juneylove.shakedown.mechanics.MovementDetectRegion;
import me.juneylove.shakedown.mechanics.Spawn;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

@SuppressWarnings("InnerClassMayBeStatic")
public abstract class WorldSetting {

    public List<Location> lootChestLocations;
    public List<List<Location>> roundBarriers; // Outer list size should match round.teamsPerWorld (TODO: verify)
    public List<InvulnerableRegion> invulnerableRegions; // TODO: verify list size is same as outer list size for roundBarriers, should correspond directly
    public List<CapturePoint> capturePoints;
    public List<MovementDetectRegion> movementDetectRegions;

    public Spawn.SpawnSetting spawnSetting;
    public Location spectatorSpawn = null;

    public abstract String getName();

    public abstract String getFolderName();

    public abstract World getWorld();

}
