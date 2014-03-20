package dbStats.EventTrackers;

import dbStats.API.Events.PlayerBlockBreak;
import dbStats.API.Statistics.EStatistic;
import dbStats.Config;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

public class DbBlockBreakEvent {

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void onPlayerHarvestBlock(BlockEvent.HarvestDropsEvent event)
    {
        if (event.block != null && !event.isCanceled() && Utilities.CanTrackPlayer(event.harvester))
        {
            int blockMeta = Config.blocksWithMetaData.contains(event.block.blockID) ? event.blockMetadata : 0;

            int hash = ("players:BlocksBroken:" + event.harvester.username + ":" + 1 + ":" + event.block.blockID + ":" + blockMeta + ":" + event.x + "" + event.y + "" + event.z).hashCode();

            MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic(hash, 1, "players", "BlocksBroken", event.harvester.username, 1, true)));
            MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic(hash, 1, "bistats", "total", event.harvester.username, event.block.blockID, blockMeta, 1, "", "break")));
        }
    }

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerBlockBreak(PlayerBlockBreak event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player))
		{
			int blockId = event.world.getBlockId(event.blockX, event.blockY, event.blockZ);
			int blockMeta = Config.blocksWithMetaData.contains(blockId) ? event.world.getBlockMetadata(event.blockX, event.blockY, event.blockZ) : 0;

            int hash = ("players:BlocksBroken:" + event.player.username + ":" + 1 + ":" + blockId + ":" + blockMeta + ":" + event.blockX + "" + event.blockY + "" + event.blockZ).hashCode();

            MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic(hash, 0, "players", "BlocksBroken", event.player.username, 1, true)));
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic(hash, 0, "bistats", "total", event.player.username, blockId, blockMeta, 1, "", "break")));
		}
	}
}
