package dbStats.EventTrackers;

import dbStats.Util.ErrorUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import dbStats.Config;
import dbStats.API.Events.PlayerBlockBreak;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;
import net.minecraftforge.event.world.BlockEvent;

public class DbBlockBreakEvent {

    @ForgeSubscribe(priority = EventPriority.LOWEST)
    public void onPlayerHarvestBlock(BlockEvent.HarvestDropsEvent event)
    {
        //TODO : Add new priority system to EStatistic that will ignore if the stat matches a recent stat but with a lower priority
        // this would allow for the multiple break events to be triggered by breaking a block but to only acknowledge 1
        // Better yet, compare the block x/y/z/ coords against each other, if they match, take the highest priority!
        if (event.harvester != null && event.block != null)
        {
            ErrorUtil.LogMessage(event.harvester.getEntityName() + " broke block " + event.block.blockID + ":" + event.blockMetadata);
        }
    }

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerBlockBreak(PlayerBlockBreak event)
	{
        if (event.player != null) { ErrorUtil.LogMessage(event.player.getEntityName()); }

		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.player))
		{
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "BlocksBroken", event.player.username, 1, true)));
			
			int blockId = event.world.getBlockId(event.blockX, event.blockY, event.blockZ);
			int blockMeta = Config.blocksWithMetaData.contains(blockId) ? event.world.getBlockMetadata(event.blockX, event.blockY, event.blockZ) : 0;
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, blockId, blockMeta, 1, "", "break")));
		}
	}
}
