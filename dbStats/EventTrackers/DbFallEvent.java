package dbStats.EventTrackers;

import dbStats.DbStats;
import dbStats.Util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;

public class DbFallEvent {

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onPlayerFall(LivingFallEvent event)
	{
		if (event.entity instanceof EntityPlayerMP && Utilities.CanTrackPlayer((EntityPlayer) event.entity))
		{
			DbStats.timedPlayerTracker.addDistanceStat(((EntityPlayerMP)event.entity).username, event.distance, "fallen");
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPlayerFallFlyable(PlayerFlyableFallEvent event)
	{
		if (Utilities.CanTrackPlayer(event.entityPlayer))
		{
			DbStats.timedPlayerTracker.addDistanceStat(event.entityPlayer.username, event.distance, "fallen");
		}
	}
}
