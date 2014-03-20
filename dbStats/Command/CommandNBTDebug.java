package dbStats.Command;

import dbStats.Util.Utilities;
import net.minecraft.command.ICommandSender;

public class CommandNBTDebug extends Command {

	public CommandNBTDebug() {
		super("dbs_nbtdebug");
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
				Utilities.AddPlayerToNBTDebugList(icommandsender.getCommandSenderName());
				sendChat(icommandsender, "Enabled item NBT debugging.");
				break;
			case "off":
				Utilities.RemovePlayerFromNBTDebugList(icommandsender.getCommandSenderName());
				sendChat(icommandsender, "Disabled item NBT debugging.");
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
