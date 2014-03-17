package dbStats.EventTrackers;

import dbStats.API.Events.ItemCrafted;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import dbStats.Util.ChatFormat;
import dbStats.Util.Utilities;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

public class DbCraftEvent {
	
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onCraft(ItemCrafted event)
	{
		if (event.amount > 0 && Utilities.CanTrackPlayer(event.player))
		{
            if (Utilities.PlayerExistsInSlotDebugginList(event.player.username))
            {
                ChatMessageComponent cmc = new ChatMessageComponent();
                cmc.addText(ChatFormat.YELLOW.toString());
                cmc.addText("[*]");
                cmc.addText(ChatFormat.GREEN.toString());
                cmc.addText(" Crafting event detected");
                cmc.addText(ChatFormat.RED.toString());
                cmc.addText(" - It's highly recommended not to add this slot to the config!");
                event.player.sendChatToPlayer(cmc);
            }

			int itemMeta = Utilities.GetItemMetaDataValue(event.item);
			String nbt = Utilities.GetItemNBT(event.item);
			
			MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "ItemsCrafted", event.player.username, event.amount, true)));
			MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, event.item.itemID, itemMeta, event.amount, nbt, "craft")));
		}
	}
}
