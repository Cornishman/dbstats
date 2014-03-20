package dbStats.Command;

import dbStats.Config;
import dbStats.DbStats;
import dbStats.Util.ChatFormat;
import net.minecraft.command.ICommandSender;

public class CommandConfig extends Command {

	public CommandConfig() {
		super("dbs_config");
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
			case "save":
				DbStats.config.save();
				sendChat(icommandsender, "DbStats config saved to file.");
				break;
			case "load":
				DbStats.config.load();
				sendChat(icommandsender, "DbStats config loaded from file.");
				break;
			case "edit":
				if (astring.length <= 1)
				{
					showUsage(icommandsender);
				}
				else
				{
					switch(astring[1].toLowerCase())
					{
					case "databasedelay":
						if (astring.length <= 2)
						{
							sendChat(icommandsender, "Current database delay is : " + Config.timeBetweenDatabaseQueries);
						}
						else
						{
							try {
								Config.timeBetweenDatabaseQueries = Integer.parseInt(astring[2]) >= 15 ? Integer.parseInt(astring[2]) : 15;
								DbStats.config.save();
								sendChat(icommandsender, "Database delay changed to : " + Config.timeBetweenDatabaseQueries);
							} catch (NumberFormatException e)
							{
								sendChat(icommandsender, ChatFormat.RED + astring[2] + " is not a number");
							}
						}
						break;
					case "playerupdatedelay":
						if (astring.length <= 2)
						{
							sendChat(icommandsender, "Current player update delay is : " + Config.timeBetweenPlayerUpdates);
						}
						else
						{
							try {
								Config.timeBetweenPlayerUpdates = Integer.parseInt(astring[2]) >= 15 ? Integer.parseInt(astring[2]) : 15;
								DbStats.config.save();
								sendChat(icommandsender, "Player update delay changed to : " + Config.timeBetweenPlayerUpdates);
							} catch (NumberFormatException e)
							{
								sendChat(icommandsender, ChatFormat.RED + astring[2] + " is not a number");
							}
						}
						break;
					case "debug":
						if (astring.length <= 2)
						{
							sendChat(icommandsender, "Debug logging is currently : " + (Config.debugMode ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
						}
						else
						{
							try {
								astring[2] = astring[2].toLowerCase().equals("on") ? "true" : astring[2];
								Config.debugMode = Boolean.parseBoolean(astring[2]);
								DbStats.config.save();
								sendChat(icommandsender, "Debug logging switched : " + (Config.debugMode ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
							} catch (Exception e)
							{
								sendChat(icommandsender, ChatFormat.RED + astring[2] + " is not a boolean");
							}
						}
						break;
					case "logsql":
						if (astring.length <= 2)
						{
							sendChat(icommandsender, "SQL query logging is currently : " + (Config.logSqlQueries ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
						}
						else
						{
							try {
								astring[2] = astring[2].toLowerCase().equals("on") ? "true" : astring[2];
								Config.logSqlQueries = Boolean.parseBoolean(astring[2]);
								DbStats.config.save();
								sendChat(icommandsender, "SQL query logging switched : " + (Config.logSqlQueries ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
							} catch (Exception e)
							{
								sendChat(icommandsender, ChatFormat.RED + astring[2] + " is not a boolean");
							}
						}
						break;
					case "creativestats":
						if (astring.length <= 2)
						{
							sendChat(icommandsender, "Creative mode stat logging is currently : " + (Config.trackCreativeModeStats ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
						}
						else
						{
							try {
								astring[2] = astring[2].toLowerCase().equals("on") ? "true" : astring[2];
								Config.trackCreativeModeStats = Boolean.parseBoolean(astring[2]);
								DbStats.config.save();
								sendChat(icommandsender, "Creative mode stats switched : " + (Config.trackCreativeModeStats ? ChatFormat.GREEN + "[on]" : ChatFormat.RED + "[off]"));
							} catch (Exception e)
							{
								sendChat(icommandsender, ChatFormat.RED + astring[2] + " is not a boolean");
							}
						}
						break; 
					default:
						sendChat(icommandsender, ChatFormat.RED + astring[1] + " not a config option");
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
		return "/" + name + " [load/save/edit]";
	}
}
