package com.GlossyPanther.SellAll.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.GlossyPanther.SellAll.Main;

public class CommandUtility implements CommandExecutor {
	
	Main plugin;
	
	public CommandUtility (Main instance){
        plugin = instance;
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
				if (plugin.perms.playerHas(player, "sellall.set"))
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
					Double globalMult = plugin.getConfig().getDouble("GlobalMultiplier");
					player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Global multiplier = " + globalMult);
					return true;
				}
				if (plugin.perms.playerHas(player, "sellall.set"))
				{
					if (args[0].equalsIgnoreCase("reload"))
					{
						try
						{
							plugin.reloadConfig();
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
						Double playerMult = plugin.getConfig().getDouble("PlayerMultipliers." + playerToFind.getUniqueId());
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " " + playerToFind.getDisplayName()+ ChatColor.BLUE + "'s multiplier = " + playerMult);
						return true;
					}
					else
					{
						player.sendMessage("fail");
						return true;
					}
				}
				if (plugin.perms.playerHas(player, "sellall.set"))
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
						plugin.multiplierFormat.setMaximumFractionDigits(1);
						plugin.Gmult = plugin.multiplierFormat.format(Gtest);
						plugin.getConfig().set("GlobalMultiplier", Double.parseDouble(plugin.Gmult));
						plugin.saveConfig();
						player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " Global multiplier has been set to " + plugin.Gmult + "!");
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
				if (plugin.perms.playerHas(player, "sellall.set"))
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
							plugin.multiplierFormat.setMaximumFractionDigits(1);
							plugin.Pmult = plugin.multiplierFormat.format(Ptest);
							plugin.getConfig().set("PlayerMultipliers." + playerForMult.getUniqueId(), Double.parseDouble(plugin.Pmult));
							plugin.saveConfig();
							player.sendMessage(ChatColor.RED + "[" + ChatColor.BLUE + "SellAll" + ChatColor.RED + "]" + ChatColor.BLUE + " " + playerForMult.getDisplayName() + ChatColor.BLUE + "'s multiplier has been set to " + plugin.Pmult + "!");
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
			if (plugin.perms.playerHas(player, "sellall.autopickup"))
			{
				if (plugin.autopickupPlayers.contains(player))
				{
					plugin.autopickupPlayers.remove(player);
					player.sendMessage(ChatColor.AQUA + "AutoPickup " + ChatColor.RED + "Disabled!");
				}
				else
				{
					plugin.autopickupPlayers.add(player);
					player.sendMessage(ChatColor.AQUA + "AutoPickup " + ChatColor.GREEN + "Enabled!");
				}
				return true;
			}
			return false;
		}
		return false;
	}
}
