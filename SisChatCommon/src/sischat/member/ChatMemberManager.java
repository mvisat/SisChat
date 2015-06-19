/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.member;

import java.net.InetAddress;
import sischat.room.*;

import java.util.*;

/**
 *
 * @author Visat
 */
public class ChatMemberManager {
    private static final int MIN_ID = 0;
    private static int latestID = MIN_ID;
    private static BitSet availableID = new BitSet();
    private static HashSet<String> usernames = new HashSet<>();

    public static synchronized ChatMember add(String name, InetAddress address, int port) {
        if (usernames.contains(name.toLowerCase())) {
            return null;
        }
        else {
            if (latestID == Integer.MAX_VALUE)
                latestID = MIN_ID;
            latestID = availableID.nextClearBit(latestID);
            // Check if member ID is full
            if (latestID == Integer.MAX_VALUE)
                return null;
            availableID.set(latestID);
            usernames.add(name.toLowerCase());
            return new ChatMember(latestID, name, address, port);
        }
    }

    public static synchronized void remove(ChatMember member) {
        ChatRoomManager.leave(member);
        usernames.remove(member.getName().toLowerCase());
        availableID.clear(member.getID());
    }

}
