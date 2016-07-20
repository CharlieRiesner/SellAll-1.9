package com.GlossyPanther.SellAll.Events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.GlossyPanther.SellAll.Main;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

import net.md_5.bungee.api.ChatColor;

public class WorldGuardListeners implements Listener {
	
	WorldGuardPlugin WG = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	Main plugin;
	
	public WorldGuardListeners (Main instance){
        plugin = instance;
    }
	
	@EventHandler
	public void blockBreakWG(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if (plugin.perms.playerHas(p, "sellall.autopickup") && plugin.autopickupPlayers.contains(p))
		{
			RegionContainer container = WG.getRegionContainer();
			RegionQuery query = container.createQuery();
			if (query.queryState(p.getLocation(), p, DefaultFlag.BLOCK_BREAK) == null || query.queryState(p.getLocation(), p, DefaultFlag.BLOCK_BREAK) == StateFlag.State.ALLOW)
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

}
