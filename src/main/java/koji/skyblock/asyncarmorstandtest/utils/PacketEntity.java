package koji.skyblock.asyncarmorstandtest.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public interface PacketEntity {
    void setup(World world);
    void spawn(Collection<Player> players, Location loc);
    void update(Collection<Player> players);
    void move(Collection<Player> players, Location loc);
    Entity getEntity();
    void destroy(Collection<Player> players);
    Set<Player> getPlayersVisibleFor();
}
