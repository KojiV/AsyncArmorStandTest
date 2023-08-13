package koji.skyblock.asyncarmorstandtest;

import com.viaversion.viaversion.api.Via;
import koji.developerkit.runnable.KRunnable;
import koji.developerkit.utils.SafeMap;
import koji.developerkit.utils.xseries.XMaterial;
import koji.skyblock.asyncarmorstandtest.armorstand.*;
import koji.skyblock.asyncarmorstandtest.utils.EntityHider;
import koji.skyblock.asyncarmorstandtest.utils.SkyblockWorld;
import koji.skyblock.asyncarmorstandtest.utils.UncollidableArmorStand;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

// TODO: 1. Test every version
//       2*. Get rotation of the head to rotate around the stand, NOT the name tag.
//
//       * = possibly won't happen
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
    public static final double[] OFFSETS = { -0.7, -0.95 };
    public static final double[] ROTATIONS = { 0.54, -0.22 };

    // This is mainly used for adjusting the armor stand to line up with the name tag.
    // Desmos came in clutch here, I hate math
    public static final double[] ADJUSTMENTS = { -0.135, 0.45 };
    public static final float[][] ARM_ANGLES = {
            { 0, 40f, 0 },
            { 313f, 226f, 2.9f }
    };

    public static float getYaw(Player player, Location stand) {
        return (float) Math.toDegrees(Math.atan2(
                player.getLocation().getZ() - stand.getZ(),
                player.getLocation().getX() - stand.getX()
        )) - 90;
    }

    @Getter private static SafeMap<UUID, Integer> correspondent;
    @Getter private static HashMap<UUID, PlayerInstance> instances;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        int versionNum = Via.getAPI().getPlayerVersion(p.getUniqueId()) <= 47 ? 0 : 1;
        correspondent.put(p.getUniqueId(), versionNum);

        new KRunnable(task -> {
            SkyblockWorld.getWorld(p.getWorld()).changedToWorld(p);
            instances.put(p.getUniqueId(), new PlayerInstance(p));
        }).runTaskLaterAsynchronously(main, 10L);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        instances.get(p.getUniqueId()).getRunnable().cancel();
        SkyblockWorld.getWorld(p.getWorld()).leftWorld(p);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        SkyblockWorld.getWorld(e.getFrom()).leftWorld(p);
        SkyblockWorld.getWorld(p.getWorld()).changedToWorld(p);

        instances.get(p.getUniqueId()).getRunnable().setWorld(SkyblockWorld.getWorld(p.getWorld()));
    }

    public static void toggleVisibility(Player p) {
        toggleVisibility(p, !canSeeMap.get(p.getUniqueId()));
    }

    public static void toggleVisibility(Player p, boolean boo) {
        canSeeMap.put(p.getUniqueId(), boo);

        Collection<Player> player = Collections.singleton(p);
        Set<UncollidableArmorStand> stands = SkyblockWorld.getWorld(p.getWorld()).getAllArmorStand();

            if(boo) stands.forEach(a -> a.spawn(player, new float[][] {
                    new float[3],
                    new float[3],
                    new float[3],
                    ARM_ANGLES[AsyncArmorStandTest.getCorrespondent().get(p.getUniqueId())],
                    new float[3],
                    new float[3]
            }, false));
        else stands.forEach(a -> a.destroy(player));
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
            //KStatic.println(playerVersion);
            armorStands = new UncollidableArmorStand[2];
            createArmorStands();

            runnable = new PetStandRunnable(this);
            runnable.runTaskTimerAsynchronously(getMain(), 10L, 1L);
            //KStatic.println(playerVersion);
        }

        public void createArmorStands() {
            armorStands = new UncollidableArmorStand[]{
                    getArmorStand(),
                    getArmorStand()
            };

            armorStands[0].setup(player.getWorld());
            armorStands[1].setup(player.getWorld());

            ArmorStand stand = armorStands[0].getEntity();
            stand.setArms(true);
            stand.setGravity(false);
            stand.setItemInHand(XMaterial.PLAYER_HEAD.parseItem());
            SkyblockWorld.getWorld(player.getWorld()).getAllArmorStand().add(armorStands[0]);

            ArmorStand nameTag = armorStands[1].getEntity();
            nameTag.setGravity(false);
            nameTag.setCustomNameVisible(true);
            nameTag.setCustomName("Name Tag");
            SkyblockWorld.getWorld(player.getWorld()).getAllArmorStand().add(armorStands[1]);

            Set<Player>[] connectedPlayers = SkyblockWorld.getWorld(player.getWorld()).getCanSeePets();
            for(int i = 0; i < 2; i++) {
                armorStands[1].spawn(connectedPlayers[i],
                        player.getLocation().add(0, 1.55, 0),
                        false
                );

                double yaw = Math.toRadians(getYaw(player, armorStands[1].getEntity().getLocation()));
                double sin = Math.sin(yaw);
                double cos = Math.cos(yaw);

                armorStands[0].spawn(connectedPlayers[i], armorStands[1].getEntity().getLocation().add(
                        cos * ROTATIONS[i] - sin * ADJUSTMENTS[i],
                        OFFSETS[i],
                        sin * ROTATIONS[i] + cos * ADJUSTMENTS[i]
                ), new float[][]{
                        new float[3],
                        new float[3],
                        new float[3],
                        ARM_ANGLES[i],
                        new float[3],
                        new float[3]
                }, false);
            }
        }
    }
}
