package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import principal.JobLogger;

import static org.hamcrest.MatcherAssert.assertThat;

class JobLoggerTest {

	private static Map dbParamsMap = new HashMap();
	String desktop = System.getProperty("user.home") + "/Desktop/";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	void testInvalidConfiguration() throws Exception {
		JobLogger invalidLogger = new JobLogger(false, false, false, true, false, false, dbParamsMap);
		exception.expectMessage("Invalid configuration");
	}

	@Test
	void testWithoutTypeLogMessage() throws Exception {
		JobLogger message = new JobLogger(true, false, false, false, false, false, dbParamsMap);
		exception.expectMessage("Error or Warning or Message must be specified");
	}

	@Test
	void testEmptyLogMessage() throws Exception {
		JobLogger emptyMessage = new JobLogger(true, false, false, false, true, false, dbParamsMap);

		emptyMessage.LogMessage(null, false, true, false);

		exception.expectMessage("Message can't be null or empty");
	}

	@After
	@Test
	void testLoggerInFileCreatingFile() throws Exception {
		dbParamsMap.put("logFileFolder", desktop);

		File toDeleteFile = new File(dbParamsMap.get("logFileFolder") + "/logFile.txt");
		if (toDeleteFile.delete()) {
			JobLogger log = new JobLogger(true, false, false, true, false, false, dbParamsMap);
			log.LogMessage("LOG Message", true, false, false);

			File checkFile = new File(dbParamsMap.get("logFileFolder") + "/logFile.txt");

			if (checkFile.exists()) {
				List<String> lines = Collections.emptyList();
				try {
					lines = Files.readAllLines(Paths.get(checkFile.getAbsolutePath()), StandardCharsets.UTF_8);
					assertThat(lines.toString(), CoreMatchers.containsString("LOG Message"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// message
	@Test
	void testLoggerMessageInFile() throws Exception {
		dbParamsMap.put("logFileFolder", desktop);
		JobLogger log = new JobLogger(true, false, false, true, false, false, dbParamsMap);
		log.LogMessage("LOG Message", true, false, false);

		File checkFile = new File(dbParamsMap.get("logFileFolder") + "/logFile.txt");

		if (checkFile.exists()) {
			List<String> lines = Collections.emptyList();
			try {
				lines = Files.readAllLines(Paths.get(checkFile.getAbsolutePath()), StandardCharsets.UTF_8);
				assertThat(lines.toString(), CoreMatchers.containsString("LOG Message"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// warning
	@Test
	void testLoggerWarningInFile() throws Exception {
		dbParamsMap.put("logFileFolder", desktop);
		JobLogger log = new JobLogger(true, false, false, false, true, false, dbParamsMap);
		log.LogMessage("Warning Message", false, true, false);

		File checkFile = new File(dbParamsMap.get("logFileFolder") + "/logFile.txt");

		if (checkFile.exists()) {
			List<String> lines = Collections.emptyList();
			try {
				lines = Files.readAllLines(Paths.get(checkFile.getAbsolutePath()), StandardCharsets.UTF_8);
				assertThat(lines.toString(), CoreMatchers.containsString("Warning Message"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// error
	@Test
	void testLoggerErrorInFile() throws Exception {
		dbParamsMap.put("logFileFolder", desktop);
		JobLogger log = new JobLogger(true, false, false, false, false, true, dbParamsMap);
		log.LogMessage("Error Message", false, false, true);

		File checkFile = new File(dbParamsMap.get("logFileFolder") + "/logFile.txt");

		if (checkFile.exists()) {
			List<String> lines = Collections.emptyList();
			try {
				lines = Files.readAllLines(Paths.get(checkFile.getAbsolutePath()), StandardCharsets.UTF_8);
				assertThat(lines.toString(), CoreMatchers.containsString("Error Message"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	void testLoggerInConsole() throws Exception {
		JobLogger logConsole = new JobLogger(false, true, false, false, true, false, dbParamsMap);
		logConsole.LogMessage("CONSOLE Message", false, true, false);
	}

	// warning
	@Test
	void testLoggerInDatabase() throws Exception {
		dbParamsMap.put("userName", "root");
		dbParamsMap.put("password", "piura");
		dbParamsMap.put("dbms", "mysql");
		dbParamsMap.put("serverName", "localhost");
		dbParamsMap.put("portNumber", 3306);

		JobLogger logDatabase = new JobLogger(false, false, true, false, true, false, dbParamsMap);
		logDatabase.LogMessage("DATABASE Message", false, true, false);

		Connection connection = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbParamsMap.get("userName"));
		connectionProps.put("password", dbParamsMap.get("password"));

		String connectionURL = "jdbc:" + dbParamsMap.get("dbms") + "://" + dbParamsMap.get("serverName") + ":"
				+ dbParamsMap.get("portNumber") + "/Log_Values?autoReconnect=true&useSSL=false";
		connection = DriverManager.getConnection(connectionURL, connectionProps);

		String sql = "select * from log_values";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		List<String> result = new ArrayList();
		while (rs.next()) {
			result.add(rs.getString("log_message"));
		}
		assertThat(result.toString(), CoreMatchers.containsString("DATABASE Message"));
		st.close();
		connection.close();
	}

	// message
	@Test
	void testLoggerMessageInDatabase() throws Exception {
		dbParamsMap.put("userName", "root");
		dbParamsMap.put("password", "piura");
		dbParamsMap.put("dbms", "mysql");
		dbParamsMap.put("serverName", "localhost");
		dbParamsMap.put("portNumber", 3306);

		JobLogger logDatabase = new JobLogger(false, false, true, false, true, false, dbParamsMap);
		logDatabase.LogMessage("DATABASE Message", true, false, false);

		Connection connection = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbParamsMap.get("userName"));
		connectionProps.put("password", dbParamsMap.get("password"));

		String connectionURL = "jdbc:" + dbParamsMap.get("dbms") + "://" + dbParamsMap.get("serverName") + ":"
				+ dbParamsMap.get("portNumber") + "/Log_Values?autoReconnect=true&useSSL=false";
		connection = DriverManager.getConnection(connectionURL, connectionProps);

		String sql = "select * from log_values";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		List<String> result = new ArrayList();
		while (rs.next()) {
			result.add(rs.getString("log_message"));
		}
		assertThat(result.toString(), CoreMatchers.containsString("DATABASE Message"));
		st.close();
		connection.close();
	}

	// error
	@Test
	void testLoggerErrorInDatabase() throws Exception {
		dbParamsMap.put("userName", "root");
		dbParamsMap.put("password", "piura");
		dbParamsMap.put("dbms", "mysql");
		dbParamsMap.put("serverName", "localhost");
		dbParamsMap.put("portNumber", 3306);

		JobLogger logDatabase = new JobLogger(false, false, true, false, true, false, dbParamsMap);
		logDatabase.LogMessage("DATABASE Message", false, false, true);

		Connection connection = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbParamsMap.get("userName"));
		connectionProps.put("password", dbParamsMap.get("password"));

		String connectionURL = "jdbc:" + dbParamsMap.get("dbms") + "://" + dbParamsMap.get("serverName") + ":"
				+ dbParamsMap.get("portNumber") + "/Log_Values?autoReconnect=true&useSSL=false";
		connection = DriverManager.getConnection(connectionURL, connectionProps);

		String sql = "select * from log_values";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		List<String> result = new ArrayList();
		while (rs.next()) {
			result.add(rs.getString("log_message"));
		}
		assertThat(result.toString(), CoreMatchers.containsString("DATABASE Message"));
		st.close();
		connection.close();
	}

}
