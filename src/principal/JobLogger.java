package principal;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
	private static boolean logToFile;
	private static boolean logToConsole;
	private static boolean logToDatabase;

	private static boolean logMessage;
	private static boolean logWarning;
	private static boolean logError;

	private static Map dbParams = new HashMap();
	private static Logger logger;

	public JobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
			boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
		logger = Logger.getLogger("My Log");

		setLogError(logErrorParam);
		setLogMessage(logMessageParam);
		setLogWarning(logWarningParam);
		setLogToDatabase(logToDatabaseParam);
		setLogToFile(logToFileParam);
		setLogToConsole(logToConsoleParam);
		setDbParams(dbParamsMap);
	}

	public void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {

		if (messageText == null || messageText.length() == 0) {
			throw new Exception("Message can't be null or empty");
		}

		if (!isLogToConsole() && !isLogToFile() && !isLogToDatabase()) {
			throw new Exception("Invalid configuration");
		}

		if ((!isLogError() && !isLogMessage() && !isLogWarning()) || (!message && !warning && !error)) {
			throw new Exception("Error or Warning or Message must be specified");
		}

		messageText.trim();
		String l = "";

		if (error && isLogError()) {
			l += "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
		}

		if (warning && isLogWarning()) {
			l += "warning " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
		}

		if (message && isLogMessage()) {
			l += "message " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
		}

		if (isLogToFile()) {
			logToFile(l);
		}

		if (isLogToConsole()) {
			logToConsole(l);
		}

		if (isLogToDatabase()) {
			logToDatabase(l);
		}
	}

	private static void logToFile(String loggedMessage) throws IOException {

		File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
		if (!logFile.exists()) {
			logFile.createNewFile();
		}

		FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt", true);

		logger.addHandler(fh);
		logger.log(Level.INFO, loggedMessage);

		fh.close();
	}

	private static void logToConsole(String loggedMessage) {

		ConsoleHandler ch = new ConsoleHandler();

		logger.addHandler(ch);
		logger.log(Level.INFO, loggedMessage);

		ch.close();
	}

	private static void logToDatabase(String loggedMessage) throws SQLException {

		Connection connection = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbParams.get("userName"));
		connectionProps.put("password", dbParams.get("password"));

		String connectionURL = "jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName") + ":"
				+ dbParams.get("portNumber") + "/Log_Values?autoReconnect=true&useSSL=false";

		connection = DriverManager.getConnection(connectionURL, connectionProps);

		String sql = "insert into log_values values ('" + loggedMessage + "'); ";
		PreparedStatement stmt = connection.prepareStatement(sql);

		stmt.executeUpdate();

		connection.close();
	}

	public static boolean isLogToFile() {
		return logToFile;
	}

	public static void setLogToFile(boolean logToFile) {
		JobLogger.logToFile = logToFile;
	}

	public static boolean isLogToConsole() {
		return logToConsole;
	}

	public static void setLogToConsole(boolean logToConsole) {
		JobLogger.logToConsole = logToConsole;
	}

	public static boolean isLogToDatabase() {
		return logToDatabase;
	}

	public static void setLogToDatabase(boolean logToDatabase) {
		JobLogger.logToDatabase = logToDatabase;
	}

	public static boolean isLogMessage() {
		return logMessage;
	}

	public static void setLogMessage(boolean logMessage) {
		JobLogger.logMessage = logMessage;
	}

	public static boolean isLogWarning() {
		return logWarning;
	}

	public static void setLogWarning(boolean logWarning) {
		JobLogger.logWarning = logWarning;
	}

	public static boolean isLogError() {
		return logError;
	}

	public static void setLogError(boolean logError) {
		JobLogger.logError = logError;
	}

	public static void setDbParams(Map dbParams) {
		JobLogger.dbParams = dbParams;
	}
}