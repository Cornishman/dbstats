package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class PlayerStatistic extends Statistic {
	
	public String amount;

	public PlayerStatistic(String table, String column, String player, int amount, boolean addToPrev) {
		super("dbstats_player", table, column, player, addToPrev, false, true, false, false);
		
		this.amount = Integer.toString(amount);
	}
	
	public PlayerStatistic(String table, String column, String player, String amount, boolean addToPrev) {
		super("dbstats_player", table, column, player, addToPrev, false, true, false, false);
		
		this.amount = amount;
	}

	@Override
	public boolean Combine(Statistic stat) {
		if (stat instanceof PlayerStatistic && this.StatisticsMatch(stat))
		{
			if (this.addToCurrent && stat.addToCurrent)
			{
				this.amount = Integer.toString(Integer.parseInt(this.amount) + Integer.parseInt(((PlayerStatistic)stat).amount));
				return true;
			}
		}
		return false;
	}
	
	public String GenerateSQL()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.column + "=");
		if (this.addToCurrent)
		{
			sb.append(this.column + " + ");
		}
		sb.append("'" + this.amount + "'");
		
		return sb.toString();
	}

	@Override
	public String GetValue() {
		return this.amount;
	}

	@Override
	public String GetSQLValueForInsertion() {
		// TODO Auto-generated method stub
		return "";
	}
}
