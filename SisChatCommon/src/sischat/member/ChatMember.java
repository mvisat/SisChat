/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.member;

import sischat.room.*;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author Visat
 */
public class ChatMember implements Serializable {
    private int id;
    private String name;
    private int roomID = ChatRoom.INVALID_ROOM_ID;
    private InetAddress address;
    private int p2pPort = 0;

    public ChatMember(int id, String name, InetAddress address, int port) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.p2pPort = port;
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

    public InetAddress getAddress() {
        return this.address;
    }

    public void setP2PPort(int port) {
        this.p2pPort = port;
    }

    public int getP2PPort() {
        return this.p2pPort;
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
