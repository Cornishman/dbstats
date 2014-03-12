package dbStats.EventTrackers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import dbStats.Config;
import dbStats.API.Events.PlayerBlockBreak;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;

public class DbBlockBreakEvent {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerBlockBreak(PlayerBlockBreak event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player))
		{
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "BlocksBroken", event.player.username, 1, true)));
			
			int blockId = event.world.getBlockId(event.blockX, event.blockY, event.blockZ);
			int blockMeta = Config.blocksWithMetaData.contains(blockId) ? event.world.getBlockMetadata(event.blockX, event.blockY, event.blockZ) : 0;
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, blockId, blockMeta, 1, "", "break")));
		}
	}
}
