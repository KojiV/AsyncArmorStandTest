package koji.skyblock.asyncarmorstandtest;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import koji.developerkit.listener.KListener;
import koji.developerkit.utils.xseries.XMaterial;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class EntityHider extends KListener {
    protected Table<Integer, Integer, Boolean> observerEntityMap = HashBasedTable.create();

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        if(!canSee(e.getPlayer(), e.getItem())) e.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT, BED, ANIMATION, NAMED_ENTITY_SPAWN,
            COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_LIVING, SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY, REL_ENTITY_MOVE, ENTITY_LOOK, ENTITY_MOVE_LOOK, ENTITY_MOVE_LOOK,
            ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS, ATTACH_ENTITY, ENTITY_METADATA,
            ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION
    };
    public enum Policy {
        WHITELIST,
        BLACKLIST,
    }

    private ProtocolManager manager;

    private final Listener bukkitListener;
    private final PacketAdapter protocolListener;

    protected final Policy policy;

    public EntityHider(Plugin plugin, Policy policy) {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");
        // Save policy
        this.policy = policy;
        this.manager = ProtocolLibrary.getProtocolManager();

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(
                bukkitListener = constructBukkit(), plugin);
        manager.addPacketListener(
                protocolListener = constructProtocol(plugin));
    }

    protected boolean setVisibility(Player observer, int entityID, boolean visible) {
        switch (policy) {
            case BLACKLIST:
                return !setMembership(observer, entityID, !visible);
            case WHITELIST:
                return setMembership(observer, entityID, visible);
            default :
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    protected boolean setMembership(Player observer, int entityID, boolean member) {
        if (member) {
            return observerEntityMap.put(observer.getEntityId(), entityID, true) != null;
        } else {
            return observerEntityMap.remove(observer.getEntityId(), entityID) != null;
        }
    }

    protected boolean getMembership(Player observer, int entityID) {
        return observerEntityMap.contains(observer.getEntityId(), entityID);
    }

    protected boolean isVisible(Player observer, int entityID) {
        boolean presence = getMembership(observer, entityID);

        return (policy == Policy.WHITELIST) == presence;
    }

    protected void removeEntity(Entity entity) {
        int entityID = entity.getEntityId();

        for (Map<Integer, Boolean> maps : observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    protected void removePlayer(Player p) {
        observerEntityMap.rowMap().remove(p.getEntityId());
    }

    private Listener constructBukkit() {
        return new Listener() {

            @EventHandler
            public void onEntityDeath(EntityDeathEvent e) {
                removeEntity(e.getEntity());
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent e) {
                for (Entity entity : e.getChunk().getEntities()) {
                    removeEntity(entity);
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePlayer(e.getPlayer());
            }
        };
    }

    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityID = event.getPacket().getIntegers().read(0);

                if (!isVisible(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }
        };
    }

    public final boolean toggleEntity(Player observer, Entity entity) {
        if (isVisible(observer, entity.getEntityId())) {
            return hideEntity(observer, entity);
        }
        return !showEntity(observer, entity);
    }

    public final boolean setHide(Player observer, Entity entity, boolean willSee) {
        if (willSee) {
            return showEntity(observer, entity);
        }
        return hideEntity(observer, entity);
    }

    public final boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);

        if (manager != null && hiddenBefore) {
            manager.updateEntity(entity, arrayList(observer));
        }
        return hiddenBefore;
    }

    public final boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);

        if (visibleBefore) {
            PacketContainer destroyEntity = new PacketContainer(ENTITY_DESTROY);
            if(XMaterial.supports(17)) {
                destroyEntity.getIntLists().write(0, new ArrayList<>(Collections.singletonList(
                        entity.getEntityId()
                )));
            } else {
                destroyEntity.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
            }

            manager.sendServerPacket(observer, destroyEntity);
        }
        return visibleBefore;
    }

    public final boolean canSee(Player observer, Entity entity) {
        validate(observer, entity);

        return isVisible(observer, entity.getEntityId());
    }

    private void validate(Player observer, Entity entity) {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.");
        Preconditions.checkNotNull(entity, "entity cannot be NULL.");
    }

    public Policy getPolicy() {
        return policy;
    }

    public void close() {
        if (manager != null) {
            HandlerList.unregisterAll(bukkitListener);
            manager.removePacketListener(protocolListener);
            manager = null;
        }
    }
}
