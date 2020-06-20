package me.uut118.creativesurvival;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockModificationListener implements Listener {
	PluginMain plugin;
	public BlockModificationListener(PluginMain plugin_) {
		plugin = plugin_;
	}
	@EventHandler
	public void onBlockBroken(BlockBreakEvent e) {
		plugin.onBlockModify(e.getPlayer(), e.getBlock().getLocation(), new Runnable() {
			BlockBreakEvent event = e;
			@Override
			public void run() {
				event.setCancelled(true);
			}
		});
	}
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent e) {
		plugin.onBlockModify(e.getPlayer(), e.getBlock().getLocation(), new Runnable() {
			BlockPlaceEvent event = e;
			@Override
			public void run() {
				event.setCancelled(true);
			}
		});
	}
	@EventHandler
	public void onTNTBlockBroken(EntityExplodeEvent e) {
		List<Block> blocks = e.blockList();
		for (int i = blocks.size()-1; i >= 0; i--) {
			Block block = blocks.get(i);
			plugin.onBlockModify(null, block.getLocation(), new Runnable() {
				@Override
				public void run() {
					blocks.remove(block);
				}
			});
		}
	}
}
