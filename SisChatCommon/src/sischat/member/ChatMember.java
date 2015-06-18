/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.member;

import sischat.room.*;

import java.io.Serializable;

/**
 *
 * @author Visat
 */
public class ChatMember implements Serializable {
    private int id;
    private String name;
    private int roomID = ChatRoom.INVALID_ROOM_ID;

    public ChatMember(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getRoomID() {
        return this.roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ChatMember))
            return false;

        ChatMember otherMember = (ChatMember)other;
        return
                (this.id == otherMember.id) &&
                (this.name.equals(otherMember.name));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.id;
        hash = 23 * hash + this.name.hashCode();
        return hash;
    }
}
