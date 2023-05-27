package koji.skyblock.asyncarmorstandtest;

import koji.developerkit.commands.KCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCMD extends KCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        AsyncArmorStandTest.toggleVisibility(p);
        return false;
    }
}
