/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.client;

/**
 *
 * @author Visat
 */
public interface ChatClientObservable {
    public void addListener(ChatClientListener listener);
    public void removeListener(ChatClientListener listener);
}
