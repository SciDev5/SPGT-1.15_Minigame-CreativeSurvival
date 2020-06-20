package me.uut118.creativesurvival.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.uut118.creativesurvival.GameManager;
import me.uut118.creativesurvival.PluginMain;

public class JoinGameCommand implements CommandExecutor, TabCompleter {
	PluginMain plugin;
	public JoinGameCommand(PluginMain plugin_) {
		plugin = plugin_;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"Error: Only players may use this command.");
			return true;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED+"Error: Invalid usage: Use /"+label+" <gameWorldId>");
			return true;
		}
		if (!plugin.minigameWorldIds.contains(args[0])) {
			sender.sendMessage(ChatColor.RED+"Error: World not found.");
			return true;
		}
		GameManager gm;
		try {
			gm = plugin.gameManagers.get(plugin.minigameWorldIds.indexOf(args[0]));
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED+"Error: World not found.");
			return true;
		}
		gm.tpPlayer((Player) sender);
		sender.sendMessage(ChatColor.GREEN+"Joined the game world.");
		if (gm.getGameRunning())
			sender.sendMessage(ChatColor.BLUE+"Game already running, set gamemode to spectator.");
		return true;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> possibleValues = new ArrayList<String>();
		if (args.length == 1)
			for (String worldId : plugin.minigameWorldIds)
				if (worldId.startsWith(args[0]))
					possibleValues.add(worldId);
		return possibleValues;
	}

}
