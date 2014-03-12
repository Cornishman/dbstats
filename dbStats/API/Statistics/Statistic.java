package dbStats.API.Statistics;


public abstract class Statistic {

	public final String key;
	public final String playerName;
	public final String table;
	public final String column;
	public final boolean addToCurrent;
	public final boolean insertStat;
	public final boolean updateStat;
	public final boolean usePlayerId;
	public final boolean insertsAllColumns;
	
	public Statistic(String key, String table, String column, String player, boolean addtocurr, boolean insert, boolean update, boolean usePlayerid, boolean insertAllColumns)
	{
		this.key = key;
		this.playerName = player;
		this.table = table.toLowerCase();
		this.column = column.toLowerCase();
		this.addToCurrent = addtocurr;
		
		this.insertStat = insert;
		this.updateStat = update;
		this.usePlayerId = usePlayerid;
		this.insertsAllColumns = insertAllColumns;
	}
	
	public abstract boolean Combine(Statistic stat);
	public abstract String GetValue();
	public abstract String GetSQLValueForInsertion();
	
	protected boolean StatisticsMatch(Statistic stat)
	{
		return stat.table.equals(this.table) && (stat.column.equals(this.column) || (stat.insertsAllColumns && this.insertsAllColumns)) && stat.playerName.equals(this.playerName);
	}
}