/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tarchan
 */
public class ShellAdapter {

    static final Logger logger = Logger.getLogger(ShellAdapter.class.getName());

    public static void main(String[] args) {
        try {
//            URL.setURLStreamHandlerFactory(new IrcURLStreamHandlerFactory());
            System.setProperty("java.protocol.handler.pkgs", "com.mac.tarchan");
            URL url = new URL("irc://irc.ircnet.ne.jp/javabreak");
//            URL url = new URL("irc://tarchan:pass@irc.ircnet.ne.jp/#javabreak");
//            URL url = new URL("irc://irc.freenode.net/#javabreak");
//            URL url = new URL("irc://irc.mozilla.org/firefox");
            URLConnection con = url.openConnection();
            con.addRequestProperty("channel", "#javabreak");
            con.addRequestProperty("channel", "#test");
            con.setRequestProperty("content-encoding", "JIS");
            con.connect();
            while (true) {
                Object reply = con.getContent();
                if (reply == null) break;

                logger.log(Level.INFO, "reply: " + reply);
                // TODO PONGを返す
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
