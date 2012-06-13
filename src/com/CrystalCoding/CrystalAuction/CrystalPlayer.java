package com.CrystalCoding.CrystalAuction;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.iCo6.system.Account;
import com.iCo6.system.Accounts;

public class CrystalPlayer implements Listener{

	public static int x1 = -1, y1 = -1, z1 = -1, x2 = -1, y2 = -1, z2 = -1;
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		
		if (p.getItemInHand().getTypeId() == CrystalAuction.selectionTool && p.hasPermission("ca.setbounds")) {
			Block b = event.getClickedBlock();
			int id = b.getTypeId();
			
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				x1 = b.getX();
				y1 = b.getY();
				z1 = b.getZ();
				b.setTypeId(id);
				event.setCancelled(true);
				p.sendMessage(ChatColor.BLUE + "First point set.");
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				x2 = b.getX();
				y2 = b.getY();
				z2 = b.getZ();
				p.sendMessage(ChatColor.BLUE + "Second point set.");
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			for (AuctionSign s : CrystalAuction.signs) {
				Location sLoc = s.s.getLocation();
				Location bLoc = event.getClickedBlock().getLocation();
				
				if (sLoc.getBlockX() == bLoc.getBlockX() && sLoc.getBlockY() == bLoc.getBlockY() && sLoc.getBlockZ() == bLoc.getBlockZ()) {
					Account account = new Accounts().get(p.getName());
					double money = account.getHoldings().getBalance();
					Auction a = AuctionHandler.getAuctionById(s.id);
					if (!a.getPlayer().getName().equalsIgnoreCase(p.getName())) {
						if (money >= a.getPrice()) {
							p.getInventory().addItem(a.getItemStack());
							account.getHoldings().subtract(a.getPrice());
							Player seller = a.getPlayer();
							new Accounts().get(seller.getName()).getHoldings().add(a.getPrice());
							p.sendMessage(ChatColor.GREEN + "You have successfully bought " + a.getAmount() + " " + CrystalAuction.getItemName(a.getItemStack().getTypeId()) + " for $" + a.getPrice() + "!");
							seller.sendMessage(ChatColor.GOLD + p.getName() + " has bought your " + CrystalAuction.getItemName(a.getItemStack().getTypeId()) + " for $" + a.getPrice() + "!");
							a.remove();
						} else {
							p.sendMessage(ChatColor.RED + "You don't have enough money to afford that!");
						}
					} else {
						p.sendMessage(ChatColor.RED + "You can't buy your own item!");
					}
				}
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
