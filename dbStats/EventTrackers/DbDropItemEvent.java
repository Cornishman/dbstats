package dbStats.EventTrackers;

import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemTossEvent;

public class DbDropItemEvent {
	
	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void onDropItem(ItemTossEvent event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player))
		{
			int itemId = event.entityItem.getEntityItem().itemID;
			int itemMeta = Utilities.GetItemMetaDataValue(event.entityItem.getEntityItem());
			String nbt = Utilities.GetItemNBT(event.entityItem.getEntityItem());
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic(0, 0, "players", "ItemsDropped", event.player.username, event.entityItem.getEntityItem().stackSize, true)));
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic(0, 0, "bistats", "total", event.player.username, itemId, itemMeta, event.entityItem.getEntityItem().stackSize, nbt, "drop")));
		}
	}
}
