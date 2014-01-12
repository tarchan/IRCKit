/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import java.io.IOException;
import java.io.PrintStream;
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
            System.setProperty("java.content.handler.pkgs", "com.mac.tarchan");
//            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s%n%4$s: %5$s%6$s%n");
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %2$s %4$s: %5$s%6$s%n");
//            URL url = new URL("irc://irc.ircnet.ne.jp/javabreak");
            URL url = new URL("irc", "irc.ircnet.ne.jp", "javabreak");
//            URL url = new URL("irc://tarchan:pass@irc.ircnet.ne.jp/#javabreak");
//            URL url = new URL("irc://irc.freenode.net/#javabreak");
//            URL url = new URL("irc://irc.mozilla.org/firefox");
            URLConnection con = url.openConnection();
            con.addRequestProperty("channel", "#javabreak");
            con.addRequestProperty("channel", "#test");
            con.setRequestProperty("content-encoding", "JIS");
            con.connect();
            String contentType = con.getContentType();
            logger.log(Level.INFO, "contentType: {0}", contentType);
            PrintStream out = new PrintStream(con.getOutputStream(), true, con.getContentEncoding());
            while (true) {
                Object obj = con.getContent();
                if (obj == null) break;

                logger.log(Level.INFO, "type: {0}", obj.getClass().getName());
                String reply = (String)obj;
                logger.log(Level.INFO, "reply: {0}", reply);
                if (reply.startsWith("PING")) {
                    String pong = reply.replaceFirst("PING", "PONG");
                    logger.log(Level.INFO, "pong: {0}", pong);
                    out.println(pong);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IRCサーバーにアクセスできません。", ex);
        }
    }
}
