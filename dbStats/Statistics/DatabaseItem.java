package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class DatabaseItem extends Statistic {
	
	private final String modID;
	private final String displayName;
	private final String unlocalisedName;
	private final int itemId;
	private final int meta;
	
	public DatabaseItem(int itemId, int meta, String displayName, String unlocalisedName, String modID)
	{
		super(0, 0, "dbstat_databaseitem", "Items", "", "server", false, true, false, false, true);
		
		this.modID = modID;
		this.displayName = displayName;
		this.unlocalisedName = unlocalisedName;
		this.itemId = itemId;
		this.meta = meta;
	}

	@Override
	public boolean Combine(Statistic stat) {
		// Always return false!
		return false;
	}

	@Override
	public String GetValue() {
		// Return nothing!
		return "";
	}

	@Override
	public String GetSQLValueForInsertion() {
		return "'" + this.itemId + "','" + this.meta + "','" + this.displayName.replace("'", "''").replace('"', '\"') + "','" + this.unlocalisedName.replace("'", "''").replace('"', '\"') + "','" + this.modID.replace("'", "''").replace('"', '\"') + "'";
	}

}
