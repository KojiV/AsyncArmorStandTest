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
    public LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite) {
        if (stand == null || overwrite) {
            stand = new EntityArmorStand(
                    ((CraftWorld) location.getWorld()).getHandle()
            );
            stand.setInvisible(true);
            stand.n(true);
            stand.noclip = true;
        }
        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
        players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));

        return (LivingEntity) stand.getBukkitEntity();
    }

    public void update(Collection<Player> players, ItemStack[] stack) {
        Packet<?>[] packets = new Packet[stack.length];
        packets[0] = new PacketPlayOutEntityMetadata(
                stand.getId(), stand.getDataWatcher(), true
        );
        for(int i = 0; i < stack.length && i != 1; i++) {
            int index = i > 0 ? i - 1 : i;
            //0, 2, 3, 4, 5
            packets[index + 1] = new PacketPlayOutEntityEquipment(
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

}