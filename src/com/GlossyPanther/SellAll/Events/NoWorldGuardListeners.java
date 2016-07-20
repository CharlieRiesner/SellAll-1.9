package com.GlossyPanther.SellAll.Events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.GlossyPanther.SellAll.Main;

import net.md_5.bungee.api.ChatColor;

public class NoWorldGuardListeners implements Listener {
	
	Main plugin;
	
	public NoWorldGuardListeners (Main instance){
        plugin = instance;
    }

	@EventHandler
	public void blockBreakNoWG(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if (plugin.perms.playerHas(p, "sellall.autopickup") && plugin.autopickupPlayers.contains(p))
		{
			for(ItemStack item : e.getBlock().getDrops())
			{
	           p.getInventory().addItem(item);
	        }
			p.giveExp(e.getExpToDrop());
	        e.setCancelled(true);
	        e.getBlock().setType(Material.AIR);
	        
	        if (p.getInventory().firstEmpty() == -1)
	        	p.sendMessage(ChatColor.RED + "Inventory Full!");
		}
	}
	
}
