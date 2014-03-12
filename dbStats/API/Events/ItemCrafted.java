package dbStats.API.Events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

@Cancelable
public class ItemCrafted extends Event {
	public final int amount;
	public final ItemStack item;
	public final EntityPlayer player;
	
	public ItemCrafted(EntityPlayer player, ItemStack item, int amount)
	{
		this.amount = amount;
		this.item = item;
		this.player = player;
	}

}
