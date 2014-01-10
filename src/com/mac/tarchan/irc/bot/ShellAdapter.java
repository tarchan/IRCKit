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
            URL url = new URL("irc:tar2014@irc.ircnet.ne.jp/#javabreak");
//            URL url = new URL("irc://tarchan:pass@irc.ircnet.ne.jp/#javabreak");
            URLConnection con = url.openConnection();
            con.addRequestProperty("channel", "#javabreak");
            con.connect();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                while (true) {
                    String line = in.readLine();
                    if (line == null) break;
                    
                    System.out.println(line);
                    logger.log(Level.INFO, line);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
