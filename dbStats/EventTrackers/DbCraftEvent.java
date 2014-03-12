package dbStats.EventTrackers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import dbStats.API.Events.ItemCrafted;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;

public class DbCraftEvent {
	
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onCraft(ItemCrafted event)
	{
		if (event.amount > 0 && Utilities.CanTrackPlayer(event.player))
		{
			int itemMeta = Utilities.GetItemMetaDataValue(event.item);
			String nbt = Utilities.GetItemNBT(event.item);
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "ItemsCrafted", event.player.username, event.amount, true)));
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, event.item.itemID, itemMeta, event.amount, nbt, "craft")));
		}
	}
}
