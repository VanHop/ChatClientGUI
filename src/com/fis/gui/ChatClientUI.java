package com.fis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fis.entities.Message;
import com.fis.util.LinkQueue;
import com.fss.ddtp.SocketTransmitter;
import com.fss.monitor.MonitorProcessor;
import com.ftu.ddtp.ChatSocketTransmitter;
import com.ftu.ddtp.DDTP;

public class ChatClientUI extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private JButton btnSend, btnConnect, btnClearMeassage, btnMakeGroup,btnManageGroup, btnSendFile, btnChoose, btnSocket;
	private JLabel lbChat, lbOnline;
	private JList<String>  listOnline, listGroup;
	private JTextArea listMessages;
	private JTextField tfMessage, tfName, tfNameFile, tfLocalhost, tfPort;
	private JPasswordField tpPass;
	private ChatSocketTransmitter channel;
	private String currentFriend;
	private Map<String, ArrayList<String>> listUserMessage;
	private int index = 0;
	private Map<String, ChatGroup> groupChats;
	private File selectedFile = null;
	public static int PORT;
	private Socket sck;
	public FileOutputStream fos;
	public PrintWriter pw;
	private Thread updateMessage,logThread;
	public LinkQueue<Message> listMessage;
	public LinkQueue<String> logtime;
	public ChatClientUI(){
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 550);
        setLayout(null);
        listMessage = new LinkQueue<Message>();
        listUserMessage = new HashMap<String, ArrayList<String>>();
        groupChats = new HashMap<String, ChatGroup>();
        logtime = new LinkQueue<String>();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            	int dialogButton = JOptionPane.YES_NO_OPTION;
				int confirm = JOptionPane.showConfirmDialog(ChatClientUI.this, "Bạn có thực sự muốn thoát?","Logout",dialogButton);
				if(confirm == JOptionPane.YES_OPTION){
					if(groupChats != null){
						for(String groupName : groupChats.keySet()){
							DDTP request = new DDTP();
							request.setString("group", groupName);
							Vector<String> userInGroup = new Vector<String>();
							for(String username : groupChats.get(groupName).getListUser())
								userInGroup.add(username);
							request.setString("username", ChatClientUI.this.getTitle());
							request.setVector("listUserInGroup", userInGroup);
							request.setString("message", ChatClientUI.this.getTitle() + " đã thoát khỏi nhóm");
							try {
								channel.sendRequest("MessageProcessor", "logoutGroup", request);
							} catch (Exception e) {
								e.printStackTrace();
								setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
							}
						}
						setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
						try {
							if(fos != null)
								fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(pw != null)
							pw.close();
					}
				} 
				else {
					setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				}
            }
        });
        tfLocalhost = new JTextField("localhost");
        tfLocalhost.setBounds(10, 10, 130, 30);
        add(tfLocalhost);
        
        tfPort = new JTextField();
        tfPort.setBounds(150, 10, 100, 30);
        add(tfPort);
        
        btnSocket = new JButton("OK");
        btnSocket.setBounds(260, 10, 100, 30);
        btnSocket.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(tfLocalhost.getText().trim().equals("") || tfPort.getText().trim().equals(""))
					return;
				PORT = Integer.parseInt(tfPort.getText().trim());
				try {
					sck = new Socket(tfLocalhost.getText().trim(),PORT);
					sck.setSoLinger(true,0);
					btnConnect.setEnabled(true);
					tfName.setEnabled(true);
					tpPass.setEnabled(true);
					tfLocalhost.setEnabled(false);
					tfPort.setEnabled(false);
					btnSocket.setEnabled(false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ChatClientUI.this, "Server chưa khởi động!","Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
        add(btnSocket);
        
        tfName = new JTextField(20);
        tfName.setBounds(10, 50+10, 130, 30);
        add(tfName);
        
        tpPass = new JPasswordField(10);
        tpPass.setBounds(150, 50+10, 100, 30);
        add(tpPass);
        
        btnConnect = new JButton("Connect");
        btnConnect.setBounds(260, 50+10, 100, 30);
        btnConnect.addActionListener(this);
        add(btnConnect);
        
        lbChat = new JLabel("Nội dung chat");
        lbChat.setBounds(10, 50+60, 100, 50);
        add(lbChat);
        
        lbOnline = new JLabel("Đang Online");
        lbOnline.setBounds(230, 50+60, 100, 50);
        add(lbOnline);
        
        JScrollPane scrollPanelMessage = new JScrollPane();
        //DefaultListModel<String> modelMessage = new DefaultListModel<String>();
        listMessages = new JTextArea();
        scrollPanelMessage.setBounds(10, 50+110, 200, 200);
        scrollPanelMessage.setViewportView(listMessages);
        add(scrollPanelMessage);
        
        JScrollPane scrollPanelOnline = new JScrollPane();
        DefaultListModel<String> modelOnine = new DefaultListModel<String>();
        listOnline = new JList<String>(modelOnine);
        scrollPanelOnline.setBounds(230, 50+110, 130, 80);
        scrollPanelOnline.setViewportView(listOnline);
        add(scrollPanelOnline);
        listOnline.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					tfMessage.setEnabled(true);
					btnSend.setEnabled(true);
					btnSendFile.setEnabled(true);
					btnChoose.setEnabled(true);
					setCurrentFriend(listOnline.getSelectedIndex());
                }
			}
		});
        JScrollPane scrollPanelGroup = new JScrollPane();
        DefaultListModel<String> modelGroup = new DefaultListModel<String>();
        listGroup = new JList<String>(modelGroup);
        scrollPanelGroup.setBounds(230, 50+200, 130, 70);
        scrollPanelGroup.setViewportView(listGroup);
        listGroup.setModel(new DefaultListModel<String>());
        add(scrollPanelGroup);
        listGroup.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					if(listGroup.getSelectedIndex() == -1){
						btnManageGroup.setEnabled(false);
						return;
					}
					DefaultListModel<String> model = (DefaultListModel<String>) listGroup.getModel();
					String groupName = model.getElementAt(index);
					ChatGroup chatGroup = groupChats.get(groupName);
					index = listGroup.getSelectedIndex();
					listGroup.clearSelection();
					if(chatGroup.getLeader().equals(ChatClientUI.this.getTitle()))
						btnManageGroup.setEnabled(true);
					else
						btnManageGroup.setEnabled(false);
                }
			}
		});
        
        MouseListener mouseListener = new MouseAdapter() {
			@SuppressWarnings("rawtypes")
			public void mouseClicked(MouseEvent mouseEvent) {
				JList theList = (JList) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						DefaultListModel<String> model = (DefaultListModel<String>) listGroup.getModel();
						String groupName = model.getElementAt(index);
						ChatGroup chatGroup = groupChats.get(groupName);
						chatGroup.loadMessage();
						chatGroup.setVisible(true);
					}
				}
			}
		};
		listGroup.addMouseListener(mouseListener);
		
        tfMessage = new JTextField(100);
        tfMessage.setBounds(10, 50+360, 200, 30);
        add(tfMessage);
        
        btnSend = new JButton("Send");
        btnSend.setBounds(230, 50+360, 130, 30);
        btnSend.addActionListener(this);
        add(btnSend);
        
        btnChoose = new JButton("Choose");
        btnChoose.setBounds(10, 50+400, 60, 30);
        btnChoose.addActionListener(this);
        add(btnChoose);
      
        tfNameFile = new JTextField();
        tfNameFile.setBounds(70, 50+400, 140, 30);
        add(tfNameFile);
        
        btnSendFile = new JButton("Send File");
        btnSendFile.setBounds(230, 50+400, 130, 30);
        btnSendFile.addActionListener(this);
        add(btnSendFile);
        
        btnMakeGroup = new JButton("Create Group");
        btnMakeGroup.setBounds(230, 50+280, 130, 30);
        btnMakeGroup.addActionListener(this);
        add(btnMakeGroup);
        
        btnManageGroup = new JButton("Manage Group");
        btnManageGroup.setBounds(230, 50+320, 130, 30);
        btnManageGroup.addActionListener(this);
        add(btnManageGroup);
        
        btnClearMeassage = new JButton("Clear All Message");
        btnClearMeassage.setBounds(10, 50+320, 200, 30);
        btnClearMeassage.addActionListener(this);
        add(btnClearMeassage);
        
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
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setEnable(false);
        
	}

	@SuppressWarnings({ "deprecation", "unused" })
	public void actionPerformed(ActionEvent event) {
		
		if(event.getSource() == btnClearMeassage){
			
			//DefaultListModel<String> model = (DefaultListModel<String>)listMessages.getModel();
	       // model.clear();
	       // listMessages.setModel(model);
			listMessages.setText("");
	        
		} else if(event.getSource() == btnConnect){
			
			if(tfName.getText().trim().equals("") || tpPass.getText().trim().equals(""))
				return;
			ChatClientUI.this.setTitle(tfName.getText().trim());
			try {
				if(!login()){
					JOptionPane.showMessageDialog(ChatClientUI.this, "Tài khoản sai hoặc đang trong trạng thái đăng nhập!",
	                        "Error", JOptionPane.ERROR_MESSAGE);
					ChatClientUI.this.setTitle("");
					return;
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(ChatClientUI.this, "Server chưa khởi động!","Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				ChatClientUI.this.setTitle("");
				return;
			}
			btnClearMeassage.setEnabled(true);
			btnConnect.setEnabled(false);
			btnMakeGroup.setEnabled(true);
			try {
				String strWorkingDir = System.getProperty("user.dir");
				if (strWorkingDir == null || strWorkingDir.equals("")) {
					strWorkingDir = System.getProperty("user.dir");
				}
				if (!strWorkingDir.endsWith("/") || !strWorkingDir.endsWith("\\")) {
					strWorkingDir += "\\";
				}
				fos= new FileOutputStream(strWorkingDir + "log\\logtime\\" + ChatClientUI.this.getTitle() + ".csv",true);
				pw= new PrintWriter(fos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			updateMessage.start();
			logThread.start();
			
		} else if(event.getSource() == btnSend){
			if(tfMessage.getText().equals("test")){
				Thread test = new Thread(new Runnable() {
					public void run() {
						DDTP request = new DDTP();
						int count = 0;
						
						while(count < 1000){
							request.setString("me", ChatClientUI.this.getTitle());
							request.setString("message",ChatClientUI.this.getTitle() + ": " + tfMessage.getText() + count);
							request.setString("friend", currentFriend);
							request.setString("idSubServer", PORT + "");
							request.setString("logTime", System.currentTimeMillis() + "");
							long startingTime = System.currentTimeMillis();
							try {
								channel.sendRequest("MessageProcessor","sendMessage",request);
								request.clear();
								Thread.sleep(3);
							} catch (Exception e){
								e.printStackTrace();
							}
							count++;
						}
						tfMessage.setText("");
					}
				});
				test.start();
				
			} else {
				if(!tfMessage.getText().equals("")){
					DDTP request = new DDTP();
					request.setString("me", this.getTitle());
					request.setString("message",this.getTitle() + ": " + tfMessage.getText());
					request.setString("friend", currentFriend);
					request.setString("idSubServer", PORT + "");
					request.setString("logTime", System.currentTimeMillis() + "");
					try {
						channel.sendRequest("MessageProcessor","sendMessage",request);
					} catch (Exception e){
						e.printStackTrace();
					}
					tfMessage.setText("");
				}
			}
//			if(!tfMessage.getText().equals("")){
//				DDTP request = new DDTP();
//				request.setString("me", this.getTitle());
//				request.setString("message",this.getTitle() + ": " + tfMessage.getText());
//				request.setString("friend", currentFriend);
//				request.setString("idSubServer", PORT + "");
//				request.setString("logTime", System.currentTimeMillis() + "");
//				try {
//					DDTP response = channel.sendRequest("MessageProcessor","sendMessage",request);
//				} catch (Exception e){
//					e.printStackTrace();
//				}
//				tfMessage.setText("");
//			}
		} else if(event.getSource() == btnMakeGroup){
			ArrayList<String> listUser =new ArrayList<String>();
			DefaultListModel<String> model = (DefaultListModel<String>) listOnline.getModel();
			for(int i = 0 ; i < model.size() ; i++)
				listUser.add(model.getElementAt(i));
			MakeGroup makeGroup = new MakeGroup(this,listUser);
			
		} else if(event.getSource() == btnManageGroup){
			DefaultListModel<String> model = (DefaultListModel<String>) listGroup.getModel();
			String groupName = model.getElementAt(index);
			ChatGroup chatGroup = groupChats.get(groupName);
			ArrayList<String> listAllUser =new ArrayList<String>();
			DefaultListModel<String> modelAllUser = (DefaultListModel<String>) listOnline.getModel();
			for(int i = 0 ; i < modelAllUser.size() ; i++)
				listAllUser.add(modelAllUser.getElementAt(i));
			listAllUser.add(this.getTitle());
			GroupManager groupManager = new GroupManager(this,listAllUser, chatGroup.getListUser(), chatGroup.getGroupName());
		} else if(event.getSource() == btnChoose){
			JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle("choosertitle");
		    int result = chooser.showOpenDialog(this);
		    if (result == JFileChooser.APPROVE_OPTION) {
		        selectedFile = chooser.getSelectedFile();
		        tfNameFile.setText(selectedFile.getPath());
		    }
		} else if(event.getSource() == btnSendFile){
			if(tfNameFile.getText().equals(""))
				return;
			if(selectedFile != null){
				try {
					byte[] mybytearray = new byte[(int) selectedFile.length()];
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile));
					bis.read(mybytearray, 0, mybytearray.length);
					DDTP request = new DDTP();
					request.setByteArray("file", mybytearray);
					request.setString("me", this.getTitle());
					request.setString("fileName", selectedFile.getName());
					request.setString("friend", currentFriend);
					request.setString("logTime", System.currentTimeMillis() + "");
					channel.sendRequest("MessageProcessor", "sendFile", request);
					bis.close();
					tfNameFile.setText("");
					selectedFile = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Map<String, ChatGroup> getGroupChats() {
		return groupChats;
	}

	public void setGroupChats(Map<String, ChatGroup> groupChats) {
		this.groupChats = groupChats;
	}

	public JList<String> getListOnline() {
		return listOnline;
	}

	public void setListOnline(JList<String> listOnline) {
		this.listOnline = listOnline;
	}


	@SuppressWarnings("deprecation")
	private boolean login() throws UnknownHostException, IOException{
		channel = new ChatSocketTransmitter(sck)
		{
			public void close()
			{
				if(msckMain != null)
				{
					super.close();
				}
			}
		};
		channel.setPackage("com.fss.thread.");
		channel.start();
		// Request to Server
		DDTP request = new DDTP();
		//request.setRequestID(String.valueOf(System.currentTimeMillis()));
		request.setString("UserName",tfName.getText());
		request.setString("Password",tpPass.getText());
		try {
			channel.sendRequest("MessageProcessor","login",request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	@SuppressWarnings({ "rawtypes", "unused" })
	private void updateListClient(DDTP response){
		Vector vtClient = response.getVector("vtClient");
		DefaultListModel<String> model = getModelUser();
		model.clear();
		for(int i = 0 ; i < vtClient.size(); i++){
			if(vtClient.elementAt(i).equals(this.getTitle()))
				continue;
			model.addElement((String) vtClient.elementAt(i));
		}
		
        listOnline.setModel(model);
	}
	public static void main(String [] args){
		new ChatClientUI();
	}
	
	public ChatSocketTransmitter getChannel() {
		return channel;
	}

//	public DefaultListModel<String> getModelMessage(){
//		return (DefaultListModel<String>) listMessages.getModel();
//	}
	
	public DefaultListModel<String> getModelUser(){
		return (DefaultListModel<String>) listOnline.getModel();
	}
	public void setCurrentFriend(int index) {
		DefaultListModel<String> model_user = getModelUser();
		if(index == -1) return;
		this.currentFriend = model_user.get(index);
		ArrayList<String> messageWithCurrentFriend = listUserMessage.get(this.currentFriend);
		if(messageWithCurrentFriend != null){
			loadListMessageWithCurrentFriend(messageWithCurrentFriend);
		} else {
			setListMessageDefault(this.currentFriend);
		}
	}
	public void loadListMessageWithCurrentFriend(ArrayList<String> listMessage){
		//DefaultListModel<String> model_message = getModelMessage();
		//model_message.clear();
		listMessages.setText("");
		for(String message : listMessage){
			listMessages.append(message + "\n");
		}
	}
	public void setListMessageDefault(String currentFriend){
		//DefaultListModel<String> model_message = getModelMessage();
		//model_message.clear();
		//listMessages.setModel(model_message);
		listMessages.setText("");
		listUserMessage.put(currentFriend, new ArrayList<String>());
	}
	public int getIndexCurrentFriend(String username){
		
		DefaultListModel<String> model = getModelUser();
		for(int i = 0 ; i < model.toArray().length ; i++){
			if(model.toArray()[i].equals(username)){
				return i;
			}
		}
		return 0;
	}
	public void addMessage(String message, String me, String friend) {
		String currentFriend = this.currentFriend;
		if(friend != null)
			currentFriend = friend;
		int index = listOnline.getSelectedIndex();
		int newIndex = getIndexCurrentFriend(currentFriend);
		if(index != newIndex || (index == newIndex && index == -1)){
			listOnline.setSelectedIndex(newIndex);
			setCurrentFriend(getIndexCurrentFriend(currentFriend));
		}
		listMessages.append(message + "\n");
        listUserMessage.get(currentFriend).add(message);
	}

	public void addUser(String username, String message) {
		//DefaultListModel<String> modelMessage = getModelMessage();
		if(username.equals(this.getTitle())){
			listMessages.append("Đăng nhập thành công!" + "\n");
			return;
		}
		DefaultListModel<String> model = getModelUser();
		for(int i = 0 ; i < model.size() ; i++){
			if(model.elementAt(i).equals(username))
				return;
		}
		model.addElement(username);
        listOnline.setModel(model);
        //modelMessage.addElement(message);
       // listMessages.setModel(modelMessage);
	}

	public void removeUser(String username, String message) {
		
		DefaultListModel<String> model = getModelUser();
		int index = 0;
		for(int i = 0 ; i < model.size() ; i++){
			if(model.get(i).equals(username)){
				index = i;
				break;
			}
		}
		model.remove(index);
        listOnline.setModel(model);
      //  DefaultListModel<String> modelMessage = getModelMessage();
      //  modelMessage.addElement(message);
      //  listMessages.setModel(modelMessage);
        listMessages.append(message + "\n");
	}

	public void makeGroup(String groupName, Vector<String> listUserInGroup, String message, String leader) {
		ArrayList<String> listUserGroup = new ArrayList<String>();
		for(String username : listUserInGroup)
			listUserGroup.add(username);
		groupChats.put(groupName, new ChatGroup(groupName, leader, listUserGroup, this));
		listGroup.clearSelection();
		//DefaultListModel<String> modelMessage = (DefaultListModel<String>) listMessages.getModel();
		//modelMessage.addElement(message);
		listMessages.append(message + "\n");
		updateListGroup();
	}

	public void addMessageInGroup(String groupName, String message) {
		ChatGroup chatGroup = groupChats.get(groupName);
		if(!chatGroup.isVisible())
			chatGroup.setVisible(true);
		chatGroup.addMessage(message);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateGroup(String groupName, Vector listUserInGroup,String leader) {
		ChatGroup chatGroup = groupChats.get(groupName);
		if(chatGroup == null){
			makeGroup(groupName, listUserInGroup, leader + " đã thêm bạn vào nhóm" + groupName, leader);
		} else if(!listUserInGroup.contains(this.getTitle())){
		//	DefaultListModel<String> modelMessage = (DefaultListModel<String>) listMessages.getModel();
		//	modelMessage.addElement(leader + " đã đẩy bạn ra khỏi nhóm " + groupName);
			listMessages.append(leader + " đã đẩy bạn ra khỏi nhóm " + groupName + "\n");
			groupChats.remove(groupName);
			chatGroup.dispose();
			updateListGroup();
		} else {
			chatGroup.updateGroup(listUserInGroup);
		}
	}
	public void updateListGroup(){
		DefaultListModel<String> model = (DefaultListModel<String>) listGroup.getModel();
		model.clear();
		for(String groupName : groupChats.keySet())
			model.addElement(groupName);
		listGroup.setModel(model);
	}
	
	public void setEnable(boolean flag){
		tfName.setEnabled(flag);
		tpPass.setEnabled(flag);
		btnChoose.setEnabled(flag);
		btnConnect.setEnabled(flag);
		btnClearMeassage.setEnabled(flag);
		btnMakeGroup.setEnabled(flag);
		btnManageGroup.setEnabled(flag);
		btnSend.setEnabled(flag);
		btnSendFile.setEnabled(flag);
		tfMessage.setEnabled(flag);
		tfNameFile.setEnabled(flag);
	}

	public void logoutGroup(String groupName, String usernamee, String message,
			Vector listUser) {
		if(this.getTitle().equals(usernamee)){
			groupChats.get(groupName).dispose();
			groupChats.remove(groupName);
			updateListGroup();
		} else {
			groupChats.get(groupName).logoutGroup(usernamee, message, listUser);
		}
		
	}

	public void receiverFile(byte[] byteArray, String user, String fileName) {
		
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int confirm = JOptionPane.showConfirmDialog(this, "Bạn đã nhận được 1 fiel từ " + user + ". Bạn có muốn lưu lại không?","Lưu file",dialogButton);
		if(confirm != JOptionPane.YES_OPTION){
			addMessage("Đã hủy nhận file từ " + user,"",user);
			return;
		}
			
		JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Save file");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);

	    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String path = chooser.getSelectedFile().getPath() + "\\" + fileName;
	    	byte[] fileByte = byteArray;
			try {
				FileOutputStream fos;
				fos = new FileOutputStream(path);
				fos.write(fileByte);
				fos.close();
				addMessage("Đã lưu file","",user);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
		
	}

	public void updateListUser(Vector<String> listUser) {
		DefaultListModel<String> model = (DefaultListModel<String>) listOnline.getModel();
		for(String username : listUser)
			if(!model.contains(username) && !username.equals(this.getTitle()))
				model.addElement(username);
		for(int i = 0 ; i < model.size() ; i++)
			if(!listUser.contains(model.get(i)))
				model.remove(i);
	}

}
