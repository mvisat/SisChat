/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.room;

import sischat.member.*;

import java.util.*;

/**
 *
 * @author Visat
 */
public class ChatRoomManager {
    private static final int MIN_ID = 0;
    private static int latestID = MIN_ID;
    private static BitSet availableID = new BitSet();
    private static HashMap<Integer, ChatRoom> rooms = new HashMap<>();

    public static synchronized ChatRoom add(String name) {
        if (latestID == Integer.MAX_VALUE)
            latestID = MIN_ID;
        latestID = availableID.nextClearBit(latestID);
        // Check if room is full
        if (latestID == Integer.MAX_VALUE)
            return null;
        ChatRoom room = new ChatRoom(latestID, name);
        rooms.put(latestID, room);
        availableID.set(latestID);
        return room;
    }

    public static synchronized ChatRoom join(ChatMember member, int roomID) {
        ChatRoom room = rooms.get(roomID);
        if (room != null) {
            room.add(member);
            member.setRoomID(roomID);
        }
        return room;
    }

    public static synchronized void leave(ChatMember member) {
        int roomID = member.getRoomID();
        ChatRoom room = rooms.get(roomID);
        if (room != null) {
            room.remove(member);
            if (room.isEmpty()) {
                availableID.clear(roomID);
                rooms.remove(roomID);
            }
        }
        member.setRoomID(ChatRoom.INVALID_ROOM_ID);
    }

    public static synchronized ChatRoom get(int roomID) {
        return rooms.get(roomID);
    }

    public static synchronized List<ChatRoom> getRooms() {
        List<ChatRoom> roomList = new LinkedList<>(rooms.values());
        return roomList;
    }
}