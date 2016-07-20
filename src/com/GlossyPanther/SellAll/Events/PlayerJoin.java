package com.GlossyPanther.SellAll.Events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.GlossyPanther.SellAll.Main;

public class PlayerJoin implements Listener {
	
	Main plugin;
	
	public PlayerJoin (Main instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if (plugin.perms.playerHas(e.getPlayer(), "sellall.set"))
		{
			if (plugin.checkForUpdate())
				e.getPlayer().sendMessage(ChatColor.BLUE + "[Sell All] " + ChatColor.RED + "Update available!");
		}
		if (!plugin.getConfig().isSet("PlayerMultipliers." + e.getPlayer().getUniqueId()))
		{
			plugin.getConfig().set("PlayerMultipliers." + e.getPlayer().getUniqueId(), 1.0);
			plugin.saveConfig();
		}
	}
}
