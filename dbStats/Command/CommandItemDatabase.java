package dbStats.Command;

import dbStats.DbStats;
import net.minecraft.command.ICommandSender;

public class CommandItemDatabase extends Command {

    public CommandItemDatabase() {
        super("dbs_itemdatabase");
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) {
        if (astring.length <= 0)
        {
            showUsage(icommandsender);
        }
        else
        {
            if (astring[0].equals("refresh"))
            {
                DbStats.instance.DumpItemIds(icommandsender.getCommandSenderName());
                sendChat(icommandsender, "Items database list being refreshed");
            }
            else
            {
                showUsage(icommandsender);
            }
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/" + name + " [refresh]";
    }
}
