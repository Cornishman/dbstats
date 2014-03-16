package dbStats.Util;

import cpw.mods.fml.common.registry.LanguageRegistry;
import dbStats.API.Column.ColumnType;
import dbStats.Config;
import dbStats.DbStats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Utilities {
	public static double Round(double unrounded, int precision)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
	    return rounded.doubleValue();
	}
	
	//Conversion to meters (estimate)
	public static double ConvertToMeters(double distance)
	{
		return Round((distance * 0.2), 2);
	}
	
	public static boolean CanTrackPlayer(EntityPlayer player)
	{
		return player instanceof EntityPlayerMP && (!(!Config.trackCreativeModeStats && player.capabilities.isCreativeMode));
	}
	
	public static boolean CanTrackPlayer(String playerName)
	{
		for (WorldServer server : DbStats.server.worldServers)
		{
			for (Object player : server.playerEntities)
			{
				if (((EntityPlayer) player).username.equals(playerName))
				{
					return CanTrackPlayer((EntityPlayer)player);
				}
			}
		}
		
		return false;
	}
	
	public static String GetDatabaseColumnType(ColumnType columnType, String databaseType)
	{
		switch(columnType)
		{
		case INT:
			return databaseType.equals("mysql") ? "int(11)" : "";
		case VARCHAR:
			return databaseType.equals("mysql") ? "varchar" : "";
		case FLOAT:
			return databaseType.equals("mysql") ? "float" : "";
		case TIMESTAMP:
			return databaseType.equals("mysql") ? "timestamp" : "";
		case BOOLEAN:
			return databaseType.equals("mysql") ? "tinyint(1)" : "";
		default:
			return "";
		}
	}
	
	public static ColumnType GetDatabaseColumnType(String columnType, String databaseType)
	{
		switch(columnType.toLowerCase())
		{
		case "int":
			return ColumnType.INT;
		case "varchar":
			return ColumnType.VARCHAR;
		case "float":
			return ColumnType.FLOAT;
		case "timestamp":
			return ColumnType.TIMESTAMP;
		case "tinyint":
			return ColumnType.BOOLEAN;
		case "bit":
			return ColumnType.BOOLEAN;
		default:
			return null;
		}
	}
	
	public static boolean IsColumnTypeNumerical(ColumnType cType)
	{
		switch(cType)
		{
		case INT:
		case FLOAT:
		case BOOLEAN:
			return true;
		default :
			return false;
		}
	}
	
	public static String GetMobName(Entity entity)
	{
		if (entity == null)
		{
			return "Unknown";
		}
		
		String mobName = entity.getEntityName();
		
		if (entity instanceof EntityPlayer)
		{
			mobName = ((EntityPlayer)entity).username;
			return mobName;
		}
		
		if (entity instanceof EntityZombie)
		{
			if (((EntityZombie)entity).isChild())
				mobName = "Baby" + mobName;
			
			if (((EntityZombie)entity).isVillager())
				mobName += "Villager";
			
			return mobName;
		}
		
		if (entity instanceof EntityCreeper)
		{
			if (((EntityCreeper)entity).getPowered())
				mobName = "Powered" + mobName;
			
			return mobName;
		}
		
		if (entity instanceof EntitySkeleton)
		{
			if (((EntitySkeleton)entity).getSkeletonType() == 1)
				mobName = "Wither" + mobName;
			
			return mobName;
		}
		
		if (entity instanceof EntitySlime)
		{
			mobName += ((EntitySlime)entity).getSlimeSize();
			
			return mobName;
		}
		
		if (entity instanceof EntityAgeable)
		{
			if (((EntityAgeable)entity).isChild())
				mobName = "Baby" + mobName;

            if (entity instanceof EntityHorse)
            {
                mobName += ((EntityHorse)entity).getOwnerName();
            }
			
			return mobName;
		}
		
		return mobName;
	}
	
	public static int GetItemMetaDataValue(ItemStack item)
	{
		return item == null ? 0 : Config.blocksWithMetaData.contains(item.itemID) ? item.getItemDamage() : 0;
	}
	
	public static String GetItemNBT(ItemStack item)
	{
		return item == null ? "" : Config.nbtSearchStrings.containsKey(item.itemID + ":" + item.getItemDamage()) 
				? NBTUtil.GetNBTValueAtTag(Config.nbtSearchStrings.get(item.itemID + ":" + item.getItemDamage()), item.getTagCompound()) : "";
	}
	
	public static String GetEnchantments(ItemStack item)
	{
		if (item == null)
			return "";
		
		String enchantments = "";
		
		NBTTagList enchantmentList = item.getEnchantmentTagList();
		
		if (enchantmentList != null)
		{
			for (int i = 0; i < enchantmentList.tagCount(); i++)
			{
				NBTTagCompound enchant = (NBTTagCompound) enchantmentList.tagAt(i);
				if (enchantments.length() > 0)
				{
					enchantments += ",";
				}
				enchantments += enchant.getShort("id") + "-" + enchant.getShort("lvl");
			}
		}
		
		return enchantments;
	}
	
	public static int GetHeldItemID(EntityPlayer player)
	{
		return player.getHeldItem() != null ? player.getHeldItem().itemID : 0;
	}
	
	public static boolean IsThisSlotCrafting(int block, int meta, int slot)
	{
		return Config.craftingSlotIndentities.get(block + ":" + meta) != null && Config.craftingSlotIndentities.get(block + ":" + meta).contains(slot);
	}
	
	public static boolean IsThisSlotSmelting(int block, int meta, int slot)
	{
		return Config.smeltingSlotIdentities.get(block + ":" + meta) != null && Config.smeltingSlotIdentities.get(block + ":" + meta).contains(slot);
	}
	
	public static void AddPlayerToSlotDebugList(String player)
	{
		if (DbStats.playersForSlotDebug == null)
		{
			DbStats.playersForSlotDebug = new ArrayList<String>();
		}
		
		if (!DbStats.playersForSlotDebug.contains(player))
		{
			DbStats.playersForSlotDebug.add(player);
		}
	}
	
	public static void RemovePlayerFromSlotDebugList(String player)
	{
		if (DbStats.playersForSlotDebug != null)
		{
			DbStats.playersForSlotDebug.remove(player);
		}
	}
	
	public static boolean PlayerExistsInSlotDebugginList(String player)
	{
		return DbStats.playersForSlotDebug != null && DbStats.playersForSlotDebug.contains(player);
	}
	
	public static void AddPlayerToNBTDebugList(String player)
	{
		if (DbStats.playersForNBTDebug == null)
		{
			DbStats.playersForNBTDebug = new ArrayList<String>();
		}
		
		if (!DbStats.playersForNBTDebug.contains(player))
		{
			DbStats.playersForNBTDebug.add(player);
		}
	}
	
	public static void RemovePlayerFromNBTDebugList(String player)
	{
		if (DbStats.playersForNBTDebug != null)
		{
			DbStats.playersForNBTDebug.remove(player);
		}
	}
	
	public static boolean PlayerExistsInNBTDebugList(String player)
	{
		return DbStats.playersForNBTDebug != null && DbStats.playersForNBTDebug.contains(player);
	}

    public static String GetLocalisedItemName(ItemStack itemStack, String modID)
    {
        //Attempt to obtain the Localised item name in anyway possible
        //This is a very messy function designed to try and obtain mod items as best it can (For the most part servers can't!)

        String unlocalisedName = itemStack.getUnlocalizedName();
        String unlocalisedNameMinusItem = unlocalisedName.replaceFirst("item.", "");

        String name = itemStack.getUnlocalizedName();

        //For safety : some mods fail to include this method in their blocks/items so it can throw exceptions
        //As for servers, a lot of mods use a proxy that causes these functions to return null
        try {
            name = itemStack.getDisplayName();
        } catch (Exception ex) { }

        if (name.contains(unlocalisedName))
        {
            try {
                name = itemStack.getItem().getItemDisplayName(itemStack);
            } catch (Exception ex) { }
        }

        if (!name.contains(unlocalisedName)) { return name; }

        name = StatCollector.translateToLocal(unlocalisedName + ".name");

        if (!name.equals(unlocalisedName + ".name")) { return name; }

        name = StatCollector.translateToLocal(unlocalisedName);

        if (!name.equals(unlocalisedName)) { return name; }

        name = StatCollector.translateToLocal("item." + unlocalisedName);

        if (!name.equals("item." + unlocalisedName)) { return name; }

        name = StatCollector.translateToLocal("item." + unlocalisedName + ".name");

        if (!name.equals("item." + unlocalisedName + ".name")) { return name; }

        name = StatCollector.translateToLocal("tile." + unlocalisedName);

        if (!name.equals("tile." + unlocalisedName)) { return name; }

        name = StatCollector.translateToLocal("tile." + unlocalisedName + ".name");

        if (!name.equals("tile." + unlocalisedName + ".name")) { return name; }

        if (!modID.isEmpty())
        {
            name = StatCollector.translateToLocal(modID + "." + unlocalisedName);

            if (!name.equals(modID + "." + unlocalisedName)) { return name; }

            name = StatCollector.translateToLocal(modID.toLowerCase() + "." + unlocalisedName);

            if (!name.equals(modID.toLowerCase() + "." + unlocalisedName)) { return name; }
        }

        name = unlocalisedName;

        if (name.isEmpty() || name.contains(unlocalisedName))
        {
            name = LanguageRegistry.instance().getStringLocalization(unlocalisedName);
        }

        if (!name.isEmpty()) { return name; }

        name = LanguageRegistry.instance().getStringLocalization(unlocalisedName + ".name");

        if (!name.isEmpty()) { return name; }

        name = LanguageRegistry.instance().getStringLocalization(unlocalisedNameMinusItem);

        if (!name.isEmpty()) { return name; }

        name = LanguageRegistry.instance().getStringLocalization(unlocalisedNameMinusItem + ".name");

        if (!name.isEmpty()) { return name; }

        if (!modID.isEmpty())
        {
            name = LanguageRegistry.instance().getStringLocalization(modID + "." + unlocalisedName);

            if (!name.isEmpty()) { return name; }

            name = LanguageRegistry.instance().getStringLocalization(modID + "." + unlocalisedName + ".name");

            if (!name.isEmpty()) { return name; }

            name = LanguageRegistry.instance().getStringLocalization(modID.toLowerCase() + "." + unlocalisedName);

            if (!name.isEmpty()) { return name; }

            name = LanguageRegistry.instance().getStringLocalization(modID.toLowerCase() + "." + unlocalisedName + ".name");
        }

        if (!name.isEmpty()) { return name; }

        //If this is reached then no other name matches could be found, so default to unlocalised + .name
        if (name.isEmpty())
        {
            name = itemStack.getUnlocalizedName() + ".name";
        }

        return name;
    }
}
