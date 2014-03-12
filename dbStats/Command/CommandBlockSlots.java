package dbStats.Command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import dbStats.Config;
import dbStats.DbStats;

public class CommandBlockSlots extends Command {

	public CommandBlockSlots() {
		super("dbs_slot");
	}
	
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (astring.length <= 1)
		{
			showUsage(icommandsender);
			return;
		}
		else
		{
			switch(astring[0].toLowerCase())
			{
			case "add":
			{
				try {
					String slotType = astring[1].toLowerCase();
					int blockId = Integer.parseInt(astring[2].split(":")[0]);
					int blockMeta = Integer.parseInt(astring[2].split(":")[1]);
					List<Integer> slots = new ArrayList<Integer>();
					for(String slot : astring[2].split(":")[2].split(","))
					{
						slots.add(Integer.parseInt(slot));
					}
					
					if (slots.size() == 0 || blockId < 0 || blockMeta < 0 || (!slotType.equals("craft") && !slotType.equals("smelt")))
					{
						showUsage(icommandsender);
						return;
					}
					
					String slotString = "";
					for(int slot : slots)
					{
						slotString += slot + ",";
					}
					
					if (slotString.length() > 0)
					{
						slotString = slotString.substring(0, slotString.length() - 1);
					}
					
					if (slotType.equals("craft"))
					{
						if (Config.craftingSlotIndentities.get(blockId + ":" + blockMeta) == null)
						{
							Config.craftingSlotIndentities.put(blockId + ":" + blockMeta, slots);
						}
						else
						{
							Config.craftingSlotIndentities.get(blockId + ":" + blockMeta).removeAll(slots);	//Remove any matches first to prevent duplicates
							Config.craftingSlotIndentities.get(blockId + ":" + blockMeta).addAll(slots);	//Then add them all back again
						}
						sendChat(icommandsender, "Added " + blockId + ":" + blockMeta + ":" + slotString + " to crafting slots.");
					}
					else
					{
						if (Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta) == null)
						{
							Config.smeltingSlotIdentities.put(blockId + ":" + blockMeta, slots);
						}
						else
						{
							Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta).removeAll(slots);	//Remove any matches first to prevent duplicates
							Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta).addAll(slots);	//Then add them all back again
						}
						sendChat(icommandsender, "Added " + blockId + ":" + blockMeta + ":" + slotString + " to smelting slots.");
					}
					
				} catch (Exception e)
				{
					showUsage(icommandsender);
					return;
				}
			}
				break;
			case "del":
			{
				try {
					String slotType = astring[1].toLowerCase();
					int blockId = Integer.parseInt(astring[2].split(":")[0]);
					int blockMeta = Integer.parseInt(astring[2].split(":")[1]);
					List<Integer> slots = new ArrayList<Integer>();
					if (astring[2].split(":").length > 2)
					{
						for(String slot : astring[2].split(":")[2].split(","))
						{
							slots.add(Integer.parseInt(slot));
						}
					}
					
					if (blockId < 0 || blockMeta < 0 || (!slotType.equals("craft") && !slotType.equals("smelt")))
					{
						showUsage(icommandsender);
						return;
					}
					
					String slotString = "";
					for(int slot : slots)
					{
						slotString += slot + ",";
					}
					
					if (slotString.length() > 0)
					{
						slotString = slotString.substring(0, slotString.length() - 1);
					}
					
					if (slotType.equals("craft"))
					{
						if (Config.craftingSlotIndentities.get(blockId + ":" + blockMeta) == null)
						{
							sendChat(icommandsender, "Could not delete crafting slot " + blockId + ":" + blockMeta + ":" + slotString + " - Not found.");
						}
						else
						{
							if (slotString.isEmpty())
							{
								Config.craftingSlotIndentities.remove(blockId + ":" + blockMeta);
								sendChat(icommandsender, "Removed all slots for blockId " + blockId + ":" + blockMeta + " from the list of crafting blocks.");
							}
							else
							{
								Config.craftingSlotIndentities.get(blockId + ":" + blockMeta).removeAll(slots);
								if (Config.craftingSlotIndentities.get(blockId + ":" + blockMeta).isEmpty())
								{
									Config.craftingSlotIndentities.remove(blockId + ":" + blockMeta);
									sendChat(icommandsender, "Removed all slots for blockId " + blockId + ":" + blockMeta + " from the list of crafting blocks.");
								}
								else
								{
									sendChat(icommandsender, "Removed slot(s) " + slotString + " for blockId " + blockId + ":" + blockMeta + " from the list of crafting blocks.");
								}
							}
						}
					}
					else
					{
						if (Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta) == null)
						{
							sendChat(icommandsender, "Could not delete smelting slot " + blockId + ":" + blockMeta + ":" + slotString + " - Not found.");
						}
						else
						{
							if (slotString.isEmpty())
							{
								Config.smeltingSlotIdentities.remove(blockId + ":" + blockMeta);
								sendChat(icommandsender, "Removed all slots for blockId " + blockId + ":" + blockMeta + " from the list of smelting blocks.");
							}
							else
							{
								Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta).removeAll(slots);
								if (Config.smeltingSlotIdentities.get(blockId + ":" + blockMeta).isEmpty())
								{
									Config.smeltingSlotIdentities.remove(blockId + ":" + blockMeta);
									sendChat(icommandsender, "Removed all slots for blockId " + blockId + ":" + blockMeta + " from the list of smelting blocks.");
								}
								else
								{
									sendChat(icommandsender, "Removed slots " + slotString + " for blockId " + blockId + ":" + blockMeta + " from the list of smelting blocks.");
								}
							}
						}
					}
					
				} catch (Exception e)
				{
					showUsage(icommandsender);
					return;
				}
			}
				break;
			default:
				showUsage(icommandsender);
				return;
			}
			
			DbStats.config.save();	//Save any changes
		}
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + name + " [add/del] [craft/smelt] [id:meta:slot]";
	}

}
