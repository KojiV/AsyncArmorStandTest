package koji.skyblock.asyncarmorstandtest.armorstand;

import com.mojang.datafixers.util.Pair;
import koji.developerkit.utils.xseries.ReflectionUtils;
import koji.developerkit.utils.xseries.XMaterial;
import koji.skyblock.asyncarmorstandtest.utils.MethodHandleAssistant;
import koji.skyblock.asyncarmorstandtest.utils.UncollidableArmorStand;
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
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("all")
public class UncollidableArmorStand_1_17 extends MethodHandleAssistant implements UncollidableArmorStand {

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

    private static final MethodHandle PACKET_LIVING_INSTANCE;
    private static final MethodHandle PACKET_META_INSTANCE;
    private static final MethodHandle PACKET_EQUIP_INSTANCE;
    private static final MethodHandle PACKET_TELEPORT_INSTANCE;
    private static final MethodHandle PACKET_DESTROY_INSTANCE;
    private static final MethodHandle ARMOR_STAND_INSTANCE;
    private static final MethodHandle VECTOR_3F_INSTANCE;

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
                "PacketPlayOutSpawnEntity" + (XMaterial.getVersion() >= 19 ? "" : "Living")
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

            PACKET_LIVING_INSTANCE = getConstructor(
                    PACKET_SPAWN_LIVING, XMaterial.getVersion() >= 19 ? ENTITY : ENTITY_LIVING
            );
            PACKET_META_INSTANCE = XMaterial.getVersion() >= 19 && getSubVersion() >= 3 ?
                    getConstructor(PACKET_METADATA, int.class, List.class) :
                    getConstructor(PACKET_METADATA, int.class, DATAWATCHER, boolean.class);
            PACKET_EQUIP_INSTANCE = getConstructor(PACKET_EQUIPMENT, int.class, List.class);
            PACKET_TELEPORT_INSTANCE = getConstructor(PACKET_TELEPORT, ENTITY);
            PACKET_DESTROY_INSTANCE = getConstructor(PACKET_DESTROY, int[].class);
            ARMOR_STAND_INSTANCE = getConstructor(ARMOR_STAND, WORLD, double.class, double.class, double.class);
            VECTOR_3F_INSTANCE = getConstructor(VECTOR_3F_CLASS, float.class, float.class, float.class);

            SET_LOCATION = getMethod(ENTITY, MethodType.methodType(
                    void.class,
                    double.class, double.class, double.class, float.class, float.class
            ), "setLocation", "a");

            SET_INVISIBLE = getMethod(ARMOR_STAND, MethodType.methodType(void.class, boolean.class),
                    "setInvisible", "j"
            );

            SET_MARKER = getMethod(ARMOR_STAND, MethodType.methodType(void.class, boolean.class),
                    "setMarker", "t", "u"
            );

            GET_DATA_WATCHER = getMethod(
                    ENTITY, MethodType.methodType(DATAWATCHER),
                    "getDataWatcher", "ai", "al", "aj"
            );

            GET_BUKKIT_ENTITY = getMethod(ENTITY, MethodType.methodType(CRAFT_ENTITY), "getBukkitEntity");

            GET_ID = getMethod(ENTITY, MethodType.methodType(int.class), "getId", "ae", "ah", "af");

            AS_NMS_COPY = getMethod(
                    ReflectionUtils.getCraftClass("inventory.CraftItemStack"),
                    MethodType.methodType(ITEM_STACK, ItemStack.class), true, "asNMSCopy"
            );

            NO_CLIP = ARMOR_STAND.getField(XMaterial.getVersion() == 17 ? "P" : "Q");

            DATA_WATCHER_SET = getMethod(DATAWATCHER,
                    MethodType.methodType(void.class, DATA_WATCHER_OBJECT, Object.class),
                    "b", "set"
            );

            DATA_WATCHER_PACK_DIRTY = XMaterial.getVersion() >= 19 ?
                    getMethod(DATAWATCHER, MethodType.methodType(List.class), "c") :
                    null;

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
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Object stand;
    private final Set<Player> visibleTo;

    public UncollidableArmorStand_1_17() {
        visibleTo = new HashSet<>();
    }

    @Override
    public void setup(World world) {
        try {
            stand = ARMOR_STAND_INSTANCE.invoke(getNMSWorld(world), 0, 0, 0);
            SET_INVISIBLE.invoke(stand, true);
            SET_MARKER.invoke(stand, true);
            NO_CLIP.set(stand, true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public LivingEntity spawn(Collection<Player> players, Location location, float[][] rotations, boolean overwrite) {
        Object craftWorld = getNMSWorld(location.getWorld());

        if (stand == null || overwrite) {
            stand = ARMOR_STAND_INSTANCE.invoke(craftWorld, location.getX(), location.getY(), location.getZ());

            SET_INVISIBLE.invoke(stand, true);
            SET_MARKER.invoke(stand, true);
            NO_CLIP.set(stand, true);
        }
        SET_LOCATION.invoke(
                stand, location.getX(),
                location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
        );

        Object spawnPacket = PACKET_LIVING_INSTANCE.invoke(stand);

        Object dataWatcher = GET_DATA_WATCHER.invoke(stand);
        Object metaPacket = XMaterial.getVersion() >= 19 ? PACKET_META_INSTANCE.invoke(
                GET_ID.invoke(stand),
                DATA_WATCHER_PACK_DIRTY.invoke(dataWatcher)
        ) : PACKET_META_INSTANCE.invoke(
                GET_ID.invoke(stand),
                dataWatcher,
                true
        );
        players.forEach(p -> {
            visibleTo.add(p);
            ReflectionUtils.sendPacket(p, spawnPacket, metaPacket);
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

        return (LivingEntity) GET_BUKKIT_ENTITY.invoke(stand);
    }

    @SneakyThrows
    @Override
    public void update(Collection<Player> players, ItemStack[] stack, boolean setData) {
        update(players, stack, GET_DATA_WATCHER.invoke(stand), true);
    }

    @SneakyThrows
    @Override
    public void update(Collection<Player> players, ItemStack[] stack, Object dataWatcher, boolean setData) {
        Object[] packets = new Object[2];
        packets[0] = XMaterial.getVersion() >= 19 ? PACKET_META_INSTANCE.invoke(
                GET_ID.invoke(stand),
                DATA_WATCHER_PACK_DIRTY.invoke(dataWatcher)
        ) : PACKET_META_INSTANCE.invoke(
                GET_ID.invoke(stand),
                dataWatcher,
                true
        );
        List<Pair<?, ?>> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            list.add(new Pair<>(getPair(i), AS_NMS_COPY.invoke(stack[i])));
        }
        packets[setData ? 1 : 0] = PACKET_EQUIP_INSTANCE.invoke(
                GET_ID.invoke(stand), list
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
    public void move(Collection<Player> players, Location loc) {
        try {
            SET_LOCATION.invoke(stand, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            Object teleport = PACKET_TELEPORT_INSTANCE.invoke(stand);
            players.forEach(p -> ReflectionUtils.sendPacket(p, teleport));
        } catch (Throwable ignored) {}
    }

    @Override
    public ArmorStand getEntity() {
        try {
            return (ArmorStand) GET_BUKKIT_ENTITY.invoke(stand);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public Object rotate(float[][] rotations) {
        try {
            DataWatcher data = (DataWatcher) GET_DATA_WATCHER.invoke(stand);
            Object[] dataWatcherObjects = new Object[] {
                    HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
            };
            for(int i = 0; i < 6; i++) {
                float[] array = rotations[i];

                DATA_WATCHER_SET.invoke(
                        data,
                        dataWatcherObjects[i],
                        VECTOR_3F_INSTANCE.invoke(array[0], array[1], array[2])
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
        Object packet = PACKET_DESTROY_INSTANCE.invoke(new int[] {(int) GET_ID.invoke(stand)});
        players.forEach(p -> {
            visibleTo.remove(p);
            ReflectionUtils.sendPacket(p, packet);
        });
    }

    @Override
    public Set<Player> getPlayersVisibleFor() {
        return visibleTo;
    }

    public static int getSubVersion() {
        List<String> stuff = new ArrayList<>(Arrays.asList(
                Bukkit.getBukkitVersion().split("-")[0].split("\\.")
        ));
        return Integer.parseInt(getOrDefault(stuff, 2, "0"));
    }
}
