package com.fis.gui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.ftu.ddtp.DDTP;

public class GroupManager extends JFrame{
	private static final long serialVersionUID = 1045257636178929643L;
	private String nameOfGroup;
	private ArrayList<String> listAllUser;
	private Vector<String> listUserInGroup;
	
	private JButton btnAdd, btnDelete, btnOK, btnCancel;
	private JLabel lbAllUsr, lbUserInGroup, lbNameGroup;
	private JList<String> JlistAllUser, JlistUserInGroup;
	private JTextField tfNameOfGroup;
	@SuppressWarnings("unused")
	private ChatClientUI parent;
	
	public GroupManager(final ChatClientUI parent,ArrayList<String> listAllUsers, final ArrayList<String> listUserIntoGroup, String groupName) throws HeadlessException {
		super();
		this.parent = parent;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 350);
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		//this.listAllUser = listAllUser;
        this.listAllUser = new ArrayList<String>();
        for(String username : listAllUsers){
        	if(!listUserIntoGroup.contains(username))
        		listAllUser.add(username);
        }
		this.listUserInGroup = new Vector<String>();
		
		lbNameGroup = new JLabel("Nhập tên Group");
		lbNameGroup.setBounds(10, 30, 100, 30);
		add(lbNameGroup);
		
		tfNameOfGroup = new JTextField(groupName);
		tfNameOfGroup.setBounds(140, 30, 100, 30);
		tfNameOfGroup.setEnabled(false);
		add(tfNameOfGroup);
		
		lbAllUsr = new JLabel("List User");
		lbAllUsr.setBounds(30, 100, 100, 30);
		lbUserInGroup = new JLabel("User In Group");
		lbUserInGroup.setBounds(180, 100, 100, 30);
		add(lbAllUsr);
		add(lbUserInGroup);
		
		JlistAllUser = new JList<String>();
		JlistAllUser = new JList<String>();
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String username : listAllUser)
			model.addElement(username);
		JlistAllUser.setModel(model);
		JlistAllUser.setBounds(10, 150, 100, 100);

		JlistUserInGroup = new JList<String>();
		JlistUserInGroup.setBounds(180, 150, 100, 100);
		DefaultListModel<String> modelGroup = new DefaultListModel<String>();
		for(String username : listUserIntoGroup)
			modelGroup.addElement(username);
		JlistUserInGroup.setModel(modelGroup);
		add(JlistAllUser);
		add(JlistUserInGroup);
		
		btnAdd = new JButton(">>");
		btnAdd.setBounds(120, 170, 50, 20);
		btnAdd.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(JlistAllUser.getSelectedIndex() == -1)
					return;
				DefaultListModel<String> modelAllUser = (DefaultListModel<String>) JlistAllUser.getModel();
				DefaultListModel<String> modelUserInGroup = (DefaultListModel<String>) JlistUserInGroup.getModel();
				
				String username = modelAllUser.get(JlistAllUser.getSelectedIndex());
				modelAllUser.remove(JlistAllUser.getSelectedIndex());
				JlistAllUser.setModel(modelAllUser);
				modelUserInGroup.addElement(username);
				JlistUserInGroup.setModel(modelUserInGroup);
				
			}
		});
		btnDelete = new JButton("<<");
		btnDelete.setBounds(120, 210, 50, 20);
		btnDelete.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(JlistUserInGroup.getSelectedIndex() == -1)
					return;
				DefaultListModel<String> modelAllUser = (DefaultListModel<String>) JlistAllUser.getModel();
				DefaultListModel<String> modelUserInGroup = (DefaultListModel<String>) JlistUserInGroup.getModel();
				
				if(parent.getTitle().equals(modelUserInGroup.get(JlistUserInGroup.getSelectedIndex())))
					return;
				
				String username = modelUserInGroup.get(JlistUserInGroup.getSelectedIndex());
				modelUserInGroup.remove(JlistUserInGroup.getSelectedIndex());
				JlistUserInGroup.setModel(modelUserInGroup);
				modelAllUser.addElement(username);
				JlistAllUser.setModel(modelAllUser);
			}
		});
		add(btnAdd);
		add(btnDelete);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(100,270,80,30);
		btnCancel.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				GroupManager.this.dispose();
			}
		});
		add(btnCancel);
		
		btnOK = new JButton("OK");
		btnOK.setBounds(200, 270, 70, 30);
		btnOK.addActionListener(new ActionListener() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void actionPerformed(ActionEvent arg0) {
				if(tfNameOfGroup.getText().trim().equals("")){
					tfNameOfGroup.requestFocus();
					return;
				} else {
					try {
						GroupManager.this.setVisible(false);
						DefaultListModel<String> modelUserInGroup = (DefaultListModel<String>) JlistUserInGroup.getModel();
						for(int i = 0;i < modelUserInGroup.size() ; i++)
							listUserInGroup.add(modelUserInGroup.getElementAt(i));
						nameOfGroup = tfNameOfGroup.getText().trim();
						DDTP request = new DDTP();
						request.setString("groupName", nameOfGroup);
						request.setVector("listUserInGroup", listUserInGroup);
						Vector principleListUser = new Vector();
						for(String username : listUserIntoGroup)
							principleListUser.add(username);
						request.setVector("listUserIntoGroup", principleListUser);
						request.setString("leader", parent.getTitle());
						parent.getChannel().sendRequest("MessageProcessor", "updateGroup", request);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		add(btnOK);
		
		setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
		
	}

	public String getNameOfGroup() {
		return nameOfGroup;
	}
	public void setNameOfGroup(String nameOfGroup) {
		this.nameOfGroup = nameOfGroup;
	}
	public ArrayList<String> getListAllUser() {
		return listAllUser;
	}
	public void setListAllUser(ArrayList<String> listAllUser) {
		this.listAllUser = listAllUser;
	}
	public Vector<String> getListUserInGroup() {
		return listUserInGroup;
	}
	public void setListUserInGroup(Vector<String> listUserInGroup) {
		this.listUserInGroup = listUserInGroup;
	}

}

