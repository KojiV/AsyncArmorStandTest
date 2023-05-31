package koji.skyblock.asyncarmorstandtest.armorstand;

import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.skyblock.asyncarmorstandtest.UncollidableArmorStand;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class UncollidableArmorStand_1_8 implements UncollidableArmorStand {

    private EntityArmorStand stand;

    @Override
    public void setup(org.bukkit.World world) {
        stand = new EntityArmorStand(((CraftWorld) world).getHandle());
        stand.setInvisible(true);
        stand.n(true);
        stand.noclip = true;
    }

    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite) {
        if (stand == null || overwrite) {
            stand = new EntityArmorStand(
                    ((CraftWorld) location.getWorld()).getHandle()
            );
            stand.setInvisible(true);
            stand.n(true);
            stand.noclip = true;
        }
        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        ArmorStand bukkitStand = getEntity();
        update(players, new ItemStack[] {
                bukkitStand.getItemInHand(),
                null,
                bukkitStand.getHelmet(),
                bukkitStand.getChestplate(),
                bukkitStand.getLeggings(),
                bukkitStand.getBoots()

        }, rotate(rotations), true);
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));

        return (LivingEntity) stand.getBukkitEntity();
    }

    @Override
    public void update(Collection<Player> players, ItemStack[] stack, boolean setData) {
        update(players, stack, stand.getDataWatcher(), setData);
    }

    @Override
    public void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher, boolean setData) {
        Packet<?>[] packets = new Packet[setData ? stack.length : stack.length - 1];
        packets[0] = new PacketPlayOutEntityMetadata(
                stand.getId(), (DataWatcher) dataWatcher, true
        );
        for(int i = 0; i < stack.length; i++) {
            if(i == 1) continue;
            int index = i > 0 ? i - 1 : i;
            //0, 2, 3, 4, 5
            packets[setData ? index + 1 : index] = new PacketPlayOutEntityEquipment(
                    stand.getId(), index, CraftItemStack.asNMSCopy(stack[i])
            );
        }
        players.forEach(p -> ReflectionUtils.sendPacket(p, (Object[]) packets));
    }

    @Override
    public void move(Collection<Player> players, double x, double y, double z, float yaw, float pitch) {
        stand.setLocation(x, y, z, yaw, pitch);
        PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(stand);
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(teleport));
    }

    @Override
    public ArmorStand getEntity() {
        return (ArmorStand) stand.getBukkitEntity();
    }

    @Override
    public Object rotate(float[][] rotations) {
        DataWatcher watcher = new DataWatcher(stand);
        // 11 = Head, 12 = Body, 13 = Left Arm, 14 = Right Arm, 15 = Left Leg, 16 = Right Leg
        for(int i = 0; i < 6; i++) {
            int dataIndex = 11 + i;

            float[] array = rotations[i];
            watcher.a(dataIndex, new Vector3f(array[0], array[1], array[2]));
        }
        return watcher;
    }

    @Override
    public void refreshVisibility(Collection<Player> players) {

    }
}