package koji.skyblock.asyncarmorstandtest;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface UncollidableArmorStand {
    default LivingEntity spawn(Collection<Player> players, Location loc) {
        return spawn(players, loc, false);
    }
    LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite);
    default void update(Collection<Player> players) {
        update(players, new ItemStack[6]);
    }
    void update(Collection<Player> players, ItemStack[] stack);
    default void move(Collection<Player> players, Location loc) {
        move(players, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    void move(Collection<Player> players, double x, double y, double z, float yaw, float pitch);
    ArmorStand getEntity();
}
