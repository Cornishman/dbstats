package dbStats.Command;

import dbStats.Config;
import dbStats.DbStats;
import dbStats.EventTrackers.*;
import dbStats.Util.ChatFormat;
import dbStats.Util.TableFormatter;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;

public class CommandTracker extends Command {

	public CommandTracker() {
		super("dbs_tracking");
	}
	
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (astring.length <= 0)
		{
			showUsage(icommandsender);
		}
		else
		{
			switch(astring[0].toLowerCase())
			{
			case "status":
				TableFormatter tf = new TableFormatter(icommandsender);
				StringBuilder sb = tf.sb;
				sb.append(ChatFormat.YELLOW + "-------- Trackers --------").append("\n");
				tf.heading("  -Tracker-").heading(" -Status-");
				tf.row("Attack").row(getTrackerStatus(Config.enableAttackLogging));
				tf.row("Hurt").row(getTrackerStatus(Config.enableHurtLogging));
				tf.row("BlockBreak").row(getTrackerStatus(Config.enableBlockBreakLogging));
				tf.row("BlockPlace").row(getTrackerStatus(Config.enableBlockPlaceLogging));
				tf.row("ItemPickup").row(getTrackerStatus(Config.enableItemPickupLogging));
				tf.row("ItemDrop").row(getTrackerStatus(Config.enableItemDropLogging));
				tf.row("Kills-Deaths").row(getTrackerStatus(Config.enableDeathKillsLogging));
				tf.row("Crafting").row(getTrackerStatus(Config.enableCraftingLogging));
				tf.row("Movement").row(getTrackerStatus(Config.enableMovementLogging));
				tf.finishTable();
				sendChat(icommandsender, tf.toString());
				break;
			case "enable":
				if (astring.length <= 1)
				{
					showUsage(icommandsender);
				}
				else
				{
					switch(astring[1].toLowerCase())
					{
					case "attack":
						if (!Config.enableAttackLogging || DbStats.playerAttackTracker == null)
						{
							Config.enableAttackLogging = true;
							DbStats.config.save();
							DbStats.playerAttackTracker = new DbEntityAttackEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerAttackTracker);
							sendChat(icommandsender, "Enabled the Attack tracker");
						}
						break;
					case "hurt":
						if (!Config.enableHurtLogging || DbStats.playerHurtTracker == null)
						{
							Config.enableHurtLogging = false;
							DbStats.config.save();
							DbStats.playerHurtTracker = new DbEntityHurtEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerHurtTracker);
							sendChat(icommandsender, "Enabled the Hurt tracker");
						}
						break;
					case "blockbreak":
						if (!Config.enableBlockBreakLogging || DbStats.playerBlockBreakTracker == null)
						{
							Config.enableBlockBreakLogging = false;
							DbStats.config.save();
							DbStats.playerBlockBreakTracker = new DbBlockBreakEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerBlockBreakTracker);
							sendChat(icommandsender, "Enabled the BlockBreak tracker");
						}
						break;
					case "blockplace":
						if (!Config.enableBlockPlaceLogging || DbStats.playerBlockPlaceTracker == null)
						{
							Config.enableBlockPlaceLogging = true;
							DbStats.config.save();
							DbStats.playerBlockPlaceTracker = new DbBlockPlaceEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerBlockPlaceTracker);
							sendChat(icommandsender, "Enabled the BlockPlace tracker");
						}
						break;
					case "itempickup":
						if (!Config.enableItemPickupLogging || DbStats.playerPickupItemTracker == null)
						{
							Config.enableItemPickupLogging = true;
							DbStats.config.save();
							DbStats.playerPickupItemTracker = new DbPickupItemEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerPickupItemTracker);
							sendChat(icommandsender, "Enabled the ItemPickup tracker");
						}
						break;
					case "itemdrop":
						if (!Config.enableItemDropLogging || DbStats.playerDropItemTracker == null)
						{
							Config.enableItemDropLogging = true;
							DbStats.config.save();
							DbStats.playerDropItemTracker = new DbDropItemEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerDropItemTracker);
							sendChat(icommandsender, "Enabled the ItemDrop tracker");
						}
						break;
					case "kills-deaths":
						if (!Config.enableDeathKillsLogging || DbStats.playerDeathKillTracker == null)
						{
							Config.enableDeathKillsLogging = true;
							DbStats.config.save();
							DbStats.playerDeathKillTracker = new DbEntityDeathEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerDeathKillTracker);
							sendChat(icommandsender, "Enabled the Kills-Deaths tracker");
						}
						break;
					case "crafting":
						if (!Config.enableCraftingLogging || DbStats.playerCraftingTracker == null)
						{
							Config.enableCraftingLogging = true;
							DbStats.config.save();
							DbStats.playerCraftingTracker = new DbCraftEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerCraftingTracker);
							DbStats.playerPickupSlotTracker = new DbPickupFromSlotEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerPickupSlotTracker);
							sendChat(icommandsender, "Enabled the Crafting tracker");
						}
						break;
					case "movement":
						if (!Config.enableMovementLogging || DbStats.playerMoveTracker == null)
						{
							Config.enableMovementLogging = true;
							DbStats.config.save();
							DbStats.playerMoveTracker = new DbMoveEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerMoveTracker);
							DbStats.playerFallTracker = new DbFallEvent();
							MinecraftForge.EVENT_BUS.register(DbStats.playerFallTracker);
							sendChat(icommandsender, "Enabled the Movement tracker");
						}
						break;
					default:
						showUsage(icommandsender);
						break;
					}
				}
				break;
			case "disable":
				if (astring.length <= 1)
				{
					showUsage(icommandsender);
				}
				else
				{
					switch(astring[1].toLowerCase())
					{
					case "attack":
						if (Config.enableAttackLogging || DbStats.playerAttackTracker != null)
						{
							Config.enableAttackLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerAttackTracker);
							DbStats.playerAttackTracker = null;
							sendChat(icommandsender, "Disabled the Attack tracker");
						}
						break;
					case "hurt":
						if (Config.enableHurtLogging || DbStats.playerHurtTracker != null)
						{
							Config.enableHurtLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerHurtTracker);
							DbStats.playerHurtTracker = null;
							sendChat(icommandsender, "Disabled the Hurt tracker");
						}
						break;
					case "blockbreak":
						if (Config.enableBlockBreakLogging || DbStats.playerBlockBreakTracker != null)
						{
							Config.enableBlockBreakLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerBlockBreakTracker);
							DbStats.playerBlockBreakTracker = null;
							sendChat(icommandsender, "Disabled the BlockBreak tracker");
						}
						break;
					case "blockplace":
						if (Config.enableBlockPlaceLogging || DbStats.playerBlockPlaceTracker != null)
						{
							Config.enableBlockPlaceLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerBlockPlaceTracker);
							DbStats.playerBlockPlaceTracker = null;
							sendChat(icommandsender, "Disabled the BlockPlace tracker");
						}
						break;
					case "itempickup":
						if (Config.enableItemPickupLogging || DbStats.playerPickupItemTracker != null)
						{
							Config.enableItemPickupLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerPickupItemTracker);
							DbStats.playerPickupItemTracker = null;
							sendChat(icommandsender, "Disabled the ItemPickup tracker");
						}
						break;
					case "itemdrop":
						if (Config.enableItemDropLogging || DbStats.playerDropItemTracker != null)
						{
							Config.enableItemDropLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerDropItemTracker);
							DbStats.playerDropItemTracker = null;
							sendChat(icommandsender, "Disabled the ItemDrop tracker");
						}
						break;
					case "kills-deaths":
						if (Config.enableDeathKillsLogging || DbStats.playerDeathKillTracker != null)
						{
							Config.enableDeathKillsLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerDeathKillTracker);
							DbStats.playerDeathKillTracker = null;
							sendChat(icommandsender, "Disabled the Kills-Deaths tracker");
						}
						break;
					case "crafting":
						if (Config.enableCraftingLogging || DbStats.playerCraftingTracker != null)
						{
							Config.enableCraftingLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerCraftingTracker);
							DbStats.playerCraftingTracker = null;
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerPickupSlotTracker);
							DbStats.playerPickupSlotTracker = null;
							sendChat(icommandsender, "Disabled the Crafting tracker");
						}
						break;
					case "movement":
						if (Config.enableMovementLogging || DbStats.playerMoveTracker != null)
						{
							Config.enableMovementLogging = false;
							DbStats.config.save();
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerMoveTracker);
							DbStats.playerMoveTracker = null;
							MinecraftForge.EVENT_BUS.unregister(DbStats.playerFallTracker);
							DbStats.playerFallTracker = null;
							sendChat(icommandsender, "Disabled the Movement tracker");
						}
						break;
					default:
						showUsage(icommandsender);
						break;
					}
				}
				break;
			default:
				showUsage(icommandsender);
				break;
			}
		}
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + name + " [enable/disable/status] [tracker]";
	}
	
	private String getTrackerStatus(boolean on)
	{
		return on ? ChatFormat.GREEN + " [On]" : ChatFormat.RED + " [Off]";
	}

}
