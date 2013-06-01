package org.zzl.choltfo.werewolfwars;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DCCommandEvent;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;
import pgDev.bukkit.DisguiseCraft.listeners.PlayerInvalidInteractEvent;


public class WerewolfWars extends JavaPlugin implements Listener {
	ArrayList<WWGame> games = new ArrayList<WWGame>();
	DisguiseCraftAPI dcAPI;
	public void setupDisguiseCraft() {
		dcAPI = DisguiseCraft.getAPI();
	}
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		setupDisguiseCraft();
		reloadConfig();
		getConfig().options().header("Werewolf Wars Config and blacklist.\r\n" +
				"\r\n" +
				"The syntax for banning a player is \r\n" +
				"[playername]: BANNED\r\n" +
				"");
	}
	
	public void onDisable() {
		System.out.println("[Werewolf Wars] Saving blacklist in config.");
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("ww")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED+"Insuffuiceunt arguents!");
				return false;
			}
			
			if (args[0].equalsIgnoreCase("infect")) {
				if (!sender.hasPermission("ww.infect")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission (ww.infect) to do that.");
					return true;
				}
				if (args.length != 3) {
					sender.sendMessage(ChatColor.RED + "Wrong number of arguments! Needs 3!");
					return true;
				}
				if (getGameByName(args[2]) == null) {
					sender.sendMessage(ChatColor.RED + "No game by that name!");
					return true;
				}
				if (getServer().getPlayer(args[3]) == null) {
					sender.sendMessage(ChatColor.RED + "No player by that name!");
					return true;
				}
				if (getGameByName(args[2]).getPlayer(getServer().getPlayer(args[2])) == null) {
					sender.sendMessage(ChatColor.RED + "No player by that name in that game!");
					return true;
				}
				getGameByName(args[2]).changePlayer(getServer().getPlayer(args[2]), WWPlayerState.infected);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("uninfect")) {
				if (!sender.hasPermission("ww.infect")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission (ww.infect) to do that.");
					return true;
				}
				if (args.length != 3) {
					sender.sendMessage(ChatColor.RED + "Wrong number of arguments! Needs 3!");
					return true;
				}
				if (getGameByName(args[2]) == null) {
					sender.sendMessage(ChatColor.RED + "No game by that name!");
					return true;
				}
				if (getServer().getPlayer(args[3]) == null) {
					sender.sendMessage(ChatColor.RED + "No player by that name!");
					return true;
				}
				if (getGameByName(args[2]).getPlayer(getServer().getPlayer(args[2])) == null) {
					sender.sendMessage(ChatColor.RED + "No player by that name in that game!");
					return true;
				}
				getGameByName(args[2]).changePlayer(getServer().getPlayer(args[2]), WWPlayerState.uninfected);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("setup") || args[0].equalsIgnoreCase("s")) {
				if (args.length == 1) {
					sender.sendMessage(giveSetupHelp());
					return true;
				}
				if (args[1] == "help") {
					sender.sendMessage(giveSetupHelp());
					return true;
				}
				if (args.length < 3){
					sender.sendMessage(giveSetupHelp());
					return true;
				}
				if (args[2].equalsIgnoreCase("spawn") || args[2].equalsIgnoreCase("s")) {
					if (getGameByName(args[1]) == null) {
						sender.sendMessage("WW game "+args[1]+" does not exist. Check your spelling.");
						return true;
					}
					getGameByName(args[1]).Spawn = ((Player)sender).getLocation();
					sender.sendMessage("Spawn point successfully set to " + ((Player)sender).getLocation().toString());
					return true;
				}
				if (args[2].equalsIgnoreCase("spawn2") || args[2].equalsIgnoreCase("s2")) {
					if (getGameByName(args[1]) == null) {
						sender.sendMessage("WW game "+args[1]+" does not exist. Check your spelling.");
						return true;
					}
					getGameByName(args[1]).wolfSpawn = ((Player)sender).getLocation();
					sender.sendMessage("Wolf spawn point successfully set to " + ((Player)sender).getLocation().toString());
					return true;
				}
				if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("d")) {
					if (getGameByName(args[1]) == null) {
						sender.sendMessage(ChatColor.RED + "That game does not exist!");
						return true;
					} else {
						sender.sendMessage(ChatColor.GREEN + "Deleted game "+args[1]+".");
						return true;
					}
					
				}
				if (args[2].equalsIgnoreCase("create") || args[2].equalsIgnoreCase("c")) {
					//Add support for multiple games, specified by args[1].
					if (getGameByName(args[1]) != null) {
						sender.sendMessage(ChatColor.RED + "A game already exists with that name!\n" +
								"Delete it with /ww s "+args[1]+" d.");
						return true;
					}
					String msg = giveSetupHelp();
					if (args.length == 3) {
						games.add(new WWGame(args[1], ((Player)sender).getWorld(), ((Player)sender).getLocation(), dcAPI,
								0, GenerationType.circle));
						msg = "Created new WW game, " + args[1] + ".\n" +
								"Spawn is at your location (" + ((Player)sender).getLocation().toString() + ").\n" +
								"Use '/ww "+args[1]+" s s2' to set wolf spawn to your location.\n" +
								"Walls were not generated. To generate them, run this same command, but " +
								"add an integer size at the end.";
					}
					if (args.length == 5) {
						games.add(new WWGame(args[1], ((Player)sender).getWorld(), ((Player)sender).getLocation(), dcAPI,
								Integer.parseInt(args[3]), GenerationType.valueOf(args[4])));
						games.get(games.size()-1).generateField();
						msg = "Created new WW game, " + args[1] + ".\n" +
								"Spawn is at your location (" + ((Player)sender).getLocation().toString() + ").\n" +
								"Use '/ww "+args[1]+" s s2' to set wolf spawn to your location.\n" +
								"Walls were generated as a " + args[3] + " with radius " + args[2].toString();
					}
					if (args.length == 4) {
						games.add(new WWGame(args[1], ((Player)sender).getWorld(), ((Player)sender).getLocation(), dcAPI,
								Integer.parseInt(args[2]), GenerationType.circle));
						games.get(games.size()-1).generateField();
						msg = "Created new WW game, " + args[1] + ".\n" +
								"Spawn is at your location (" + ((Player)sender).getLocation().toString() + ").\n" +
								"Use '/ww "+args[1]+" s s2' to set wolf spawn to your location.\n" +
								"Walls were generated as a circle with radius " + args[2].toString();
					}
					sender.sendMessage(msg);
					return true;
				}
				
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				String msg = "Joinable WW games:\n";
				msg += ChatColor.GREEN + "GREEN denotes game has not started.\n";
				msg += ChatColor.BLUE + "BLUE denotes game has started.\n";
				for (WWGame game : games) {
					msg += (game.started ? ChatColor.BLUE : ChatColor.GREEN) +  game.name + ChatColor.RESET +
							" - " + game.playersInGame.size() + " players in game.\n";
				}
				sender.sendMessage(msg);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (getConfig().getString(((Player)sender).getName()) != "BANNED") {
						if (getGameByName(args[1]).equals(null)) {
							sender.sendMessage("World "+args[1]+" does not exist. Check your spelling.");
							return true;
						}
						getGameByName(args[1]).addPlayer((Player)sender);
					} else {
						sender.sendMessage(ChatColor.RED + 
								"You have been banned from playing Werewolf wars on this server.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player only command.");
				}
				return true;
			}
			//This currently doen't do shit.
			if (args[0].equalsIgnoreCase("start")) {
				if (sender instanceof Player) {
					if (sender.hasPermission("ww.start")) {
						sender.sendMessage(ChatColor.GREEN + "Starting game.");
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permission (ww.start) to do that.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "That command is player only.");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("kick")) {
				if (args.length != 3) {
					sender.sendMessage(ChatColor.RED+"Insuffuiceunt arguents! Specify player name!");
					return true;
				}
				if (sender.hasPermission("ww.kick")) {
					if (getServer().getPlayer(args[2]) != null) {
						sender.sendMessage(ChatColor.GREEN + "Kicking "+args[2]+" from WereWolf Wars.");
						getGameByName(args[1]).kickPlayer(getServer().getPlayer(args[2]));
					} else {
						sender.sendMessage(ChatColor.RED + "Player " + args[2] + " does not exist. Check your spelling.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission (ww.kick) to do that.");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("ban")) {
				if (args.length != 2) {
					sender.sendMessage(ChatColor.RED+"Insuffuiceunt arguents! Specify player name!");
					return true;
				}
				if (sender.hasPermission("ww.ban")) {
					sender.sendMessage(ChatColor.GREEN + "Banning "+args[1]+" from WereWolf Wars.");
					if (getServer().getPlayer(args[1]) != null) {
						for (WWGame game : games) { 
							if (game.getPlayer(getServer().getPlayer(args[1])) != null) {
								game.kickPlayer(getServer().getPlayer(args[1])); 
							}
						}
					}
					getConfig().set(args[1], "BANNED");
					saveConfig();
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission (ww.ban) to do that.");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("pardon")) {
				if (args.length != 2) {
					sender.sendMessage(ChatColor.RED+"Insuffuiceunt arguents! Specify player name!");
					return true;
				}
				if (sender.hasPermission("ww.ban")) {
					sender.sendMessage(ChatColor.GREEN + "Pardoning "+args[1]+" from WereWolf Wars.");
					getConfig().set(args[1], null);
					saveConfig();
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission (ww.ban) to do that.");
				}
				return true;
			}
		}
		return false;
	}
	
	public String giveSetupHelp() {
		String msg = "";
			msg += (ChatColor.GOLD + "Werewolf Wars setup help\n");
			msg += (ChatColor.RESET +"  <argument> represents an argument.\n");
			msg += (ChatColor.RESET +"  {circle/square} represents a choice between two options.\n");
			msg += (ChatColor.RESET +"  [argument] represents an optional argument.\n");
			msg += (ChatColor.BLUE  +"Options:\n");
			msg += (ChatColor.RESET +"  create or c:\n");
			msg += (ChatColor.RESET +"    Creates a new Werewolf wars game.\n");
			msg += (ChatColor.ITALIC +"    /ww s [game name] c [radius] {circle/square}\n");
			msg += (ChatColor.RESET +"    would create a game called [gamename] with {circle/square} borders [radius] wide.\n");
			msg += (ChatColor.RESET +"    The last two arguments can be ommited, leaving either no walls created, or circular walls if only the shape is ommited.\n");
			msg += (ChatColor.RESET +"  spawn or s:\n");
			msg += (ChatColor.RESET +"    Sets the default spawn of the specified Werewolf wars game.\n");
			msg += (ChatColor.RESET +"    /ww s [game name] s\n");
			msg += (ChatColor.RESET +"  spawn2 or s2:\n");
			msg += (ChatColor.RESET +"    Sets the wolf spawn of the specified Werewolf wars game.\n");
			msg += (ChatColor.RESET +"    /ww s [game name] s\n");
		return msg;
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		for (WWGame game : games) {
			for (WWPlayer player : game.playersInGame) {
				if (player.player == p) {
					player.reConstitute(dcAPI);
					game.playersInGame.remove(player);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onUndisguise(DCCommandEvent event) {
		System.out.println("COMMANDEERED!");
		event.getPlayer().sendMessage("Sent DCCommand!");
		for (WWGame game : games) {
			if (event.getPlayer().getWorld().equals(game.world)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "You cannot do that here!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerInjury(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		for (WWGame game : games) {
			if (event.getEntity().getWorld() == game.world) {
				if (((Player)event.getEntity()).getHealth() - event.getDamage() < 1) {
					event.setCancelled(true);
					game.playerDied(game.getPlayer((Player)event.getEntity()));
					((Player)event.getEntity()).sendMessage("You done died.");
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
	}
	
	@EventHandler
	public void onDisguiseAttack(PlayerInvalidInteractEvent event) {
		for (WWGame game : games) {
			if (event.getPlayer().getWorld() == game.world) {
				if (game.getPlayer(event.getPlayer()) !=null) {
					game.getPlayer(event.getPlayer()).score += 50;
					return;
				}
			}
		}
	}
	
	public WWGame getGameByName(String name) {
		for (WWGame game : games) {
			if (game.name.equalsIgnoreCase(name)) return game;
		}
		return null;
	}
}

class WWGame {
	public String name;
	public ArrayList<WWPlayer> playersInGame = new ArrayList<WWPlayer>();
	public World world;
	public Location Spawn;
	public int ArenaSize;
	public DisguiseCraftAPI dcAPI;
	GenerationType generationType = GenerationType.circle;
	public Location wolfSpawn;
	public boolean started;
	
	WWGame (String lname, World lworld, Location lSpawn, DisguiseCraftAPI ldcAPI, int size, GenerationType gt) {
		name = lname;
		world = lworld;
		Spawn = lSpawn;
		dcAPI = ldcAPI;
		ArenaSize = size;
		generationType = gt;
	}
	
	public void generateField() {
		System.out.println("GENERATING LAG, r = " + ArenaSize);
		int radius = ArenaSize;
		int radiusSquared = radius * radius;
		int innerRadSquared = (radius -2) * (radius -2);
		int relX = Spawn.getBlockX();
		int relZ = Spawn.getBlockZ();
		
		if (generationType == GenerationType.circle) {
			for(int x = (-radius)+relX; x <= radius+relX; x++) {
				for(int z = (-radius)+relZ; z <= radius+relZ; z++) {
				System.out.println(">> " + x + ", "+ z + ", " + ((x-relX) * (x-relX) + (z-relZ) * (z-relZ)));
					if(((x-relX) * (x-relX) + (z-relZ) * (z-relZ)) >= innerRadSquared && ((x-relX) * (x-relX) + (z-relZ) * (z-relZ)) <= radiusSquared ) {
						for (int y = 0; y< Spawn.getBlockY()+25; y++) {
							world.getBlockAt(x, y, z).setType(Material.BEDROCK);
							System.out.println("Set " + x+ " "+ z+ " "+y + " ");
						}
					}
				}
			}
		}
		
		if (generationType == GenerationType.square) {
			for(int x = (-radius)+relX; x <= radius+relX; x++) {
				for (int y = 0; y< Spawn.getBlockY()+25; y++) {
					world.getBlockAt(x, y, relZ+radius).setType(Material.BEDROCK);
					System.out.println("Set " + x+ " "+ (relZ+radius)+ " "+y + " ");
				}
			}
			for(int x = (-radius)+relX; x <= radius+relX; x++) {
				for (int y = 0; y< Spawn.getBlockY()+25; y++) {
					world.getBlockAt(x, y, relZ-radius).setType(Material.BEDROCK);
					System.out.println("Set " + x+ " "+ (relZ-radius)+ " "+y + " ");
				}
			}
			for(int z = (-radius)+relZ; z <= radius+relZ; z++) {
				for (int y = 0; y< Spawn.getBlockY()+25; y++) {
					world.getBlockAt(relX+radius, y, z).setType(Material.BEDROCK);
					System.out.println("Set " + (relX+radius)+ " "+ z + " "+y + " ");
				}
			}
			for(int z = (-radius)+relZ; z <= radius+relZ; z++) {
				for (int y = 0; y< Spawn.getBlockY()+25; y++) {
					world.getBlockAt(relX-radius, y, z).setType(Material.BEDROCK);
					System.out.println("Set " + (relX-radius)+ " "+ z + " "+y + " ");
				}
			}
		}
	}
	
	public void reset() {
		for (WWPlayer player : playersInGame) {
			player.state = WWPlayerState.uninfected;
			player.player.teleport(Spawn);
			player.score = 0;
			player.player.sendMessage("Game reset.");
			player.player.setPlayerListName(player.player.getName() + "-" + ChatColor.BLUE + "WW");
			if (dcAPI.isDisguised(player.player)) dcAPI.undisguisePlayer(player.player);
		}
		System.out.println("[Werewolf Wars] Game reset.");
	}
	
	public WWPlayer getPlayer(Player player) {
		for (WWPlayer Gplayer : playersInGame) {
			if (player.equals(Gplayer.player)) return Gplayer;
		}
		return null;
	}
	
	public void addPlayer(Player player) {
		if (getPlayer(player) != null) {
			player.sendMessage("You are already in game! ");
			return;
		}
		playersInGame.add(new WWPlayer(player, dcAPI));
		player.teleport(Spawn);
		changePlayer(player, WWPlayerState.uninfected);
		player.sendMessage("Joining game.");
		player.sendMessage(listPlayers());
	}
	
	public String listPlayers() {
		String Return = "Players currently in this game:\n";
		for (WWPlayer player : playersInGame) {
			Return = Return + "-> " + player.player.getPlayerListName() + "\n" + ChatColor.RESET;
		}
		return Return;
	}

	public String getDetails() {
		return generationType.toString() + " shaped, " + ArenaSize + " radius, inside "
	+ world.getName() + "";
	}
	
	public void kickPlayer(Player player) {
		for (WWPlayer Gplayer : playersInGame) {
			if (Gplayer.player.equals(player)) {
				player.teleport(Gplayer.originalPosition);
				playersInGame.remove(Gplayer);
				player.setPlayerListName(null);
				
				player.sendMessage(ChatColor.BLUE + "You have left/been removed from the Werewolf Wars game.");
				return;
			}
		}
	}
	
	public void changePlayer (Player player, WWPlayerState state) {
		for (WWPlayer Gplayer : playersInGame) {
			if (Gplayer.player.equals(player)) {
				Gplayer.state = state;
				if (state == WWPlayerState.infected) {
					Gplayer.player.setPlayerListName(Gplayer.player.getName() + "-" + ChatColor.RED + "WW");
					dcAPI.disguisePlayer(player, new Disguise(dcAPI.newEntityID(), DisguiseType.Wolf));
					player.sendMessage("You are now" + ChatColor.ITALIC + ChatColor.RED + " infected");
				} else {
					Gplayer.player.setPlayerListName(Gplayer.player.getName() + "-" + ChatColor.BLUE + "WW");
					if (dcAPI.isDisguised(player)) dcAPI.undisguisePlayer(player);
					player.sendMessage("You are no longer infected");
				}
			}
		}
	}
	
	public void broadcast(String message) {
		for (WWPlayer player : playersInGame) {
			player.player.sendMessage(message);
		}
	}

	public void playerDied(WWPlayer player){
		if (dcAPI.isDisguised(player.player)) dcAPI.undisguisePlayer(player.player);
		changePlayer (player.player, WWPlayerState.infected);
		respawnPlayer(player);
	}
	
	public void respawnPlayer(WWPlayer player) {
		player.player.teleport((player.state == WWPlayerState.infected ? wolfSpawn : Spawn));
	}
	
	public void checkWinner() {
		boolean hasWon = true;
		for (WWPlayer player : playersInGame) {
			if (player.state == WWPlayerState.uninfected) {
				hasWon = false;
			}
		}
		if (!hasWon) return;
		int highestScore = 0;
		WWPlayer winner = null;
		for (WWPlayer player : playersInGame) {
			if (player.score >= highestScore) {
				highestScore = player.score;
				winner = player;
			}
		}
		broadcast("Game over!");
		broadcast("The werewolves won.");
		broadcast(winner.player.getDisplayName() + " had the highest score at " + winner.score);
		reset();
	}
}

class WWPlayer {
	WWPlayer(Player lplayer, DisguiseCraftAPI dcAPI) {
		player = lplayer;
		originalPosition = player.getLocation();
		score = 0;
		state = WWPlayerState.uninfected;
		originalGamemode = lplayer.getGameMode();
		originalInventory = player.getInventory().getContents();
		if (dcAPI.isDisguised(player)) dcAPI.undisguisePlayer(player);
	}
	public void reConstitute (DisguiseCraftAPI dcAPI) {
		player.getInventory().setContents(originalInventory);
		player.setGameMode(originalGamemode);
		player.teleport(originalPosition);
		if (dcAPI.isDisguised(player)) dcAPI.undisguisePlayer(player);
	}
	Player player;
	WWPlayerState state = WWPlayerState.uninfected;
	int score = 0;
	
	Location originalPosition;
	GameMode originalGamemode;
	ItemStack[] originalInventory;
}

enum WWPlayerState {
	infected,
	uninfected
}

enum GenerationType {
	square,
	circle
}
