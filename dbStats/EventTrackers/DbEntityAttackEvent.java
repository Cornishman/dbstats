package dbStats.EventTrackers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.DamageStatistic;
import dbStats.Util.Utilities;

public class DbEntityAttackEvent {
	
	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void OnEntityAttackEvent(LivingAttackEvent event)
	{
//		if (event.source.getEntity() instanceof EntityPlayer || event.source.getSourceOfDamage() instanceof EntityPlayer && !event.isCanceled())
//		{
//			ErrorUtil.LogMessage("PLayer did it");
//		}
		if (event.source.getEntity() instanceof EntityPlayerMP || event.source.getSourceOfDamage() instanceof EntityPlayerMP && !event.isCanceled())
		{
			EntityPlayerMP player = (EntityPlayerMP) (event.source.getSourceOfDamage() instanceof EntityPlayerMP ? event.source.getSourceOfDamage() : event.source.getEntity());
			if (Utilities.CanTrackPlayer(player))
			{
				MinecraftForge.EVENT_BUS.post(new EStatistic(new DamageStatistic("EntityDamages", "", player.username, event.source.damageType,  Utilities.GetMobName(event.entity), 
						Utilities.GetHeldItemID(player), Utilities.GetItemMetaDataValue(player.getHeldItem()), Utilities.GetEnchantments(player.getHeldItem()), event.ammount, 
						Utilities.GetItemNBT(player.getHeldItem()), "damage")));
			}
		}
	}

}
