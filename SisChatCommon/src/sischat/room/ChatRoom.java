/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.room;

import java.io.Serializable;
import sischat.member.*;

import java.util.*;

/**
 *
 * @author Visat
 */
public class ChatRoom implements Serializable {
    private int id;
    private String name;
    private List<ChatMember> members;

    public static final int INVALID_ROOM_ID = -1;

    public ChatRoom(ChatRoom chatRoom) {
        this.id = chatRoom.id;
        this.name = chatRoom.name;
        members = new LinkedList<>();
        for (ChatMember member: chatRoom.members)
            members.add(member);
    }

    public ChatRoom(int roomID, String roomName) {
        this.id = roomID;
        this.name = roomName;
        this.members = new LinkedList<>();
    }

    public void add(ChatMember member) {
        members.add(member);
    }

    public void remove(ChatMember member) {
        members.remove(member);
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<ChatMember> getMembers() {
        return this.members;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
