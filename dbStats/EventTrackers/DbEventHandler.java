package dbStats.EventTrackers;

import dbStats.API.Table;
import dbStats.Config;
import dbStats.DbStats;
import dbStats.Util.ErrorUtil;
import net.minecraftforge.event.ForgeSubscribe;

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
