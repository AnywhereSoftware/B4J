
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4j.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;

@Version(1.61f)
@Events(values={"QueryComplete (Success As Boolean, Crsr As ResultSet)",
		"NonQueryComplete (Success As Boolean)", "Ready (Success As Boolean)"})
@ShortName("SQL")
public class SQL implements CheckForReinitialize{
	@Hide
	public Connection connection;
	@Hide
	public static final int THREAD_LOCK_TIMEOUT = 60000;
	@Hide
	public ReentrantLock sqliteLock;
	private volatile ArrayList<Object[]> nonQueryStatementsList = new ArrayList<Object[]>();

	/**
	 * Initializes the SQL object. You also need to add the JDBC driver jar to your project with the #AdditionalJar attribute.
	 *DriverClass - The matching JDBC driver. For example (MySQL): com.mysql.jdbc.Driver
	 *JdbcUrl - The connection url. For example (MySQL): jdbc:mysql://localhost/test?characterEncoding=utf8
	 */
	public void Initialize(String DriverClass, String JdbcUrl) throws ClassNotFoundException, SQLException {
		Initialize2(DriverClass, JdbcUrl, null, null);
	}
	/**
	 * Similar to Initialize method. Passes the given UserName and Password to the database.
	 */
	public void Initialize2(String DriverClass, String JdbcUrl, String UserName, String Password) throws SQLException {
		try {
			Class.forName(DriverClass);
		} catch (ClassNotFoundException c) {
			throw new RuntimeException("Class not found: " + DriverClass + "\nAre you missing an #AdditionalJar attribute setting?");
		}
		connection = DriverManager.getConnection(JdbcUrl, UserName, Password);
	}
	private static SQL cloneMe(SQL sql) {
		SQL ret = new SQL();
		ret.connection = sql.connection;
		ret.nonQueryStatementsList = sql.nonQueryStatementsList;
		ret.sqliteLock = sql.sqliteLock;
		return ret;
	}
	/**
	 * Asynchronously initializes the SQL connection. The Ready event will be raised when the connection is ready or if an error has occurred.
	 *The EventName parameter sets the sub that will handle the Ready event.
	 *Example:<code>
	 *Sub Process_Globals
	 *	Dim sql1 As SQL
	 *End Sub
	 *
	 *Sub AppStart (Args() As String)
	 *	sql1.InitializeAsync("sql1", "com.mysql.jdbc.Driver", _
	 *		"jdbc:mysql://localhost/example", "username", "password")
	 *	StartMessageLoop 'only required in a console app
	 *End Sub
	 *
	 *Sub sql1_Ready (Success As Boolean)
	 *	Log(Success)
	 *	If Success = False Then
	 *		Log(LastException)
	 *		Return
	 *	End If
	 *	Dim rs As ResultSet = sql1.ExecQuery("SELECT table_name FROM information_schema.tables")
	 *	Do While rs.NextRow
	 *		Log(rs.GetString2(0))
	 *	Loop
	 *	rs.Close
	 *End Sub</code>
	 */
	public void InitializeAsync(BA ba, String EventName, final String DriverClass, final String JdbcUrl, final String UserName, final String Password) {
		BA.runAsync(ba, this, EventName + "_ready", new Object[] {false}, new Callable<Object[]>() {

			@Override
			public Object[] call() throws Exception {
				Initialize2(DriverClass, JdbcUrl, UserName, Password);
				return new Object[] {true};
			}
		});
	}

	/**
	 * Opens the SQLite database file. A new database will be created if it does not exist and CreateIfNecessary is true.
	 *Note that you should add the following attribute to the main module:
	 *<code>#AdditionalJar: sqlite-jdbc-3.7.2</code>
	 *Example:<code>
	 *Dim SQL1 As SQL
	 *SQL1.InitializeSQLite(File.DirApp, "MyDb.db", True)</code>
	 */
	public void InitializeSQLite(String Dir, String FileName, boolean CreateIfNecessary) throws ClassNotFoundException, SQLException, FileNotFoundException {
		if ("".equals(Dir))
			Dir = null;
		File f = new File(Dir, FileName);
		if (CreateIfNecessary == false && f.exists() == false)
			throw new FileNotFoundException(f.toString());
		Initialize("org.sqlite.JDBC", "jdbc:sqlite:" + f.toString().replace('\\', '/'));
		createSqliteLock();
	}
	@Hide
	public void createSqliteLock() {
		sqliteLock  = new ReentrantLock();
	}
	/**
	 * The SQL library allows you to create and manage SQL databases.
	 *Using this library you can connect to any type of SQL database.
	 *See this <link>link|http://www.basic4ppc.com/android/forum/threads/sql-tutorial.35185/</link> for more information.
	 */
	public static void LIBRARY_DOC() {

	}
	protected void checkNull() {
		if (connection == null)
			throw new RuntimeException("Object should first be initialized.");
	}
	/**
	 * Tests whether the database is initialized and opened.
	 */
	public boolean IsInitialized() {
		if (connection == null)
			return false;
		try {
			return connection.isClosed() == false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Executes a single non query SQL statement.
	 *Example:<code>
	 *SQL1.ExecNonQuery("CREATE TABLE table1 (col1 TEXT , col2 INTEGER, col3 INTEGER)")</code>
	 *It will be significantly faster to explicitly start a transaction before applying any changes to the database.
	 */
	public void ExecNonQuery(String Statement) throws SQLException {
		checkNull();
		Statement st = connection.createStatement();
		try {
			startLock();
			st.execute(Statement);
		} finally {
			try {
				st.close();
			}
			finally {
				releaseLock();
			}

		}
	}
	private void startLock() {
		if (sqliteLock != null) {
			try {
				if (!sqliteLock.tryLock(THREAD_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
					System.err.println("Thread is waiting for more than 60 seconds for the previous transaction to complete...");
					Thread.dumpStack();
					sqliteLock.lock();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	private void releaseLock() {
		if (sqliteLock != null)
			sqliteLock.unlock();
	}
	/**
	 * Executes a single non query SQL statement.
	 *The statement can include question marks which will be replaced by the items in the given list.
	 *Note that B4J converts arrays to lists implicitly.
	 *The values in the list should be strings, numbers or bytes arrays.
	 *Example:<code>
	 *SQL1.ExecNonQuery2("INSERT INTO table1 VALUES (?, ?, 0)", Array As Object("some text", 2))</code>
	 */
	public void ExecNonQuery2(String Statement, List Args) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(Statement);
		try {
			int numArgs = Args.IsInitialized() == false ? 0 : Args.getSize();
			for (int i = 0; i < numArgs; i++) {
				ps.setObject(i + 1, Args.Get(i));
			}
			startLock();
			ps.execute();
		} finally {
			try {
				ps.close();
			}
			finally {
				releaseLock();
			}
		}
	}
	/**
	 * Adds a non-query statement to the batch of statements.
	 *The statements are (asynchronously) executed when you call ExecNonQueryBatch.
	 *Args parameter can be Null if it is not needed.
	 *Example:<code>
	 *For i = 1 To 1000
	 *	sql.AddNonQueryToBatch("INSERT INTO table1 VALUES (?)", Array(Rnd(0, 100000)))
	 *Next
	 *Dim SenderFilter As Object = sql.ExecNonQueryBatch("SQL")
	 *Wait For (SenderFilter) SQL_NonQueryComplete (Success As Boolean)
	 *Log("NonQuery: " & Success)</code>
	 */
	public void AddNonQueryToBatch(String Statement, List Args) {
		nonQueryStatementsList.add(new Object[] {Statement, Args});
	}
	/**
	 * Asynchronously executes a batch of non-query statements (such as INSERT).
	 *The NonQueryComplete event is raised after the statements are completed.
	 *You should call AddNonQueryToBatch one or more times before calling this method to add statements to the batch.
	 *Note that this method internally begins and ends a transaction.
	 *Returns an object that can be used as the sender filter for Wait For calls.
	 *Example:<code>
	 *For i = 1 To 1000
	 *	sql.AddNonQueryToBatch("INSERT INTO table1 VALUES (?)", Array(Rnd(0, 100000)))
	 *Next
	 *Dim SenderFilter As Object = sql.ExecNonQueryBatch("SQL")
	 *Wait For (SenderFilter) SQL_NonQueryComplete (Success As Boolean)
	 *Log("NonQuery: " & Success)</code>
	 */
	public Object ExecNonQueryBatch(final BA ba, final String EventName) {
		final ArrayList<Object[]> myList = nonQueryStatementsList;
		nonQueryStatementsList = new ArrayList<Object[]>();
		final SQL ret = SQL.cloneMe(this);
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				synchronized (connection) {
					try {
						BeginTransaction();
						HashMap<String, PreparedStatement> cachedPS = new HashMap<String, PreparedStatement>();
						try {
							
							for (Object[] o: myList) {
								String Statement = (String)o[0];
								List Args = (List)o[1];
								PreparedStatement ps = cachedPS.get(Statement);
								if (ps == null) {
									ps = connection.prepareStatement(Statement);
									cachedPS.put(Statement, ps);
								}
								int numArgs = Args.IsInitialized() == false ? 0 : Args.getSize();
								for (int i = 0; i < numArgs; i++) {
									ps.setObject(i + 1, Args.Get(i));
								}
								ps.execute();

							}
						} finally {
							for (PreparedStatement ps : cachedPS.values()) {
								try {
									ps.close();
								} catch (Exception pse) {
									pse.printStackTrace();
								}
							}
						}
						TransactionSuccessful();
						ba.raiseEventFromDifferentThread(ret, null, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
								true, new Object[] {true});
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Rollback();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(ret, null, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
								true, new Object[] {false});
					} 
				}
			}

		}, null, 0);
		return ret;
	}

	/**
	 * Asynchronously executes the given query. The QueryComplete event will be raised when the results are ready.
	 *Returns an object that can be used as the sender filter for Wait For calls.
	 *Example:<code>
	 *Dim SenderFilter As Object = sql.ExecQueryAsync("SQL", "SELECT * FROM table1", Null)
	 *Wait For (SenderFilter) SQL_QueryComplete (Success As Boolean, rs As ResultSet)
	 *If Success Then
	 *	Do While rs.NextRow
	 *		Log(rs.GetInt2(0))
	 *	Loop
	 *	rs.Close
	 *Else
	 *	Log(LastException)
	 *End If</code>
	 */
	public Object ExecQueryAsync(final BA ba, final String EventName, final String Query, final List Args) {
		final SQL ret = SQL.cloneMe(this);
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				try {
					ResultSetWrapper c = ExecQuery2(Query, Args);
					ba.raiseEventFromDifferentThread(ret, null, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
							true, new Object[] {true, c});
				} catch (Exception e) {
					e.printStackTrace();
					ba.setLastException(e);
					ba.raiseEventFromDifferentThread(ret, null, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
							true, new Object[] {false, AbsObjectWrapper.ConvertToWrapper(new ResultSetWrapper(), null)});
				}
			}

		}, this, 0);
		return ret;
	}

	/**
	 * Executes the query and returns a cursor which is used to go over the results.
	 *Example:<code>
	 *Dim Cursor As ResultSet
	 *Cursor = SQL1.ExecQuery("SELECT col1, col2 FROM table1")
	 *Do While Cursor.NextRow
	 *	Log(Cursor.GetString("col1"))
	 *	Log(Cursor.GetInt("col2"))
	 *Loop</code>
	 */
	public ResultSetWrapper ExecQuery(String Query) throws SQLException {
		checkNull();
		return ExecQuery2(Query, null);
	}
	/**
	 * Executes the query and returns a cursor which is used to go over the results.
	 *The query can include question marks which will be replaced with the values in the array.
	 *Example:<code>
	 *Dim Cursor As ResultSet
	 *Cursor = sql1.ExecQuery2("SELECT col1 FROM table1 WHERE col3 = ?", Array As String(22))</code>
	 *SQLite will try to convert the string values based on the columns types.
	 */
	public ResultSetWrapper ExecQuery2(String Query, List Args) throws SQLException {
		checkNull();
		PreparedStatement ps = connection.prepareStatement(Query);
		if (Args != null && Args.IsInitialized()) {
			for (int i = 0;i < Args.getSize();i++) {
				ps.setObject(i + 1, Args.Get(i));
			}
		}
		ResultSetWrapper rs = new ResultSetWrapper();
		rs.setObject(ps.executeQuery());
		ResultSetWrapper.closePS.put(rs.getObject(), ps);
		return rs;
	}
	/**
	 * Create a statement object which you can use with ExecCall to call stored procedures.
	 */
	public Object CreateCallStatement(String Query, List Args) throws SQLException {
		checkNull();
		CallableStatement cs = connection.prepareCall(Query);
		if (Args != null && Args.IsInitialized()) {
			for (int i = 0;i < Args.getSize();i++) {
				cs.setObject(i + 1, Args.Get(i));
			}
		}
		return cs;
	}
	/**
	 * Executes a call statement previously created with CreateCallStatement.
	 */
	public ResultSetWrapper ExecCall(Object CallStatement) throws SQLException {
		checkNull();
		CallableStatement cs = (CallableStatement)CallStatement;
		ResultSetWrapper rs = new ResultSetWrapper();
		rs.setObject(cs.executeQuery());
		ResultSetWrapper.closePS.put(rs.getObject(), cs);
		return rs;
	}

	/**
	 * Executes the query and returns the value in the first column and the first row (in the result set).
	 *Returns Null if no results were found.
	 *Example:<code>
	 *Dim NumberOfMatches As Int
	 *NumberOfMatches = SQL1.ExecQuerySingleResult("SELECT count(*) FROM table1 WHERE col2 > 300")</code>
	 */
	public String ExecQuerySingleResult(String Query) throws SQLException {
		return ExecQuerySingleResult2(Query, null);
	}
	/**
	 * Executes the query and returns the value in the first column and the first row (in the result set).
	 *Returns Null if no results were found.
	 *Example:<code>
	 *Dim NumberOfMatches As Int
	 *NumberOfMatches = SQL1.ExecQuerySingleResult2("SELECT count(*) FROM table1 WHERE col2 > ?", Array As String(300))</code>
	 */
	public String ExecQuerySingleResult2(String Query, List Args) throws SQLException {
		checkNull();
		ResultSetWrapper cursor = ExecQuery2(Query, Args);
		try {
			if (!cursor.NextRow())
				return null;
			if (cursor.getColumnCount() == 0)
				return null;
			return cursor.GetString2(0);
		} finally {
			cursor.Close();
		}
	}
	/**
	 * Begins a transaction. A transaction is a set of multiple "writing" statements that are atomically committed,
	 *hence all changes will be made or no changes will be made.
	 *As a side effect those statements will be executed significantly faster (in the default case a transaction is implicitly created for
	 *each statement).
	 *It is very important to handle transaction carefully and close them.
	 *The transaction is considered successful only if TransactionSuccessful is called. Otherwise no changes will be made.
	 *Typical usage:<code>
	 *SQL1.BeginTransaction
	 *Try
	 *	'block of statements like:
	 *	For i = 1 to 1000
	 *		SQL1.ExecNonQuery("INSERT INTO table1 VALUES(...)
	 *	Next
	 *	SQL1.TransactionSuccessful
	 *Catch
	 *	Log(LastException.Message) 
	 *  SQL1.RollBack 'no changes will be made
	 *End Try
	 *</code>
	 */
	public void BeginTransaction() throws SQLException{
		checkNull();
		startLock();
		connection.setAutoCommit(false);
	}
	/**
	 * Commits the statements and ends the transaction.
	 */
	public void TransactionSuccessful() throws SQLException{
		try {
			connection.setAutoCommit(true);
		} finally {
			releaseLock();
		}
	}
	/**
	 * Rollbacks the changes from the current transaction and closes the transaction.
	 */
	public void Rollback() throws SQLException {
		try {
			connection.rollback();
			connection.setAutoCommit(true);
		} finally {
			releaseLock();
		}
	}

	/**
	 * Closes the database.
	 *Does not do anything if the database is not opened or was closed before.
	 */
	public void Close() throws SQLException {
		if (sqliteLock != null && sqliteLock.isHeldByCurrentThread())
			releaseLock();
		if (connection != null && connection.isClosed() == false)
			connection.close();
	}


	@ShortName("ResultSet")
	public static class ResultSetWrapper extends AbsObjectWrapper<ResultSet> {
		@Hide
		public static final ConcurrentHashMap<ResultSet, Statement> closePS = new ConcurrentHashMap<ResultSet, Statement>();
		/**
		 * Moves the cursor to the next result. Returns false when the cursor reaches the end.
		 *Example:<code>
		 *Do While ResultSet1.NextRow
		 * 'Work with Row
		 *Loop</code>
		 */
		public boolean NextRow() throws SQLException {
			return getObject().next();
		}
		/**
		 * Returns the name of the column at the specified index.
		 *The first column index is 0.
		 */
		public String GetColumnName(int Index) throws SQLException {
			return getObject().getMetaData().getColumnLabel(Index + 1);
		}
		/**
		 * Gets the number of columns available in the result set.
		 */
		public int getColumnCount() throws SQLException {
			return getObject().getMetaData().getColumnCount();
		}
		/**
		 * Returns the Int value stored in the column at the given ordinal.
		 *The value will be converted to Int if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetInt2(0))</code>
		 */
		public int GetInt2(int Index) throws SQLException {
			return getObject().getInt(Index + 1);
		}
		/**
		 * Returns the Int value stored in the given column.
		 *The value will be converted to Int if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetInt("col2"))</code>
		 */
		public int GetInt(String ColumnName) throws SQLException {
			return getObject().getInt(ColumnName);
		}
		/**
		 * Returns the String value stored in the column at the given ordinal.
		 *The value will be converted to String if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetString2(0))</code>
		 */
		public String GetString2(int Index) throws SQLException {
			return getObject().getString(Index + 1);
		}
		/**
		 * Returns the String value stored in the given column.
		 *The value will be converted to String if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetString("col2"))</code>
		 */
		public String GetString(String ColumnName) throws SQLException {
			return getObject().getString(ColumnName);
		}
		/**
		 * Returns the Long value stored in the column at the given ordinal.
		 *The value will be converted to Long if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetLong2(0))</code>
		 */
		public Long GetLong2(int Index) throws SQLException {
			return getObject().getLong(Index + 1);
		}
		/**
		 * Returns the Long value stored in the given column.
		 *The value will be converted to Long if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetLong("col2"))</code>
		 */
		public Long GetLong(String ColumnName) throws SQLException {
			return getObject().getLong(ColumnName);
		}
		/**
		 * Returns the Double value stored in the column at the given ordinal.
		 *The value will be converted to Double if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetDouble2(0))</code>
		 */
		public Double GetDouble2(int Index) throws SQLException {
			return getObject().getDouble(Index + 1);
		}
		/**
		 * Returns the Double value stored in the given column.
		 *The value will be converted to Double if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetDouble("col2"))</code>
		 */
		public Double GetDouble(String ColumnName) throws SQLException {
			return getObject().getDouble(ColumnName);
		}
		/**
		 * Returns the blob stored in the given column.
		 *Example:<code>
		 *Dim Buffer() As Byte
		 *Buffer = Cursor.GetBlob("col1")</code>
		 */
		public byte[] GetBlob(String ColumnName) throws SQLException {
			return getObject().getBytes(ColumnName);
		}
		/**
		 * Returns the blob stored in the column at the given ordinal.
		 *Example:<code>
		 *Dim Buffer() As Byte
		 *Buffer = Cursor.GetBlob2(0)</code>
		 */
		public byte[] GetBlob2(int Index) throws SQLException {
			return getObject().getBytes(Index + 1);
		}
		/**
		 * Closes the cursor and frees resources.
		 */
		public void Close() throws SQLException {
			getObject().close();
			Statement ps = closePS.remove(getObject());
			if (ps != null)
				ps.close();
		}

	}
}



