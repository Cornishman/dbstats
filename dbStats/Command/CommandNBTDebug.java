package dbStats.Command;

import net.minecraft.command.ICommandSender;
import dbStats.Util.Utilities;

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
