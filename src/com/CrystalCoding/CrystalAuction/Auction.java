package com.CrystalCoding.CrystalAuction;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Auction {

	public static final int TICK_COUNT = 120;
	private int amount, price, ticks, id;
	private ItemStack itemStack;
	private Player p;
	
	public Auction(int amount, int price, ItemStack itemStack, Player p, int id) {
		this.amount = amount;
		this.price = price;
		this.itemStack = itemStack;
		ticks = TICK_COUNT;
		this.p = p;
		this.id = id;
		AuctionHandler.auctions.add(this);
	}
	
	public int getAmount() {
		return amount;
	}
	
	public int getPrice() {
		return price;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public void addTick() {
		ticks--;
		checkEnd();
	}
	
	public void checkEnd() {
		if (ticks == 0) {
			cancel();
			p.sendMessage(ChatColor.RED + "No one bought your item!");
			remove();
		}
	}
	
	public void remove() {
		AuctionHandler.toRemove.add(this);
		if (AuctionHandler.useSigns())
			CrystalAuction.removeSignById(id);
	}
	
	public int getTicks() {
		return ticks;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public int getId() {
		return id;
	}
	
	public void cancel() {
		p.getInventory().addItem(itemStack);
		remove();
	}
}
