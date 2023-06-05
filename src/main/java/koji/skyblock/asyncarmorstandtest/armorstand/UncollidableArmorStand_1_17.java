package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.KBase;
import koji.developerkit.runnable.KRunnable;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.developerkit.utils.xseries.XMaterial;
import koji.skyblock.asyncarmorstandtest.AsyncArmorStandTest;
import koji.skyblock.asyncarmorstandtest.UncollidableArmorStand;
import lombok.SneakyThrows;
import net.minecraft.network.syncher.DataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private static final Class<?> PACKET_DESTROY;
    private static final Class<?> ENTITY;
    private static final Class<?> ENTITY_LIVING;
    private static final Class<?> ARMOR_STAND;
    private static final Class<?> WORLD;
    private static final Class<?> CRAFT_ENTITY;
    private static final Class<?> ITEM_STACK;
    private static final Class<?> DATA_WATCHER_OBJECT;

    private static final Constructor<?> PACKET_LIVING_INSTANCE;
    private static final Constructor<?> PACKET_META_INSTANCE;
    private static final Constructor<?> PACKET_EQUIP_INSTANCE;
    private static final Constructor<?> PACKET_TELEPORT_INSTANCE;
    private static final Constructor<?> PACKET_DESTROY_INSTANCE;
    private static final Constructor<?> ARMOR_STAND_INSTANCE;
    private static final Constructor<?> VECTOR_3F_INSTANCE;
    private static final Constructor<?> DATA_WATCHER_INSTANCE;

    private static final MethodHandle SET_LOCATION;
    private static final MethodHandle SET_INVISIBLE;
    private static final MethodHandle SET_MARKER;
    private static final MethodHandle GET_DATA_WATCHER;

    private static final MethodHandle GET_BUKKIT_ENTITY;
    private static final MethodHandle GET_ID;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle DATA_WATCHER_SET;
    private static final MethodHandle DATA_WATCHER_PACK_DIRTY;

    private static final Field NO_CLIP;

    private static final Object MAINHAND;
    private static final Object OFFHAND;
    private static final Object HELMET;
    private static final Object CHESTPLATE;
    private static final Object LEGGINGS;
    private static final Object BOOTS;
    private static final Object HEAD;
    private static final Object BODY;
    private static final Object LEFT_ARM;
    private static final Object RIGHT_ARM;
    private static final Object LEFT_LEG;
    private static final Object RIGHT_LEG;

    static {
        PACKET_SPAWN_LIVING = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutSpawnEntity" + (XMaterial.getVersion() == 19 ? "" : "Living")
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
        PACKET_DESTROY = ReflectionUtils.getNMSClass(
                "network.protocol.game",
                "PacketPlayOutEntityDestroy"
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
        DATA_WATCHER_OBJECT = ReflectionUtils.getNMSClass(
                "network.syncher",
                "DataWatcherObject"
        );
        Class<?> DATA_WATCHER_SERIALIZER = ReflectionUtils.getNMSClass(
                "network.syncher",
                "DataWatcherSerializer"
        );
        Class<?> ENUM_ITEM_SLOT = ReflectionUtils.getNMSClass(
                "world.entity",
                "EnumItemSlot"
        );

        assert ENTITY != null && ARMOR_STAND != null && WORLD != null &&
                PACKET_SPAWN_LIVING != null && PACKET_METADATA != null &&
                PACKET_EQUIPMENT != null && PACKET_TELEPORT != null &&
                PACKET_DESTROY != null && DATA_WATCHER_OBJECT != null &&
                DATAWATCHER != null && ENUM_ITEM_SLOT != null && DATA_WATCHER_SERIALIZER != null;

        try {
            Class<?> VECTOR_3F_CLASS = Class.forName("net.minecraft.core.Vector3f");

            PACKET_LIVING_INSTANCE = PACKET_SPAWN_LIVING.getConstructor(
                    XMaterial.getVersion() == 19 ? ENTITY : ENTITY_LIVING
            );

            PACKET_META_INSTANCE = XMaterial.getVersion() == 19 ?
                    PACKET_METADATA.getConstructor(int.class, List.class) :
                    PACKET_METADATA.getConstructor(int.class, DATAWATCHER, boolean.class);

            PACKET_EQUIP_INSTANCE = PACKET_EQUIPMENT.getConstructor(
                    int.class, List.class
            );
            PACKET_TELEPORT_INSTANCE = PACKET_TELEPORT.getConstructor(
                    ENTITY
            );
            PACKET_DESTROY_INSTANCE = PACKET_DESTROY.getConstructor(
                    int[].class
            );

            ARMOR_STAND_INSTANCE = ARMOR_STAND.getConstructor(
                    WORLD, double.class, double.class, double.class
            );
            VECTOR_3F_INSTANCE = VECTOR_3F_CLASS.getConstructor(
                    float.class, float.class, float.class
            );
            DATA_WATCHER_INSTANCE = DATAWATCHER.getConstructor(ENTITY);

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
                    XMaterial.getVersion() == 17 ? "setMarker" :
                            getSubVersion() != 4 ? "t" : "u",
                    MethodType.methodType(void.class, boolean.class)
            );
            GET_DATA_WATCHER = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    XMaterial.getVersion() == 17 ? "getDataWatcher" : nineteenTwoOrBelow() ? "ai" :
                            getSubVersion() == 3 ? "al" : "aj",
                    MethodType.methodType(DATAWATCHER)
            );
            GET_BUKKIT_ENTITY = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    "getBukkitEntity",
                    MethodType.methodType(CRAFT_ENTITY)
            );
            GET_ID = MethodHandles.lookup().findVirtual(
                    ENTITY,
                    XMaterial.getVersion() == 17 ? "getId" : nineteenTwoOrBelow() ? "ae" :
                            getSubVersion() == 3 ? "ah" : "af",
                    MethodType.methodType(int.class)
            );
            AS_NMS_COPY = MethodHandles.lookup().findStatic(
                    ReflectionUtils.getCraftClass("inventory.CraftItemStack"),
                    "asNMSCopy",
                    MethodType.methodType(ITEM_STACK, ItemStack.class)
            );

            NO_CLIP = ARMOR_STAND.getField(XMaterial.getVersion() == 17 ? "P" : "Q");

            DATA_WATCHER_SET = MethodHandles.lookup().findVirtual(
                    DATAWATCHER,
                    XMaterial.getVersion() >= 18 ? "b" : "set",
                    MethodType.methodType(void.class, DATA_WATCHER_OBJECT, Object.class)
            );
            DATA_WATCHER_PACK_DIRTY = XMaterial.getVersion() == 19 ? MethodHandles.lookup().findVirtual(
                    DATAWATCHER, "c", MethodType.methodType(List.class)
            ) : null;

            MAINHAND = ENUM_ITEM_SLOT.getDeclaredField("a").get(null);
            OFFHAND = ENUM_ITEM_SLOT.getDeclaredField("b").get(null);
            HELMET = ENUM_ITEM_SLOT.getDeclaredField("f").get(null);
            CHESTPLATE = ENUM_ITEM_SLOT.getDeclaredField("e").get(null);
            LEGGINGS = ENUM_ITEM_SLOT.getDeclaredField("d").get(null);
            BOOTS = ENUM_ITEM_SLOT.getDeclaredField("c").get(null);

            boolean newestNames = XMaterial.getVersion() == 19 && getSubVersion() == 4;
            HEAD = ARMOR_STAND.getDeclaredField(newestNames ? "bC" : "bH").get(null);
            BODY = ARMOR_STAND.getDeclaredField(newestNames ? "bD" : "bI").get(null);
            LEFT_ARM = ARMOR_STAND.getDeclaredField(newestNames ? "bE" : "bJ").get(null);
            RIGHT_ARM = ARMOR_STAND.getDeclaredField(newestNames ? "bF" : "bK").get(null);
            LEFT_LEG = ARMOR_STAND.getDeclaredField(newestNames ? "bG" : "bL").get(null);
            RIGHT_LEG = ARMOR_STAND.getDeclaredField(newestNames ? "bH" : "bM").get(null);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Object armorStand;

    @Override
    public void setup(World world) {
        try {
            armorStand = ARMOR_STAND_INSTANCE.newInstance(getNMSWorld(world), 0, 0, 0);
            SET_INVISIBLE.invoke(armorStand, true);
            SET_MARKER.invoke(armorStand, true);
            NO_CLIP.set(armorStand, true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite) {
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

        Object spawnPacket = PACKET_LIVING_INSTANCE.newInstance(
                armorStand
        );
        players.forEach(p -> ReflectionUtils.sendPacket(p, spawnPacket));

        return (LivingEntity) GET_BUKKIT_ENTITY.invoke(armorStand);
    }

    @SneakyThrows
    @Override
    public void update(Collection<Player> players, ItemStack[] stack, boolean setData) {
        update(players, stack, GET_DATA_WATCHER.invoke(armorStand), true);
    }

    @SneakyThrows
    @Override
    public void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher, boolean setData) {
        Object[] packets = new Object[2];
        packets[0] = XMaterial.getVersion() == 19 ? PACKET_META_INSTANCE.newInstance(
                GET_ID.invoke(armorStand),
                DATA_WATCHER_PACK_DIRTY.invoke(dataWatcher)
        ) : PACKET_META_INSTANCE.newInstance(
                GET_ID.invoke(armorStand),
                dataWatcher,
                true
        );
        List<Pair<?, ?>> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            list.add(new Pair<>(getPair(i), AS_NMS_COPY.invoke(stack[i])));
        }
        packets[setData ? 1 : 0] = PACKET_EQUIP_INSTANCE.newInstance(
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

    @Override
    public Object rotate(float[][] rotations) {
        try {
            DataWatcher data = (DataWatcher) GET_DATA_WATCHER.invoke(armorStand);
            Object[] dataWatcherObjects = new Object[] {
                    HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
            };
            for(int i = 0; i < 6; i++) {
                float[] array = rotations[i];

                DATA_WATCHER_SET.invoke(
                        data,
                        dataWatcherObjects[i],
                        VECTOR_3F_INSTANCE.newInstance(array[0], array[1], array[2])
                );
            }
            return data;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SneakyThrows @Override
    public void destroy(Collection<Player> players) {
        // Either way I get a warning, why IntelliJ?
        Object packet = PACKET_DESTROY_INSTANCE.newInstance((Object) new int[] {(int) GET_ID.invoke(armorStand)});
        players.forEach(p -> ReflectionUtils.sendPacket(p, packet));
    }

    public static boolean nineteenTwoOrBelow() {
        return XMaterial.getVersion() == 18 || getSubVersion() < 3;
    }

    public static int getSubVersion() {
        return Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[2]);
    }
}
