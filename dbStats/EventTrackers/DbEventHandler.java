package dbStats.EventTrackers;

import net.minecraftforge.event.ForgeSubscribe;
import dbStats.Config;
import dbStats.DbStats;
import dbStats.API.Table;
import dbStats.Util.ErrorUtil;

public class DbEventHandler {
	
	@ForgeSubscribe
	public void onRegisterTable(Table table)
	{
		if (DbStats.database != null)
		{
			if (Config.debugMode)
			{
				ErrorUtil.LogMessage("Registering table " + table.tableName);
			}
			DbStats.database.registerTable(table);
		}
	}

}
