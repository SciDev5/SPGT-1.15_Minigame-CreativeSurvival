package me.uut118.creativesurvival;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MovementListener implements Listener {
	PluginMain plugin;
	
	public MovementListener(PluginMain plugin_) {
		plugin = plugin_;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		plugin.onPlayerTeleport(e.getPlayer(), new Runnable() {
			@Override
			public void run() {
				e.setCancelled(true);
			}
		});
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		plugin.onPlayerMovement(e.getPlayer(),e.getTo(),new Runnable() {
			@Override
			public void run() {
				e.setCancelled(true);
			}
		});
	}
}
