/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.msg;

import java.io.Serializable;

/**
 *
 * @author Visat
 */
public class ChatLoginInfo implements Serializable {
    private String name;
    private int port;

    public ChatLoginInfo(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return this.name;
    }

    public int getPort() {
        return this.port;
    }
}
