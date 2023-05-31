package koji.skyblock.asyncarmorstandtest;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Collection;

public interface UncollidableArmorStand {

    void setup(World world);

    default LivingEntity spawn(Collection<Player> players, boolean overwrite) {
        Location loc = getEntity().getLocation();
        return spawn(players, loc, overwrite);
    }

    default LivingEntity spawn(Collection<Player> players, float[][] rotate) {
        return spawn(players, rotate, getEntity().getLocation());
    }
    default LivingEntity spawn(Collection<Player> players, float[][] rotate, Location loc) {
        return spawn(players, loc, rotate, false);
    }
    default LivingEntity spawn(Collection<Player> players, float[][] rotate, boolean overwrite) {
        return spawn(players, getEntity().getLocation(), rotate, overwrite);
    }

    default LivingEntity spawn(Collection<Player> players, Location loc, boolean overwrite) {
        ArmorStand stand = getEntity();
        return spawn(players, loc, new float[][] {
                fromEulerAngle(stand.getHeadPose()),
                fromEulerAngle(stand.getBodyPose()),
                fromEulerAngle(stand.getLeftArmPose()),
                fromEulerAngle(stand.getRightArmPose()),
                fromEulerAngle(stand.getLeftLegPose()),
                fromEulerAngle(stand.getRightLegPose())
        }, overwrite);
    }

    LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite);
    default void update(Collection<Player> players, boolean setData) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                stand.getItemInHand(),
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        }, setData);
    }
    default void update(Collection<Player> players, Object dataWatcher) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                stand.getItemInHand(),
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        }, dataWatcher, true);
    }
    void update(Collection<Player> players, ItemStack[] stack, boolean setData);
    void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher, boolean setData);
    default void move(Collection<Player> players, Location loc) {
        move(players, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    void move(Collection<Player> players, double x, double y, double z, float yaw, float pitch);
    ArmorStand getEntity();

    Object rotate(float[][] rotations);

    void destroy(Collection<Player> players);

    default float[] fromEulerAngle(EulerAngle angle) {
        return new float[] {
                (float) Math.toDegrees(angle.getX()),
                (float) Math.toDegrees(angle.getY()),
                (float) Math.toDegrees(angle.getZ())
        };
    }
}
