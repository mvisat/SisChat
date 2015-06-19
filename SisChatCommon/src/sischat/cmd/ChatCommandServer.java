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
public enum ChatCommandServer implements ChatCommandType {
    SERVER_LOGIN_SUCCESS,
    SERVER_LOGIN_FAIL,
    SERVER_LOGOUT,
    SERVER_MEMBER_UPDATE,
    SERVER_ROOM_LIST,
    SERVER_ROOM_CREATE_SUCCESS,
    SERVER_ROOM_CREATE_FAIL,
    SERVER_ROOM_JOIN_SUCCESS,
    SERVER_ROOM_JOIN_FAIL,
    SERVER_ROOM_LEAVE,
    SERVER_ROOM_MEMBER_LEAVE,
    SERVER_ROOM_MEMBER_JOIN,
    SERVER_MESSAGE_SEND,
}
