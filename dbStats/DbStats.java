package dbStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbStats.Events.DbStatsCraftingHandler;
import dbStats.Util.Utilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.ItemData;
import dbStats.API.Column;
import dbStats.API.Column.ColumnType;
import dbStats.API.Table;
import dbStats.API.Statistics.EStatistic;
import dbStats.Command.CommandBlockSlots;
import dbStats.Command.CommandConfig;
import dbStats.Command.CommandNBTDebug;
import dbStats.Command.CommandSlotDebug;
import dbStats.Command.CommandTracker;
import dbStats.EventTrackers.DbBlockBreakEvent;
import dbStats.EventTrackers.DbBlockPlaceEvent;
import dbStats.EventTrackers.DbCraftEvent;
import dbStats.EventTrackers.DbDropItemEvent;
import dbStats.EventTrackers.DbEntityAttackEvent;
import dbStats.EventTrackers.DbEntityDeathEvent;
import dbStats.EventTrackers.DbEntityHurtEvent;
import dbStats.EventTrackers.DbEventHandler;
import dbStats.EventTrackers.DbFallEvent;
import dbStats.EventTrackers.DbMoveEvent;
import dbStats.EventTrackers.DbPickupFromSlotEvent;
import dbStats.EventTrackers.DbPickupItemEvent;
import dbStats.EventTrackers.DbPlayerTracker;
import dbStats.Statistics.DatabaseFluid;
import dbStats.Statistics.DatabaseItem;
import dbStats.Util.ErrorUtil;
import dbStats.databases.DatabaseQueue;
import dbStats.databases.IDatabaseHandler;
import dbStats.databases.MySQL.MysqlHandler;

import static dbStats.Util.ErrorUtil.*;

@Mod(modid="DbStats", name="DbStats", version="0.0.1")
@NetworkMod(clientSideRequired=false)
public class DbStats {

	@Instance(value="DbStats")
	public static DbStats instance;
	public static MinecraftServer server;
	public static Logger log = Logger.getLogger("DbStats");
	public static IDatabaseHandler database;
	public static ErrorUtil errorUtil;
	public static DatabaseQueue statsQueue;
	
	public static List<String> playersForSlotDebug;
	public static List<String> playersForNBTDebug;
	
	public static DbPlayerTracker timedPlayerTracker;
	
	public static DbMoveEvent playerMoveTracker;
	public static DbFallEvent playerFallTracker;
	public static DbBlockBreakEvent playerBlockBreakTracker;
	public static DbBlockPlaceEvent playerBlockPlaceTracker;
	public static DbPickupItemEvent playerPickupItemTracker;
	public static DbEntityDeathEvent playerDeathKillTracker;
	public static DbDropItemEvent playerDropItemTracker;
	public static DbCraftEvent playerCraftingTracker;
	public static DbEntityHurtEvent playerHurtTracker;
	public static DbEntityAttackEvent playerAttackTracker;
	public static DbPickupFromSlotEvent playerPickupSlotTracker;
	
	
	public static Config config;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		log.setParent(FMLLog.getLogger());
		errorUtil = new ErrorUtil(log);
		
		config = new Config(event.getSuggestedConfigurationFile());
		config.load();
		config.save();
		
		if (Config.databaseType.equals("mysql"))
		{
			database = new MysqlHandler();
			MinecraftForge.EVENT_BUS.register(new DbEventHandler());
			RegisterDefaultTables();
		}
		else
		{
			log.log(Level.INFO, "Invalid database type specified - " + Config.databaseType);
			database = null;
		}
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//Stub
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		//Stub
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		server = event.getServer();

		RegisterCommands(event);
		
		if (server.getEntityWorld().isRemote)
		{
			log.log(Level.INFO, "Remote world(server) detected, disabling DbStats locally.");
			return;
		}
		
		if (database.IsReady())
		{
			if (!database.checkDatabase())
			{
				log.log(Level.INFO, "Database check failed!");
				database = null;
			}
		}
		
		if (database == null || !database.IsReady())
		{
			log.log(Level.SEVERE, "DbStats encountered errors and has had it's functionality disabled.");
			return;
		}
		
		//Make sure its registered before we start trying to post stats!
		statsQueue = new DatabaseQueue();
		MinecraftForge.EVENT_BUS.register(statsQueue);
		
		if (Config.dumpItemIds)
		{
			DumpItemIds();
			Config.dumpItemIds = false;	//Disable future dumping once done!
			config.save();
		}
		
		if (Config.dumpFluids)
		{
			DumpFluids();
			Config.dumpFluids = false; //Disable future dumping once done!
			config.save();
		}
		
//		GameRegistry.registerCraftingHandler(new DbStatsCraftingHandler());
		
		//Currently not disabling this option! - Future option!
		timedPlayerTracker = new DbPlayerTracker();
		GameRegistry.registerPlayerTracker(timedPlayerTracker);
		
		initTrackers();
	}
	
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		if (database == null || !database.IsReady())
			return;
		
		log.log(Level.INFO, "Server Stopping - Stopping all trackers.");
		
		unInitTrackers();
		
		timedPlayerTracker.stopAllPlayerTrackerEvents();
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event)
	{
		if (database == null || !database.IsReady())
			return;
		
		log.log(Level.INFO, "Server Stopped - Pushing the current queue to the database.");
		statsQueue.StopQueueHandler();
		
		if (database.isConnceted()) database.disconnect();
		
		if (statsQueue != null)
		{
			MinecraftForge.EVENT_BUS.unregister(statsQueue);
		}
		
		log.log(Level.INFO, "Finished pushing all queued stats to database.");
	}
	
	private void initTrackers()
	{
		if (Config.enableBlockBreakLogging)
		{
			playerBlockBreakTracker = new DbBlockBreakEvent();
			MinecraftForge.EVENT_BUS.register(playerBlockBreakTracker);
		}
		if (Config.enableBlockPlaceLogging)
		{
			playerBlockPlaceTracker = new DbBlockPlaceEvent();
			MinecraftForge.EVENT_BUS.register(playerBlockPlaceTracker);
		}
		
		if (Config.enableMovementLogging)
		{
			playerMoveTracker = new DbMoveEvent();
			MinecraftForge.EVENT_BUS.register(playerMoveTracker);
			playerFallTracker = new DbFallEvent();
			MinecraftForge.EVENT_BUS.register(playerFallTracker);
		}
		
		if (Config.enableDeathKillsLogging)
		{
			playerDeathKillTracker = new DbEntityDeathEvent();
			MinecraftForge.EVENT_BUS.register(playerDeathKillTracker);
		}
		
		if (Config.enableCraftingLogging)
		{
			playerCraftingTracker = new DbCraftEvent();
			MinecraftForge.EVENT_BUS.register(playerCraftingTracker);
			playerPickupSlotTracker = new DbPickupFromSlotEvent();
			MinecraftForge.EVENT_BUS.register(playerPickupSlotTracker);
            GameRegistry.registerCraftingHandler(new DbStatsCraftingHandler());
		}
		
		if (Config.enableItemDropLogging)
		{
			playerDropItemTracker = new DbDropItemEvent();
			MinecraftForge.EVENT_BUS.register(playerDropItemTracker);
		}
		if (Config.enableItemPickupLogging)
		{
			playerPickupItemTracker = new DbPickupItemEvent();
			MinecraftForge.EVENT_BUS.register(playerPickupItemTracker);
		}
		
		if (Config.enableAttackLogging)
		{
			playerAttackTracker = new DbEntityAttackEvent();
			MinecraftForge.EVENT_BUS.register(playerAttackTracker);
		}
		if (Config.enableHurtLogging)
		{
			playerHurtTracker = new DbEntityHurtEvent();
			MinecraftForge.EVENT_BUS.register(playerHurtTracker);
		}
	}
	
	private void unInitTrackers()
	{
		if (playerMoveTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerMoveTracker);
		}
		if (playerFallTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerFallTracker);
		}
		
		if (playerBlockBreakTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerBlockBreakTracker);
		}
		if (playerBlockPlaceTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerBlockPlaceTracker);
		}
		
		if (playerDeathKillTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerDeathKillTracker);
		}
		
		if (playerDropItemTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerDropItemTracker);
		}
		if (playerPickupItemTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerPickupItemTracker);
		}
		
		if (playerCraftingTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerCraftingTracker);
		}
		if (playerPickupSlotTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerPickupSlotTracker);
		}
		
		if (playerAttackTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerAttackTracker);
		}
		if (playerHurtTracker != null)
		{
			MinecraftForge.EVENT_BUS.unregister(playerHurtTracker);
		}
	}
	
	private static void RegisterCommands(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandSlotDebug());
		event.registerServerCommand(new CommandConfig());
		event.registerServerCommand(new CommandBlockSlots());
		event.registerServerCommand(new CommandTracker());
		event.registerServerCommand(new CommandNBTDebug());
	}
	
	private void DumpItemIds()
	{
		database.deleteAllTableInfo(database.findTableByName("items"));
		
		LogMessage("Dumping items to database");
		
		ArrayList<Integer> itemsWithMetadata = new ArrayList<Integer>();
		HashMap<String, String> detectedMods = new HashMap<String, String>(); 
		
		for (ModContainer mod : Loader.instance().getModList()) 
		{
			detectedMods.put(mod.getModId(), mod.getName());
	    }
		
		NBTTagList itemDataList = new NBTTagList();
	    GameData.writeItemData(itemDataList);

	    for (int i = 0; i < itemDataList.tagCount(); i++)
	    {
	    	ItemData itemData = new ItemData((NBTTagCompound)itemDataList.tagAt(i));
	    	
			Item item = Item.itemsList[itemData.getItemId()];
			if (item != null)
			{
				String modId = detectedMods.get(itemData.getModId());
				modId = modId == null ? "Minecraft" : modId;
				
				ItemStack is = new ItemStack(item);
				if (item.getHasSubtypes())
				{
					if (Config.saveItemIdsWithMetadata)
					{
						itemsWithMetadata.add(is.itemID);
					}
					ArrayList<String> unlocalizedNames = new ArrayList<String>(16);
					unlocalizedNames.add(is.getUnlocalizedName());
					MinecraftForge.EVENT_BUS.post(new EStatistic(new DatabaseItem(is.itemID, 0, Utilities.GetLocalisedItemName(is), is.getUnlocalizedName(), modId)));
					
					for(int j = 1; j < 16; j++)
					{
						try {
							is.setItemDamage(j);
							if (is.getUnlocalizedName() != null && !unlocalizedNames.contains(is.getUnlocalizedName()))
							{
								unlocalizedNames.add(is.getUnlocalizedName());
								MinecraftForge.EVENT_BUS.post(new EStatistic(new DatabaseItem(is.itemID, j, Utilities.GetLocalisedItemName(is), is.getUnlocalizedName(), modId)));
							}
						} catch(Exception ex)
						{
							// Do nothing, otherwise excessive error spam
//							LogException("Error thrown on itemID Dump (" + i + ":" + j + ") - ", ex);
						}
					}
					unlocalizedNames.clear();
				}
				else
				{
					try {
						MinecraftForge.EVENT_BUS.post(new EStatistic(new DatabaseItem(is.itemID, 0, Utilities.GetLocalisedItemName(is), is.getUnlocalizedName(), modId)));
					} catch (Exception ex)
					{
						LogException("Error thrown on itemId Dump (" + i + ") - ", ex);
					}
				}
			}
		}
		
		if (Config.saveItemIdsWithMetadata && itemsWithMetadata.size() > 0)
		{
			Config.blocksWithMetaData.addAll(itemsWithMetadata);
			config.save();	//Save the new blockIds
		}
	}
	
	private void DumpFluids()
	{
		database.deleteAllTableInfo(database.findTableByName("fluids"));
		LogMessage("Dumping fluids to database");
		Map<String, Integer> fluids = FluidRegistry.getRegisteredFluidIDs();
		Ordering<String> valueCompartor = Ordering.natural().onResultOf(Functions.forMap(fluids));
		fluids = ImmutableSortedMap.copyOf(fluids, valueCompartor);
		
		for(Map.Entry<String, Integer> fluid : fluids.entrySet())
		{
			Fluid f = FluidRegistry.getFluid(fluid.getValue());
			MinecraftForge.EVENT_BUS.post(new EStatistic(new DatabaseFluid(f.getID(), f.getName(), f.getUnlocalizedName(), f.getBlockID(), f.getColor(), f.getDensity(), f.getLuminosity(), f.getTemperature(), f.getViscosity(), f.isGaseous())));
		}
	}
	
	private void RegisterDefaultTables()
	{
		Column[] columns = new Column[]{
				new Column("ID", ColumnType.INT, 0, false, "", true, false, true),
				new Column("PlayerName", ColumnType.VARCHAR, 40, false, "", false, true, false),
				new Column("Logins", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Playtime", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("BlocksPlaced", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("BlocksBroken", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Kills", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Deaths", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("DimensionTeleports", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("ItemsPickedUp", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("ItemsDropped", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("ItemsCrafted", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("ItemsSmelted", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("LastLogin", ColumnType.TIMESTAMP, 0, false, "CURRENT_TIMESTAMP", false, false, false),
				new Column("FirstSeen", ColumnType.TIMESTAMP, 0, false, "0000-00-00 00:00:00", false, false, false),
				new Column("LastSeen", ColumnType.TIMESTAMP, 0, false, "0000-00-00 00:00:00", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("Players", columns, ""));
		
		columns = new Column[]{
			new Column("PlayerId", ColumnType.INT, 0, false, "0", true, false, false),
			new Column("method", ColumnType.VARCHAR, 40, false, "", true, false, false),
			new Column("distance", ColumnType.FLOAT, 0, false, "0", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("Distances", columns, "distance"));
		
		columns = new Column[]{
			new Column("PlayerId", ColumnType.INT, 0, false, "0", true, false, false),
			new Column("BI-ID", ColumnType.INT, 0, false, "0", true, false, false),
			new Column("Meta", ColumnType.INT, 0, false, "0", true, false, false),
			new Column("NBT", ColumnType.VARCHAR, 40, false, "", true, false, false),
			new Column("Action", ColumnType.VARCHAR, 6, false, "", true, false, false),
			new Column("Total", ColumnType.INT, 0, false, "0", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("bistats", columns, "total"));
		
		columns = new Column[]{
				new Column("PlayerId", ColumnType.INT, 0, false, "0", true, false, false),
				new Column("How", ColumnType.VARCHAR, 30, false, "", true, false, false),
				new Column("What", ColumnType.VARCHAR, 30, false, "", true, false, false),
				new Column("heldId", ColumnType.INT, 0, false, "0", true, false, false),
				new Column("heldMeta", ColumnType.INT, 0, false, "0", true, false, false),
				new Column("heldNBT", ColumnType.VARCHAR, 40, false, "", true, false, false),
				new Column("Enchants", ColumnType.VARCHAR, 30, false, "", true, false, false),
				new Column("Action", ColumnType.VARCHAR, 6, false, "", true, false, false),
				new Column("Total", ColumnType.FLOAT, 0, false, "0", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("EntityDamages", columns, "total"));
		
		columns = new Column[]{
				new Column("Id", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Meta", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("DisplayName", ColumnType.VARCHAR, 100, false, "", false, false, false),
				new Column("unlocalizedName", ColumnType.VARCHAR, 100, false, "", false, false, false),
				new Column("ModId", ColumnType.VARCHAR, 100, false, "", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("Items", columns, ""));
		
		columns = new Column[]{
				new Column("Id", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Name", ColumnType.VARCHAR, 100, false, "", false, false, false),
				new Column("unlocalizedName", ColumnType.VARCHAR, 100, false, "", false, false, false),
				new Column("BlockId", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Color", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Density", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Luminosity", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Temperature", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Viscosity", ColumnType.INT, 0, false, "0", false, false, false),
				new Column("Gaseous", ColumnType.BOOLEAN, 0, false, "0", false, false, false)
		};
		MinecraftForge.EVENT_BUS.post(new Table("Fluids", columns, ""));
	}
}
