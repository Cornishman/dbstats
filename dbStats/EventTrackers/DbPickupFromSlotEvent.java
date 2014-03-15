package dbStats.EventTrackers;

import java.lang.reflect.Field;

import dbStats.Util.TileEntityUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import dbStats.API.Events.PickupFromSlot;
import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Util.Utilities;

public class DbPickupFromSlotEvent {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onPickupFromSlot(PickupFromSlot event)
	{
		if (event.player != null && event.itemStack != null && event.slot != null && event.player instanceof EntityPlayerMP 
				&& Utilities.CanTrackPlayer(event.player) && !(event.slot.inventory instanceof InventoryPlayer))
		{
            TileEntityUtil.Block modBlock = TileEntityUtil.GetModBlock(event.slot.inventory);

			int blockId = modBlock.ID;
			int blockMeta = modBlock.Meta;
			
			if (Utilities.IsThisSlotCrafting(blockId, blockMeta, event.slot.slotNumber))
			{
				MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, event.itemStack.itemID, Utilities.GetItemMetaDataValue(event.itemStack),
						event.itemStack.stackSize, Utilities.GetItemNBT(event.itemStack), "craft")));
			}
			else if (Utilities.IsThisSlotSmelting(blockId, blockMeta, event.slot.slotNumber)){
				MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", event.player.username, event.itemStack.itemID, Utilities.GetItemMetaDataValue(event.itemStack),
						event.itemStack.stackSize, Utilities.GetItemNBT(event.itemStack), "smelt")));
			}
			
			if (Utilities.PlayerExistsInSlotDebugginList(event.player.username))
			{
				ChatMessageComponent cmc = new ChatMessageComponent();
				cmc.setColor(EnumChatFormatting.YELLOW);
				if (Utilities.IsThisSlotCrafting(blockId, blockMeta, event.slot.slotNumber) || Utilities.IsThisSlotSmelting(blockId, blockMeta, event.slot.slotNumber))
				{
					cmc.addText("[*]");
					cmc.setColor(EnumChatFormatting.GREEN);
				}
				cmc.addText("You picked up - " + event.itemStack.stackSize + "x" + event.itemStack.getDisplayName() + ", from slot " + event.slot.slotNumber + " in blockId " + blockId + ":" + blockMeta);
				event.player.sendChatToPlayer(cmc);
			}
		}
	}
}
