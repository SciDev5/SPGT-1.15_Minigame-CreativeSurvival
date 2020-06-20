package me.uut118.creativesurvival;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import me.uut118.creativesurvival.commands.EndGameCommand;
import me.uut118.creativesurvival.commands.JoinGameCommand;
import me.uut118.creativesurvival.commands.StartGameCommand;

public class PluginMain extends JavaPlugin {
	public ArrayList<World> minigameWorlds;
	public ArrayList<String> minigameWorldIds;
	public ArrayList<GameManager> gameManagers;
	@Override
	public void onEnable() {
		saveDefaultConfig();
		minigameWorldIds = (ArrayList<String>) getConfig().getStringList("gameworlds");
		minigameWorlds = new ArrayList<World>();
		gameManagers = new ArrayList<GameManager>();
		for (String worldName : minigameWorldIds) {
			World world = loadWorld(worldName);
			minigameWorlds.add(world);
			gameManagers.add(new GameManager(this, world, worldName));
		}
		PluginMain self = this;
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				self.doTickTimers();
			}
		}, 0L, 20L);
		getServer().getPluginManager().registerEvents(new DeathListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockModificationListener(this), this);
		getServer().getPluginManager().registerEvents(new SwitchWorldListener(this), this);
		getServer().getPluginManager().registerEvents(new MovementListener(this), this);
		JoinGameCommand jgc = new JoinGameCommand(this);
		getServer().getPluginCommand("joingame_mcs").setExecutor(jgc);
		getServer().getPluginCommand("joingame_mcs").setTabCompleter(jgc);
		StartGameCommand bgc = new StartGameCommand(this);
		getServer().getPluginCommand("begingame_mcs").setExecutor(bgc);
		getServer().getPluginCommand("begingame_mcs").setTabCompleter(bgc);
		getServer().getPluginCommand("begingame_mcs").setPermission("creativeSurvivalMinigame.commands.beginGame.use");
		EndGameCommand egc = new EndGameCommand(this);
		getServer().getPluginCommand("endgame_mcs").setExecutor(egc);
		getServer().getPluginCommand("endgame_mcs").setTabCompleter(egc);
		getServer().getPluginCommand("endgame_mcs").setPermission("creativeSurvivalMinigame.commands.endGame.use");
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}
	
	protected void doTickTimers() {
		for (GameManager gameManager : gameManagers) {
			gameManager.tickTime();
		}
	}
	
	public void onPlayerDeath(Player player, String deathMessage, Runnable noDefaultDeathMsg) {
		if (minigameWorlds.contains(player.getWorld())) {
			noDefaultDeathMsg.run();
			gameManagers.get(minigameWorlds.indexOf(player.getWorld())).killPlayer(player, deathMessage);
		}
	}
	public void onBlockModify(Player player, Location blockPos, Runnable cancelCallback) {
		if (minigameWorlds.contains(blockPos.getWorld()))
			gameManagers.get(minigameWorlds.indexOf(blockPos.getWorld())).blockChange(player, blockPos, cancelCallback);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		p.teleport(new Location(getServer().getWorld(args[0]),0,0,0));
		return true;
	}

	public World loadWorld(String worldId) {
		WorldCreator worldCreator = WorldCreator.name("world_minigame_"+worldId);
		worldCreator.generator(new ChunkGenerator() {
			@Override
			public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
				return createChunkData(world);
			}
		});
		World world = worldCreator.createWorld();
		world.setPVP(false);
		return world;
	}

	public void playerChangeWorld(World from, World to, Player player) {
		if (minigameWorlds.contains(from))
			gameManagers.get(minigameWorlds.indexOf(from)).removePlayer(player);
		if (minigameWorlds.contains(to))
			gameManagers.get(minigameWorlds.indexOf(to)).tpPlayer(player);
	}

	public void onPlayerTeleport(Player player, Runnable cancel) {
		if (minigameWorlds.contains(player.getWorld()))
			if (gameManagers.get(minigameWorlds.indexOf(player.getWorld())).getGameRunning())
				cancel.run();
	}

	public void onPlayerMovement(Player player, Location location, Runnable cancel) {
		if (minigameWorlds.contains(player.getWorld()))
			gameManagers.get(minigameWorlds.indexOf(player.getWorld())).playerMovement(player,location,cancel);
	}
	
	/* GAME OUTLINE: 
	 *  - 5 minutes to build home base in creative.
	 *  - Each player has a block to protect (ex. player head)
	 *  - After 5 min, switch to survival, try to destroy other team's block.
	 *  - Indestructible blocks, overenchanted / indestructible items disallowed.
	 * 
	 */
}
