package dbStats.databases.MySQL;

import com.mysql.jdbc.DatabaseMetaData;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import dbStats.API.Column;
import dbStats.API.Column.ColumnType;
import dbStats.API.Statistics.Statistic;
import dbStats.API.Table;
import dbStats.Config;
import dbStats.DbStats;
import dbStats.Util.ErrorUtil;
import dbStats.Util.Utilities;
import dbStats.databases.IDatabaseHandler;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;

public class MysqlHandler implements IDatabaseHandler {
	
	public final static String type = "MySQL";
	private final boolean isReady;
    private boolean databaseChecked;
	
	private static Connection con = null;
	public MysqlHandler()
	{
		boolean ready;
		
		try {
			loadDriver();
			Connection connection = DriverManager.getConnection("jdbc:mysql://" + Config.serverAddress + ":" + Config.serverPort, Config.db_userName, Config.db_password);
			
			if (connection != null)
			{
				ready = true;
				connection.close();
			}
			else
			{
				DbStats.log.log(Level.SEVERE, "Failed to obtain a connection to the server");
				ready = false;
			}
		} catch (SQLException e) {
			DbStats.log.log(Level.SEVERE, "Failed to load mysql jdbc driver. " + e.getMessage());
			ready = false;
		}
		
		isReady = ready;
        databaseChecked = false;
	}
	
	private void loadDriver() throws SQLException
	{
		try {
			//Load the driver into the classpath first
			ModClassLoader loader = (ModClassLoader) Loader.instance().getModClassLoader();
			loader.addFile(new File(new File(".").getCanonicalPath() + "/libs/mysql-connector-java.jar"));
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex) {
			throw new SQLException(ex.getMessage());
		}
	}
	
	public boolean IsReady()
	{
		return isReady;
	}

    public boolean IsDatabaseChecked()
    {
        return databaseChecked;
    }

	@Override
	public void connect() {
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + Config.serverAddress + ":" + Config.serverPort + "/" 
					+ Config.databaseName + "?allowMultiQueries=true&user=" + Config.db_userName + "&password=" + Config.db_password);
		} catch(SQLException ex)
		{
			DbStats.log.log(Level.SEVERE, "Failed to connect: " + ex.getMessage());
			con = null;
		}
	}

	@Override
	public void disconnect() {
		if (con != null)
			try {
				con.close();
				con = null;
			} catch (SQLException e) {
				DbStats.log.log(Level.SEVERE, e.getMessage());
			}
	}

	@Override
	public boolean isConnceted() {
		if (con != null)
        {
            try {
                con.createStatement().executeQuery("SELECT 1 as db_connection_test");
            }
            catch (SQLException ex)
            {
                ErrorUtil.LogWarning("Possible Mysql connection timeout detected, attempting reconnect");
                disconnect();
                connect();
            }

            return true;
        }
		
		return false;
	}
	
	@Override
	public boolean checkDatabase() {
		//Check database exists, if not add it
		try {
			Connection connection = DriverManager.getConnection("jdbc:mysql://" + Config.serverAddress + ":" + Config.serverPort, Config.db_userName, Config.db_password);
		
			DatabaseMetaData metadata = (DatabaseMetaData) connection.getMetaData();
			
			ResultSet resultSet;
			resultSet = metadata.getCatalogs();
			boolean dbExists = false;
			String databaseName = Config.databaseName;
			
			while(resultSet.next())
			{
				if (resultSet.getString(1).toLowerCase().equals(databaseName.toLowerCase()))
				{
					dbExists = true;
					break;
				}
			}
			
			if (!dbExists)
			{
				con = connection;
				ErrorUtil.LogMessage(databaseName + " database not found. Attempting to create...");
				String create = "CREATE DATABASE `" + databaseName + "` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;";
				QueryResult qr = executeQuery(create, false);
				if (qr != null &&  !qr.hadResults)
				{
					ErrorUtil.LogMessage(databaseName + " database created.");
					dbExists = true;
				}
				else
				{
					ErrorUtil.LogWarning(databaseName + " database failed to create!");
				}
			}
			
			connect();
			
			if (dbExists)
			{
				for(Table table : tables)
				{
					resultSet = metadata.getTables(databaseName, null, table.tableName, null);
					if(resultSet.next())
					{
                        ErrorUtil.LogMessage(table.tableName + " table found, checking keys");
                        if (!CheckKeys(metadata.getIndexInfo(databaseName, null, table.tableName, true, true), table))
                        {
                            disconnect();
                            connection.close();
                            databaseChecked = false;
                            return false;
                        }

						ErrorUtil.LogMessage(table.tableName + " table found, checking columns.");
						if (!CheckColumns(metadata, table, databaseName))
                        {
                            disconnect();
                            connection.close();
                            databaseChecked = false;
                            return false;
                        }
					}
					else
					{
						ErrorUtil.LogMessage(table.tableName + " table not found! Attempting to insert.");
						if (!executeQuery(generateTableConstructionString(table), false).hadResults)
						{
							ErrorUtil.LogMessage(table.tableName + " table created.");
						}
						else
						{
							return false;
						}
					}
				}
			}
			
			disconnect();
			connection.close();
            databaseChecked = true;
		} catch (SQLException e) {
            ErrorUtil.LogMessage("Database check failed!");
			ErrorUtil.LogWarning(e.getMessage());
            databaseChecked = false;
			return false;
		}
		
		return true;
	}

    private boolean CheckColumns(DatabaseMetaData metadata, Table table, String databaseName)
            throws SQLException
    {
        for(Column c : table.columns)
        {
            ResultSet resultSet = metadata.getColumns(databaseName, null, table.tableName, c.name);
            if(!resultSet.next())
            {
                ErrorUtil.LogMessage(table.tableName + "-" + c.name + " column missing, attempting database restructure.");
                String rename = "RENAME TABLE `" + table.tableName + "` TO `" + table.tableName + "_old`;";
                if (!executeQuery(rename, false).hadResults)
                {
                    executeQuery(generateTableConstructionString(table), false);
                    String insert = "INSERT INTO `" + table.tableName + "` SELECT * FROM `" + table.tableName + "_old`";
                    QueryResult qr = executeQuery(insert, true);
                    if (qr.hadResults)	//Insert returns nothing
                    {
                        ErrorUtil.LogMessage(table.tableName + " failed to update!");
                        return false;
                    }
                    else
                    {
                        ErrorUtil.LogMessage(table.tableName + " updated. " + qr.returnedValue);
                        String drop = "DROP TABLE `" + table.tableName + "_old`";
                        executeQuery(drop, false);
                    }
                }
            }
            else
            {
                // Compare current column setting with database one, if something has changed we need to alter table...
                ColumnType columnType = Utilities.GetDatabaseColumnType(resultSet.getString("TYPE_NAME").toLowerCase(), "mysql");
                int columnSize = resultSet.getInt("COLUMN_SIZE");
                String columnDefault = resultSet.getString("COLUMN_DEF");
                if (columnDefault == null && Utilities.IsColumnTypeNumerical(columnType) && !c.autoIncrement)
                {
                    columnDefault = "0";
                }
                else if (columnDefault == null)
                {
                    columnDefault = "";
                }

                if (columnType != c.type)
                {
                    ErrorUtil.LogMessage(c.name + " - Column type mismatch " + columnType + " != " + Utilities.GetDatabaseColumnType(c.type, "mysql") + ". Attempting column modify");
                    if (!ModifyColumn(table, c))
                    {
                        return false;
                    }
                }
                else if (c.type == ColumnType.VARCHAR && c.dataSize != columnSize)
                {
                    ErrorUtil.LogMessage(c.name + " - Column size mismatch " + columnSize + " != " + c.dataSize + ". Attempting column modify");
                    if (!ModifyColumn(table, c))
                    {
                        return false;
                    }
                }
                else if (!c.defaultValue.equals(columnDefault))
                {
                    ErrorUtil.LogMessage(c.name + " - Column default mismatch " + columnDefault + " != " + c.defaultValue + ". Attempting column modify");
                    if (!ModifyColumn(table, c))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean CheckKeys(ResultSet resultSet, Table table)
            throws SQLException
    {

        ArrayList<String> foundPKeys = new ArrayList<>();
        ArrayList<String> foundUKeys = new ArrayList<>();

        ArrayList<String> pKeys = new ArrayList<>();
        ArrayList<String> uKeys = new ArrayList<>();

        for(Column column : table.columns)
        {
            if (column.primaryKey)
            {
                pKeys.add(column.name);
            }
            if (column.uniqueKey)
            {
                uKeys.add(column.name);
            }
        }

        while(resultSet.next())
        {
            if (resultSet.getString("INDEX_NAME").toLowerCase().equals("primary"))
            {
                foundPKeys.add(resultSet.getString("COLUMN_NAME"));
            }
            else if (resultSet.getString("INDEX_NAME").toLowerCase().equals("uniquekey"))
            {
                foundUKeys.add(resultSet.getString("COLUMN_NAME"));
            }
        }

        boolean pKeysMatch = true;
        boolean uKeysMatch = true;

        for(String pKey : pKeys)
        {
            if (!foundPKeys.contains(pKey))
            {
                ErrorUtil.LogMessage("Primary key '" + pKey + "' not found in " + table.tableName + ".");
                pKeysMatch = false;
            }
        }

        if (pKeysMatch)
        {
            for(String pKey : foundPKeys)
            {
                if (!pKeys.contains(pKey))
                {
                    ErrorUtil.LogMessage("Primary key '" + pKey + "' found in " + table.tableName + " but not in table spec.");
                    pKeysMatch = false;
                }
            }
        }

        for(String uKey : uKeys)
        {
            if (!foundUKeys.contains(uKey))
            {
                ErrorUtil.LogMessage("Unique key '" + uKey + "' not found in " + table.tableName + ".");
                uKeysMatch = false;
            }
        }

        if (uKeysMatch)
        {
            for(String uKey : foundUKeys)
            {
                if (!uKeys.contains(uKey))
                {
                    ErrorUtil.LogMessage("Unique key '" + uKey + "' found in " + table.tableName + " but not in table spec.");
                    uKeysMatch = false;
                }
            }
        }

        if (!pKeysMatch)
        {
            ErrorUtil.LogMessage("Primary key didn't match for table " + table.tableName + ", attempting recreation of Primary key...");

            if (foundPKeys.size() > 0)
            {
                if (executeQuery("ALTER TABLE `" + table.tableName + "` DROP PRIMARY KEY", false).hadResults)
                {
                    ErrorUtil.LogMessage("Failed to drop current Primary key for" + table.tableName);
                    return false;
                }
            }
            if (!executeQuery(generateAlterTablePrimaryKeys(table), false).hadResults)
            {
                ErrorUtil.LogMessage("Succesfully modified Primary Key for " + table.tableName);
            }
        }

        if (!uKeysMatch)
        {
            ErrorUtil.LogMessage("Unique key didn't match for " + table.tableName + ", attempting recreation of Unique key...");

            if (foundUKeys.size() > 0)
            {
                if (executeQuery("ALTER TABLE '" + table.tableName + "' DROP INDEX 'uniqueKey'", false).hadResults)
                {
                    ErrorUtil.LogMessage("Failed to drop current Unique key for " + table.tableName);
                    return false;
                }
            }

            if (!executeQuery(generateAlterTableUniqueKeys(table), false).hadResults)
            {
                ErrorUtil.LogMessage("Succesfully modified Unique Key for " + table.tableName);
            }
        }

        return true;
    }

    private String generateAlterTablePrimaryKeys(Table table)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ALTER TABLE `");
        sb.append(table.tableName);
        sb.append("` ADD PRIMARY KEY (");

        String pKeyString = "";
        for(Column column : table.columns)
        {
            if (column.primaryKey)
            {
                pKeyString += " `" + column.name + "`,";
            }
        }
        pKeyString = pKeyString.substring(0, pKeyString.length() - 1);

        sb.append(pKeyString);
        sb.append(")");

        return sb.toString();
    }

    private String generateAlterTableUniqueKeys(Table table)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ALTER TABLE `");
        sb.append(table.tableName);
        sb.append("` ADD UNIQUE INDEX `uniqueKey` (");

        String uKeyString = "";
        for(Column column : table.columns)
        {
            if (column.uniqueKey)
            {
                uKeyString += " `" + column.name + "`,";
            }
        }
        uKeyString = uKeyString.substring(0, uKeyString.length() - 1);

        sb.append(uKeyString);
        sb.append(")");

        return sb.toString();
    }
	
	private boolean ModifyColumn(Table t, Column c)
	{
		String query = generateAlterTable(t, c);
		if (!executeQuery(query, false).hadResults)
		{
			ErrorUtil.LogMessage("Succesfully modified column");
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean checkForPlayer(String playerName) {
		
		boolean found = false;
		String query = "SELECT * FROM players WHERE `playername` = '" + playerName + "'";
		QueryResult result = executeQuery(query, true);
		
		if (result != null && result.rs != null)
		{
			try {
				while (result.rs.next() && !found)
				{
					if (result.rs.getString("playername").equals(playerName)) {
						found = true;
					}
				}
			} catch (SQLException e) { }
		}
		
		cleanupQuery(result);
				
		return found;
	}
	
	public int getPlayerId(String playerName)
	{
		int id = 0;
		String query = "SELECT * FROM players WHERE `playername` = '" + playerName + "'";
		QueryResult result = executeQuery(query, true);
		
		if (result != null && result.rs != null)
		{
			try {
				while (result.rs.next() && id == 0)
				{
					if (result.rs.getString("playername").equals(playerName)) {
						id = result.rs.getInt("playerid");
					}
				}
			} catch (SQLException e) { }
		}
		
		cleanupQuery(result);
		
		return id;
	}
	
	private class QueryResult {
		Statement stmt;
		ResultSet rs;
		boolean hadResults = false;
		int returnedValue = -1;
		
		public QueryResult(Statement stmt, ResultSet rs) {
			this.stmt = stmt;
			this.rs = rs;
		}
	}
	
	private QueryResult executeQuery(String query, boolean expectResults)
	{
		if (!isConnceted()) connect();	//Connect if not connected!
		
		DbStats.errorUtil.LogSqlStatment(query);
		
		try {
			Statement stmt;
			ResultSet rs = null;
			
			stmt = con.createStatement();
			boolean hadResults = stmt.execute(query); 
			
			if (hadResults) rs = stmt.getResultSet();
			
			QueryResult qr = new QueryResult(stmt, rs);
			qr.hadResults = hadResults;
			
			if (!expectResults)
			{
				qr.returnedValue = stmt.getUpdateCount();
			}
			
			return qr;
		} catch(SQLException ex){
			DbStats.log.log(Level.SEVERE, "SQLException: " + ex.getMessage());
			return null;
		}
	}
	
	private int executeUpdate(String query)
	{
		if (!isConnceted()) connect();	//Connect if not connected!
		
		DbStats.errorUtil.LogSqlStatment(query);
		
		try {
			Statement stmt;
			
			stmt = con.createStatement();

            return stmt.executeUpdate(query);
		} catch(SQLException ex){
			DbStats.log.log(Level.SEVERE, "SQLException: " + ex.getMessage());
			return 0;
		}
	}
	
	private void cleanupQuery(QueryResult qr)
	{
		if (qr != null) {
			if (qr.rs != null)
			{
				try {
					qr.rs.close();
				} catch (SQLException e) { }
				
				qr.rs = null;
			}
			
			if (qr.stmt != null) {
				try {
					qr.stmt.close();
				} catch (SQLException e) { }
				
				qr.stmt = null;
			}
		}
	}

	@Override
	public boolean upsertStatistic(Statistic statistic) {
		String query = "";
		
		ErrorUtil.LogMessage("Upserting individual stat, " + statistic.key);
		
//		if (statistic.type == StatType.Player)
//		{
//			PlayerStatistic stat = (PlayerStatistic)statistic;
//			
//			if (stat.AddToPrev)
//				query = MySQLQueries.UpdatePlayerStat.query;
//			else
//				query = MySQLQueries.SetPlayerStat.query;
//			
//			query = query.replace("@Column", stat.TableColumn.column);
//			query = query.replace("@Player", stat.PlayerName);
//			query = query.replace("@Amount", stat.Amount);
//		}
//		
//		if (statistic.type == StatType.Block)
//		{
//			BlockStatistic stat = (BlockStatistic)statistic;
//			
//			query = MySQLQueries.UpdateBlockStat.query;
//			
//			if (stat.TableColumn.table == TableUtil.BrokenBlocks_BlockId.table)
//			{
//				query = query.replace("@Table", TableUtil.BrokenBlocks_BlockId.table);
//				query = query.replace("@PlayerIdColumn", TableUtil.BrokenBlocks_PlayerId.column);
//				query = query.replace("@BlockIdColumn", TableUtil.BrokenBlocks_BlockId.column);
//				query = query.replace("@BlockMetaColumn", TableUtil.BrokenBlocks_BlockMeta.column);
//				query = query.replace("@NoBlocksColumn", TableUtil.BrokenBlocks_NoBlocksBroken.column);
//			}
//			
//			query = query.replace("@Player", stat.PlayerName);
//			query = query.replace("@BlockId", stat.BlockID);
//			query = query.replace("@BlockMeta", stat.BlockMeta);
//			query = query.replace("@Amount", Integer.toString(stat.Amount));
//		}
//		
//		if (statistic.type == StatType.Distance)
//		{
//			DistanceStatistic stat = (DistanceStatistic)statistic;
//			
//			query = MySQLQueries.UpdateDistanceStat.query;
//			
//			query = query.replace("@Player", stat.PlayerName);
//			query = query.replace("@DistanceColumn", stat.TableColumn.column);
//			query = query.replace("@Distance", Double.toString(stat.distance));
//		}

        return executeUpdate(query) > 0;

    }

	@Override
	public boolean insertNewPlayer(String playerName) {

        return executeQuery(("INSERT INTO players (`playername`,`firstseen`,`logins`) VALUES('" + playerName + "','") + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) + "', '1')", false).returnedValue >= 0;
    }
	
	//This groups all similar stats into single queries and executes them
	//@return - All stats that were not grouped
	public ArrayList<Statistic> GroupStatsIntoSingleQueriesAndExecute(ArrayList<Statistic> stats)
	{
		ArrayList<Statistic> remainingStats = new ArrayList<Statistic>();
		
		ArrayList<ArrayList<Statistic>> statLists = new ArrayList<ArrayList<Statistic>>();
		
//		DbStats.errorUtil.LogMessage(stats.toString());
		
		while(!stats.isEmpty())
		{
			boolean found = false;
			
			for(ArrayList<Statistic> gStat : statLists)
			{
				Statistic stat = stats.get(0);
				if (stat.key.equals(gStat.get(0).key) && stat.playerName.equals(gStat.get(0).playerName))
				{
					gStat.add(stat);
					stats.remove(stats.get(0));
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				ArrayList<Statistic> newList = new ArrayList<Statistic>();
				newList.add(stats.get(0));
				statLists.add(newList);
				stats.remove(stats.get(0));
			}
		}
		
//		DbStats.errorUtil.LogMessage(statLists.toString());
		
		for(ArrayList<Statistic> gStat : statLists)
		{
//			DbStats.errorUtil.LogMessage(gStat.toString());
			StringBuilder sb = new StringBuilder();
			Statistic currentStat = gStat.get(0);
			
			if (currentStat.usePlayerId)
			{
				sb.append("SET @pId = (SELECT ID FROM players WHERE PlayerName='").append(currentStat.playerName).append("');");
			}
			
			if (currentStat.insertStat && currentStat.updateStat)
			{
				sb.append("INSERT INTO ").append(currentStat.table).append(" (");
				
				//Produce a string of columns that will be updated
				ArrayList<String> columnNames = new ArrayList<String>();
				for(Statistic stat : gStat)
				{
					if (!columnNames.contains(stat.column) && !stat.insertsAllColumns)
						columnNames.add(stat.column);
				}
				
				String columnToUpdate = "";
				//If columns are blank.. use all columns for that table!
				if (columnNames.isEmpty() && gStat.get(0).insertsAllColumns)
				{
					for(Table table : tables)
					{
						if (table.tableName.equals(gStat.get(0).table))
						{
							for(Column columns : table.columns)
							{
								columnNames.add(columns.name);
							}
							columnToUpdate = table.GetUpdateString();
						}
					}
				}
				
				if (columnNames.size() <= 0)
				{
					ErrorUtil.LogWarning("Table name not found - " + currentStat.table);
				}
				
				//Create the columns tag
				for(String columnName : columnNames)
				{
					sb.append("`").append(columnName).append("`,");
				}
				sb.deleteCharAt(sb.length()-1);
				
				sb.append(") VALUES");
				
//				Now create a list of values to insert
				StringBuilder values = new StringBuilder();
				for(Statistic stat : gStat)
				{
					if (stat.insertsAllColumns)
					{
						values.append("(").append(stat.GetSQLValueForInsertion()).append(")");
						values.append(",");
					}
				}
				values.deleteCharAt(values.length() - 1);
				
				sb.append(values.toString());
				sb.append(" ON DUPLICATE KEY UPDATE ");
				sb.append("").append(columnToUpdate).append("=");
				if (gStat.get(0).addToCurrent)
				{
					sb.append(columnToUpdate).append("+");
				}
				sb.append("VALUES(").append(columnToUpdate).append(")");
				sb.append(";");
				
				executeQuery(sb.toString(), false);
			}
			else if (currentStat.insertStat)
			{
				sb.append("INSERT INTO ").append(currentStat.table).append(" (");
				
				//Produce a string of columns that will be updated
				ArrayList<String> columnNames = new ArrayList<String>();
				for(Statistic stat : gStat)
				{
					if (!columnNames.contains(stat.column) && !stat.insertsAllColumns)
						columnNames.add(stat.column);
				}
				
				//If columns are blank.. use all columns for that table!
				if (columnNames.isEmpty() && gStat.get(0).insertsAllColumns)
				{
					for(Table table : tables)
					{
						if (table.tableName.equals(gStat.get(0).table))
						{
							for(Column columns : table.columns)
							{
								columnNames.add(columns.name);
							}
							table.GetUpdateString();
						}
					}
				}
				
				//Create the columns tag
				for(String columnName : columnNames)
				{
					sb.append("`").append(columnName).append("`,");
				}
				sb.deleteCharAt(sb.length()-1);
				
				sb.append(") VALUES");
				
//				Now create a list of values to insert
				StringBuilder values = new StringBuilder();
				for(Statistic stat : gStat)
				{
					if (stat.insertsAllColumns)
					{
						values.append("(").append(stat.GetSQLValueForInsertion()).append(")");
						values.append(",");
					}
				}
				values.deleteCharAt(values.length() - 1);
				
				sb.append(values.toString()).append(";");
				
				executeQuery(sb.toString(), false);
			}
			else if (currentStat.updateStat)
			{
				sb.append("UPDATE ").append(currentStat.table).append(" SET ");
				
				for(Statistic stat : gStat)
				{
					sb.append(stat.column).append("=");
					if (stat.addToCurrent)
					{
						sb.append(stat.column).append("+");
					}
					sb.append("'").append(stat.GetValue()).append("'");
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);	//Delete last ,
				
				if (!gStat.get(0).usePlayerId)
				{
					sb.append(" WHERE PlayerName='").append(currentStat.playerName).append("'");
				}
				else
				{
					sb.append(" WHERE playerid=@pId");
				}
				
				executeQuery(sb.toString(), false);
			}
		}
		
		return remainingStats;
	}

	
	@Override
	public boolean registerTable(Table table) {
		if (tables.contains(table))
		{
			DbStats.log.log(Level.WARNING, "Table already exists for - " + table.tableName);
			return false;
		}
		else
		{
			tables.add(table);
			return true;
		}
	}

	private String generateAlterTable(Table table, Column column)
	{

        return ("ALTER TABLE " + table.tableName) + " MODIFY COLUMN " + generateColumnDefString(column);
	}
	
	private String generateColumnDefString(Column c)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("`").append(c.name).append("`");
		sb.append(" ").append(Utilities.GetDatabaseColumnType(c.type, "mysql"));
		if (c.type == ColumnType.VARCHAR)
		{
			sb.append("(").append(Integer.toString(c.dataSize)).append(")");
		}
		
		if (!c.allowNull) { sb.append(" NOT NULL"); }
		if (!c.defaultValue.isEmpty() && !c.defaultValue.equals("CURRENT_TIMESTAMP")) { sb.append(" DEFAULT '").append(c.defaultValue).append("'"); }
		if (c.defaultValue.equals("CURRENT_TIMESTAMP")) { sb.append(" DEFAULT CURRENT_TIMESTAMP"); }
		if (c.autoIncrement) { sb.append(" AUTO_INCREMENT"); }
		
		return sb.toString();
	}

	@Override
	public String generateTableConstructionString(Table table) {
		StringBuilder sb = new StringBuilder();

		String pKeys = "";
		String uKeys = "";
		
		sb.append("CREATE TABLE IF NOT EXISTS `");
		sb.append(table.tableName);
		sb.append("` (");
		for(Column c : table.columns)
		{
			sb.append(generateColumnDefString(c));
//			sb.append("`" + c.name + "`");
//			sb.append(" " + Utilities.GetDatabaseColumnType(c.type, "mysql"));
//			if (c.type == ColumnType.VARCHAR)
//			{
//				sb.append("(" + Integer.toString(c.dataSize) + ")");
//			}
//			
//			if (!c.allowNull) { sb.append(" NOT NULL"); }
//			if (!c.defaultValue.isEmpty() && !c.defaultValue.equals("CURRENT_TIMESTAMP")) { sb.append(" DEFAULT '" + c.defaultValue + "'"); }
//			if (c.defaultValue.equals("CURRENT_TIMESTAMP")) { sb.append(" DEFAULT CURRENT_TIMESTAMP"); }
//			if (c.autoIncrement) { sb.append(" AUTO_INCREMENT"); }
			
			if (c.primaryKey) {
				if (pKeys.length() > 0) { pKeys += ","; }
				pKeys += "`" + c.name + "`"; 
			}
			if (c.uniqueKey) {
				if (uKeys.length() > 0) { uKeys += ","; }
				uKeys += "`" + c.name + "`";
			}
			
			sb.append(",");
		}
		
		if (pKeys.length() > 0)
		{
			sb.append(" PRIMARY KEY (").append(pKeys).append("),");
		}
		if (uKeys.length() > 0)
		{
			sb.append(" UNIQUE KEY `uniqueKey` (").append(uKeys).append("),");
		}
		
		sb.deleteCharAt(sb.length()-1);	//Remove the last ,
		sb.append(")");
		sb.append(" ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		
		return sb.toString();
	}

	@Override
	public void deleteAllTableInfo(Table table) {
		if (table == null)
			return;

        executeQuery(("DELETE FROM " + table.tableName), false);
	}

	@Override
	public Table findTableByName(String name) {
		for(Table table : tables)
		{
			if (table.tableName.equals(name.toLowerCase()))
			{
				return table;
			}
		}
		
		return null;
	}
}
