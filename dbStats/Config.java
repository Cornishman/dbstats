package dbStats;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import java.io.File;
import java.util.*;

public class Config {
	private static Configuration config;
	
	public static TreeSet<Integer> blocksWithMetaData;
	public static HashMap<String, String> nbtSearchStrings;
	public static HashMap<String, List<Integer>> craftingSlotIndentities;
	public static HashMap<String, List<Integer>> smeltingSlotIdentities; 
	
	public static int timeBetweenDatabaseQueries;
	public static int timeBetweenPlayerUpdates;
	private static int sizeOfQueueBeforeForceUpdate;
	
	public static boolean trackCreativeModeStats;
	public static boolean debugMode;
	public static boolean logSqlQueries;
	
	public static String databaseType;
	public static String serverAddress;
	public static String serverPort;
	public static String databaseName;
	public static String db_userName;
	public static String db_password;
	
	public static boolean dumpItemIds;
	public static boolean saveItemIdsWithMetadata;
	public static boolean dumpFluids;
	
	public static boolean enableBlockPlaceLogging;
	public static boolean enableBlockBreakLogging;
	public static boolean enableItemPickupLogging;
	public static boolean enableItemDropLogging;
	public static boolean enableDeathKillsLogging;
	public static boolean enableMovementLogging;
	public static boolean enableCraftingLogging;
	public static boolean enableAttackLogging;
	public static boolean enableHurtLogging;
    public static boolean enableMiscLogging;
	
	public Config(File configFile)
	{
		config = new Configuration(configFile);
	}
	
	public void setupOptions(boolean loading)
	{
		Property blocksToSaveWithMetadataList = config.get(Configuration.CATEGORY_BLOCK, "BlockIdsWithMetadata", new int[] { });
		blocksToSaveWithMetadataList.comment = "List of blockID's to save in the database with metadata";
		if (loading)
		{
			int[] blocksToSaveWithMetadata = blocksToSaveWithMetadataList.getIntList();
			blocksWithMetaData = new TreeSet<Integer>();
			for (int blockID : blocksToSaveWithMetadata)
			{
				blocksWithMetaData.add(blockID);
			}
		}
		else
		{
			TreeSet<Integer> sortedBlocks = new TreeSet<Integer>(blocksWithMetaData);
			String[] blocks = new String[blocksWithMetaData.size()];
			int i = 0;
			for(Integer blockid : sortedBlocks)
			{
				blocks[i] = Integer.toString(blockid);
				i++;
			}
			blocksToSaveWithMetadataList.set(blocks);
		}
		
		Property nbtQueryStrings = config.get(Configuration.CATEGORY_BLOCK, "NBTTagSearches", new String[] {});
		nbtQueryStrings.comment = "NBTtag value to save with an item to the database.";
		nbtQueryStrings.comment += "\nFormatted like \"blockId:meta/TagToSave\"";
		nbtQueryStrings.comment += "\nThe TagToSave can be a list of tags and subtags, for example:";
		nbtQueryStrings.comment += "\nBees from forestry would be \"13340:0/Genome/Chromosomes[0]/UID0\" this is because we want the UID0 value of the first chomosome tag in the genome tag of the NBT data";
		nbtQueryStrings.comment += "\nThis would get the primary species of the bee and use it when saving to the database";
		if (loading)
		{
			nbtSearchStrings = new HashMap<String, String>();
			String[] nbtQueries = nbtQueryStrings.getStringList();
			for(String nbtQuery : nbtQueries)
			{
				String key = nbtQuery.substring(1, nbtQuery.indexOf("/"));
				String value = nbtQuery.substring(nbtQuery.indexOf("/") + 1, nbtQuery.length() - 1);
				nbtSearchStrings.put(key, value);
			}
		}
		else
		{
			String[] nbtQueries = new String[nbtSearchStrings.size()];
			int i = 0;
			for(Map.Entry<String, String> nbtQuery : nbtSearchStrings.entrySet())
			{
				nbtQueries[i] = "\"" + nbtQuery.getKey() + "/" + nbtQuery.getValue() + "\"";
				i++;
			}
			nbtQueryStrings.set(nbtQueries);
		}
		
		Property craftingSlotConfigs = config.get(Configuration.CATEGORY_BLOCK, "craftingBlockSlots", new String[] {});
		craftingSlotConfigs.comment = "List of blockids and slots that correspond to crafting interfaces.";
		craftingSlotConfigs.comment += "\nFormatted like blockId:blockMeta:slot,slot,slot";
		craftingSlotConfigs.comment += "\nie. 4406:2:6,8 would indicate blockId 4406 with metadata 2, any items pulled from slot 6 or 8 are crafting results";
		if (loading)
		{
			craftingSlotIndentities = new HashMap<String, List<Integer>>();
			String[] craftingIdents = craftingSlotConfigs.getStringList();
			for(String craftingIdent : craftingIdents)
			{
				craftingIdent = craftingIdent.replace("\"", "");
				String key = craftingIdent.split(":")[0] + ":" + craftingIdent.split(":")[1];
				String[] slotNums = craftingIdent.split(":")[2].split(",");
				List<Integer> slots = new ArrayList<Integer>(slotNums.length);
				for(int i = 0; i < slotNums.length; i++)
				{
					slots.add(Integer.parseInt(slotNums[i]));
				}
				craftingSlotIndentities.put(key, slots);
			}
		}
		else
		{
			String[] craftingSlotIdents = new String[craftingSlotIndentities.size()];
			int i = 0;
			for(Map.Entry<String, List<Integer>> craftingSlotIdent : craftingSlotIndentities.entrySet())
			{
				String slots = "";
				for(int slot : craftingSlotIdent.getValue())
				{
					slots += slot + ",";
				}
				slots = slots.substring(0, slots.length() - 1);
				craftingSlotIdents[i] = "\"" + craftingSlotIdent.getKey() + ":" + slots + "\"";
				i++;
			}
			craftingSlotConfigs.set(craftingSlotIdents);
		}
		
		Property smeltingSlotConfigs = config.get(Configuration.CATEGORY_BLOCK, "smeltingBlockSlots", new String[] {});
		smeltingSlotConfigs.comment = "List of blockids and slots that correspond to smelting interfaces.";
		smeltingSlotConfigs.comment += "\nFormatted like blockId:blockMeta:slot,slot,slot";
		smeltingSlotConfigs.comment += "\nie. 4406:2:6,8 would indicate blockId 4406 with metadata 2, any items pulled from slot 6 or 8 are smelting results";
		if (loading)
		{
			smeltingSlotIdentities = new HashMap<String, List<Integer>>();
			String[] smeltingIdents = smeltingSlotConfigs.getStringList();
			for(String smeltingIdent : smeltingIdents)
			{
				smeltingIdent = smeltingIdent.replace("\"", "");
				String key = smeltingIdent.split(":")[0] + ":" + smeltingIdent.split(":")[1];
				String[] slotNums = smeltingIdent.split(":")[2].split(",");
				List<Integer> slots = new ArrayList<Integer>(slotNums.length);
				for(int i = 0; i < slotNums.length; i++)
				{
					slots.add(Integer.parseInt(slotNums[i]));
				}
				smeltingSlotIdentities.put(key, slots);
			}
		}
		else
		{
			String[] smeltingSlotIdents = new String[smeltingSlotIdentities.size()];
			int i = 0;
			for(Map.Entry<String, List<Integer>> smeltingSlotIdent : smeltingSlotIdentities.entrySet())
			{
				String slots = "";
				for(int slot : smeltingSlotIdent.getValue())
				{
					slots += slot + ",";
				}
				slots = slots.substring(0, slots.length() - 1);
				smeltingSlotIdents[i] = "\"" + smeltingSlotIdent.getKey() + ":" + slots + "\"";
				i++;
			}
			smeltingSlotConfigs.set(smeltingSlotIdents);
		}
		
		
		
		Property queryDelays = config.get(Configuration.CATEGORY_GENERAL, "databaseDelay", 15);
		queryDelays.comment = "Time (in seconds) between updating the database";
		if (loading)
		{
			timeBetweenDatabaseQueries = queryDelays.getInt() >= 15 ? queryDelays.getInt() : 15;	//Enforce minimum of 15
		}
		else
		{
			queryDelays.set(timeBetweenDatabaseQueries >= 15 ? timeBetweenDatabaseQueries : 15);	//Enforce minimum of 15
		}
		
		Property playerDelay = config.get(Configuration.CATEGORY_GENERAL, "playerUpdateDelay", 15);
		playerDelay.comment = "Time (in seconds) between updating player statistics (Distance, timeplayed etc).\n NOTE: This does not query the database, this adds the queries to the queue for the next databaseDelay call.";
		if (loading)
		{
			timeBetweenPlayerUpdates = playerDelay.getInt() >= 15 ? playerDelay.getInt() : 15;	//Enforce minimum of 15
		}
		else
		{
			playerDelay.set(timeBetweenPlayerUpdates >= 15 ? timeBetweenPlayerUpdates : 15);	//Enforce minimum of 15
		}
		
		Property queueSize = config.get(Configuration.CATEGORY_GENERAL, "forceQueueSize", 0);
		queueSize.comment = "Size of the query queue before forcing database updates.\n(Can be used to prevent large queues from hammering the database).\nSetting this to 0 disables this feature.";
		if (loading)
		{
			sizeOfQueueBeforeForceUpdate = queueSize.getInt() >= 0 ? queueSize.getInt() : 0;	//Enforce positive numbers
		}
		else
		{
			queueSize.set(sizeOfQueueBeforeForceUpdate >= 0 ? sizeOfQueueBeforeForceUpdate : 0);	//Enforce positive numbers
		}
		
		Property trackCreative = config.get(Configuration.CATEGORY_GENERAL, "logCreativeStats", false);
		trackCreative.comment = "Whether to track statistics for players in creative mode.";
		if (loading)
		{
			trackCreativeModeStats = trackCreative.getBoolean(false);
		}
		else
		{
			trackCreative.set(trackCreativeModeStats);
		}
		
		Property debugMessages = config.get(Configuration.CATEGORY_GENERAL, "debug", false);
		debugMessages.comment = "If enabled prints debugging information to the console.";
		if (loading)
		{
			debugMode = debugMessages.getBoolean(false);
		}
		else
		{
			debugMessages.set(debugMode);
		}
		
		
		Property sqlMessages = config.get(Configuration.CATEGORY_GENERAL, "logSqlMessages", false);
		sqlMessages.comment = "If enabled and debugging is enabled, will print every sql statement as they are sent! \n(Highly advised to keep this off unless you know what you are doing)";
		if (loading)
		{
			logSqlQueries = sqlMessages.getBoolean(false);
		}
		else
		{
			sqlMessages.set(logSqlQueries);
		}
		
		Property itemDump = config.get(Configuration.CATEGORY_GENERAL, "dumpItemIds", false);
		itemDump.comment = "If enabled when starting up a game, all ingame item information will be dumped into the 'items' table in the database.\nWill automatically set back to false once done to prevent future calls.";
		if (loading)
		{
			dumpItemIds = itemDump.getBoolean(false);
		}
		else
		{
			itemDump.set(dumpItemIds);
		}
		
		Property autoSaveItemIdsWithMetadata = config.get(Configuration.CATEGORY_GENERAL, "autoGetMetadataItems", "true");
		autoSaveItemIdsWithMetadata.comment = "If set to true, when a dump of items is run it will find all item ids that have metadata subitems and add them to the BlockIds list.";
		saveItemIdsWithMetadata = autoSaveItemIdsWithMetadata.getBoolean(true);
		
		Property fluidDump = config.get(Configuration.CATEGORY_GENERAL, "dumpFluids", false);
		fluidDump.comment = "If enabled when starting up a game, all ingame fluids information will be dumped into the 'fluids' table in the database.\nWill automatically set back to false once done to prevent future calls.";
		if (loading)
		{
			dumpFluids = fluidDump.getBoolean(false);
		}
		else
		{
			fluidDump.set(dumpFluids);
		}
		
		//-------Enable/Disable of trackers-----------
		config.addCustomCategoryComment("Trackers", "Enable/Disable of various gameplay statistics being tracked");
		
		Property trackDeathkills = config.get("Trackers", "deathKills", true);
		if (loading)
		{
			enableDeathKillsLogging = trackDeathkills.getBoolean(true);
		}
		else
		{
			trackDeathkills.set(enableDeathKillsLogging);
		}
		
		Property trackItemPickups = config.get("Trackers", "itemPickups", true);
		if (loading)
		{
			enableItemPickupLogging = trackItemPickups.getBoolean(true);
		}
		else
		{
			trackItemPickups.set(enableItemPickupLogging);
		}
		
		Property trackItemDrops = config.get("Trackers", "itemDrops", true);
		if (loading)
		{
			enableItemDropLogging = trackItemDrops.getBoolean(true);
		}
		else
		{
			trackItemDrops.set(enableItemDropLogging);
		}
		
		Property trackBlockBreak = config.get("Trackers", "blockBreak", true);
		if (loading)
		{
			enableBlockBreakLogging = trackBlockBreak.getBoolean(true);
		}
		else
		{
			trackBlockBreak.set(enableBlockBreakLogging);
		}
		
		Property trackBlockPlace = config.get("Trackers", "blockPlace", true);
		if (loading)
		{
			enableBlockPlaceLogging = trackBlockPlace.getBoolean(true);
		}
		else
		{
			trackBlockPlace.set(enableBlockPlaceLogging);
		}
		
		Property trackMovement = config.get("Trackers", "movement", true);
		if (loading)
		{
			enableMovementLogging = trackMovement.getBoolean(true);
		}
		else
		{
			trackMovement.set(enableMovementLogging);
		}
		
		Property trackCrafting = config.get("Trackers", "crafting", true);
		if (loading)
		{
			enableCraftingLogging = trackCrafting.getBoolean(true);
		}
		else
		{
			trackCrafting.set(enableCraftingLogging);
		}
		
		Property trackHurt = config.get("Trackers", "hurt", true);
		if (loading)
		{
			enableHurtLogging = trackHurt.getBoolean(true);
		}
		else
		{
			trackHurt.set(enableHurtLogging);
		}
		
		Property trackDamage = config.get("Trackers", "attack", true);
		if (loading)
		{
			enableAttackLogging = trackDamage.getBoolean(true);
		}
		else
		{
			trackDamage.set(enableAttackLogging);
		}

        Property trackMisc = config.get("Trackers", "misc", true);
        if (loading)
        {
            enableMiscLogging = trackMisc.getBoolean(true);
        }
        else
        {
            trackMisc.set(enableMiscLogging);
        }

		//---------------------------------------------
		
		//Database info
		Property databaseT = config.get("Database", "databaseType", "MySQL");
		databaseT.comment = "Type of database to use (MySQL)";
		databaseType = databaseT.getString().toLowerCase();
		
		Property hostname = config.get("Database", "hostname", "localhost");
		hostname.comment = "Server address to connect to (ie. localhost)";
		serverAddress = hostname.getString();
		
		Property hostport = config.get("Database", "hostport", "3306");
		hostport.comment = "Port to connect to the database";
		serverPort = hostport.getString();
		
		Property dbName = config.get("Database", "database", "dbStats");
		dbName.comment = "Name of the database";
		databaseName = dbName.getString();
		
		Property user = config.get("Database", "username", "steve");
		user.comment = "Username to connect to the database";
		db_userName = user.getString();
		
		Property pass = config.get("Database", "password", "changeMe");
		pass.comment = "Password to connect to the database";
		db_password = pass.getString();
	}
	
	public void load()
	{
		config.load();
		setupOptions(true);
	}
	
	public void save() {
		setupOptions(false);
		config.save();
	}

	public int queryDelay()
	{
		return timeBetweenDatabaseQueries * 1000;
	}
	
	public int playerUpdateDelay()
	{
		return timeBetweenPlayerUpdates * 1000;
	}
	
	public boolean forceQueueUpdate(int size)
	{
		return sizeOfQueueBeforeForceUpdate > 0 && size > sizeOfQueueBeforeForceUpdate;
	}
}
