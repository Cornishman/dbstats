package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class DistanceStatistic extends Statistic {
	
	public double distance;
	public final String method;

	public DistanceStatistic(String table, String column, String method, String player, double distance) {
		super(0, 0, "dbstats_distance", table, column, player, true, true, true, true, true);

		this.distance = distance;
		this.method = method;
	}

	@Override
	public boolean Combine(Statistic stat) {
		if (stat instanceof DistanceStatistic && this.StatisticsMatch(stat))
		{
			if (this.method.equals(((DistanceStatistic)stat).method))
			{
				this.distance += ((DistanceStatistic)stat).distance;
				return true;
			}
		}
		return false;
	}

	@Override
	public String GetValue() {
		return Double.toString(this.distance);
	}

	@Override
	public String GetSQLValueForInsertion() {
		return "@pId" + ",'" + this.method + "','" + this.distance + "'";
	}

}
