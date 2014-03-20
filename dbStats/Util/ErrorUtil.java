package dbStats.Util;

import dbStats.API.Statistics.Statistic;
import dbStats.Config;
import dbStats.databases.IDatabaseHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorUtil {
	
	private static Logger log;
	
	public ErrorUtil(Logger log)
	{
		ErrorUtil.log = log;
	}
	
	public void LogSqlStatment(String sql)
	{
		if (Config.debugMode && Config.logSqlQueries)
		{
			log.log(Level.INFO, "sql - " + sql);
		}
	}

	public static void LogStatisticError(Statistic stat)
	{
		String message;
		
		message = IDatabaseHandler.type + " :  Failed to update " + stat.column + " in " + stat.table + " table for player " + stat.playerName;
		
		log.log(Level.WARNING, message);
	}
	
	public static void LogMessage(String message)
	{
		if (Config.debugMode)
		{
			log.log(Level.INFO, message);
		}
	}
	
	public static void LogException(Exception e)
	{
		LogException("", e);
	}
	
	public static void LogException(String message, Exception e)
	{
		if (Config.debugMode)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.log(Level.WARNING, "Exception caught - " + message + "\n" + sw.toString());
		}
	}
	
	public static void LogWarning(String message)
	{
		log.log(Level.WARNING, message);
	}
}
