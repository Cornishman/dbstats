package dbStats.Command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

public class Command extends CommandBase {
	public final String name;
	
	public Command(String name)
	{
		this.name = name;
	}
	
	public static void sendChat(ICommandSender commandSender, String message) {
		sendChat(commandSender, message, EnumChatFormatting.YELLOW);
    }
	
	public static void sendChat(ICommandSender commandSender, String message, EnumChatFormatting color)
	{
		while (message != null) {
        	ChatMessageComponent cmc = new ChatMessageComponent();
        	cmc.setColor(color);
        	
            int nlIndex = message.indexOf('\n');
            if (nlIndex == -1) {
                cmc.addText(message);
                message = null;
            }
            else {
                cmc.addText(message.substring(0, nlIndex));
                message = message.substring(nlIndex + 1);
            }
            commandSender.sendChatToPlayer(cmc);
        }
	}
	
	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return null;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
	}
	
	public void showUsage(ICommandSender icommandsender)
	{
		sendChat(icommandsender, getCommandUsage(icommandsender), EnumChatFormatting.RED);
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}