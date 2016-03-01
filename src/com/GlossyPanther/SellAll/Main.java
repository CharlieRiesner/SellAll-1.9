package com.GlossyPanther.SellAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;
    public static Permission perms = null;
    public String Gmult = null;
    public String Pmult = null;
    public static List<Player> autopickupPlayers = new ArrayList<Player>();
    public boolean updateAvailable = false;
    DecimalFormat moneyFormat = new DecimalFormat("#.##");
    DecimalFormat multiplierFormat = new DecimalFormat("#.#");

	@Override
	public void onDisable() {
		logger.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		if (getServer().getPluginManager().getPlugin("WorldGuard") != null)
		{
			getServer().getPluginManager().registerEvents(new WorldGuardListeners(), this);
			logger.info("Registered WorldGuardListeners");
		}
		else
		{
			getServer().getPluginManager().registerEvents(new NoWorldGuardListeners(), this);
			logger.info("Registered NoWorldGuardListeners");
		}
	    try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        logger.warning("Failed to start metrics!");
	    }
	    if (!getConfig().isSet("CheckForUpdates"))
	    {
	    	getConfig().set("CheckForUpdates", true);
	    	saveConfig();
	    }
	    if (getConfig().getBoolean("CheckForUpdates"))
	    	updateAvailable = checkForUpdate();
		logger.info(getDescription().getName() + " verson " + getDescription().getVersion() + " has been enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.DARK_RED + "Only players can execute this command!");
			return true;
		}
		Player player = (Player)sender;
		if (cmd.getName().equalsIgnoreCase("sellall"))
		{
			if (args.length == 0 || args.length > 3)
			{
				player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.GREEN + " Created by GlossyPanther!");
				player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " View global multiplier" + ChatColor.RED + " /sellall global");
				player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " View your multiplier" + ChatColor.RED + " /sellall player <player>");
				if (perms.playerHas(player, "sellall.set"))
				{
					player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set global multiplier" + ChatColor.RED + " /sellall global <amount>");
					player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set player multiplier" + ChatColor.RED + " /sellall player <player> <amount>");
					player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Reload config file" + ChatColor.RED + " /sellall reload");
					return true;
				}
			}
			else if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("global"))
				{
					Double globalMult = getConfig().getDouble("GlobalMultiplier");
					player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Global multiplier = " + globalMult);
					return true;
				}
				if (perms.playerHas(player, "sellall.set"))
				{
					if (args[0].equalsIgnoreCase("reload"))
					{
						try
						{
							reloadConfig();
						}
						catch (Exception e)
						{
							player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.DARK_RED + " Failed to reload config!");
							return true;
						}
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Config reloaded!");
						return true;
					}
					else
					{
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set global multiplier" + ChatColor.RED + " /sellall global <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set player multiplier" + ChatColor.RED + " /sellall player <player> <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Reload config file" + ChatColor.RED + " /sellall reload");
						return true;
					}
				}
				else
				{
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that!");
					return true;
				}
			}
			else if (args.length == 2)
			{
				if (args[0].equalsIgnoreCase("player"))
				{
					if (Bukkit.getPlayerExact(args[1]) != null)
					{
						Player playerToFind = Bukkit.getPlayerExact(args[1]);
						Double playerMult = getConfig().getDouble("PlayerMultipliers." + playerToFind.getUniqueId());
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " " + playerToFind.getDisplayName()+ ChatColor.BLUE + "'s multiplier = " + playerMult);
						return true;
					}
					else
					{
						player.sendMessage("fail");
						return true;
					}
				}
				if (perms.playerHas(player, "sellall.set"))
				{
					if (args[0].equalsIgnoreCase("global"))
					{
						Double Gtest = null;
						try
						{
							 Gtest = Double.parseDouble(args[1]);
						}
						catch (Exception e)
						{
							player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " The multiplier you entered is not valid!");
							return true;
						}
						multiplierFormat.setMaximumFractionDigits(1);
						Gmult = multiplierFormat.format(Gtest);
						getConfig().set("GlobalMultiplier", Double.parseDouble(Gmult));
						saveConfig();
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Global multiplier has been set to " + Gmult + "!");
						return true;
					}
					else
					{
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set global multiplier" + ChatColor.RED + " /sellall global <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set player multiplier" + ChatColor.RED + " /sellall player <player> <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Reload config file" + ChatColor.RED + " /sellall reload");
						return true;
					}
				}
				else
				{
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that!");
					return true;
				}
			}
			else if (args.length == 3)
			{
				if (perms.playerHas(player, "sellall.set"))
				{
					if (args[0].equalsIgnoreCase("player"))
					{
						if (Bukkit.getPlayerExact(args[1]) != null)
						{
							Player playerForMult = Bukkit.getPlayerExact(args[1]);
							Double Ptest = null;
							try
							{
								 Ptest = Double.parseDouble(args[2]);
							}
							catch (Exception e)
							{
								player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " The multiplier you entered is not valid!");
								return true;
							}
							multiplierFormat.setMaximumFractionDigits(1);
							Pmult = multiplierFormat.format(Ptest);
							getConfig().set("PlayerMultipliers." + playerForMult.getUniqueId(), Double.parseDouble(Pmult));
							saveConfig();
							player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " " + playerForMult.getDisplayName() + ChatColor.BLUE + "'s multiplier has been set to " + Pmult + "!");
							return true;
						}
						else
						{
							player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " That player is not online!");
							return true;
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set global multiplier" + ChatColor.RED + " /sellall global <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Set player multiplier" + ChatColor.RED + " /sellall player <player> <amount>");
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Reload config file" + ChatColor.RED + " /sellall reload");
						return true;
					}
				}
				else
				{
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that!");
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("autopickup"))
		{
			if (perms.playerHas(player, "sellall.autopickup"))
			{
				if (autopickupPlayers.contains(player))
				{
					autopickupPlayers.remove(player);
					player.sendMessage(ChatColor.AQUA + "AutoPickup " + ChatColor.RED + "Disabled!");
				}
				else
				{
					autopickupPlayers.add(player);
					player.sendMessage(ChatColor.AQUA + "AutoPickup " + ChatColor.GREEN + "Enabled!");
				}
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean setupEconomy()
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
		{
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		if (e.getClickedBlock() == null)
			return;
		if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN)
		{
			Player player = e.getPlayer();
			Sign sign = (Sign)e.getClickedBlock().getState();
			if ((perms.playerHas(player, "sellall.break") && player.getItemInHand().getType() == Material.TNT) && (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[sell all]") || sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "[sell all]")))
			{
				e.setCancelled(true);
				e.getClickedBlock().setType(Material.AIR);
				player.sendMessage(ChatColor.GREEN + "Sign successfully broken!");
				return;
			}
			if (perms.playerHas(player, "sellall.use") && sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[sell all]"))
			{
				e.setCancelled(true);
				int quantityFound = 0;
				Material sellingMaterial = Material.getMaterial(sign.getLine(2));
				for (ItemStack is : player.getInventory())
				{
					if (is != null)
					{
						if (is.getType() == sellingMaterial)
							quantityFound += is.getAmount();
					}
				}
				if (quantityFound < 1)
				{
					player.sendMessage(ChatColor.RED + "You do not have any " + sellingMaterial.name() + " to sell!");
					return;
				}
				else
				{
					double pricePer = Double.parseDouble(sign.getLine(3).replace("$","").replace(" /ea", ""));
					player.getInventory().remove(sellingMaterial);
					double amountToGive = quantityFound * pricePer;
					Double globalMult = getConfig().getDouble("GlobalMultiplier");
					Double playerMult = getConfig().getDouble("PlayerMultipliers." + player.getUniqueId());
					double newAmountToGive = amountToGive;
					if (globalMult != 1 && globalMult != 0)
						newAmountToGive = newAmountToGive * globalMult;
					if (playerMult != 1 && playerMult != 0)
						newAmountToGive = newAmountToGive * playerMult;
					econ.depositPlayer(player, newAmountToGive);
					player.updateInventory();
					Sound sound = null;
					for (Sound s : Sound.values())
					{
						if (s.toString().equalsIgnoreCase("orb_pickup"))
						{
							sound = s;
							break;
						}
					}
					if (sound == null)
						sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
					player.playSound(player.getLocation(), sound, 1.0F, 1.5F);
					player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "SOLD: " + ChatColor.DARK_GREEN.toString() + quantityFound + "x " + sellingMaterial.name() + ChatColor.AQUA.toString() + " for $" + moneyFormat.format(newAmountToGive));
					return;
				}
			}
			if (sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "[Sell All]"))
			{
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onSignCreate(SignChangeEvent e)
	{
		Player player = e.getPlayer();
		
		if (e.getLine(0).equalsIgnoreCase("[sell all]"))
		{
			if (perms.playerHas(player, "sellall.create"))
			{
				Material sellingMaterial = Material.matchMaterial(e.getLine(2));
				if (sellingMaterial == null)
				{
					e.setLine(0, ChatColor.RED + "[Sell All]");
					player.sendMessage(ChatColor.RED + "Unknown material on line 3!");
					return;
				}
				int sellingAmount = 0;
				try
				{
					sellingAmount = Integer.parseInt(e.getLine(3));
				}
				catch (Exception except)
				{
					e.setLine(0, ChatColor.RED + "[Sell All]");
					player.sendMessage(ChatColor.RED + "Unknown price on line 4!");
					return;
				}
				String sellingMaterialLine = sellingMaterial.name();
				String sellingPriceLine = "$" + sellingAmount + " /ea";
				e.setLine(0, ChatColor.BLUE + "[Sell All]");
				e.setLine(1, "ALL");
				e.setLine(2, sellingMaterialLine);
				e.setLine(3, sellingPriceLine);
				player.sendMessage(ChatColor.GREEN + "SellAll sign successfully created!");
				return;
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
				e.setCancelled(true);
				return;
			}
		}
		if (e.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[sell all]"))
		{
			player.sendMessage(ChatColor.RED + "This is not the correct way to make a SellAll sign!");
			e.setCancelled(true);
			return;
		}
	}
	
	public boolean checkForUpdate()
	{
		try 
		{
			logger.info("[SellAll] Checking for a new version...");
			URL url = new URL("https://raw.githubusercontent.com/GlossyPanther/SellAll/master/version.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = br.readLine()) != null)
			{
				if (!getDescription().getVersion().equalsIgnoreCase(str))
				{
					logger.info("[SellAll] New update available!");
					logger.info("Your version is " + getDescription().getVersion());
					logger.info("The latest version is " + str);
					return true;
				}
			}
		}
		catch (IOException e)
		{
			logger.warning("Failed to check for new version!");
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if (perms.playerHas(e.getPlayer(), "sellall.set"))
		{
			if (checkForUpdate())
				e.getPlayer().sendMessage(ChatColor.BLUE + "[Sell All] " + ChatColor.RED + "Update available!");
		}
		if (!getConfig().isSet("PlayerMultipliers." + e.getPlayer().getUniqueId()))
		{
			getConfig().set("PlayerMultipliers." + e.getPlayer().getUniqueId(), 1.0);
			saveConfig();
		}
	}
}
