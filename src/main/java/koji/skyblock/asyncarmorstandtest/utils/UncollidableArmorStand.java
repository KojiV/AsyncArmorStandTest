package koji.skyblock.asyncarmorstandtest.utils;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Collection;

public interface UncollidableArmorStand extends PacketEntity {
    @Override default void spawn(Collection<Player> players, Location loc) {
        spawn(players, loc, true);
    }

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

    @Override default void update(Collection<Player> players) {
        update(players, true);
    }

    default void update(Collection<Player> players, boolean setData) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                null,
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        }, setData);
    }
    default void update(Collection<Player> players, float[][] floats, boolean setData) {
        ArmorStand stand = getEntity();
        update(players, new ItemStack[] {
                stand.getItemInHand(),
                null,
                stand.getHelmet(),
                stand.getChestplate(),
                stand.getLeggings(),
                stand.getBoots()
        }, rotate(floats), setData);
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
        }, dataWatcher, true);
    }
    void update(Collection<Player> players, ItemStack[] stack, boolean setData);
    void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher, boolean setData);
    ArmorStand getEntity();

    Object rotate(float[][] rotations);

    default float[] fromEulerAngle(EulerAngle angle) {
        return new float[] {
                (float) Math.toDegrees(angle.getX()),
                (float) Math.toDegrees(angle.getY()),
                (float) Math.toDegrees(angle.getZ())
        };
    }
}
