/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.p2p;

import sischat.member.ChatMember;
import sischat.msg.ChatMessage;

/**
 *
 * @author Visat
 */
public interface P2PListener {
    public void onConnected(ChatMember member);
    public void onDisconnected(ChatMember member);
    public void onMessage(ChatMessage message);
}
