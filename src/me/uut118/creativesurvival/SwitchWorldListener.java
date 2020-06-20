package me.uut118.creativesurvival;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SwitchWorldListener implements Listener {
	PluginMain plugin;
	public SwitchWorldListener(PluginMain plugin_) {
		plugin = plugin_;
	}
	@EventHandler
	void onChangeWorld(PlayerChangedWorldEvent e) {
		plugin.playerChangeWorld(e.getFrom(),e.getPlayer().getWorld(),e.getPlayer());
	}
	@EventHandler
	void onLeaveGame(PlayerQuitEvent e) {
		plugin.playerChangeWorld(e.getPlayer().getWorld(),null,e.getPlayer());
	}
	@EventHandler
	void onJoinGame(PlayerJoinEvent e) {
		plugin.playerChangeWorld(null,e.getPlayer().getWorld(),e.getPlayer());
	}
}
