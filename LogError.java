package com.xml.errorLogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * @author harshmistry
 *	This class is used to initialize the error file, before error is written
 *	<baseerror>
 *		<error id="">
 *			<time></time>
 *			<name></name>  			  		i.e. class name of error (e.g. FileNotFound, ParseException etc...)
 *			<classname></classname>   		i.e. class from where error is thrown
 *			<functionname></functionname>
 *			<errormessage></errormessage>
 *		</error>
 *	</baseerror>
 *
 *  For testing this code, look at bottom of this code
 *  Please include jdom-2.0.6.jar before running this code,  http://www.jdom.org/dist/binary/jdom-2.0.6.zip
 */

public class LogError {
	private static File errorFile;
	private static SAXBuilder saxBuilder;
	private static Document document;
	private static Integer errorId;
	private static Element rootElement;
	
	static
	{
		Path errFilePath = Paths.get(ErrorLoggingVariable.strErrFilePath);	
		if(Files.exists(errFilePath))
		//check if error file exists or not
		{
			errorFile = new File(ErrorLoggingVariable.strErrFilePath);
			saxBuilder = new SAXBuilder();
			if(null != errorFile && errorFile.length()>0)
			{
				try {
					document=saxBuilder.build(errorFile);
				} catch (JDOMException e) {
					LogError.logError("JDOMException", LogError.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage());
				} catch (IOException e) {
					LogError.logError("IOException", LogError.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage());
				}
			}
		}
		else if(!Files.exists(errFilePath))
		{
			errFilePath = Paths.get(ErrorLoggingVariable.strErrFileDir);
			if(Files.notExists(errFilePath))
			//check if the folder exists, i.e. ErrorLogging/ErrorFile.xml	
				new File(ErrorLoggingVariable.strErrFileDir).mkdir();
			errorFile = new File(ErrorLoggingVariable.strErrFilePath);
		}
		initErrorId();
	}
	
	private static void initErrorId()
	/*
	 * This function is used to initialize the errorId which is used in <error id=""></error> 
	 */
	{
		if(null != errorFile && errorFile.length()==0)
		//if ErrorFile.xml is blank then initialize errorId
			errorId=new Integer(1);
		else if(null != errorFile && errorFile.length()>0)
		//if ErrorFile.xml has some content written, then check for two possibilities, i.e. if content are in proper format or not	
		{
			rootElement=document.getRootElement();
			if(null != rootElement)
			//1. if content in file is in order, then initialize errorId = last_error_id in file + 1	
			{
				errorId = new Integer(rootElement.getChildren().size()+1); 
			}
			else
			//2. if content is not in order, then re-initialize errorId	
				errorId=new Integer(1);
		}
	}
	
	public static void logError(String name,String className,String functionName,String errorMsg)
	{
		//Element rootElement=null;
		Element errorElement=null;
		if(null != errorFile && errorFile.length()>0)
		{
			//rootElement=document.getRootElement();
			if(null == rootElement)
			{
				//create root element
				rootElement= new Element(ErrorLoggingVariable.strBaseTagName);
				document = new Document(rootElement);
				errorElement=createTags(name,className,functionName,errorMsg);
			}
			else
			{
				errorElement=createTags(name,className,functionName,errorMsg);
			}	
		}
		else if(null != errorFile && errorFile.length()==0)
		{
			//create root element
			rootElement= new Element(ErrorLoggingVariable.strBaseTagName);
			document = new Document(rootElement);
			errorElement=createTags(name,className,functionName,errorMsg);
		}
		
		if(null != rootElement) //add error tag to root tag of document
			rootElement.addContent(errorElement);
		
		//write content into xml file
		writeIntoErrFile();
	}
	
	private static Element createTags(String name,String className,String functionName,String errorMsg)
	/*
	 * This function creates all tag that is written into ErrorFile.xml and returns the first child i.e. <error id=""></error>
	 */
	{
		//Create error tag
		Element errorTag=new Element(ErrorLoggingVariable.strTagName);
		errorTag.setAttribute(new Attribute(ErrorLoggingVariable.strTagAttributeName, errorId.toString()));
		errorId++;
		
		//create time tag
		DateFormat df=new SimpleDateFormat(ErrorLoggingVariable.strDateFormat);
		Date date=new Date();
		Element timeTag=writeTag(ErrorLoggingVariable.strFirstChildName,df.format(date));
		
		//create name tag
		Element nameTag=writeTag(ErrorLoggingVariable.strSecondChildName,name);
		
		//create classname tag
		Element classNameTag=writeTag(ErrorLoggingVariable.strThirdChildName,className);
		
		//create functionname tag
		Element functionNameTag=writeTag(ErrorLoggingVariable.strFourthChildName,functionName);
		
		//create error message tag
		Element errMsgTag=writeTag(ErrorLoggingVariable.strSecondChildName,errorMsg);
		
		//add all element into error tag
		errorTag.addContent(timeTag);
		errorTag.addContent(nameTag);
		errorTag.addContent(classNameTag);
		errorTag.addContent(functionNameTag);
		errorTag.addContent(errMsgTag);
		
		return errorTag;
	}
	
	private static Element writeTag(String tagName,String tagValue)
	{
		Element element=new Element(tagName);
		element.setText(tagValue);
		return element;
	}
	
	private static void writeIntoErrFile()
	{
		Writer writer=null;
		try {
		writer=new FileWriter(errorFile);
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		xmlOutputter.output(document,writer);
		} catch (IOException e) {
			LogError.logError("IOException", LogError.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage());
		} finally{
			try {
				if(null != writer)
					writer.close();
			} catch (IOException e) {
				LogError.logError("IOException", LogError.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage());
			}
		}
	}
	
	//for testing this code
	/*public static void main(String[] args) {
	 	//LogError.logError("StackOverflow",ClassName.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(), "Example error message");
		LogError.logError("StackOverflow",InitErrorFile.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(), "Example error message");
		LogError.logError("StackOverflow--2",InitErrorFile.class.getCanonicalName(), new Object(){}.getClass().getEnclosingMethod().getName(), "Example error message--2");
		System.out.println("Finish");
	}*/
}