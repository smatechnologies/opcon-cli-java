package com.smatechnologies.opcon.command.api.interfaces;

public interface ICmdConstants {

	public static final String BACKSLASH = "\\";
	public static final String COLON = ":";
	public static final String COMMA = ",";
	public static final String DASH = "-";
	public static final String DOLLAR = "$";
	public static final String DOUBLE_QUOTES = "\"";
	public static final String DOUBLE_LEFT_SQUARE_BRACKET = "[[";
	public static final String DOUBLE_RIGHT_SQUARE_BRACKET = "]]";
	public static final String EQUAL = "=";
	public static final String EMPTY_STRING = "";
	public static final String ESCAPED_DOT = "\\.";
	public static final String LEFT_BRACKET = "(";
	public static final String LEFT_SQUARE_BRACKET = "[";
	public static final String PERIOD = ".";
	public static final String PIPE = "|";
	public static final String PLUS = "+";
	public static final String QUOTE = "\"";
	public static final String RIGHT_BRACKET = ")";
	public static final String RIGHT_SQUARE_BRACKET = "]";
	public static final String SEMI_COLON = ";";
	public static final String SINGLE_QUOTE = "'";
	public static final String SLASH = "/";
	public static final String SPACE = " ";
	public static final String UNDERSCORE = "_";
	public static final String UPPERCASE_C = "C";
	public static final String UPPERCASE_Y = "Y";
	public static final String UPPERCASE_M = "M";
	public static final String UPPERCASE_S = "U";
	public static final String UPPERCASE_U = "S";

	public static final String ZERO = "0";
	public static final int DAY_PARAMETER = 0;
	public static final int MONTH_PARAMETER = 1;
	public static final int YEAR_PARAMETER = 2;
	public static final String DEBUG_ON = "ON";
	public static final String TRUE = "True";
	
	// general
	public static final String SOFTWARE_VERSION = "1.1.4";
	public static final String SYSTEM_USER_DIRECTORY = "user.dir";
	public static final String CONFIG_FILE_NAME = "Connector.config";
	public static final String LOG_DATE_TIME_FORMAT = "yyyyMMdd HH:mm:ss";
	public static final String CURRENT_DATE_PATTERN = "yyyy-MM-dd";
	
	// config item definitions
	public static final String GENERAL_HEADER = "GENERAL";
	public static final String GENERAL_DEBUG = "DEBUG";
	public static final String OPCON_API_HEADER = "OPCON API CONNECTION";
	public static final String OPCON_API_SERVER = "SERVER";
	public static final String OPCON_API_PORT = "PORT";
	public static final String OPCON_API_USING_TLS = "USING_TLS";
	public static final String OPCON_API_USER = "USER";
	public static final String OPCON_API_PASSWORD = "PASSWORD";

	   interface PropertyTypes {
			public static final String OPCON_INSTANCE = "OI.";
			public static final String SCHEDULE_INSTANCE = "SI.";
			public static final String JOB_INSTANCE = "JI.";
	   }
	
	interface LogBackConstant {

        String LOG_PATH = "logback.path";
        String DEBUG_DEPENDENCIES = "logback.debug.dependencies";
        String DEBUG_API = "logback.debug.api";
        String LEVEL_STDOUT_KEY = "logback.level.stdout";
        String LEVEL_FILE_KEY = "logback.level.file";
        String STDOUT_PATTERN_KEY = "logback.stdout.pattern";
        String MAXHISTORY_FILE_KEY = "logback.maxhistory.file";
        String LEVEL_DEBUG_VALUE = "TRACE";
        String STDOUT_PATTERN_DEBUG_VALUE = "FULL";
    }
}
