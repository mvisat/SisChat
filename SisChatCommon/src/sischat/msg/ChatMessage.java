/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.msg;

import sischat.member.ChatMember;

import java.io.Serializable;


/**
 *
 * @author Visat
 */
public class ChatMessage implements Serializable {
    private ChatMember sender;
    private String content;

    public ChatMessage(ChatMember sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public ChatMember getSender() {
        return this.sender;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return this.sender.toString() + ": " + this.content;
    }
}
