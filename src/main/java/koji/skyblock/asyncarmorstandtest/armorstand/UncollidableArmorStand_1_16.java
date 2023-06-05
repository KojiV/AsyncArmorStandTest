package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.runnable.KRunnable;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.skyblock.asyncarmorstandtest.AsyncArmorStandTest;
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
    public void setup(org.bukkit.World world) {
        stand = new EntityArmorStand(
                EntityTypes.ARMOR_STAND,
                ((CraftWorld) world).getHandle()
        );
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.noclip = true;
    }

    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite) {
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

        new KRunnable(task -> {
            ArmorStand bukkitStand = getEntity();
            update(players, new ItemStack[] {
                    bukkitStand.getItemInHand(),
                    null,
                    bukkitStand.getHelmet(),
                    bukkitStand.getChestplate(),
                    bukkitStand.getLeggings(),
                    bukkitStand.getBoots()

            }, rotate(rotations), true);
        }).runTaskLaterAsynchronously(AsyncArmorStandTest.getMain(), 5L);

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
        Packet<?>[] packets = new Packet[2];
        packets[0] = new PacketPlayOutEntityMetadata(
                stand.getId(), (DataWatcher) dataWatcher, true
        );
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            list.add(new Pair<>(getSlot(i), CraftItemStack.asNMSCopy(stack[i])));
        }
        packets[setData ? 1 : 0] = new PacketPlayOutEntityEquipment(stand.getId(), list);
        players.forEach(p -> ReflectionUtils.sendPacket(p, (Object[]) packets));
    }

    private EnumItemSlot getSlot(int i) {
        switch (i) {
            default: return EnumItemSlot.MAINHAND;
            case 1: return EnumItemSlot.OFFHAND;
            case 2: return EnumItemSlot.HEAD;
            case 3: return EnumItemSlot.CHEST;
            case 4: return EnumItemSlot.LEGS;
            case 5: return EnumItemSlot.FEET;
        }
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
    public Object rotate(float[][] rotations) {
        DataWatcher data = new DataWatcher(stand);
        DataWatcherObject<Vector3f>[] labels = new DataWatcherObject[] {
                EntityArmorStand.c, // Head
                EntityArmorStand.d, // Body
                EntityArmorStand.e, // Left Arm
                EntityArmorStand.f, // Right Arm
                EntityArmorStand.g, // Left Leg
                EntityArmorStand.bh // Right Leg
        };
        for(int i = 0; i < 6; i++) {
            float[] array = rotations[i];
            data.register(labels[i], new Vector3f(array[0], array[1], array[2]));
        }
        return data;
    }

    @Override
    public void destroy(Collection<Player> players) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(stand.getId());
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
    }
}