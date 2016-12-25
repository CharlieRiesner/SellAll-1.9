package com.GlossyPanther.SellAll.Events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.GlossyPanther.SellAll.Main;

public class SignCreate implements Listener {
	
	Main plugin;
	
	public SignCreate(Main instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void onSignCreate(SignChangeEvent e)
	{
		Player player = e.getPlayer();
		
		if (e.getLine(0).equalsIgnoreCase("[sell all]"))
		{
			if (plugin.perms.playerHas(player, "sellall.create"))
			{
				Material sellingMaterial = Material.matchMaterial(e.getLine(2).split(":")[0]);
				if (sellingMaterial == null)
				{
					e.setLine(0, ChatColor.RED + "[Sell All]");
					player.sendMessage(ChatColor.RED + "Unknown material on line 3!");
					return;
				}
				double sellingAmount = 0;
				try
				{
					sellingAmount = Double.parseDouble(e.getLine(3));
				}
				catch (Exception except)
				{
					e.setLine(0, ChatColor.RED + "[Sell All]");
					player.sendMessage(ChatColor.RED + "Unknown price on line 4!");
					return;
				}
				String sellingMaterialDataValue = "";
				if (e.getLine(2).split(":").length > 1)
					sellingMaterialDataValue = ":" + e.getLine(2).split(":")[1];
				String sellingMaterialLine = sellingMaterial.name() + sellingMaterialDataValue;
				String sellingPriceLine = "$" + plugin.moneyFormat.format(sellingAmount) + " /ea";
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
}
