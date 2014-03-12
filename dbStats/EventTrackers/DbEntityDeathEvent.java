package dbStats.EventTrackers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.DamageStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.Utilities;

public class DbEntityDeathEvent {

	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void onEntityDeath(LivingDeathEvent event)
	{
		if (!event.isCanceled())
		{
			//Player was killed by something
			if (event.entityLiving instanceof EntityPlayer && Utilities.CanTrackPlayer((EntityPlayer)event.entityLiving))
			{
				EntityPlayer player = (EntityPlayer)event.entityLiving;
				MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "Deaths", player.username, 1, true)));
				
				String how = "", what = "", enchantments = "", nbt = "";
				int itemId = 0, itemMeta = 0;
				
				how = event.source.damageType;
				
				if (event.source.getEntity() != null)
				{
					what = Utilities.GetMobName(event.source.getEntity());
					
					if (event.source.getEntity().getLastActiveItems() != null && event.source.getEntity().getLastActiveItems().length > 0
							&& event.source.getEntity().getLastActiveItems()[0] != null)
					{
						itemId = event.source.getEntity().getLastActiveItems()[0].itemID;
						enchantments = Utilities.GetEnchantments(event.source.getEntity().getLastActiveItems()[0]);
						nbt = Utilities.GetItemNBT(event.source.getEntity().getLastActiveItems()[0]);
						itemMeta = Utilities.GetItemMetaDataValue(event.source.getEntity().getLastActiveItems()[0]);
					}
				}
				
				MinecraftForge.EVENT_BUS.post(new EStatistic(new DamageStatistic("EntityDamages", "", player.username, how, what, itemId, itemMeta, enchantments, 1, nbt, "death")));
			}
			
			//Player killed something
			if (event.source.getEntity() instanceof EntityPlayer || event.source.getSourceOfDamage() instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)(event.source.getEntity() instanceof EntityPlayer ? event.source.getEntity() : event.source.getSourceOfDamage());
				if (Utilities.CanTrackPlayer(player))
				{
					MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "Kills", player.username, 1, true)));
					
					String how = "", what = "", enchantments = "", nbt = "";
					int itemId = 0, itemMeta = 0;
					
					how = event.source.damageType;
					
					if (event.entity != null)
					{
						what = Utilities.GetMobName(event.entity);
						
						if (player.getHeldItem() != null)
						{
							itemId = player.getHeldItem().itemID;
							
							enchantments = Utilities.GetEnchantments(player.getHeldItem());
							nbt = Utilities.GetItemNBT(player.getHeldItem());
							itemMeta = Utilities.GetItemMetaDataValue(player.getHeldItem());
						}
					}
					
					MinecraftForge.EVENT_BUS.post(new EStatistic(new DamageStatistic("EntityDamages", "", player.username, how, what, itemId, itemMeta, enchantments, 1, nbt, "kill")));
				}
			}
		}
	}
}
