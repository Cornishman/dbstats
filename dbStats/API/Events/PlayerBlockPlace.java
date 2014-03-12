package dbStats.API.Events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

@Cancelable
public class PlayerBlockPlace extends Event {
	public final World world;
	  public final int blockX;
	  public final int blockY;
	  public final int blockZ;
	  public final EntityPlayer player;
	  public final ItemStack itemStack;

	  public PlayerBlockPlace(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z)
	  {
	    this.world = world;
	    this.blockX = x;
	    this.blockY = y;
	    this.blockZ = z;
	    this.player = player;
	    this.itemStack = itemStack;
	  }
}
