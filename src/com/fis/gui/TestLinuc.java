package com.fis.gui;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import com.fis.entities.Message;
import com.fis.util.LinkQueue;
import com.fss.monitor.MonitorProcessor;
import com.ftu.ddtp.ChatSocketTransmitter;
import com.ftu.ddtp.DDTP;

public class TestLinuc{

	private static final long serialVersionUID = 1L;
	public ChatSocketTransmitter channel;
	public String currentFriend;
	public Map<String, ArrayList<String>> listUserMessage;
	public int index = 0;
	public static int PORT = 6666;
	public Socket sck;
	public FileOutputStream fos, fFileMessage;
	public PrintWriter pw, pFileMessage;
	public Thread updateMessage,logThread;
	public LinkQueue<Message> listMessage;
	public LinkQueue<String> logtime;
	public TestLinuc(){
		
		
        listMessage = new LinkQueue<Message>();
       /// listUserMessage = new HashMap<String, ArrayList<String>>();
        logtime = new LinkQueue<String>();

        MonitorProcessor.setRootObject(this);
    	updateMessage = new Thread(new Runnable() {
			
			public void run() {
				while(channel.isOpen()){
					Message message = listMessage.dequeueWait(10);
					if(message == null)
						continue;
					addMessage(message.getMessage(), message.getMe(), message.getFriend());
				}
			}
		});
    	logThread = new Thread(new Runnable() {
			
			public void run() {
				while(channel.isOpen()){
					String log = logtime.dequeueWait(10);
					if(log == null)
						continue;
					pw.write(log + "\n");
					pw.flush();
				}
			}
		});
        
	}
	public static void main(String [] args){
		final TestLinuc test =  new TestLinuc();
		
		args = new String[3];
		
//		args[0] = "b";
//		args[1] = "a";
//		args[2] = "0";
		
		args[0] = "a";
		args[1] = "b";
		args[2] = "1";
		System.out.println(args[0]+args[1]+args[2]);
		try {
			test.sck = new Socket("localhost",6666);
			test.sck.setSoLinger(true,0);
			
			final String username = args[0];
			final String friend = args[1];
			
			test.channel = new ChatSocketTransmitter(test.sck)
			{
				public void close()
				{
					if(msckMain != null)
					{
						super.close();
					}
				}
			};
			test.channel.setPackage("com.fis.thread.");
			test.channel.start();
			// Request to Server
			DDTP request = new DDTP();
			request.setString("UserName",username);
			request.setString("Password","a");
			test.channel.sendRequest("MessageProcessor","login",request);
			try {
				String strWorkingDir = System.getProperty("user.dir");
				if (strWorkingDir == null || strWorkingDir.equals("")) {
					strWorkingDir = System.getProperty("user.dir");
				}
				if (!strWorkingDir.endsWith("/") || !strWorkingDir.endsWith("\\")) {
					strWorkingDir += "/";
				}
				test.fos= new FileOutputStream(strWorkingDir + "log/logtime/" + username + ".csv",true);
				test.pw= new PrintWriter(test.fos);
				
				test.fFileMessage = new FileOutputStream(strWorkingDir + "log/logtime/" + username + "Mes.csv",true);
				test.pFileMessage = new PrintWriter(test.fFileMessage);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			//Thread.sleep(3000);
			
			test.updateMessage.start();
			test.logThread.start();
			System.out.println("ok");
			if(args[2].equals("1")){
				System.out.println("start");
				//Thread.sleep(5000);
				Thread test1 = new Thread(new Runnable() {
					public void run() {
						DDTP request = new DDTP();
						int count = 0;
						
						while(count < 10000){
							request.setString("me", username);
							request.setString("message",username + ": " + "test" + count);
							request.setString("friend", friend);
							request.setString("idSubServer", PORT + "");
							request.setString("logTime", System.currentTimeMillis() + "");
							long startingTime = System.currentTimeMillis();
							try {
								test.channel.sendRequest("MessageProcessor","sendMessage",request);
								request.clear();
								Thread.sleep(3);
							} catch (Exception e){
								e.printStackTrace();
							}
							count++;
						}
						System.out.println("finish");
					}
				});
				test1.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addMessage(String message, String me, String friend) {
		pFileMessage.write(me + "," + message + "," + friend + "\n");
		pFileMessage.flush();
	}
}
