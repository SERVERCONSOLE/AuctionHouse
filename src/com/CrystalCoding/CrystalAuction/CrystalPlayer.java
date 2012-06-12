package com.CrystalCoding.CrystalAuction;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CrystalPlayer implements Listener{

	public static int x1 = -1, y1 = -1, z1 = -1, x2 = -1, y2 = -1, z2 = -1;
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		if (player.getItemInHand().getTypeId() == CrystalAuction.selectionTool && player.hasPermission("ca.setbounds")) {
			Block b = event.getClickedBlock();
			int id = b.getTypeId();
			
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				x1 = b.getX();
				y1 = b.getY();
				z1 = b.getZ();
				b.setTypeId(id);
				event.setCancelled(true);
				player.sendMessage(ChatColor.BLUE + "First point set.");
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				x2 = b.getX();
				y2 = b.getY();
				z2 = b.getZ();
				player.sendMessage(ChatColor.BLUE + "Second point set.");
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
	    Player player = event.getPlayer();
	    
	    for (Auction a : AuctionHandler.auctions) {
	    	if (a.getPlayer().getName().equalsIgnoreCase(player.getName()))
	    		a.cancel();
	    }
	}
}
