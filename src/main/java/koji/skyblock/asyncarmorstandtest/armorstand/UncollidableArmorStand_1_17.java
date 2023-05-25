package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.KBase;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.developerkit.utils.xseries.XMaterial;
import koji.skyblock.asyncarmorstandtest.UncollidableArmorStand;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UncollidableArmorStand_1_17 extends KBase implements UncollidableArmorStand {

    private static final Class<?> PACKET_SPAWN_LIVING;
    private static final Class<?> DATAWATCHER;
    private static final Class<?> PACKET_METADATA;
    private static final Class<?> PACKET_EQUIPMENT;
    private static final Class<?> PACKET_TELEPORT;
    private static final Class<?> ENTITY;
    private static final Class<?> ENTITY_LIVING;
    private static final Class<?> ARMOR_STAND;
    private static final Class<?> WORLD;
    private static final Class<?> CRAFT_ENTITY;
    private static final Class<?> ITEM_STACK;

    private static final Constructor<?> PACKET_LIVING_INSTANCE;
    private static final Constructor<?> PACKET_META_INSTANCE;
    private static final Constructor<?> PACKET_EQUIP_INSTANCE;
    private static final Constructor<?> PACKET_TELEPORT_INSTANCE;
    private static final Constructor<?> ARMOR_STAND_INSTANCE;

    private static final MethodHandle SET_LOCATION;
    private static final MethodHandle SET_INVISIBLE;
    private static final MethodHandle SET_MARKER;
    private static final MethodHandle GET_DATA_WATCHER;

    private static final MethodHandle GET_BUKKIT_ENTITY;
    private static final MethodHandle GET_ID;
    private static final MethodHandle AS_NMS_COPY;

    private static final Field NO_CLIP;

    private static final Object MAINHAND;
    private static final Object OFFHAND;
    private static final Object HELMET;
    private static final Object CHESTPLATE;
    private static final Object LEGGINGS;
    private static final Object BOOTS;

    static {
        PACKET_SPAWN_LIVING = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutSpawnEntityLiving"
        );
        DATAWATCHER = ReflectionUtils.getNMSClass(
                "network.syncher",
                "DataWatcher"
        );
        PACKET_METADATA = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutEntityMetadata"
        );
        PACKET_EQUIPMENT = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutEntityEquipment"
        );
        PACKET_TELEPORT = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutEntityTeleport"
        );
        ENTITY = ReflectionUtils.getNMSClass(
                "world.entity",
                "Entity"
        );
        ENTITY_LIVING = ReflectionUtils.getNMSClass(
                "world.entity",
                "EntityLiving"
        );
        ARMOR_STAND = ReflectionUtils.getNMSClass(
                "world.entity.decoration",
                "EntityArmorStand"
        );
        WORLD = ReflectionUtils.getNMSClass(
                "world.level",
                "World"
        );
        CRAFT_ENTITY = ReflectionUtils.getCraftClass(
                "entity.CraftEntity"
        );
        ITEM_STACK = ReflectionUtils.getNMSClass(
                "world.item",
                "ItemStack"
        );
        Class<?> ENUM_ITEM_SLOT = ReflectionUtils.getNMSClass(
                "world.entity",
                "EnumItemSlot"
        );

        assert ENTITY != null && ARMOR_STAND != null && WORLD != null &&
                PACKET_SPAWN_LIVING != null && PACKET_METADATA != null &&
                PACKET_EQUIPMENT != null && PACKET_TELEPORT != null &&
                ENUM_ITEM_SLOT != null;

        try {
            PACKET_LIVING_INSTANCE = PACKET_SPAWN_LIVING.getConstructor(
                    ENTITY_LIVING
            );
            PACKET_META_INSTANCE = PACKET_METADATA.getConstructor(
                    int.class, DATAWATCHER, boolean.class
            );
            PACKET_EQUIP_INSTANCE = PACKET_EQUIPMENT.getConstructor(
                    int.class, List.class
            );
            PACKET_TELEPORT_INSTANCE = PACKET_TELEPORT.getConstructor(
                    ENTITY
            );

            ARMOR_STAND_INSTANCE = ARMOR_STAND.getConstructor(
                    WORLD, double.class, double.class, double.class
            );

            SET_LOCATION = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    XMaterial.getVersion() == 17 ? "setLocation" : "a",
                    MethodType.methodType(
                            void.class,
                            double.class, double.class, double.class, float.class, float.class
                    )
            );
            SET_INVISIBLE = MethodHandles.lookup().findVirtual(
                    ARMOR_STAND,
                    XMaterial.getVersion() == 17 ? "setInvisible" : "j",
                    MethodType.methodType(void.class, boolean.class)
            );
            SET_MARKER = MethodHandles.lookup().findVirtual(
                    ARMOR_STAND,
                    XMaterial.getVersion() == 17 ? "setMarker" : "t",
                    MethodType.methodType(void.class, boolean.class)
            );
            GET_DATA_WATCHER = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    XMaterial.getVersion() == 17 ? "getDataWatcher" : "ai",
                    MethodType.methodType(DATAWATCHER)
            );
            GET_BUKKIT_ENTITY = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    "getBukkitEntity",
                    MethodType.methodType(CRAFT_ENTITY)
            );
            GET_ID = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    XMaterial.getVersion() == 17 ? "getId" : "ae",
                    MethodType.methodType(int.class)
            );
            AS_NMS_COPY = MethodHandles.lookup().findStatic(
                    ReflectionUtils.getCraftClass("inventory.CraftItemStack"),
                    "asNMSCopy",
                    MethodType.methodType(ITEM_STACK, ItemStack.class)
            );

            NO_CLIP = ARMOR_STAND.getField(XMaterial.getVersion() == 17 ? "P" : "Q");

            MAINHAND = ENUM_ITEM_SLOT.getDeclaredField("a").get(null);
            OFFHAND = ENUM_ITEM_SLOT.getDeclaredField("b").get(null);
            HELMET = ENUM_ITEM_SLOT.getDeclaredField("f").get(null);
            CHESTPLATE = ENUM_ITEM_SLOT.getDeclaredField("e").get(null);
            LEGGINGS = ENUM_ITEM_SLOT.getDeclaredField("d").get(null);
            BOOTS = ENUM_ITEM_SLOT.getDeclaredField("c").get(null);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object armorStand;

    @SneakyThrows
    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, boolean overwrite) {
        Object craftWorld = getNMSWorld(location.getWorld());

        if (armorStand == null || overwrite) {
            armorStand = ARMOR_STAND_INSTANCE.newInstance(
                    craftWorld, location.getX(), location.getY(), location.getZ()
            );

            SET_INVISIBLE.invoke(armorStand, true);
            SET_MARKER.invoke(armorStand, true);
            NO_CLIP.set(armorStand, true);
        }
        SET_LOCATION.invoke(
                armorStand, location.getX(),
                location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
        );

        Object spawnPacket = PACKET_LIVING_INSTANCE.newInstance(
                armorStand
        );
        players.forEach(p -> ReflectionUtils.sendPacket(p, spawnPacket));

        return (LivingEntity) GET_BUKKIT_ENTITY.invoke(armorStand);
    }

    @SneakyThrows
    @Override
    public void update(Collection<Player> players, ItemStack[] stack) {
        Object[] packets = new Object[2];
        packets[0] = PACKET_META_INSTANCE.newInstance(
                GET_ID.invoke(armorStand),
                GET_DATA_WATCHER.invoke(armorStand),
                true
        );
        List<Pair<?, ?>> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            list.add(new Pair<>(getPair(i), AS_NMS_COPY.invoke(stack[i])));
        }
        packets[1] = PACKET_EQUIP_INSTANCE.newInstance(
                GET_ID.invoke(armorStand), list
        );
        players.forEach(p -> ReflectionUtils.sendPacket(p, packets));
    }

    private Object getPair(int i) {
        switch (i) {
            default: return MAINHAND;
            case 1: return OFFHAND;
            case 2: return HELMET;
            case 3: return CHESTPLATE;
            case 4: return LEGGINGS;
            case 5: return BOOTS;
        }
    }

    @Override
    public void move(Collection<Player> players, double x, double y, double z, float yaw, float pitch) {
        try {
            SET_LOCATION.invoke(armorStand, x, y, z, yaw, pitch);
            Object teleport = PACKET_TELEPORT_INSTANCE.newInstance(armorStand);
            players.forEach(p -> ReflectionUtils.sendPacket(p, teleport));
        } catch (Throwable ignored) {}
    }

    @Override
    public ArmorStand getEntity() {
        try {
            return (ArmorStand) GET_BUKKIT_ENTITY.invoke(armorStand);
        } catch (Throwable e) {
            return null;
        }
    }

}