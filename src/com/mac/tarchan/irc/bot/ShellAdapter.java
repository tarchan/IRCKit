/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mac.tarchan.irc.bot;

import com.mac.tarchan.irc.IrcURLStreamHandlerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            URL.setURLStreamHandlerFactory(new IrcURLStreamHandlerFactory());
//            URL url = new URL("irc://irc.ircnet.ne.jp/#javabreak");
//            URL url = new URL("irc://tarchan:pass@irc.ircnet.ne.jp/#javabreak");
            URL url = new URL("irc://irc.freenode.net/#javabreak");
            URLConnection con = url.openConnection();
            con.addRequestProperty("channel", "#javabreak");
            con.addRequestProperty("channel", "#test");
            con.setRequestProperty("content-encoding", "JIS");
            con.connect();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "JIS"))) {
                while (true) {
                    String line = in.readLine();
                    // TODO String line = con.getContent();
                    Object reply = con.getContent();
                    if (line == null) break;
                    
                    logger.log(Level.INFO, line);
                    logger.log(Level.INFO, "reply: " + reply);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
