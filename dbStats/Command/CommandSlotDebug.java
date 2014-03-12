package dbStats.Command;

import net.minecraft.command.ICommandSender;
import dbStats.Util.Utilities;

public class CommandSlotDebug extends Command {

	public CommandSlotDebug() {
		super("dbs_slotdebug");
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
			case "on":
				Utilities.AddPlayerToSlotDebugList(icommandsender.getCommandSenderName());
				sendChat(icommandsender, "Enabled inventory slot debugging.");
				break;
			case "off":
				Utilities.RemovePlayerFromSlotDebugList(icommandsender.getCommandSenderName());
				sendChat(icommandsender, "Disabled inventory slot debugging.");
				break;
			default:
				showUsage(icommandsender);
				break;
			}
		}
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + name + " [on/off]";
	}

}
