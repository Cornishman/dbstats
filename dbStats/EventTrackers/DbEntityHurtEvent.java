package dbStats.EventTrackers;

import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.DamageStatistic;
import dbStats.Util.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class DbEntityHurtEvent {
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onLivingHurt(LivingHurtEvent event)
	{
		if (event.entity instanceof EntityPlayerMP && Utilities.CanTrackPlayer((EntityPlayer)event.entity))
		{
			Entity damager = event.source.getSourceOfDamage() instanceof EntityLiving ? event.source.getSourceOfDamage() : event.source.getEntity() instanceof EntityLiving ? event.source.getEntity() : null;
			int itemId = 0, itemMeta = 0;
			String enchantments = "", nbt = "";
			
			if (damager != null && damager.getLastActiveItems() != null && damager.getLastActiveItems().length > 0 && damager.getLastActiveItems()[0] != null)
			{
				itemId = damager.getLastActiveItems()[0].itemID;
				enchantments = Utilities.GetEnchantments(damager.getLastActiveItems()[0]);
				nbt = Utilities.GetItemNBT(damager.getLastActiveItems()[0]);
				itemMeta = Utilities.GetItemMetaDataValue(damager.getLastActiveItems()[0]);
			}
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new DamageStatistic(0, 0, "EntityDamages", "", Utilities.GetMobName(event.entity), event.source.damageType, Utilities.GetMobName(damager),
					itemId, itemMeta, enchantments, event.ammount, nbt, "hurt")));
		}
	}

}
