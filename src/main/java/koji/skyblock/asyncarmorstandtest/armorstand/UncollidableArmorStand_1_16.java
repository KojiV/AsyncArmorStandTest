package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.skyblock.asyncarmorstandtest.UncollidableArmorStand;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UncollidableArmorStand_1_16 implements UncollidableArmorStand {
    EntityArmorStand stand;

    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite) {
        if(stand == null || overwrite) {
            stand = new EntityArmorStand(
                    EntityTypes.ARMOR_STAND,
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
        Packet<?>[] packets = new Packet[2];
        packets[0] = new PacketPlayOutEntityMetadata(
                stand.getId(), (DataWatcher) dataWatcher, true
        );
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            switch (i) {
                default:
                    list.add(new Pair<>(
                            EnumItemSlot.MAINHAND,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
                case 1:
                    list.add(new Pair<>(
                            EnumItemSlot.OFFHAND,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
                case 2:
                    list.add(new Pair<>(
                            EnumItemSlot.HEAD,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
                case 3:
                    list.add(new Pair<>(
                            EnumItemSlot.CHEST,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
                case 4:
                    list.add(new Pair<>(
                            EnumItemSlot.LEGS,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
                case 5:
                    list.add(new Pair<>(
                            EnumItemSlot.FEET,
                            CraftItemStack.asNMSCopy(stack[i])
                    ));
                    break;
            }
        }
        packets[1] = new PacketPlayOutEntityEquipment(stand.getId(), list);
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