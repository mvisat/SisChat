/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.p2p;

/**
 *
 * @author Visat
 */
public interface P2PObservable {
    public void addListener(P2PListener listener);
    public void removeListener(P2PListener listener);
}
