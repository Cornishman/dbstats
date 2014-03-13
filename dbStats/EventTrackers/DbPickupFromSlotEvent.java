package dbStats.EventTrackers;

import java.lang.reflect.Field;

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
			int blockId = 0;
			int blockMeta = 0;
			if (event.slot.inventory instanceof TileEntity)
			{
				TileEntity te = (TileEntity) event.slot.inventory;
				blockId = te.blockType != null ? te.blockType.blockID : 0;
				blockId = blockId <= 0 ? te.worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord) : blockId;
				blockMeta = te.blockMetadata <= 0 ? te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) : te.blockMetadata;
			}
			else
			{
				//Not tile entity (Mod item?) try and work out what
				try{
					//Forestry mod
					Field forestryTile = event.slot.inventory.getClass().getDeclaredField("tile");
					forestryTile.setAccessible(true);
					if (forestryTile != null)
					{
						TileEntity te = (TileEntity)forestryTile.get(event.slot.inventory);
						blockId = te.blockType != null ? te.blockType.blockID : 0;
						blockId = blockId <= 0 ? te.worldObj.getBlockId(te.xCoord, te.yCoord, te.zCoord) : blockId;
						blockMeta = te.blockMetadata <= 0 ? te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) : te.blockMetadata;
					}
				}
				catch (Exception e)
				{}	//Do nothing
			}
			
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
