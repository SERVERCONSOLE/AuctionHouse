package com.CrystalCoding.CrystalAuction;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class AuctionHandler {

	public static ArrayList<Auction> auctions = new ArrayList<Auction>();
	public static ArrayList<Auction> toRemove = new ArrayList<Auction>();
	
	public AuctionHandler() {
		File f = new File("plugins/CrystalAuction/");
		if (!f.exists())
			f.mkdir();
	}
	
	public void updateAuctions() {
		if (auctions.size() == 0)
			CrystalAuction.totalAuctionsSet = 0;
		
		for (Auction a : auctions)
			a.addTick();
		
		for (Auction a : toRemove)
			auctions.remove(a);
	}
	
	public static Auction getAuctionById(int id) {
		for (Auction a : auctions)
			if (a.getId() == id)
				return a;
		return null;
	}
	
	public static Auction getAuctionByOwner(String name) {
		for (Auction a : auctions)
			if (a.getPlayer().getName().equalsIgnoreCase(name))
				return a;
		return null;
	}
	
	public static void setLocation(int x, int y, int z, int x1, int y1, int z1) {
		writeFile(x + " " + y + " " + z + " " + x1 + " " + y1 + " " + z1);
	}
	
	public static String loadBoundsFile() {
		try {
			File f = new File("plugins/CrystalAuction/bounds.txt");
			if (f.exists()) {
				Scanner scan = new Scanner(f);
				String line = scan.nextLine();
				scan.close();
				return line;
			} else {
				f.createNewFile();
				writeFile("1 1 1 1 1 1");
			}
		} catch (Exception e) {
			CrystalAuction.log.warning("[CrystalSkills] An error has occured while reading the Bounds file!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeFile(String bounds) {
		try {
			File f = new File("plugins/CrystalAuction/bounds.txt");
			PrintWriter pw = new PrintWriter(f);
			pw.write(bounds);
			pw.close();
		} catch (Exception e) {
			CrystalAuction.log.warning("[CrystalSkills] An error has occured while creating the Bounds file!");
			e.printStackTrace();
		}
	}
	
	public static Location[] getBoundLocations(Player p) {
		try {
			String[] coords = loadBoundsFile().split(" ");
			Location[] locs = { new Location(p.getWorld(), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])),  new Location(p.getWorld(), Integer.parseInt(coords[3]), Integer.parseInt(coords[4]), Integer.parseInt(coords[5])) };
			return locs;
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Error loading bounds. Please contact a server admin immediately.");
			e.printStackTrace();
		}
		return null;
	}
}
