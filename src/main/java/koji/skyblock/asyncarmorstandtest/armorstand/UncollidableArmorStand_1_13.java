package koji.skyblock.asyncarmorstandtest.armorstand;

import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.skyblock.asyncarmorstandtest.UncollidableArmorStand;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class UncollidableArmorStand_1_13 implements UncollidableArmorStand {
    EntityArmorStand stand;

    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite) {
        if(stand == null || overwrite) {
            stand = new EntityArmorStand(
                    ((CraftWorld) location.getWorld()).getHandle()
            );
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.noclip = true;
        }
        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
        
        return (LivingEntity) stand.getBukkitEntity();
    }

    @Override
    public void update(Collection<Player> players, ItemStack[] stack) {
        update(players, stack, stand.getDataWatcher());
    }

    @Override
    public void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher) {
        Packet<?>[] packets = new Packet[stack.length + 1];
        packets[0] = new PacketPlayOutEntityMetadata(
                stand.getId(), (DataWatcher) dataWatcher, true
        );
        for (int i = 0; i < stack.length; i++) {
            EnumItemSlot slot;
            switch (i) {
                default:
                    slot = EnumItemSlot.MAINHAND;
                    break;
                case 1:
                    slot = EnumItemSlot.OFFHAND;
                    break;
                case 2:
                    slot = EnumItemSlot.HEAD;
                    break;
                case 3:
                    slot = EnumItemSlot.CHEST;
                    break;
                case 4:
                    slot = EnumItemSlot.LEGS;
                    break;
                case 5:
                    slot = EnumItemSlot.FEET;
                    break;
            }
            packets[i + 1] = new PacketPlayOutEntityEquipment(
                    stand.getId(), slot, CraftItemStack.asNMSCopy(stack[i])
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

    @Override @SuppressWarnings("unchecked")
    public void rotate(Collection<Player> players, float[][] rotations) {
        DataWatcher data = stand.getDataWatcher();
        DataWatcherObject<Vector3f>[] labels = new DataWatcherObject[] {
                EntityArmorStand.b, // Head
                EntityArmorStand.c, // Body
                EntityArmorStand.d, // Left Arm
                EntityArmorStand.e, // Right Arm
                EntityArmorStand.f, // Left Leg
                EntityArmorStand.g // Right Leg
        };
        for(int i = 0; i < 6; i++) {
            float[] array;
            if(i >= rotations.length) array = new float[3];
            else array = rotations[i];

            data.set(labels[i], new Vector3f(array[0], array[1], array[2]));
        }
    }
}