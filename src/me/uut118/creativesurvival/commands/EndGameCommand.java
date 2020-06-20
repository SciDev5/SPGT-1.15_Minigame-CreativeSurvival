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

public class EndGameCommand implements CommandExecutor, TabCompleter {
	PluginMain plugin;
	public EndGameCommand(PluginMain plugin_) {
		plugin = plugin_;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"Error: Only players may use this command.");
			return true;
		}
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED+"Error: Invalid usage: Use /"+label);
			return true;
		}
		Player player = (Player) sender;
		if (!plugin.minigameWorlds.contains(player.getWorld())) {
			sender.sendMessage(ChatColor.RED+"Error: World not used for this minigame.");
			return true;
		}
		GameManager gm;
		try {
			gm = plugin.gameManagers.get(plugin.minigameWorlds.indexOf(player.getWorld()));
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED+"Error: World not used for this minigame.");
			return true;
		}
		if (gm.getGameRunning()) {
			gm.endGame();
			sender.sendMessage(ChatColor.GREEN+"Ended the game.");
		} else {
			sender.sendMessage(ChatColor.RED+"Error: Game was not running.");
		}
		return true;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> possibleValues = new ArrayList<String>();
		return possibleValues;
	}

}
