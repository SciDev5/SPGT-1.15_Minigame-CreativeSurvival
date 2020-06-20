package me.uut118.creativesurvival;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
	PluginMain plugin;
	
	public DeathListener(PluginMain plugin_) {
		plugin = plugin_;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		plugin.onPlayerDeath(e.getEntity(), e.getDeathMessage(), new Runnable() {
			@Override
			public void run() {
				e.setDeathMessage(null);
			}
		});
	}
}
