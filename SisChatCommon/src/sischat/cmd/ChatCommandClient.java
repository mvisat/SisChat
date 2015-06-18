/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.cmd;

/**
 *
 * @author Visat
 */
public enum ChatCommandClient implements ChatCommandType {
    CLIENT_LOGIN,
    CLIENT_LOGOUT,
    CLIENT_ROOM_CREATE,
    CLIENT_ROOM_LIST,
    CLIENT_ROOM_JOIN,
    CLIENT_ROOM_LEAVE,
    CLIENT_MESSAGE_SEND,
}
