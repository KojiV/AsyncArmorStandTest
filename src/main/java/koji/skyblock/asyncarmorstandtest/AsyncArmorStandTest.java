package koji.skyblock.asyncarmorstandtest;

import com.viaversion.viaversion.api.Via;
import koji.developerkit.runnable.KRunnable;
import koji.developerkit.utils.SafeMap;
import koji.developerkit.utils.xseries.XMaterial;
import koji.skyblock.asyncarmorstandtest.armorstand.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

// TODO: Get rotation of the head to rotate around the stand, NOT the name tag.
public final class AsyncArmorStandTest extends JavaPlugin implements Listener {

    @Getter private static AsyncArmorStandTest main;

    @Getter private static EntityHider hider;

    @Override
    public void onEnable() {
        main = this;
        hider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
        canSeeMap = new SafeMap<>(true);
        correspondent = new SafeMap<>(
                XMaterial.supports(9) ? 1 : 0
        );
        instances = new HashMap<>();

        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("test").setExecutor(new TestCMD());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        hider.close();
    }

    // For these, the first value is 1.8, second is 1.9+
    static final double[] OFFSETS = { -0.7, -0.95 };
    static final double[] ROTATIONS = { 0.51, 0.82 };

    // This is mainly used for adjusting the armor stand to line up with the name tag.
    // Desmos came in clutch here, I hate math
    static final double[] ADJUSTMENTS = { -0.17, -0.45 };
    private static final EulerAngle[] ARM_ANGLES = {
            new EulerAngle(0, Math.toRadians(40), 0),
            new EulerAngle(
                    Math.toRadians(311),
                    Math.toRadians(44),
                    Math.toRadians(356.5)
            )
    };

    public static float getYaw(Player player, Location stand) {
        return (float) Math.toDegrees(Math.atan2(
                player.getLocation().getZ() - stand.getZ(),
                player.getLocation().getX() - stand.getX()
        )) - 90;
    }

    @Getter private static SafeMap<UUID, Integer> correspondent;
    private static HashMap<UUID, PlayerInstance> instances;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        int versionNum = Via.getAPI().getPlayerVersion(p.getUniqueId()) <= 47 ? 0 : 1;
        correspondent.put(p.getUniqueId(), versionNum);
        SkyblockWorld.getWorld(p.getWorld()).changedToWorld(p);

        new KRunnable(task -> instances.put(p.getUniqueId(), new PlayerInstance(p))
        ).runTaskLaterAsynchronously(main, 1L);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        SkyblockWorld.getWorld(p.getWorld()).leftWorld(p);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        SkyblockWorld.getWorld(e.getFrom()).leftWorld(p);
        SkyblockWorld.getWorld(p.getWorld()).changedToWorld(p);

        instances.get(p.getUniqueId()).getRunnable().setWorld(SkyblockWorld.getWorld(p.getWorld()));
    }

    public static UncollidableArmorStand getArmorStand() {
        switch (XMaterial.getVersion()) {
            case 8: return new UncollidableArmorStand_1_8();
            case 9: return new UncollidableArmorStand_1_9();
            case 10: return new UncollidableArmorStand_1_10();
            case 11: return new UncollidableArmorStand_1_11();
            case 12: return new UncollidableArmorStand_1_12();
            case 13: return new UncollidableArmorStand_1_13();
            case 14: return new UncollidableArmorStand_1_14();
            case 15: return new UncollidableArmorStand_1_15();
            case 16: return new UncollidableArmorStand_1_16();
            default: return new UncollidableArmorStand_1_17();
        }
    }
    @Getter private static HashMap<UUID, Boolean> canSeeMap;

    public static class PlayerInstance {
        @Getter private final Player player;
        @Getter private UncollidableArmorStand[] armorStands;
        @Getter private final PetStandRunnable runnable;
        @Getter private final int playerVersion;

        public PlayerInstance(Player p) {
            player = p;
            playerVersion = Via.getAPI().getPlayerVersion(p.getUniqueId()) <= 47 ? 0 : 1;
            armorStands = new UncollidableArmorStand[2];
            createArmorStands();

            runnable = new PetStandRunnable(this);
            runnable.runTaskTimerAsynchronously(getMain(), 10L, 1L);
        }

        public void createArmorStands() {
            armorStands = new UncollidableArmorStand[]{
                    getArmorStand(),
                    getArmorStand()
            };

            Set<Player>[] connectedPlayers = SkyblockWorld.getWorld(player.getWorld()).getCanSeePets();
            /*KStatic.println(connectedPlayers[0],
                    connectedPlayers[1],
                    player.getWorld().getName()
            );*/
            Set<Player> players = connectedPlayers[0];
            players.addAll(connectedPlayers[1]);

            ArmorStand armorStand = (ArmorStand) armorStands[0].spawn(
                    players, player.getLocation().add(
                            0, 1.55 - OFFSETS[playerVersion], 0
                    )
            );
            double yaw = Math.toRadians(getYaw(player, armorStand.getLocation()));
            ArmorStand nameTag = (ArmorStand) armorStands[1].spawn(
                    players, armorStand.getLocation().add(
                            -Math.cos(yaw) * ROTATIONS[playerVersion],
                            -OFFSETS[playerVersion],
                            -Math.sin(yaw) * ROTATIONS[playerVersion]
                    )
            );

            armorStand.setArms(true);
            armorStand.setGravity(false);
            nameTag.setGravity(false);
            nameTag.setCustomNameVisible(false);
            armorStands[1].update(players, new ItemStack[6]);

            for(int i = 0; i < 2; i++) {
                if (!connectedPlayers[i].isEmpty()) {
                    armorStand.setRightArmPose(ARM_ANGLES[i]);
                    armorStands[0].update(connectedPlayers[i], new ItemStack[]{
                            XMaterial.PLAYER_HEAD.parseItem(), null, null,
                            null, null, null
                    });
                }
            }
        }
    }
}