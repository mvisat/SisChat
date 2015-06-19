/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.client;

import java.util.List;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.text.*;
import sischat.member.ChatMember;
import sischat.msg.ChatMessage;
import sischat.p2p.P2PClient;
import sischat.p2p.P2PListener;
import sischat.p2p.P2PServer;
import sischat.room.ChatRoom;

/**
 *
 * @author Visat
 */
public class ChatClientUI extends javax.swing.JFrame implements ChatClientListener, P2PListener {
    private ChatMember myself = null;
    private ChatClient chatClient = new ChatClient();
    private DefaultListModel listMember = new DefaultListModel();
    private P2PServer p2pServer;
    private HashMap<ChatMember, ChatP2P> p2pMap = new HashMap<>();

    /**
     * Creates new form ChatClientUI
     */
    public ChatClientUI() {
        initComponents();
        resetComponents();
        chatClient.addListener(this);
        initP2PServer();
    }

    private void initP2PServer() {
        p2pServer = new P2PServer();
        p2pServer.addListener(this);
    }

    private void resetComponents() {
        resetPanelServer();
        setEnabledComponents(panelServer, true);

        resetPanelUser();
        setEnabledComponents(panelUser, false);

        resetPanelRoom();
        setEnabledComponents(panelRoom, false);

        resetPanelMessage();
        setEnabledComponents(panelMessage, false);
    }

    private void resetPanelServer() {
        buttonConnect.setText("Connect");
    }

    private void resetPanelUser() {
        textUsername.setText("");
        buttonLogin.setText("Login");
        textRoomName.setText("");
    }

    private void resetPanelRoom() {
        buttonJoinLeaveRoom.setText("Join");
        boxRoomList.removeAllItems();
    }

    private void resetPanelMessage() {
        labelChatRoom.setText("Chat Room");
        textMessagePane.setText("");
        textMessage.setText("");
        listMember.removeAllElements();
        buttonPrivateChat.setEnabled(false);
    }

    private void setEnabledComponents(Container container, boolean enabled) {
        for (Component c: container.getComponents())
            c.setEnabled(enabled);
    }

    private void showDialogError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showDialogWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showDialogInformation(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void appendMessage(String message, Color bgColor, Color fgColor, int alignment, boolean bold, boolean pasteled) {
        StyledDocument doc = textMessagePane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        if (pasteled) {
            Color bgPastelColor = new Color(
                (bgColor.getRed()+255)/2,
                (bgColor.getGreen()+255)/2,
                (bgColor.getBlue()+255)/2);
            StyleConstants.setBackground(style, bgPastelColor);
            Color fgPastelColor = new Color(
                (fgColor.getRed()+255)/2,
                (fgColor.getGreen()+255)/2,
                (fgColor.getBlue()+255)/2);
            StyleConstants.setForeground(style, fgPastelColor);
        }
        else {
            StyleConstants.setBackground(style, bgColor);
            StyleConstants.setForeground(style, fgColor);
        }
        StyleConstants.setAlignment(style, alignment);
        StyleConstants.setBold(style, bold);
        try {
            doc.insertString(doc.getLength(), message, style);
            doc.setParagraphAttributes(doc.getLength()-message.length(), doc.getLength()-1, style, false);
        }
        catch (BadLocationException e) {}
    }

    @Override
    public void onLoginSuccess(ChatClient sender, ChatMember member) {
        System.out.println("onLoginSuccess");
        myself = member;
        setEnabledComponents(panelUser, false);
        buttonLogin.setText("Logout");
        buttonLogin.setEnabled(true);
        setEnabledComponents(panelRoom, true);
        textRoomName.requestFocus();
        sender.doRoomList();
    }

    @Override
    public void onLoginFail(ChatClient sender) {
        System.out.println("onLoginFail");
        buttonLogin.setEnabled(true);
        showDialogError("Error", "Sorry, that username has already been taken.");
        textUsername.requestFocus();
    }

    @Override
    public void onRoomList(ChatClient sender, List<ChatRoom> rooms) {
        System.out.println("onRoomList");
        boxRoomList.removeAllItems();
        for (ChatRoom room: rooms)
            boxRoomList.addItem(room);
        if (boxRoomList.getItemCount() == 0)
            buttonJoinLeaveRoom.setEnabled(false);
        else {
            boxRoomList.setSelectedIndex(0);
            buttonJoinLeaveRoom.setEnabled(true);
        }
    }

    @Override
    public void onRoomCreationSuccess(ChatClient sender, ChatRoom room) {
        System.out.println("onRoomCreationSuccess");
        boxRoomList.addItem(room);
        boxRoomList.setSelectedItem(room);
        sender.doJoinRoom(room.getID());
    }

    @Override
    public void onRoomCreationFail(ChatClient sender) {
        System.out.println("onRoomFail");
        showDialogError("Error", "Failed to create room.");
        sender.doRoomList();
    }

    @Override
    public void onRoomJoinSuccess(ChatClient sender, ChatRoom room) {
        System.out.println("onRoomJoinSuccess");
        setEnabledComponents(panelRoom, false);
        buttonJoinLeaveRoom.setText("Leave");
        buttonJoinLeaveRoom.setEnabled(true);
        setEnabledComponents(panelMessage, true);
        resetPanelMessage();
        labelChatRoom.setText("Chat Room - " + room.getName());
        for (ChatMember member: room.getMembers())
            listMember.addElement(member);
        textMessage.requestFocus();
    }

    @Override
    public void onRoomJoinFail(ChatClient sender) {
        showDialogError("Error", "Sorry, this room is no longer exists.");
        sender.doRoomList();
    }

    @Override
    public void onMessage(ChatClient sender, ChatMessage message) {
        System.out.println("onMessage");
        if (message.getSender().equals(myself)) {
            appendMessage("\n" + myself.toString() + "\n", Color.white, Color.red, StyleConstants.ALIGN_RIGHT, true, true);
            appendMessage(message.getContent() + "\n", Color.white, Color.black, StyleConstants.ALIGN_RIGHT, false, false);
            textMessagePane.setCaretPosition(textMessagePane.getStyledDocument().getLength());
        }
        else {
            appendMessage("\n" + message.getSender().getName() + "\n", Color.white, Color.blue, StyleConstants.ALIGN_LEFT, true, true);
            appendMessage(message.getContent() + "\n", Color.white, Color.black, StyleConstants.ALIGN_LEFT, false, false);
        }
    }

    @Override
    public void onRoomMemberLeave(ChatClient sender, ChatMember member) {
        System.out.println("onRoomMemberLeave");
        listMember.removeElement(member);
        appendMessage(member.toString() + " left the chat.\n", Color.white, Color.gray, StyleConstants.ALIGN_CENTER, false, false);
    }

    @Override
    public void onRoomMemberJoin(ChatClient sender, ChatMember member) {
        System.out.println("onRoomMemberJoin");
        if (!member.equals(myself)) {
            listMember.addElement(member);
            appendMessage(member.toString() + " joined the chat.\n", Color.white, Color.gray, StyleConstants.ALIGN_CENTER, false, false);
        }
        else {
            appendMessage("You joined the chat.\n", Color.white, Color.gray, StyleConstants.ALIGN_CENTER, false, false);
        }
    }

    @Override
    public void onLogout(ChatClient sender) {
        System.out.println("onLogout");
        myself = null;
        resetPanelMessage();
        resetPanelRoom();
        setEnabledComponents(panelMessage, false);
        setEnabledComponents(panelRoom, false);
        setEnabledComponents(panelUser, true);
        buttonLogin.setText("Login");
    }

    @Override
    public void onRoomLeave(ChatClient sender) {
        resetPanelMessage();
        setEnabledComponents(panelMessage, false);
        setEnabledComponents(panelRoom, true);
        buttonJoinLeaveRoom.setText("Join");
        buttonJoinLeaveRoom.setEnabled(false);
        sender.doRoomList();
    }

    @Override
    public void onDisconnected(ChatClient sender) {
        showDialogError("Error", "Sorry, you are disconnected from server.");
        resetComponents();
    }

    @Override
    public void onConnected(ChatMember member) {
        if (!p2pMap.containsKey(member)) {
            if (member != myself) {
                P2PClient client = new P2PClient(myself, member);
                if (client.connect()) {
                    ChatP2P p2p = new ChatP2P(client);
                    p2p.setLocationRelativeTo(this);
                    p2p.setVisible(true);
                    p2pMap.put(member, p2p);
                    client.doLogin();
                    System.out.println("doLogin");
                }
                else {
                    showDialogError("Error", "Sorry, failed to initiate private chat with " + member.getName() + ".");
                }
            }
            else {
                showDialogError("Error", "Sorry, you can't chat with yourself.");
            }
        }
        else {
            p2pMap.get(member).setVisible(true);
        }
    }

    @Override
    public void onDisconnected(ChatMember member) {
        /*
        if (p2pMap.containsKey(member)) {
            p2pMap.get(member).dispose();
            p2pMap.remove(member);
        }
        */
    }

    @Override
    public void onMessage(ChatMessage message) {
        System.out.println("on P2PMessage");
        if (p2pMap.containsKey(message.getSender())) {
            p2pMap.get(message.getSender()).onMessage(message);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMemberList = new javax.swing.JPopupMenu();
        menuPrivateMessage = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        panelServer = new javax.swing.JPanel();
        labelServerAddress = new javax.swing.JLabel();
        textServerAddress = new javax.swing.JTextField();
        textServerPort = new javax.swing.JFormattedTextField();
        labelServerPort = new javax.swing.JLabel();
        buttonConnect = new javax.swing.JButton();
        panelUser = new javax.swing.JPanel();
        labelUsername = new javax.swing.JLabel();
        textUsername = new javax.swing.JTextField();
        buttonLogin = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        panelRoom = new javax.swing.JPanel();
        boxRoomList = new javax.swing.JComboBox();
        textRoomName = new javax.swing.JTextField();
        labelRoomName = new javax.swing.JLabel();
        buttonCreateRoom = new javax.swing.JButton();
        buttonJoinLeaveRoom = new javax.swing.JButton();
        buttonRefreshRoom = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        panelMessage = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listRoomMember = new javax.swing.JList(listMember);
        textMessage = new javax.swing.JTextField();
        buttonSend = new javax.swing.JButton();
        labelChatRoom = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textMessagePane = new javax.swing.JTextPane();
        buttonPrivateChat = new javax.swing.JButton();

        menuPrivateMessage.setText("jMenuItem1");
        menuPrivateMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPrivateMessageActionPerformed(evt);
            }
        });
        popupMemberList.add(menuPrivateMessage);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SisChat");
        setName("frameMain"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        labelServerAddress.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelServerAddress.setLabelFor(textServerAddress);
        labelServerAddress.setText("Server Address:");

        textServerAddress.setText("localhost");
        textServerAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textServerAddressActionPerformed(evt);
            }
        });

        textServerPort.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        textServerPort.setText("1500");
        textServerPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textServerPortActionPerformed(evt);
            }
        });

        labelServerPort.setText("Port:");

        buttonConnect.setText("Connect");
        buttonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelServerLayout = new javax.swing.GroupLayout(panelServer);
        panelServer.setLayout(panelServerLayout);
        panelServerLayout.setHorizontalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelServerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textServerAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelServerPort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelServerLayout.setVerticalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelServerPort)
                    .addComponent(buttonConnect)
                    .addComponent(textServerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelServerAddress))
                .addGap(0, 0, 0))
        );

        labelUsername.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelUsername.setText("Username:");

        textUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textUsernameActionPerformed(evt);
            }
        });
        textUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textUsernameKeyTyped(evt);
            }
        });

        buttonLogin.setText("Login");
        buttonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelUserLayout = new javax.swing.GroupLayout(panelUser);
        panelUser.setLayout(panelUserLayout);
        panelUserLayout.setHorizontalGroup(
            panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUserLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textUsername)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelUserLayout.setVerticalGroup(
            panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(textUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(buttonLogin)
                .addComponent(labelUsername))
        );

        textRoomName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textRoomNameActionPerformed(evt);
            }
        });

        labelRoomName.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelRoomName.setText("Room Name:");

        buttonCreateRoom.setText("Create");
        buttonCreateRoom.setMinimumSize(new java.awt.Dimension(100, 23));
        buttonCreateRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateRoomActionPerformed(evt);
            }
        });

        buttonJoinLeaveRoom.setText("Join");
        buttonJoinLeaveRoom.setMinimumSize(new java.awt.Dimension(100, 23));
        buttonJoinLeaveRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonJoinLeaveRoomActionPerformed(evt);
            }
        });

        buttonRefreshRoom.setText("Refresh");
        buttonRefreshRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRefreshRoomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelRoomLayout = new javax.swing.GroupLayout(panelRoom);
        panelRoom.setLayout(panelRoomLayout);
        panelRoomLayout.setHorizontalGroup(
            panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRoomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buttonRefreshRoom, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                    .addComponent(labelRoomName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textRoomName)
                    .addComponent(boxRoomList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonCreateRoom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonJoinLeaveRoom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelRoomLayout.setVerticalGroup(
            panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRoomLayout.createSequentialGroup()
                .addGroup(panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCreateRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textRoomName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRoomName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRoomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonJoinLeaveRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boxRoomList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonRefreshRoom)))
        );

        listRoomMember.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listRoomMember.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listRoomMemberMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listRoomMemberMouseReleased(evt);
            }
        });
        listRoomMember.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listRoomMemberValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listRoomMember);

        textMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textMessageActionPerformed(evt);
            }
        });

        buttonSend.setText("Send");
        buttonSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSendActionPerformed(evt);
            }
        });

        labelChatRoom.setText("Chat Room");

        jLabel2.setLabelFor(listRoomMember);
        jLabel2.setText("Member List");

        textMessagePane.setEditable(false);
        jScrollPane3.setViewportView(textMessagePane);

        buttonPrivateChat.setText("Private Chat");
        buttonPrivateChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrivateChatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMessageLayout = new javax.swing.GroupLayout(panelMessage);
        panelMessage.setLayout(panelMessageLayout);
        panelMessageLayout.setHorizontalGroup(
            panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMessageLayout.createSequentialGroup()
                        .addComponent(textMessage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSend, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMessageLayout.createSequentialGroup()
                        .addComponent(labelChatRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(buttonPrivateChat, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelMessageLayout.setVerticalGroup(
            panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMessageLayout.createSequentialGroup()
                .addGroup(panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelChatRoom)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSend)
                    .addComponent(buttonPrivateChat))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addComponent(panelServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRoom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSeparator2)
            .addComponent(jSeparator3)
            .addComponent(panelUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textServerAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textServerAddressActionPerformed
        buttonConnect.doClick();
    }//GEN-LAST:event_textServerAddressActionPerformed

    private void buttonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConnectActionPerformed
        buttonConnect.setEnabled(false);
        if (chatClient.isConnected()) {
            chatClient.disconnect();
            resetComponents();
        }
        else {
            try {
                textServerAddress.setText(textServerAddress.getText().trim());
                textServerPort.setText(textServerPort.getText().trim());
                if (textServerAddress.getText().length() == 0) {
                    textServerAddress.requestFocus();
                }
                else if (textServerPort.getText().length() == 0) {
                    textServerPort.requestFocus();
                }
                else {
                    if (!chatClient.connect(
                            textServerAddress.getText(),
                            Integer.parseInt(textServerPort.getText())))
                        showDialogError("Error", "Failed to connect to the server.");
                }
            }
            catch (Exception e) {
                showDialogError("Error", e.getMessage());
            }
        }
        boolean connected = chatClient.isConnected();
        setEnabledComponents(panelServer, !connected);
        setEnabledComponents(panelUser, connected);
        buttonConnect.setText(connected ? "Disconnect" : "Connect");
        buttonConnect.setEnabled(true);
        if (connected) {
            textUsername.requestFocus();
            p2pServer.connect();
            p2pServer.start();
            System.out.println("PORT: " + p2pServer.getPort());
            chatClient.start();
        }
    }//GEN-LAST:event_buttonConnectActionPerformed

    private void textUsernameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textUsernameKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isLetterOrDigit(c) || Character.isISOControl(c)))
            evt.consume();
    }//GEN-LAST:event_textUsernameKeyTyped

    private void buttonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoginActionPerformed
        if (chatClient.isConnected()) {
            if (chatClient.isLoggedIn()) {
                chatClient.doLogout();
                p2pServer.disconnect();
                myself = null;
            }
            else {
                textUsername.setText(textUsername.getText().trim());
                String username = textUsername.getText();
                if (username.length() > 0)
                    chatClient.doLogin(username, p2pServer.getPort());
                else
                    textUsername.requestFocus();
            }
        }
    }//GEN-LAST:event_buttonLoginActionPerformed

    private void buttonCreateRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCreateRoomActionPerformed
        if (chatClient.isConnected()) {
            textRoomName.setText(textRoomName.getText().trim());
            String roomName = textRoomName.getText();
            if (roomName.length() > 0)
                chatClient.doCreateRoom(roomName);
            else
                textRoomName.requestFocus();
        }
    }//GEN-LAST:event_buttonCreateRoomActionPerformed

    private void buttonJoinLeaveRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonJoinLeaveRoomActionPerformed
        if (chatClient.isJoiningRoom()) {
            chatClient.doLeaveRoom();
        }
        else {
            ChatRoom room = (ChatRoom) boxRoomList.getSelectedItem();
            if (room != null) {
                chatClient.doJoinRoom(room.getID());
            }
        }
    }//GEN-LAST:event_buttonJoinLeaveRoomActionPerformed

    private void buttonSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSendActionPerformed
        if (chatClient.isConnected()) {
            textMessage.setText(textMessage.getText().trim());
            String message = textMessage.getText();
            if (message.length() > 0) {
                chatClient.doMessage(message);
                textMessage.setText("");
            }
            else {
                textMessage.requestFocus();
            }
        }
    }//GEN-LAST:event_buttonSendActionPerformed

    private void buttonRefreshRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRefreshRoomActionPerformed
        chatClient.doRoomList();
    }//GEN-LAST:event_buttonRefreshRoomActionPerformed

    private void textUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textUsernameActionPerformed
        buttonLogin.doClick();
    }//GEN-LAST:event_textUsernameActionPerformed

    private void textServerPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textServerPortActionPerformed
        buttonConnect.doClick();
    }//GEN-LAST:event_textServerPortActionPerformed

    private void textMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textMessageActionPerformed
        buttonSend.doClick();
    }//GEN-LAST:event_textMessageActionPerformed

    private void textRoomNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textRoomNameActionPerformed
        buttonCreateRoom.doClick();
    }//GEN-LAST:event_textRoomNameActionPerformed

    private void listRoomMemberMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listRoomMemberMousePressed
        if (evt.isPopupTrigger() && !listRoomMember.isSelectionEmpty() &&
                listRoomMember.locationToIndex(evt.getPoint()) == listRoomMember.getSelectedIndex()) {
            ChatMember member = (ChatMember)listRoomMember.getSelectedValue();
            if (member != null) {
                menuPrivateMessage.setActionCommand(member.getName());
                menuPrivateMessage.setText("Send message to " + member.getName());
                menuPrivateMessage.setEnabled(!member.equals(myself));
                popupMemberList.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_listRoomMemberMousePressed

    private void listRoomMemberMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listRoomMemberMouseReleased
        listRoomMemberMousePressed(evt);
    }//GEN-LAST:event_listRoomMemberMouseReleased

    private void menuPrivateMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPrivateMessageActionPerformed
        // TODO: Chat via P2P
        if (!listRoomMember.isSelectionEmpty()) {
            ChatMember member = (ChatMember)listRoomMember.getSelectedValue();
            onConnected(member);
        }
    }//GEN-LAST:event_menuPrivateMessageActionPerformed

    private void buttonPrivateChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrivateChatActionPerformed
        if (!listRoomMember.isSelectionEmpty())
            onConnected((ChatMember)listRoomMember.getSelectedValue());
    }//GEN-LAST:event_buttonPrivateChatActionPerformed

    private void listRoomMemberValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listRoomMemberValueChanged
        buttonPrivateChat.setEnabled(!listRoomMember.isSelectionEmpty() && listRoomMember.getSelectedValue() != myself);
    }//GEN-LAST:event_listRoomMemberValueChanged

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        p2pServer.disconnect();
        chatClient.disconnect();
    }//GEN-LAST:event_formWindowClosed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatClientUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new ChatClientUI();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox boxRoomList;
    private javax.swing.JButton buttonConnect;
    private javax.swing.JButton buttonCreateRoom;
    private javax.swing.JButton buttonJoinLeaveRoom;
    private javax.swing.JButton buttonLogin;
    private javax.swing.JButton buttonPrivateChat;
    private javax.swing.JButton buttonRefreshRoom;
    private javax.swing.JButton buttonSend;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelChatRoom;
    private javax.swing.JLabel labelRoomName;
    private javax.swing.JLabel labelServerAddress;
    private javax.swing.JLabel labelServerPort;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JList listRoomMember;
    private javax.swing.JMenuItem menuPrivateMessage;
    private javax.swing.JPanel panelMessage;
    private javax.swing.JPanel panelRoom;
    private javax.swing.JPanel panelServer;
    private javax.swing.JPanel panelUser;
    private javax.swing.JPopupMenu popupMemberList;
    private javax.swing.JTextField textMessage;
    private javax.swing.JTextPane textMessagePane;
    private javax.swing.JTextField textRoomName;
    private javax.swing.JTextField textServerAddress;
    private javax.swing.JFormattedTextField textServerPort;
    private javax.swing.JTextField textUsername;
    // End of variables declaration//GEN-END:variables

}
