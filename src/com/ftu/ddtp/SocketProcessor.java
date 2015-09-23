package com.ftu.ddtp;


import com.ftu.ddtp.DDTP;
import com.ftu.ddtp.ProcessorStorage;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class SocketProcessor implements ProcessorStorage
{
	/////////////////////////////////////////////////////////////////
	// Variables
	/////////////////////////////////////////////////////////////////
	public ChatSocketTransmitter channel = null;
	public DDTP request = null;
	public DDTP response = new DDTP();
	public AuthenticateInterface authenticator = null;
	public LogInterface log = null;
	/////////////////////////////////////////////////////////////////
	/**
	 * Get module name associate with this processor
	 * @return module name associate with this processor
	 * @author Thai Hoang Hiep
	 */
	/////////////////////////////////////////////////////////////////
	protected String getModuleName()
	{
		return null;
	}
	/////////////////////////////////////////////////////////////////
	// Override function
	/////////////////////////////////////////////////////////////////
	public DDTP getResponse()
	{
		return response;
	}
	/////////////////////////////////////////////////////////////////
	public void setRequest(DDTP request)
	{
		this.request = request;
	}
	/////////////////////////////////////////////////////////////////
	public void setCaller(Object objCaller)
	{
		if(objCaller instanceof ChatSocketTransmitter)
			channel = (ChatSocketTransmitter)objCaller;
	}
	/////////////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	/////////////////////////////////////////////////////////////////
	public void prepareProcess() throws Exception
	{
	}
	/////////////////////////////////////////////////////////////////
	/**
	 *
	 */
	/////////////////////////////////////////////////////////////////
	public void processCompleted()
	{
	}
	/////////////////////////////////////////////////////////////////
	/**
	 *
	 */
	/////////////////////////////////////////////////////////////////
	public void processFailed()
	{
	}
}
