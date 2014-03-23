package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class MiscStat extends Statistic {

    public int amount;
    public final String statName;
    public final String info1;

    public MiscStat(String table, String column, String player, String statName, String info1, int amount)
    {
        super(0, 0, "dbstats_misc", table, column, player, true, true, true, true, true);

        this.amount = amount;
        this.statName = statName;
        this.info1 = info1;
    }

    @Override
    public boolean Combine(Statistic stat) {
        if (stat instanceof MiscStat && this.StatisticsMatch(stat))
        {
            if (this.statName.equals(((MiscStat) stat).statName)
                    && this.info1.equals(((MiscStat) stat).info1))
            {
                this.amount += ((MiscStat) stat).amount;
                return true;
            }
        }

        return false;
    }

    @Override
    public String GetValue() {
        return Integer.toString(this.amount);
    }

    @Override
    public String GetSQLValueForInsertion() {
        return "@pId" + ",'" + this.statName + "','" + this.info1 + "','" + this.amount + "'";
    }
}
