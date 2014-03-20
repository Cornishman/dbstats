package dbStats.Statistics;

import dbStats.API.Statistics.Statistic;

public class DatabaseFluid extends Statistic {
	
	private final int id;
	private final String name;
	private final String unlocalizedName;
	private final int blockId;
	private final int color;
	private final int density;
	private final int luminosity;
	private final int temperature;
	private final int viscosity;
	private final boolean gaseous;
	
	public DatabaseFluid(int fluidId, String fluidName, String fluidUnlocalizedName, int fluidBlockId, int fluidColor, int fluidDensity, int fluidLuminosity, int fluidTemperature, int fluidViscosity, boolean fluidIsGaseous)
	{
		super(0, 0, "dbstat_databasefluid", "Fluids", "", "server", false, true, false, false, true);
		
		this.id = fluidId;
		this.name = fluidName;
		this.unlocalizedName = fluidUnlocalizedName;
		this.blockId = fluidBlockId;
		this.color = fluidColor;
		this.density = fluidDensity;
		this.luminosity = fluidLuminosity;
		this.temperature = fluidTemperature;
		this.viscosity = fluidViscosity;
		this.gaseous = fluidIsGaseous;
	}

	@Override
	public boolean Combine(Statistic stat) {
		// Always return false
		return false;
	}

	@Override
	public String GetValue() {
		// Return nothing
		return "";
	}

	@Override
	public String GetSQLValueForInsertion() {
		return "'" + this.id + "','" + this.name + "','" + this.unlocalizedName + "','" + this.blockId + "','" + this.color + "','" + this.density + "','" + this.luminosity + "','" 
				+ this.temperature + "','" + this.viscosity + "','" + (this.gaseous ? 1 : 0) + "'";
	}

}
