package com.fss.monitor;

import java.util.Vector;

import com.fis.entities.Message;
import com.fis.gui.ChatClientUI;
import com.fis.gui.TestLinuc;
import com.ftu.ddtp.SocketProcessor;

/**
 * <p>Title: </p>
 * <p>Description:
 *     MonitorProcessor only read request from server
 *     and pass response for it
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author
 *  - Thai Hoang Hiep
 *  - Dang Dinh Trung
 * @version 2.0
 */

public class MonitorProcessor extends SocketProcessor
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private static Object mobjRoot = null;
	@SuppressWarnings("rawtypes")
	private static Class mclsRoot = MonitorProcessor.class;
	////////////////////////////////////////////////////////
	// RootObject Hashtable
	////////////////////////////////////////////////////////
	public static void setRootObject(Object obj)
	{
		mobjRoot = obj;
		mclsRoot = obj.getClass();
	}
	////////////////////////////////////////////////////////
	public static Object getRootObject()
	{
		return mobjRoot;
	}
	////////////////////////////////////////////////////////
	@SuppressWarnings("rawtypes")
	public static Class getRootClass()
	{
		return mclsRoot;
	}

	///////////////////////////////////////////////////////////
	@SuppressWarnings({ })
	public void userConnected()
	{
		try
		{
			String username = request.getString("username");
			String message = request.getString("message");
			((ChatClientUI)getRootObject()).addUser(username, message);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void receiveMessage(){
		String strMessage = request.getString("message");
		String me = request.getString("me");
		String friend = request.getString("friend");
		Long time = Long.parseLong(request.getString("logTime"));
		((ChatClientUI)getRootObject()).logtime.enqueueNotify(me+","+friend+","+strMessage+ ","+(System.currentTimeMillis()-time));
		Message message = new Message();
		message.setMessage(strMessage);
		message.setMe(me);
		message.setFriend(friend);
		((ChatClientUI)getRootObject()).listMessage.enqueueNotify(message);
	}
	public void userDisconnected()
	{
		try
		{
			((ChatClientUI)getRootObject()).removeUser(request.getString("username"), request.getString("message"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void makeGroup(){
		//Long time = Long.parseLong(request.getString("logTime"));
//		((ChatClientUI)getRootObject()).pw.write("LogTime: " + (System.currentTimeMillis() - time) + "\n");
//		((ChatClientUI)getRootObject()).pw.flush();
		String groupName = request.getString("groupName");
		Vector listUserIntoGroup = request.getVector("listUserInGroup");
		String message = request.getString("message");
		String leader = request.getString("leader");
		((ChatClientUI)getRootObject()).makeGroup(groupName, listUserIntoGroup, message, leader);
	}
	public void sendMessageIntoGroup(){
		Long time = Long.parseLong(request.getString("logTime"));
		((ChatClientUI)getRootObject()).pw.write("LogTime: " + (System.currentTimeMillis() - time) + "\n");
		((ChatClientUI)getRootObject()).pw.flush();
		String groupName = request.getString("groupName");
		String message = request.getString("message");
		((ChatClientUI)getRootObject()).addMessageInGroup(groupName, message);
	}
	@SuppressWarnings("rawtypes")
	public void updateGroup(){
		String groupName = request.getString("groupName");
		Vector listUserInGroup = request.getVector("listUserInGroup");
		//String message = request.getString("message");
		String leader = request.getString("leader");
		((ChatClientUI)getRootObject()).updateGroup(groupName, listUserInGroup,leader);
	}
	public void receiveFile(){
		Long time = Long.parseLong(request.getString("logTime"));
		//((ChatClientUI)getRootObject()).logtime.add("LogTime: " + (System.currentTimeMillis() - time));
		((ChatClientUI)getRootObject()).pw.write("LogTime: " + (System.currentTimeMillis() - time) + "\n");
		((ChatClientUI)getRootObject()).pw.flush();
		((ChatClientUI)getRootObject()).receiverFile(request.getByteArray("file"),request.getString("me"), request.getString("fileName"));
	}
	@SuppressWarnings("rawtypes")
	public void logoutGroup(){
		String groupName = request.getString("group");
		String usernamee = request.getString("username");
		String message = request.getString("message");
		Vector listUser = request.getVector("listUserInGroup");
		((ChatClientUI)getRootObject()).logoutGroup(groupName, usernamee, message,listUser);
	}
	public void updateListUser(){
		Vector<String> listUser = request.getVector("listUser");
		((ChatClientUI)getRootObject()).updateListUser(listUser);
	}
}
