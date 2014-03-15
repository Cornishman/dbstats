package dbStats.Events;

import java.util.logging.Level;

import dbStats.API.Statistics.EStatistic;
import dbStats.Statistics.BlockItemStatistic;
import dbStats.Statistics.PlayerStatistic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ICraftingHandler;
import dbStats.DbStats;
import dbStats.Util.Utilities;
import net.minecraftforge.common.MinecraftForge;

public class DbStatsCraftingHandler implements ICraftingHandler{

	@Override
	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) {
		//This does not work nicely (Item.stacksize is always 0 on shift click and on normal click it's stacksize = stacksize + whatever is on mouse.count)

        //Applied Energistics
        if (player instanceof EntityPlayerMP && Utilities.CanTrackPlayer(player))
        {
            if (craftMatrix.getClass().toString().equals("class appeng.common.AppEngInternalInventory"))
            {
//                DbStats.log.log(Level.INFO, "Craft - " + item.itemID + " x " + item.stackSize);

                int itemMeta = Utilities.GetItemMetaDataValue(item);
                String nbt = Utilities.GetItemNBT(item);

                MinecraftForge.EVENT_BUS.post(new EStatistic(new PlayerStatistic("players", "ItemsCrafted", player.username, item.stackSize, true)));
                MinecraftForge.EVENT_BUS.post(new EStatistic(new BlockItemStatistic("bistats", "total", player.username, item.itemID, itemMeta, item.stackSize, nbt, "craft")));
            }
        }
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {
		if (player instanceof EntityPlayerMP && item.stackSize > 0 && Utilities.CanTrackPlayer(player))
		{
			DbStats.log.log(Level.INFO, "Smelt - " + item.itemID + " x " + item.stackSize);
		}
	}

}
