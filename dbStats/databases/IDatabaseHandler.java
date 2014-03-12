package dbStats.databases;

import java.util.ArrayList;

import dbStats.API.Table;
import dbStats.API.Statistics.Statistic;

public interface IDatabaseHandler {
	
	static ArrayList<Table> tables = new ArrayList<Table>();
	
	public final static String type = "";
	public final boolean isReady = false;

	public void connect();
	public void disconnect();
	public boolean isConnceted();
	
	public boolean checkDatabase();
	
	public boolean checkForPlayer(String playerName);
	public boolean insertNewPlayer(String playerName);
	public boolean upsertStatistic(Statistic statistic);
	
	public ArrayList<Statistic> GroupStatsIntoSingleQueriesAndExecute(ArrayList<Statistic> stats);
	
	public boolean IsReady();
	
	public boolean registerTable(Table table);
	public Table findTableByName(String name);
	
	public String generateTableConstructionString(Table table);
	
	public void deleteAllTableInfo(Table table);
}
