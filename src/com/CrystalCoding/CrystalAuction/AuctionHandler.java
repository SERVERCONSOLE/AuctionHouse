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
		loadConfigFile();
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
		writeFile(new String[]{ x + " " + y + " " + z + " " + x1 + " " + y1 + " " + z1, loadConfigFile()[1], loadConfigFile()[2]});
	}
	
	public static void setSignLocation(int x, int y, int z, int x1, int y1, int z1) {
		if (z == z1) {
			writeFile(new String[]{ loadConfigFile()[0], x + " " + y + " " + z + " " + "z", loadConfigFile()[2] });
		} else if (x == x1) {
			writeFile(new String[]{ loadConfigFile()[0], x + " " + y + " " + z + " " + "x", loadConfigFile()[2]});
		}
	}
	
	public static String[] loadConfigFile() {
		try {
			File f = new File("plugins/CrystalAuction/config.txt");
			if (f.exists()) {
				String[] info = new String[3];
				Scanner scan = new Scanner(f);
				info[0] = scan.nextLine();
				info[1] = scan.nextLine();
				info[2] = scan.nextLine();
				scan.close();
				return info;
			} else {
				f.createNewFile();
				writeFile(new String[]{ "1 1 1 1 1 1", "1 1 1 z", "no" });
			}
		} catch (Exception e) {
			CrystalAuction.log.warning("[CrystalSkills] An error has occured while reading the Config file!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeFile(String[] info) {
		try {
			File f = new File("plugins/CrystalAuction/config.txt");
			PrintWriter pw = new PrintWriter(f);
			pw.write(info[0] + "\r\n");
			pw.write(info[1] + "\r\n");
			pw.write(info[2]);
			pw.close();
		} catch (Exception e) {
			CrystalAuction.log.warning("[CrystalSkills] An error has occured while creating the Config file!");
			e.printStackTrace();
		}
	}
	
	public static Location[] getBoundLocations(Player p) {
		try {
			String[] coords = loadConfigFile()[0].split(" ");
			Location[] locs = { new Location(p.getWorld(), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])),  new Location(p.getWorld(), Integer.parseInt(coords[3]), Integer.parseInt(coords[4]), Integer.parseInt(coords[5])) };
			return locs;
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Error loading Bounds. Please contact a server admin immediately.");
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean useSigns() {
		String str = loadConfigFile()[2];
		return str.equalsIgnoreCase("yes");
	}
}
