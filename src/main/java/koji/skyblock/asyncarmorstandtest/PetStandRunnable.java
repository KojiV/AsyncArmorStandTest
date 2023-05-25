package koji.skyblock.asyncarmorstandtest;

import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

import static koji.skyblock.asyncarmorstandtest.AsyncArmorStandTest.*;


public class PetStandRunnable extends BukkitRunnable {
    private static final double SPEED;
    private static final double DISTANCE;
    static {
        SPEED = 4.5;
        DISTANCE = 0.42;
    }

    private final AsyncArmorStandTest.PlayerInstance instance;
    private final Player player;
    @Setter private SkyblockWorld world;

    public PetStandRunnable(AsyncArmorStandTest.PlayerInstance p) {
        instance = p;
        player = p.getPlayer();
        world = SkyblockWorld.getWorld(p.getPlayer().getWorld());

        degree = 0;
        lastY = 0;
    }

    private int degree;
    private double lastY;

    @Override
    public void run() {
        if(degree == Integer.MAX_VALUE) degree = 0;

        // Fucking hate math
        double radians = Math.toRadians(degree);
        double y = Math.sin(radians * SPEED) * DISTANCE;

        Location playerLoc = player.getLocation().add(0, 1.55, 0);
        Location mainLoc = instance.getArmorStands()[0].getEntity().getLocation();
        Location nameTagLoc = instance.getArmorStands()[1].getEntity().getLocation();

        // Gets the distance between the armor stand and the player (squared)
        double distance = 0.0;
        if(mainLoc.getWorld() == player.getWorld()) {
            distance = playerLoc.distanceSquared(nameTagLoc);
        }

        // If it's more than sqrt(2.5) blocks away (keep in mind it goes by squared value)
        if (distance > 2.5) {
            // Moves the name tag closer to the player at the speed of the average player
            mainLoc = mainLoc.add(playerLoc.toVector()
                    .subtract(nameTagLoc.toVector())
                    .normalize()
                    .multiply(0.3)
            );

            mainLoc.setYaw(getYaw(player, nameTagLoc));
        }
        mainLoc.add(0, y - lastY, 0);

        double yaw = Math.toRadians(mainLoc.getYaw());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        if(mainLoc.getWorld() != player.getWorld()) {
            mainLoc.getWorld().getPlayers().forEach(p -> {
                AsyncArmorStandTest.getHider().hideEntity(p, instance.getArmorStands()[0].getEntity());
                AsyncArmorStandTest.getHider().hideEntity(p, instance.getArmorStands()[1].getEntity());
            });
            instance.createArmorStands();
        }

        for(int i = 0; i < 2; i++) {
            if(distance > 400 || mainLoc.getWorld() != player.getWorld()) {
                mainLoc = player.getLocation().add(0, 1.55 - OFFSETS[i], 0);
            }
            Set<Player> players = world.getCanSeePets()[i];
            if(!players.isEmpty()) {
                nameTagLoc = mainLoc.clone().add(
                        -cos * ROTATIONS[i] + sin * ADJUSTMENTS[i],
                        -OFFSETS[i],
                        -sin * ROTATIONS[i] - cos * ADJUSTMENTS[i]
                );
                instance.getArmorStands()[1].move(players, nameTagLoc);
                instance.getArmorStands()[0].move(players, mainLoc);
            }
        }
        lastY = y;
        degree++;
    }
}
