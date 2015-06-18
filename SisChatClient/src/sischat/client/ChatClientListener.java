/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.client;

import java.util.List;
import sischat.member.ChatMember;
import sischat.msg.ChatMessage;
import sischat.room.ChatRoom;

/**
 *
 * @author Visat
 */
public interface ChatClientListener {
    public void onLoginSuccess(ChatClient sender, ChatMember member);
    public void onLoginFail(ChatClient sender);
    public void onLogout(ChatClient sender);
    public void onRoomList(ChatClient sender, List<ChatRoom> rooms);
    public void onRoomCreationSuccess(ChatClient sender, ChatRoom room);
    public void onRoomCreationFail(ChatClient sender);
    public void onRoomJoinSuccess(ChatClient sender, ChatRoom room);
    public void onRoomJoinFail(ChatClient sender);
    public void onRoomLeave(ChatClient sender);
    public void onRoomMemberJoin(ChatClient sender, ChatMember member);
    public void onRoomMemberLeave(ChatClient sender, ChatMember member);
    public void onMessage(ChatClient sender, ChatMessage message);
    public void onDisconnected(ChatClient sender);
}
