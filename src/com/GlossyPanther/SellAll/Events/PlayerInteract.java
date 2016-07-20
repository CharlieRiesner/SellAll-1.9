package com.GlossyPanther.SellAll.Events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.GlossyPanther.SellAll.Main;

public class PlayerInteract implements Listener {
	
	Main plugin;
	
	public PlayerInteract (Main instance){
        plugin = instance;
    }
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		if (e.getClickedBlock() == null)
			return;
		if (e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN)
		{
			Player player = e.getPlayer();
			Sign sign = (Sign)e.getClickedBlock().getState();
			if ((plugin.perms.playerHas(player, "sellall.break") && player.getInventory().getItemInMainHand().getType() == Material.TNT) && (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[sell all]") || sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "[sell all]")))
			{
				e.setCancelled(true);
				e.getClickedBlock().setType(Material.AIR);
				player.sendMessage(ChatColor.GREEN + "Sign successfully broken!");
				return;
			}
			if (plugin.perms.playerHas(player, "sellall.use") && sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "[sell all]"))
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
					Double globalMult = plugin.getConfig().getDouble("GlobalMultiplier");
					Double playerMult = plugin.getConfig().getDouble("PlayerMultipliers." + player.getUniqueId());
					double newAmountToGive = amountToGive;
					if (globalMult != 1 && globalMult != 0)
						newAmountToGive = newAmountToGive * globalMult;
					if (playerMult != 1 && playerMult != 0)
						newAmountToGive = newAmountToGive * playerMult;
					plugin.econ.depositPlayer(player, newAmountToGive);
					player.updateInventory();
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
					player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "SOLD: " + ChatColor.DARK_GREEN.toString() + quantityFound + "x " + sellingMaterial.name() + ChatColor.AQUA.toString() + " for $" + plugin.moneyFormat.format(newAmountToGive));
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

}
