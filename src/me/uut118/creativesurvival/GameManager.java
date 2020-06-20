package me.uut118.creativesurvival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class GameManager {
	PluginMain plugin;
	ArrayList<Player> players;
	HashMap<Player, Boolean> playersAlive;
	HashMap<Player, Boolean> playersWithBlock;
	HashMap<Player, Location> playerBedLocations;
	HashMap<Player, ItemStack[]> playerInventories;
	ArrayList<Location> basePoses;
	ArrayList<Material> baseMaterials;
	int baseBuildRadius = 0;
	World world;
	String worldId;
	int counter = -1;
	Scoreboard scoreboard;
	Objective sidebarDisplay;
	String gameStateStr = ""+ChatColor.GRAY+ChatColor.BOLD+"Not Started";
	int gameState = -1;
	Location spawnLocation;
	double spawnProtection;
	public GameManager(PluginMain plugin_, World world_, String worldId_) {
		plugin = plugin_;
		world = world_;
		worldId = worldId_;
		counter = -1;
		scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
		sidebarDisplay = scoreboard.registerNewObjective("sidebar", "dummy", ChatColor.GREEN+"Creative"+ChatColor.AQUA+"Survival");
		sidebarDisplay.setDisplaySlot(DisplaySlot.SIDEBAR);
		basePoses = new ArrayList<Location>();
		baseMaterials = new ArrayList<Material>();
		try {
			spawnLocation = new Location(world, plugin.getConfig().getInt("worlddata."+worldId+".spawn.location.x"), plugin.getConfig().getInt("worlddata."+worldId+".spawn.location.y"), plugin.getConfig().getInt("worlddata."+worldId+".spawn.location.z"));
			spawnProtection = plugin.getConfig().getDouble("worlddata."+worldId+".spawn.protection");
			baseBuildRadius = plugin.getConfig().getInt("worlddata."+worldId+".buildRadius");
			for (String pos : plugin.getConfig().getStringList("worlddata."+worldId+".basePoses")) {
				String[] coordStrs = pos.split(",");
				basePoses.add(new Location(world, Integer.parseInt(coordStrs[0]), Integer.parseInt(coordStrs[1]), Integer.parseInt(coordStrs[2])));
			}
			for (String matName : plugin.getConfig().getStringList("worlddata."+worldId+".baseMaterials"))
				baseMaterials.add(Material.getMaterial(matName));
		} catch (Exception e) {
			basePoses = new ArrayList<Location>();
			baseMaterials = new ArrayList<Material>();
			plugin.getLogger().log(Level.WARNING, "Failed to set up world "+worldId+"! Invalid config!");
		}
			
	}
	
	public ArrayList<Player> getSpectators() {
		ArrayList<Player> spectators = (ArrayList<Player>) world.getPlayers();
		spectators.removeAll(players);
		return spectators; // Everyone who isn't a player is a spectator;
	}
	
	public Player getPlayerBlockByLocation(Location loc) {
		for (int i = 0; i < players.size(); i++)
			if (loc.distanceSquared(basePoses.get(i)) < 0.9 && playersWithBlock.get(players.get(i))) return players.get(i);
		return null;
	}
	
	public boolean getGameRunning() {
		return gameState != -1;
	}
	public boolean beginGame() {
		if (world.getPlayers().size() < 2)
			return false;
		world.save();
		world.setAutoSave(false);
		world.setPVP(true);
		players = (ArrayList<Player>) world.getPlayers();
		while(players.size() > basePoses.size() || players.size() > baseMaterials.size()) {
			players.get(players.size()-1).setGameMode(GameMode.SPECTATOR);
			players.remove(players.size()-1); // Remove late, over-capacity players from game.
		}
		playersAlive = new HashMap<Player, Boolean>();
		playersWithBlock = new HashMap<Player, Boolean>();
		playerBedLocations = new HashMap<Player, Location>();
		playerInventories = new HashMap<Player, ItemStack[]>();
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			playerInventories.put(p,p.getInventory().getContents());
			p.getInventory().clear();
			playerBedLocations.put(p,p.getBedSpawnLocation());
			p.teleport(basePoses.get(i).clone().add(0.5, 2, 0.5));
			p.setBedSpawnLocation(p.getLocation(basePoses.get(i).clone().add(0, 2, 0)),true);
			playersAlive.put(p, true);
			playersWithBlock.put(p, true);
			world.getBlockAt(basePoses.get(i)).setBlockData(baseMaterials.get(i).createBlockData());
			p.setScoreboard(scoreboard);
		}
		counter = 0;
		for (Player plr : players)
			plr.setGameMode(GameMode.CREATIVE);
		gameState = 0;
		gameStateStr = ChatColor.GREEN+"Build";
		titleMessage("Build a base","protect your block in.");
		return true;
	}
	
	public void endGame() {
		gameState = -1;
		counter = -1;
		gameStateStr = ""+ChatColor.GRAY+ChatColor.BOLD+"Not Started";
		for (Player p : players) if (playersAlive.get(p)) {
			p.setBedSpawnLocation(playerBedLocations.get(p));
			p.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
			p.getInventory().setContents(playerInventories.get(p));
		}
		for (Player player : world.getPlayers()) {
			player.teleport(player.getBedSpawnLocation()!=null?player.getBedSpawnLocation():plugin.getServer().getWorld("world").getSpawnLocation());
			player.setGameMode(player.getServer().getDefaultGameMode());
		}
		plugin.getServer().unloadWorld(world, false);
		world = plugin.loadWorld(worldId);
		plugin.minigameWorlds.set(plugin.minigameWorldIds.indexOf(worldId), world);
		players = new ArrayList<Player>();
	}
	
	public void tickTime() {
		ArrayList<String> strs = new ArrayList<String>();
		if (counter == -1) {
			// The game hasn't started yet, do nothing.
			strs.add("Game State: "+gameStateStr);
			strs.add("Min Players: "+ChatColor.BLUE+ChatColor.BOLD+2);
			strs.add("Max Players: "+ChatColor.RED+ChatColor.BOLD+Math.min(basePoses.size(), baseMaterials.size()));
			strs.add("Player count: "+(world.getPlayers().size()>Math.min(basePoses.size(),baseMaterials.size())?ChatColor.RED:world.getPlayers().size()<2?ChatColor.BLUE:ChatColor.GREEN)+ChatColor.BOLD+world.getPlayers().size());
		} else if (counter >= 0) {
			switch(counter) {
			case 295:
				titleMessage("Survival","in 5 seconds.");
				break;
			case 300:
				gameState = 1;
				gameStateStr = ChatColor.GOLD+"Survive";
				for (Player plr : players)
					plr.setGameMode(GameMode.SURVIVAL);
				break;
			case 1800:
				gameState = 2;
				gameStateStr = ChatColor.DARK_RED+""+ChatColor.BOLD+"Sudden Death";
				for (int i = 0; i < players.size(); i++)
					if (playersWithBlock.get(players.get(i))) 
						world.getBlockAt(basePoses.get(i)).breakNaturally();
				titleMessage(ChatColor.RED+"Sudden Death", "Your blocks were destroyed, u can die.");
				for (Player p : players) playersWithBlock.put(p, false);
				break;
			}
			strs.add("Time: "+ChatColor.BLUE+""+counter/60+":"+(counter%60<10?"0":"")+counter%60);
			strs.add("Game State: "+gameStateStr);
			strs.add("");
			strs.add(ChatColor.GRAY+""+ChatColor.BOLD+"Players: ");
			for (Player p : players)
				strs.add(p.getName()+" "+(new String[]{ChatColor.GREEN+"✔",ChatColor.GOLD+""+ChatColor.BOLD+"!",ChatColor.RED+""+ChatColor.BOLD+"X"})[playersWithBlock.get(p)?0:playersAlive.get(p)?1:2]);
			counter++;
		}
		setSidebar(strs);
	}
	
	public void chatMessage(String msg) {
		for (Player p : world.getPlayers()) 
			p.sendMessage(msg);
	}
	public void titleMessage(String msg, String subtitle) {
		for (Player plr : players)
			plr.sendTitle(msg, subtitle, 10, 20, 20);
	}
	public void setSidebar(ArrayList<String> lines) {
		sidebarDisplay.unregister();
		sidebarDisplay = scoreboard.registerNewObjective("sidebar", "dummy", ChatColor.GREEN+"Creative"+ChatColor.AQUA+"Survival");
		sidebarDisplay.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (int i = 0; i < lines.size(); i++) {
			Score score = sidebarDisplay.getScore(lines.get(i));
			score.setScore(lines.size()-i);
		}
	}
	public void killPlayer(Player player, String deathMessage) {
		if (!getGameRunning()) return;
		if (playersWithBlock.get(player)) {
			chatMessage(ChatColor.RED+deathMessage+"!");
			basePoses.get(players.indexOf(player)).clone().add(0, 2, 0).getBlock().breakNaturally();
			basePoses.get(players.indexOf(player)).clone().add(0, 3, 0).getBlock().breakNaturally();
		} else if (playersAlive.get(player)) {
			player.setHealth(player.getHealthScale());
			playersAlive.put(player,false);
			chatMessage(ChatColor.RED+deathMessage+"! "+ChatColor.BOLD+"(Final kill)");
			player.setBedSpawnLocation(playerBedLocations.get(player));
			player.getInventory().setContents(playerInventories.get(player));
			playerEliminated();
		} else {
			playerEliminated();
		}
	}

	private void playerEliminated() {
		int stillIn = 0;
		Player playerStillIn = null;
		for (Player player : players) if (playersAlive.get(player)) {
			stillIn++;
			playerStillIn = player;
		}
		if (stillIn == 1) {
			chatMessage(ChatColor.GREEN+""+ChatColor.BOLD+"Game Over: "+playerStillIn.getName()+" wins.");
			endGame();
		} else if (stillIn == 0) {
			chatMessage(ChatColor.GREEN+""+ChatColor.BOLD+"Game Over: There were no winners.");
			endGame();
		}
	}

	public void blockChange(Player player, Location blockPos, Runnable cancelCallback) {
		if (!getGameRunning()) return;
		if (blockPos.distanceSquared(spawnLocation) <= spawnProtection*spawnProtection) {
			if (!player.hasPermission("creativeSurvivalMinigame.modifyGame"))
				cancelCallback.run();
			return;
		}
		for (int i = 0; i < players.size(); i++) {
			if (basePoses.get(i).clone().add(0, 2, 0).distanceSquared(blockPos) < 0.9 || basePoses.get(i).clone().add(0, 3, 0).distanceSquared(blockPos) < 0.9) {
				cancelCallback.run();
				return;
			}
		}
		switch (gameState) {
		case -1: // Game not started, no block changes should happen except operator.
			if (player != null) if (player.hasPermission("creativeSurvivalMinigame.modifyGame")) break;
			cancelCallback.run();
			break;
		case 0: // Game in build mode
			if (player == null || getPlayerBlockByLocation(blockPos) != null) cancelCallback.run();
			else if (!players.contains(player) && !player.hasPermission("creativeSurvivalMinigame.modifyGame")) cancelCallback.run();
			else {
				Location basePos = basePoses.get(players.indexOf(player));
				if (Math.abs(basePos.getBlockX()-blockPos.getBlockX()) > baseBuildRadius || Math.abs(basePos.getBlockY()-blockPos.getBlockY()) > baseBuildRadius || Math.abs(basePos.getBlockZ()-blockPos.getBlockZ()) > baseBuildRadius) {
					cancelCallback.run();
				}
			}
			break;
		case 1: // Game in survival mode
			Player attackedPlayer = getPlayerBlockByLocation(blockPos);
			if (player == null ? attackedPlayer != null : attackedPlayer == player)
				cancelCallback.run();
			else if (player != null && attackedPlayer != null) {
				attackedPlayer.sendTitle("Block destroyed!", "You will not respawn!",10, 40, 20);
				chatMessage(ChatColor.GOLD+attackedPlayer.getName()+" had their block destroyed by "+player.getName()+"!");
				playersWithBlock.put(attackedPlayer,false);
			}
			break;
		case 2: // Sudden death mode
			// Players can modify all blocks so do nothing.
			break;
		}
	}

	public void tpPlayer(Player player) {
		player.teleport(spawnLocation.clone().add(0.5, 0, 0.5));
		player.setHealth(20);
		if (getGameRunning()) if (players.contains(player) && playersAlive.get(player)) {
			playersWithBlock.put(player,false);
			playersAlive.put(player,false);
			chatMessage(ChatColor.YELLOW+""+ChatColor.BOLD+""+player.getName()+ChatColor.RESET+""+ChatColor.YELLOW+" left the game and was eliminated.");
			player.setBedSpawnLocation(playerBedLocations.get(player));
			player.getInventory().setContents(playerInventories.get(player));
			playerEliminated();
		}
		player.setScoreboard(scoreboard);
		if (gameState == -1)
			player.setGameMode(GameMode.ADVENTURE);
		else
			player.setGameMode(GameMode.SPECTATOR);
	}

	public void removePlayer(Player player) {
		player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
		if (getGameRunning() && players.contains(player)) {
			playersWithBlock.put(player,false);
			playersAlive.put(player,false);
			chatMessage(ChatColor.YELLOW+""+ChatColor.BOLD+""+player.getName()+ChatColor.RESET+""+ChatColor.YELLOW+" left the game and was eliminated.");
			player.setBedSpawnLocation(playerBedLocations.get(player));
			player.getInventory().setContents(playerInventories.get(player));
			playerEliminated();
		}
	}

	public void playerMovement(Player player, Location location, Runnable cancel) {
		if (!getGameRunning()) return;
		if (!players.contains(player)) return;
		if (playersAlive.get(player)) return;
		switch (gameState) {
		case 0:
			if (location.distanceSquared(basePoses.get(players.indexOf(player))) > baseBuildRadius*baseBuildRadius*3)
				cancel.run();
			break;
		}
	}
}
