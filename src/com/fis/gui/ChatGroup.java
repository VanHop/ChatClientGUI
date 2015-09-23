package com.fis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.ftu.ddtp.DDTP;

public class ChatGroup extends JFrame{

	private static final long serialVersionUID = 2068669607586590615L;
	ChatClientUI parent;
	private JList<String> JlistMessage, JlistUserInGroup;
	private JButton btnSend, btnLogout, btnClear, btnClose;
	private JLabel lbListMessage, lbListUser;
	private JTextField tfMessage;
	private String groupName;
	private ArrayList<String> listMessage;
	private ArrayList<String> listUser;
	private String leader;
	public ChatGroup(String groupName, String leader, ArrayList<String> listUserInGroup,
			ChatClientUI chatClientUI) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 300);
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        parent = chatClientUI;
        this.setTitle(parent.getTitle() + " - " + groupName);
        
        this.groupName = groupName;
        this.leader = leader;
        
        listUser = listUserInGroup;
        listMessage = new ArrayList<String>();
        lbListMessage = new JLabel("Nội dung");
        lbListMessage.setBounds(30, 10, 100, 30);
        add(lbListMessage);
        
        lbListUser = new JLabel("Online");
        lbListUser.setBounds(240, 10, 100, 30);
        add(lbListUser);
        
        JScrollPane scrollPanelMessage = new JScrollPane();
        JlistMessage = new JList<String>();
        JlistMessage.setModel(new DefaultListModel<String>());
        scrollPanelMessage.setBounds(10, 50, 170, 150);
        scrollPanelMessage.setViewportView(JlistMessage);
        add(scrollPanelMessage);
        
        JScrollPane scrollPanelOnline = new JScrollPane();
        JlistUserInGroup = new JList<String>();
        DefaultListModel<String> modelOnline = new DefaultListModel<String>();
        for(String username : listUserInGroup)
        	modelOnline.addElement(username);
        JlistUserInGroup.setModel(modelOnline);
        scrollPanelOnline.setBounds(190, 50, 140, 100);
        scrollPanelOnline.setViewportView(JlistUserInGroup);
        add(scrollPanelOnline);
        
        btnLogout = new JButton("Exit");
        btnLogout.setBounds(190, 170, 70, 30);
        btnLogout.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int dialogButton = JOptionPane.YES_NO_OPTION;
				int confirm = JOptionPane.showConfirmDialog(ChatGroup.this, "Bạn có thực sự muốn thoát?","Logout",dialogButton);
				if(confirm == JOptionPane.YES_OPTION){
					try{
						DDTP request = new DDTP();
						request.setString("group", ChatGroup.this.groupName);
						Vector<String> userInGroup = new Vector<String>();
						for(String username : listUser)
							userInGroup.add(username);
						request.setString("username", parent.getTitle());
						request.setVector("listUserInGroup", userInGroup);
						request.setString("message", parent.getTitle() + " đã thoát khỏi nhóm");
						parent.getChannel().sendRequest("MessageProcessor", "logoutGroup", request);
					} catch(Exception e){
						e.printStackTrace();
					}
					
					
				}
			}
		});
        add(btnLogout);
        
        btnClose = new JButton("Close");
        btnClose.setBounds(270, 170, 70, 30);
        btnClose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				ChatGroup.this.dispose();
				parent.getListOnline().clearSelection();
			}
		});
        add(btnClose);
        
        tfMessage = new JTextField();
        tfMessage.setBounds(10, 210, 170, 30);
        add(tfMessage);
        
        btnSend = new JButton("Send");
        btnSend.setBounds(190, 210, 70, 30);
        btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					Vector<String> listUser = new Vector<String>();
					DefaultListModel<String> modelUserInGroup = (DefaultListModel<String>) JlistUserInGroup.getModel();
					for(int i = 0 ; i < modelUserInGroup.size() ; i++){
						listUser.add(modelUserInGroup.elementAt(i));
					}
					DDTP request = new DDTP();
					request.setVector("listUserInGroup", listUser);
					request.setString("groupName", ChatGroup.this.groupName);
					request.setString("message",parent.getTitle() + ": " + tfMessage.getText().trim());
					request.setString("logTime", System.currentTimeMillis() + "");
					parent.getChannel().sendRequest("MessageProcessor", "sendMessageIntoGroup", request);
					tfMessage.setText("");
				} catch(Exception e){
					e.printStackTrace();
				}
				
			}
		});
        add(btnSend);
        
        btnClear = new JButton("Clear");
        btnClear.setBounds(270, 210, 70, 30);
        btnClear.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				DefaultListModel<String> model = (DefaultListModel<String>)JlistMessage.getModel();
				model.clear();
				JlistMessage.setModel(model);
			}
		});
        add(btnClear);
        
        setLocationRelativeTo(null);
        setVisible(false);
        setResizable(false);
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public ArrayList<String> getListUser() {
		return listUser;
	}

	public void setListUser(ArrayList<String> listUser) {
		this.listUser = listUser;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public void addMessage(String message) {
		listMessage.add(message);
		DefaultListModel<String> model = (DefaultListModel<String>) JlistMessage.getModel();
		model.addElement(message);
	}
	public void loadMessage() {
		DefaultListModel<String> model = (DefaultListModel<String>) JlistMessage.getModel();
		model.clear();
		for(String message : listMessage)
			model.addElement(message);
		JlistMessage.setModel(model);
	}

	@SuppressWarnings("rawtypes")
	public void updateGroup(Vector listUserIntoGroup) {
		for(int i = 0 ; i < listUserIntoGroup.size() ; i++){
			if(!listUser.contains(listUserIntoGroup.get(i))){
				addMessage(listUserIntoGroup.get(i) + " đã được thêm vào nhóm");
				listUser.add((String) listUserIntoGroup.get(i));
			}
		}
		for(int i = 0 ; i < listUser.size() ; i++){
			if(!listUserIntoGroup.contains(listUser.get(i))){
				addMessage(listUser.get(i) + " đã bị đẩy khỏi nhóm");
				listUser.remove(i);
				i--;
			}
		}
		DefaultListModel<String> model = (DefaultListModel<String>) JlistUserInGroup.getModel();
		model.clear();
		for(int i = 0 ; i < listUserIntoGroup.size() ; i++)
			if(!parent.getTitle().equals((String) listUserIntoGroup.get(i)))
				model.addElement((String) listUserIntoGroup.get(i));
		JlistUserInGroup.setModel(model);
		
	}
	public void logoutGroup(String username, String message, Vector<String> listUser){
		this.listUser.clear();
		for(String user : listUser)
			if(!user.equals(username))
				this.listUser.add(user);
		DefaultListModel<String> model = (DefaultListModel<String>) JlistUserInGroup.getModel();
		model.clear();
		for(String user : listUser)
			if(!user.equals(username))
				model.addElement(user);
		JlistUserInGroup.setModel(model);
		addMessage(message);
	}
}
