package koji.skyblock.asyncarmorstandtest;

import koji.developerkit.commands.KCommand;
import koji.developerkit.utils.xseries.XMaterial;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Set;

import static koji.skyblock.asyncarmorstandtest.AsyncArmorStandTest.*;

public class TestCMD extends KCommand {

    UncollidableArmorStand stand;
    UncollidableArmorStand nameTag;
    Location loc;
    boolean visible;

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player p = (Player) commandSender;

        boolean override = false;
        if (args.length != 0) {
            int version = Integer.parseInt(args[0]);
            OFFSETS[version] = Double.parseDouble(args[1]);
            ROTATIONS[version] = Double.parseDouble(args[2]);
            ADJUSTMENTS[version] = Double.parseDouble(args[3]);
            ARM_ANGLES[version][0] = Float.parseFloat(args[4]);
            ARM_ANGLES[version][1] = Float.parseFloat(args[5]);
            ARM_ANGLES[version][2] = Float.parseFloat(args[6]);
            override = Boolean.parseBoolean(args[7]);
        }

        if (stand == null || override) {
            for (int i = 0; i < 2; i++) {
                Set<Player> canSeePets = SkyblockWorld.getWorld(p.getWorld()).getCanSeePets()[i];
                if (stand != null) canSeePets.forEach(pl ->
                        AsyncArmorStandTest.getHider().hideEntity(pl, stand.getEntity())
                );
                if (nameTag != null) canSeePets.forEach(pl ->
                        AsyncArmorStandTest.getHider().hideEntity(pl, nameTag.getEntity())
                );
            }

            stand = AsyncArmorStandTest.getArmorStand();
            nameTag = AsyncArmorStandTest.getArmorStand();
            stand.setup(p.getWorld());
            nameTag.setup(p.getWorld());
            loc = p.getLocation();

            ArmorStand entity = stand.getEntity();
            entity.setArms(true);
            entity.setGravity(false);
            entity.setItemInHand(XMaterial.PLAYER_HEAD.parseItem());

            ArmorStand name = nameTag.getEntity();
            name.setCustomName("Name Tag");
            name.setGravity(false);
            name.setCustomNameVisible(true);
            name.setMarker(true);
            name.setVisible(false);

            for (int i = 0; i < 2; i++) {
                Set<Player> canSeePets = SkyblockWorld.getWorld(p.getWorld()).getCanSeePets()[i];
                entity = (ArmorStand) stand.spawn(canSeePets,
                        p.getLocation().clone().add(0, 1.55 - OFFSETS[i], 0),
                        new float[][]{
                                new float[3],
                                new float[3],
                                new float[3],
                                ARM_ANGLES[i],
                                new float[3],
                                new float[3]
                        }, false);

                double yaw = Math.toRadians(loc.getYaw());
                double sin = Math.sin(yaw);
                double cos = Math.cos(yaw);

                nameTag.spawn(SkyblockWorld.getWorld(p.getWorld()).getAllCanSeePlayers(),
                        entity.getLocation().clone().add(
                                -cos * ROTATIONS[i] + sin * ADJUSTMENTS[i],
                                -OFFSETS[i],
                                -sin * ROTATIONS[i] - cos * ADJUSTMENTS[i]
                        ), false
                );
            }
            visible = true;
        } else {
            AsyncArmorStandTest.toggleVisibility(p);
        }
        return false;
    }
}
