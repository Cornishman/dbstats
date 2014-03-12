package dbStats.EventTrackers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent;
import dbStats.DbStats;
import dbStats.Util.Utilities;

public class DbMoveEvent {
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onPlayerMove(LivingEvent.LivingUpdateEvent event)
	{
		if (!event.isCanceled())
		{
			if (event.entity instanceof EntityPlayer && Utilities.CanTrackPlayer((EntityPlayer) event.entity))
			{
				EntityPlayer player = (EntityPlayer)event.entity;
				
				if (player.isEntityAlive() && player.ticksExisted > 50)	//Ticks existed, prevent spawn counting to movement
				{
					double d = player.getDistance(player.field_71091_bM, player.field_71096_bN, player.field_71097_bO);
					d = Utilities.Round(d, 2);
					if (d < 10 && d > 0) {
						String movementMethod = "";
						
						if (player.isInWater() && !player.isRiding())
						{
							movementMethod = "swam";
						}
						else if (player.isOnLadder())
						{
							movementMethod = "climbed";
						}
						else if (player.isSprinting() && !player.capabilities.isFlying)
						{
							movementMethod = "sprinted";
						}
						else if (player.isSneaking() && !player.capabilities.isFlying)
						{
							movementMethod = "sneaked";
						}
						else if (player.isRiding() && player.ridingEntity != null)
						{
							movementMethod = player.ridingEntity.getEntityName().toLowerCase();
						}
						else if (player.capabilities.isFlying || !player.onGround && player.fallDistance == 0)
						{
							movementMethod = "flown";
						}
						else if (!player.capabilities.isFlying && player.fallDistance == 0 && player.onGround)
						{
							//Assume walking
							movementMethod = "walked";
						}
						
						if (!movementMethod.isEmpty())
						{
							DbStats.timedPlayerTracker.addDistanceStat(player.username, d, movementMethod);
						}
					}
				}
			}
		}
	}
}
