package koji.skyblock.asyncarmorstandtest;

import koji.skyblock.asyncarmorstandtest.utils.SkyblockWorld;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Set;

import static koji.skyblock.asyncarmorstandtest.AsyncArmorStandTest.*;


public class PetStandRunnable extends BukkitRunnable {
    private static final double SPEED;
    private static final double DISTANCE;
    static {
        SPEED = 4.5;
        DISTANCE = 0.42;
    }

    private final PlayerInstance instance;
    private final Player player;
    @Setter private SkyblockWorld world;

    public PetStandRunnable(PlayerInstance p) {
        instance = p;
        player = p.getPlayer();
        world = SkyblockWorld.getWorld(p.getPlayer().getWorld());

        degree = 0;
        lastY = 0;
        nameTagLoc = instance.getArmorStands()[1].getEntity().getLocation();
    }

    private int degree;
    private double lastY;
    private Location nameTagLoc;

    @Override
    public void run() {
        if(degree == Integer.MAX_VALUE) degree = 0;

        // Fucking hate math
        double radians = Math.toRadians(degree);
        double y = Math.sin(radians * SPEED) * DISTANCE;

        Location playerLoc = player.getLocation().add(0, 1.55, 0);
        Location nameTagLoc = this.nameTagLoc;

        // Gets the distance between the armor stand and the player (squared)
        double distance = 0.0;
        if(nameTagLoc.getWorld() == player.getWorld()) {
            distance = playerLoc.distanceSquared(nameTagLoc);
        }

        // If it's more than sqrt(3) blocks away (keep in mind it goes by squared value)
        if (distance > 3) {
            // Moves the name tag closer to the player at the speed of the average player
            nameTagLoc = nameTagLoc.add(playerLoc.toVector()
                    .subtract(nameTagLoc.toVector())
                    .normalize()
                    .multiply(0.3)
            );

            nameTagLoc.setYaw(getYaw(player, nameTagLoc));
        }
        nameTagLoc.add(0, y - lastY, 0);

        double yaw = Math.toRadians(nameTagLoc.getYaw());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        if(nameTagLoc.getWorld() != player.getWorld()) {
            nameTagLoc.getWorld().getPlayers().forEach(p -> {
                getHider().hideEntity(p, instance.getArmorStands()[0].getEntity());
                getHider().hideEntity(p, instance.getArmorStands()[1].getEntity());
            });
            instance.createArmorStands();
        }

        for(int i = 0; i < 2; i++) {
            if(distance > 400 || nameTagLoc.getWorld() != player.getWorld()) {
                nameTagLoc = player.getLocation().add(0, 1.55, 0);
            }
            Set<Player> players = world.getCanSeePets()[i];
            if(!players.isEmpty()) {
                Location mainLoc = nameTagLoc.clone().add(
                        cos * ROTATIONS[i] - sin * ADJUSTMENTS[i],
                        OFFSETS[i],
                        sin * ROTATIONS[i] + cos * ADJUSTMENTS[i]
                );
                if(getCorrespondent().get(player.getUniqueId()) == i) {
                    this.nameTagLoc = nameTagLoc;
                }
                instance.getArmorStands()[1].move(players, nameTagLoc);
                instance.getArmorStands()[0].move(players, mainLoc);
            }
        }
        lastY = y;
        degree++;
    }

    @Override public synchronized void cancel() throws IllegalStateException {
        Arrays.stream(instance.getArmorStands()).forEach(a ->
                getHider().hideEntity(player, a.getEntity())
        );
        super.cancel();
    }
}
