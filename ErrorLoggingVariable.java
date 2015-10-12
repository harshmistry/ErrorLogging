package com.xml.errorLogging;
/**
 * 
 * @author harshmistry
 *	This class holds all final variable names used for error logging i.e. reading or writing into ErrorFile.xml
 */

public class ErrorLoggingVariable {
 
	 static final String strErrFileDir 			= "ErrorLogging";
	 static final String strErrFilePath 		= "ErrorLogging/ErrorFile.xml";
	 static final String strTagName 			= "error";
	 static final String strBaseTagName 		= "baseerror";
	 static final String strTagAttributeName 	= "id";
	 static final String strFirstChildName 		= "time";
	 static final String strSecondChildName 	= "errormessage";
	 static final String strThirdChildName 		= "classname";
	 static final String strFourthChildName 	= "functionname";
	 static final String strFifthChildName 		= "errormessage";
	 static final String strDateFormat			= "dd/MM/yyyy HH:mm:ss";
}
