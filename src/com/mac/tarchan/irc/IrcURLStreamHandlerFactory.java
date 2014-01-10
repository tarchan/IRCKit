/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mac.tarchan.irc;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * IrcURLStreamHandlerFactory
 * 
 * @author tarchan
 */
public class IrcURLStreamHandlerFactory implements URLStreamHandlerFactory {

    static final String IRC_PROTOCOL = "irc";

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return IRC_PROTOCOL.equals(protocol) ? new Handler() : null;
    }
}
