package com.CrystalCoding.CrystalAuction;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;

public class CrystalAuction extends JavaPlugin{

	public static Server s;
	public static final Logger log = Logger.getLogger("Minecraft");
	public static AuctionHandler auctionHandler;
	public static int totalAuctionsSet = 0; //ids
	public static int selectionTool = 280; //stick
	public static ArrayList<AuctionSign> signs = new ArrayList<AuctionSign>(); //not working with current bukkit
	
	public void onDisable() {
	}

	public void onEnable() {
		s = getServer();
		s.getPluginManager().registerEvents(new CrystalPlayer(), this);
		PluginDescriptionFile pdfFile = getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		auctionHandler = new AuctionHandler();
		
		s.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() {
				auctionHandler.updateAuctions();
			}
		}, 20L, 20L);


	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String comm = command.getName();
		String[] myArgs = args;
		String allArgs = "";
		
		if (myArgs == null)
			myArgs = new String[0];
		
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			if (comm.equalsIgnoreCase("auction")) {
				if (canAuction(p.getName())) {
					if (myArgs.length >= 1) {
						if (myArgs[0].equalsIgnoreCase("setbounds")) {
							try {
								if (p.hasPermission("ca.setbounds")) {
									if (CrystalPlayer.x1 != -1 && CrystalPlayer.y1 != -1 && CrystalPlayer.z1 != -1 && CrystalPlayer.x2 != -1 && CrystalPlayer.y2 != -1 && CrystalPlayer.z2 != -1) {
										AuctionHandler.setLocation(CrystalPlayer.x1, CrystalPlayer.y1, CrystalPlayer.z1, CrystalPlayer.x2, CrystalPlayer.y2, CrystalPlayer.z2);
										p.sendMessage(ChatColor.GREEN + "The new bounds where players can use Crystal Auction is now set.");
									} else {
										p.sendMessage(ChatColor.GREEN + "Please select the bounds by left and right clicking with the stick.");
									}
								} else {
									p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
								}
								return true;
							} catch (Exception e) {
								e.printStackTrace();
							}
							return true;
						} else if (myArgs[0].equalsIgnoreCase("setsigns")) {
							try {
								if (p.hasPermission("ca.setbounds")) {
									if (CrystalPlayer.x1 == CrystalPlayer.x2 || CrystalPlayer.z1 == CrystalPlayer.z2) {
										AuctionHandler.setSignLocation(CrystalPlayer.x1, CrystalPlayer.y1, CrystalPlayer.z1, CrystalPlayer.x2, CrystalPlayer.y2, CrystalPlayer.z2);
										p.sendMessage(ChatColor.GREEN + "The new bounds where the signs are put is now set.");
									} else {
										p.sendMessage(ChatColor.RED + "Please select the bounds by left and right clicking with the stick.");
									}
								} else {
									p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
								}
								return true;
							} catch (Exception e) {
								e.printStackTrace();
							}
							return true;
						} else if (insideAuctionArea(p)) {
							try {
								int price = -1;
								int amount = -1;
								ItemStack is = p.getInventory().getItemInHand();
								if (myArgs[0].equalsIgnoreCase("info") || myArgs[0].equalsIgnoreCase("help")) {
									showHelp(p);
									return true;
								} else if (myArgs[0].equalsIgnoreCase("sell")) {
									if (myArgs.length == 3) {
										if (isNumeric(myArgs[1])) {
											price = Integer.parseInt(myArgs[1]);
											if (isNumeric(myArgs[2])) {
												amount = Integer.parseInt(myArgs[2]);
												if (is.getAmount() >= amount && amount <= 64) {
													double money = new Accounts().get(p.getName()).getHoldings().getBalance();
													if (money - (price * .1) >= 0) {
														totalAuctionsSet++;
														ItemStack clone = is.clone();
														clone.setAmount(amount);
														p.getInventory().removeItem(is);
														new Auction(amount, price, clone, p, totalAuctionsSet);
														ItemStack clone2 = clone.clone();
														clone2.setAmount(is.getAmount() - amount);
														p.getInventory().addItem(clone2);
														Account account = new Accounts().get(p.getName());
														account.getHoldings().subtract(((int) price * .1));
														s.broadcastMessage(ChatColor.GOLD + p.getName() + " is selling " + amount + " " + getItemName(is.getTypeId()) + " for $" + price + "! Buy at ID: " + totalAuctionsSet + ".");
														if (AuctionHandler.useSigns())
															putSign(totalAuctionsSet, getItemName(is.getTypeId()), price, amount, p);													
														p.sendMessage(ChatColor.GREEN + "Your auction was successfully made and there was a tax price of $" + ((int) price * .1) + "!");
														return true;
													} else {
														p.sendMessage(ChatColor.RED + "You don't have enough money to pay for tax of $" + (price * .1) + "!");
													}
												} else {
													p.sendMessage(ChatColor.RED + "Make sure you have enough of the item to sell!");
												}
											} else {
												p.sendMessage(ChatColor.RED + "Use /auction sell <price> <amount>");
											}
										} else {
											p.sendMessage(ChatColor.RED + "Use /auction sell <price> <amount>");
										}
									} else {
										p.sendMessage(ChatColor.RED + "Use /auction sell <price> <amount>");
									}
									return true;
								} else if (myArgs[0].equalsIgnoreCase("buy")) {
									try {
										Account account = new Accounts().get(p.getName());
										double money = account.getHoldings().getBalance();
										Auction a = AuctionHandler.getAuctionById(Integer.parseInt(myArgs[1]));
										if (a != null) {
											if (!a.getPlayer().getName().equalsIgnoreCase(p.getName())) {
												if (money >= a.getPrice()) {
													p.getInventory().addItem(a.getItemStack());
													account.getHoldings().subtract(a.getPrice());
													Player seller = a.getPlayer();
													new Accounts().get(seller.getName()).getHoldings().add(a.getPrice());
													p.sendMessage(ChatColor.GREEN + "You have successfully bought " + a.getAmount() + " " + getItemName(a.getItemStack().getTypeId()) + " for $" + a.getPrice() + "!");
													seller.sendMessage(ChatColor.GOLD + p.getName() + " has bought your " + getItemName(a.getItemStack().getTypeId()) + " for $" + a.getPrice() + "!");
													a.remove();
												} else {
													p.sendMessage(ChatColor.RED + "You don't have enough money to afford that!");
												}
											} else {
												p.sendMessage(ChatColor.RED + "You can't buy your own item!");
											}
										} else {
											p.sendMessage(ChatColor.RED + "That auction does not exist!");
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									return true;
								} else if (myArgs != null && myArgs[0].equalsIgnoreCase("list")) {
									if (myArgs.length > 1) {
										if (myArgs[1].equalsIgnoreCase("item")) {
											if (myArgs.length > 2) {
												int count = 0;
												for (Auction a : AuctionHandler.auctions) {
													if (!isNumeric(myArgs[2])) {
														if (getItemName(a.getItemStack().getTypeId()).toLowerCase().indexOf((myArgs[2].replace("_", " ").toLowerCase())) > -1) {
															p.sendMessage(ChatColor.AQUA + "Item: " + getItemName(a.getItemStack().getTypeId()) + " - Amount: " + a.getAmount() + " - Price: $" + a.getPrice() + " - Id: " + a.getId());
															count++;
														}
													} else if (isNumeric(myArgs[2])) {
														if (a.getItemStack().getTypeId() == Integer.parseInt(myArgs[2])) {
															p.sendMessage(ChatColor.AQUA + "Item: " + getItemName(a.getItemStack().getTypeId()) + " - Amount: " + a.getAmount() + " - Price: $" + a.getPrice() + " - Id: " + a.getId());
															count++;
														}
													}
												}
												if (count == 0)
													p.sendMessage(ChatColor.AQUA + "No auctions are selling an item by that name/id!");
												return true;
											} else {
												p.sendMessage(ChatColor.AQUA + "Please use /auction info");
												return true;
											}
										} else if (myArgs[1].equalsIgnoreCase("mine")) {
											int count = 0;
											for (Auction a : AuctionHandler.auctions) {
												if (a.getPlayer().getName().equalsIgnoreCase(p.getName())) {
													p.sendMessage(ChatColor.AQUA + "Item: " + getItemName(a.getItemStack().getTypeId()) + " - Amount: " + a.getAmount() + " - Price: $" + a.getPrice() + " - Id: " + a.getId());
													count++;
												}
											}
											
											if (count == 0)
												p.sendMessage(ChatColor.AQUA + "You don't have any auctions!");
											return true;
										}
									} else {
										int count = 0;
										
										for (Auction a : AuctionHandler.auctions) {
											if (!a.getPlayer().getName().equalsIgnoreCase(p.getName())) {
												p.sendMessage(ChatColor.AQUA + "Item: " + getItemName(a.getItemStack().getTypeId()) + " - Amount: " + a.getAmount() + " - Price: $" + a.getPrice() + " - Id: " + a.getId());
												count++;
											}
										}
										
										if (count == 0)
											p.sendMessage(ChatColor.AQUA + "There are no auctions at the moment.");
										
										return true;
									}
								} else if (myArgs[0].equalsIgnoreCase("cancel")) {
									try {
										Auction a = AuctionHandler.getAuctionById(Integer.parseInt(myArgs[1]));
										if (a != null) {
											if (a.getPlayer().getName().equalsIgnoreCase(p.getName())) {
												a.cancel();
												p.sendMessage(ChatColor.AQUA + "Your auction was canceled!");
											} else {
												p.sendMessage(ChatColor.AQUA + "That is not your auction!");
											}
												
										} else {
											p.sendMessage(ChatColor.RED + "That auction does not exist!");
										}
										return true;
									} catch (Exception e) {
										e.printStackTrace();
									}
									return true;
								}
							} catch (Exception e) {
								e.printStackTrace();
								return true;					
							}
						} else {
							p.sendMessage(ChatColor.RED + "You need to be in the Auction Area to use the Auction Commands!");
							return true;
						}
					} else {
						showHelp(p);
						return true;
					}
				} else {
					p.sendMessage(ChatColor.RED + "You dont have permissions to auction!");
					return true;
				}
				return false;
			} else {
				showHelp(p);
				return true;
			}
 		}
		return false;
	}
	
	public static void showHelp(Player p) {
		p.sendMessage(ChatColor.AQUA + "------[ Athcraft - AuctionHouse ]------");
		p.sendMessage(ChatColor.AQUA + "--------[ Made By Baseball435 ]--------");
		p.sendMessage(ChatColor.GOLD + "The item you are holding will be the one sold.");
		p.sendMessage(ChatColor.GOLD + "/auction sell <price> <amount> - Sets a new auction");
		p.sendMessage(ChatColor.GOLD + "/auction buy <id> - Buys auction by their id");
		p.sendMessage(ChatColor.GOLD + "/auction cancel <id> - Cancels your auctions by id");
		p.sendMessage(ChatColor.GOLD + "/auction list - shows all auctions not including yours");
		p.sendMessage(ChatColor.GOLD + "/auction list item <name/id> - Shows all auctions by item");
		p.sendMessage(ChatColor.GOLD + "/auction list mine - shows all of your auctions");
		if (p.hasPermission("ca.setbounds"))
			p.sendMessage(ChatColor.GOLD + "/auction setbounds - Sets the bounds of the Auction place. Use the stick to select the places. -ADMINS ONLY-");
	}
	
	public static boolean isNumeric(String n) {
		try {   
			Integer i = Integer.parseInt(n);   
	    } catch(NumberFormatException e) {   
	    	return false;   
	    }   
	    return true;
	}
	
	public static boolean canAuction(String name) {
			Player p = s.getPlayer(name).getPlayer();
			if (p.hasPermission("ca.auction")) {
				return true;
			}
		return false;
	}
	
	public static void putSign(int id, String item, int price, int amount, Player p) {
		try {
			String[] coords = AuctionHandler.loadConfigFile()[1].split(" ");
			String offset = coords[3];
			Block b = p.getWorld().getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			
			if (offset.equalsIgnoreCase("x") && id != 1)
				b = p.getWorld().getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]) + (id * 2) - 2);
			else if (offset.equalsIgnoreCase("z") && id != 1)
				b = p.getWorld().getBlockAt(Integer.parseInt(coords[0]) + (id * 2) - 2, Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			
			b.setTypeId(68);
			Sign s = (Sign) b.getState();
				
			if (offset.equalsIgnoreCase("x"))
				s.getLocation().setZ(b.getLocation().getZ() + id - 1);
			else if (offset.equalsIgnoreCase("z"))
				s.getLocation().setX(b.getLocation().getX() + id - 1);
				
			s.update();
			s.setLine(0, "ID: " + id);
			s.setLine(1, item);
			s.setLine(2, "P: " + price);
			s.setLine(3, "A: " + amount + "");
			s.update();
			
			new AuctionSign(p, item, id, price, amount, s);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean insideAuctionArea(Player p) {
		Location l = p.getLocation();
		
		if (AuctionHandler.getBoundLocations(p) == null)
			return false;
		
		Location l1 = AuctionHandler.getBoundLocations(p)[0];
		Location l2 = AuctionHandler.getBoundLocations(p)[1];	
		
		boolean inX = false, inZ = false;
		
		if (l1.getX() >= l2.getX()) {
			if (l.getX() <= l1.getX() && l.getX() >= l2.getX())
				inX = true;
		} else if (l2.getX() >= l1.getX()) {
			if (l.getX() >= l1.getX() && l.getX() <= l2.getX())
				inX = true;
		}
			
		if (l1.getZ() >= l2.getZ()) {
			if (l.getZ() <= l1.getZ() && l.getZ() >= l2.getZ())
				inZ = true;
		} else if(l2.getZ() >= l1.getZ()) {
			if (l.getZ() >= l1.getZ() && l.getZ() <= l2.getZ())
				inZ = true;
		}
		return inX && inZ;
	}
	
	public static void removeSignById(int id) {
		for (AuctionSign s : signs)
			if (s.id == id)
				s.p.getWorld().getBlockAt(s.s.getLocation()).setTypeId(0);
	}
	
	public static String getItemName(int id) {
		switch(id) {
			case 1: return "Stone";
			case 2: return "Grass";
			case 3: return "Dirt";
			case 4: return "Cobblestone";
			case 5: return "Wooden plank";
			case 6: return "Sapling";
			case 7: return "Bedrock";
			case 8: return "Water";
			case 9: return "Water";
			case 10: return "Lava";
			case 11: return "Lava";
			case 12: return "Sand";
			case 13: return "Gravel";
			case 14: return "Gold Ore";
			case 15: return "Iron Ore";
			case 16: return "Coal Ore";
			case 17: return "Wood";
			case 18: return "Leaves";
			case 19: return "Sponge";
			case 20: return "Glass";
			case 21: return "Lapis Lazuli Ore";
			case 22: return "Lapis Lazuli Block";
			case 23: return "Dispenser";
			case 24: return "Sandstone";
			case 25: return "Noteblock";
			case 26: return "Bed Block";
			case 27: return "Powered Rail";
			case 28: return "Detector Rail";
			case 29: return "Sticky Piston";
			case 30: return "Web";
			case 31: return "Shrub";
			case 32: return "Dead Shrub";
			case 33: return "Piston";
			case 34: return "Piston Head";
			case 35: return "Wool";
			case 37: return "Flower";
			case 38: return "Flower";
			case 39: return "Mushroom";
			case 40: return "Mushroom";
			case 41: return "Gold Block";
			case 42: return "Iron Block";
			case 43: return "Double Slab";
			case 44: return "Slab";
			case 45: return "Brick";
			case 46: return "TNT";
			case 47: return "Bookshelf";
			case 48: return "Mossy Cobblestone";
			case 49: return "Obsidian";
			case 50: return "Torch";
			case 51: return "Fire";
			case 52: return "Monster Spawner";
			case 53: return "Wooden Stairs";
			case 54: return "Chest";
			case 55: return "Redstone Wire";
			case 56: return "Diamond Ore";
			case 57: return "Diamond Block";
			case 58: return "Workbench";
			case 59: return "Wheat Crop";
			case 60: return "Soil";
			case 61: return "Furnace";
			case 62: return "Burning Furnace";
			case 63: return "Sign";
			case 64: return "Wooden Door";
			case 65: return "Ladder";
			case 66: return "Rails";
			case 67: return "Cobblestone Stairs";
			case 68: return "Sign";
			case 69: return "Lever";
			case 70: return "Stone Pressure Plate";
			case 71: return "Iron Door";
			case 72: return "Wooden Pressure Plate";
			case 73: return "Redstone Ore";
			case 74: return "Redstone Ore";
			case 75: return "Redstone Torch";
			case 76: return "Redstone Torch";
			case 77: return "Stone Button";
			case 78: return "Snow";
			case 79: return "Ice";
			case 80: return "Snow";
			case 81: return "Cactus";
			case 82: return "Clay";
			case 83: return "Sugar Cane";
			case 84: return "Jukebox";
			case 85: return "Fence";
			case 86: return "Pumpkin";
			case 87: return "Netherrack";
			case 88: return "Soul Sand";
			case 89: return "Glowstone";
			case 90: return "Portal";
			case 91: return "Jack-O-Lantern";
			case 92: return "Cake";
			case 93: return "Redstone Repeater";
			case 94: return "Redstone Repeater";
			case 95: return "Chest";
			case 96: return "Trapdoor";
			case 97: return "Silverfish Stone";
			case 98: return "Stone Brick";
			case 99: return "Red Mushroom Cap";
			case 100: return "Brown Mushroom Cap";
			case 101: return "Iron Bars";
			case 102: return "Glass Pane";
			case 103: return "Melon Block";
			case 104: return "Pumpkin Stem";
			case 105: return "Melon Stem";
			case 106: return "Vines";
			case 107: return "Fence Gate";
			case 108: return "Brick Stairs";
			case 109: return "Stone Brick Stairs";
			case 110: return "Mycelium";
			case 111: return "Lily Pad";
			case 112: return "Nether Brick";
			case 113: return "Nether Brick Fence";
			case 114: return "Nether Brick Stairs";
			case 115: return "Nether Wart";
			case 256: return "Iron Shovel";
			case 257: return "Iron Pickaxe";
			case 258: return "Iron Axe";
			case 259: return "Flint and Steel";
			case 260: return "Apple";
			case 261: return "Bow";
			case 262: return "Arrow";
			case 263: return "Coal";
			case 264: return "Diamond";
			case 265: return "Iron Ingot";
			case 266: return "Gold Ingot";
			case 267: return "Iron Sword";
			case 268: return "Wooden Sword";
			case 269: return "Wooden Shovel";
			case 270: return "Wooden Pickaxe";
			case 271: return "Wooden Axe";
			case 272: return "Stone Sword";
			case 273: return "Stone Shovel";
			case 274: return "Stone Pickaxe";
			case 275: return "Stone Axe";
			case 276: return "Diamond Sword";
			case 277: return "Diamond Shovel";
			case 278: return "Diamond Pickaxe";
			case 279: return "Diamond Axe";
			case 280: return "Stick";
			case 281: return "Bowl";
			case 282: return "Mushroom Soup";
			case 283: return "Gold Sword";
			case 284: return "Gold Shovel";
			case 285: return "Gold Pickaxe";
			case 286: return "Gold Axe";
			case 287: return "String";
			case 288: return "Feather";
			case 289: return "Sulphur";
			case 290: return "Wooden Hoe";
			case 291: return "Stone Hoe";
			case 292: return "Iron Hoe";
			case 293: return "Diamond Hoe";
			case 294: return "Gold Hoe";
			case 295: return "Wheat Seeds";
			case 296: return "Wheat";
			case 297: return "Bread";
			case 298: return "Leather Helmet";
			case 299: return "Leather Chestplate";
			case 300: return "Leather Leggings";
			case 301: return "Leather Boots";
			case 302: return "Chainmail Helmet";
			case 303: return "Chainmail Chestplate";
			case 304: return "Chainmail Leggings";
			case 305: return "Chainmail Boots";
			case 306: return "Iron Helmet";
			case 307: return "Iron Chestplate";
			case 308: return "Iron Leggings";
			case 309: return "Iron Boots";
			case 310: return "Diamond Helmet";
			case 311: return "Diamond Chestplate";
			case 312: return "Diamond Leggings";
			case 313: return "Diamond Boots";
			case 314: return "Gold Helmet";
			case 315: return "Gold Chestplate";
			case 316: return "Gold Leggings";
			case 317: return "Gold Boots";
			case 318: return "Flint";
			case 319: return "Raw Porkchop";
			case 320: return "Cooked Porkchop";
			case 321: return "Painting";
			case 322: return "Golden Apple";
			case 323: return "Sign";
			case 324: return "Wooden Door";
			case 325: return "Bucket";
			case 326: return "Water Bucket";
			case 327: return "Lava Bucket";
			case 328: return "Minecart";
			case 329: return "Saddle";
			case 330: return "Iron Door";
			case 331: return "Redstone";
			case 332: return "Snowball";
			case 333: return "Boat";
			case 334: return "Leather";
			case 335: return "Milk Bucket";
			case 336: return "Clay Brick";
			case 337: return "Clay Ball";
			case 338: return "Sugarcane";
			case 339: return "Paper";
			case 340: return "Book";
			case 341: return "Slimeball";
			case 342: return "Storage Minecart";
			case 343: return "Powered Minecart";
			case 344: return "Egg";
			case 345: return "Compass";
			case 346: return "Fishing Rod";
			case 347: return "Clock";
			case 348: return "Glowstone Dust";
			case 349: return "Raw Fish";
			case 350: return "Cooked Fish";
			case 351: return "Ink Sack/Dye";
			case 352: return "Bone";
			case 353: return "Sugar";
			case 354: return "Cake";
			case 355: return "Bed";
			case 356: return "Redstone Repeater";
			case 357: return "Cookie";
			case 358: return "Map";
			case 359: return "Shears";
			case 360: return "Melon";
			case 361: return "Pumpkin Seeds";
			case 362: return "Melon Seeds";
			case 363: return "Raw Beef";
			case 364: return "Cooked Beef";
			case 365: return "Raw Chicken";
			case 366: return "Cooked Chicken";
			case 367: return "Rotten Flesh";
			case 368: return "Ender Pearl";
			case 369: return "Blaze Rod";
			case 370: return "Ghast Tear";
			case 371: return "Gold Nugget";
			case 372: return "Nether Wart Seeds";
			case 373: return "Potion";
			case 374: return "Glass Bottle";
			case 375: return "Spider Eye";
			case 376: return "Fermented Spider Eye";
			case 377: return "Blaze Powder";
			case 378: return "Magma Cream";
			case 2256: return "Gold Music Disc";
			case 2257: return "Green Music Disc";
		}
		return "";
	}
}

class AuctionSign {
	
	public int id, price, amount;
	public String item;
	public Player p;
	public Sign s;
	
	public AuctionSign(Player p, String item, int id, int price, int amount, Sign s) {
		this.p = p;
		this.item = item;
		this.id = id;
		this.price = price;
		this.amount = amount;
		this.s = s;
		CrystalAuction.signs.add(this);
	}
	
}
