package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.skyblock.asyncarmorstandtest.utils.UncollidableArmorStand;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class UncollidableArmorStand_1_16 implements UncollidableArmorStand {
    EntityArmorStand stand;
    private final Set<Player> visibleTo;

    public UncollidableArmorStand_1_16() {
        visibleTo = new HashSet<>();
    }

    @Override
    public void setup(org.bukkit.World world) {
        stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, ((CraftWorld) world).getHandle());
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.noclip = true;
    }

    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite) {
        if(stand == null || overwrite) {
            stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, ((CraftWorld) location.getWorld()).getHandle());
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.noclip = true;
        }
        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        PacketPlayOutEntityMetadata test = new PacketPlayOutEntityMetadata(
                stand.getId(), stand.getDataWatcher(), true
        );
        players.forEach(p -> {
            visibleTo.add(p);
            ReflectionUtils.sendPacket(p, packet, test);
        });

        ArmorStand bukkitStand = getEntity();
        update(players, new ItemStack[] {
                bukkitStand.getItemInHand(),
                null,
                bukkitStand.getHelmet(),
                bukkitStand.getChestplate(),
                bukkitStand.getLeggings(),
                bukkitStand.getBoots()

        }, rotate(rotations), true);

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
    public void move(Collection<Player> players, Location loc) {
        stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(stand);
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(teleport));
    }

    @Override
    public ArmorStand getEntity() {
        return (ArmorStand) stand.getBukkitEntity();
    }

    @Override @SuppressWarnings("unchecked")
    public Object rotate(float[][] rotations) {
        DataWatcher data = stand.getDataWatcher();
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
            data.set(labels[i], new Vector3f(array[0], array[1], array[2]));
        }
        return data;
    }

    @Override
    public void destroy(Collection<Player> players) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(stand.getId());
        players.forEach(p -> {
            visibleTo.remove(p);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        });
    }

    @Override
    public Set<Player> getPlayersVisibleFor() {
        return visibleTo;
    }
}