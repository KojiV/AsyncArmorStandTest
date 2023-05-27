package koji.skyblock.asyncarmorstandtest;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface UncollidableArmorStand {
    default LivingEntity spawn(Collection<Player> players) {
        return spawn(players, getEntity().getLocation());
    }
    default LivingEntity spawn(Collection<Player> players, Location loc) {
        return spawn(players, loc, false);
    }
    default LivingEntity spawn(Collection<Player> players, boolean overwrite) {
        return spawn(players, getEntity().getLocation(), overwrite);
    }
    LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite);
    default void update(Collection<Player> players) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                null,
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        });
    }
    default void update(Collection<Player> players, Object dataWatcher) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                null,
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        }, dataWatcher);
    }
    void update(Collection<Player> players, ItemStack[] stack);
    void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher);
    default void move(Collection<Player> players, Location loc) {
        move(players, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    void move(Collection<Player> players, double x, double y, double z, float yaw, float pitch);
    ArmorStand getEntity();

    void rotate(Collection<Player> players, float[][] rotations);
}
