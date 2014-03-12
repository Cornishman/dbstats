package dbStats.EventTrackers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import dbStats.API.Events.PlayerBlockPlace;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;

public class DbBlockPlaceEvent {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerBlockPlace(PlayerBlockPlace event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player) && event.itemStack != null)
		{
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "BlocksPlaced", event.player.username, 1, true)));
			
			int blockId = event.itemStack.itemID;
			int blockMeta = Utilities.GetItemMetaDataValue(event.itemStack);
			String nbt = Utilities.GetItemNBT(event.itemStack);
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, blockId, blockMeta, 1, nbt, "place")));
		}
	}
}
