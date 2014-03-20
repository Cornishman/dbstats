package dbStats.EventTrackers;

import dbStats.API.Events.PlayerBlockPlace;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

public class DbBlockPlaceEvent {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerBlockPlace(PlayerBlockPlace event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player) && event.itemStack != null)
		{
            if (event.itemStack.getItem() instanceof ItemBlock || event.itemStack.getItem() instanceof ItemBlockWithMetadata)
            {
//                ErrorUtil.LogMessage("This item is a block!");

                MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic(0, 0, "players", "BlocksPlaced", event.player.username, 1, true)));

                int blockId = event.itemStack.itemID;
                int blockMeta = Utilities.GetItemMetaDataValue(event.itemStack);
                String nbt = Utilities.GetItemNBT(event.itemStack);

                MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic(0, 0, "bistats", "total", event.player.username, blockId, blockMeta, 1, nbt, "place")));
            }
		}
	}
}
