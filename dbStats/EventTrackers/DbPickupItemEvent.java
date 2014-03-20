package dbStats.EventTrackers;

import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.NBTUtil;
import dbStats.Util.Utilities;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class DbPickupItemEvent {

	@ForgeSubscribe(priority = EventPriority.HIGH)
	public void onPickupItem(EntityItemPickupEvent event)
	{
		if (!event.isCanceled() && Utilities.CanTrackPlayer(event.entityPlayer))
		{
			int itemId = event.item.getEntityItem().itemID;
			int itemMeta = Utilities.GetItemMetaDataValue(event.item.getEntityItem());
			String nbt = Utilities.GetItemNBT(event.item.getEntityItem());
			int amount = event.item.getEntityItem().stackSize > 0 ? event.item.getEntityItem().stackSize : 1;
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic(0, 0, "players", "ItemsPickedUp", event.entityPlayer.username, amount, true)));
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic(0, 0, "bistats", "total", event.entityPlayer.username, itemId, itemMeta, amount, nbt, "pickup")));
			
			if (Utilities.PlayerExistsInNBTDebugList(event.entityPlayer.username))
			{
				//Display a chat message to the player detailing the NBT info
				ChatMessageComponent cmc = new ChatMessageComponent();
				cmc.addText(NBTUtil.GetNBTDataForDebug(itemId, itemMeta, event.item.getEntityItem().getTagCompound()));
				cmc.setColor(EnumChatFormatting.YELLOW);
				event.entityPlayer.sendChatToPlayer(cmc);
			}
		}
	}
}
