/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    static final Logger log = Logger.getLogger(ShellAdapter.class.getName());
    private URLConnection con;
    private BufferedReader in;
    private PrintStream out;

    public static void main(String[] args) {
        try {
            new ShellAdapter().run();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IRCサーバーにアクセスできません。", ex);
        }
    }

    public ShellAdapter() throws IOException {
        log.info("ShellAdapter connecting...");
//        System.setProperty("java.net.useSystemProxies", "true");
//            URL.setURLStreamHandlerFactory(new IrcURLStreamHandlerFactory());
        System.setProperty("java.protocol.handler.pkgs", "com.mac.tarchan");
        System.setProperty("java.content.handler.pkgs", "com.mac.tarchan");
//            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s%n%4$s: %5$s%6$s%n");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %2$s %4$s: %5$s%6$s%n");
//            URL url = new URL("irc://irc.ircnet.ne.jp/javabreak");
//            URL url = new URL("irc://irc.ircnet.ne.jp/#javabreak");
        URL url = new URL("irc", "irc.ircnet.ne.jp", "javabreak");
//            URL url = new URL("irc://tarchan:pass@irc.ircnet.ne.jp/#javabreak");
//            URL url = new URL("irc://irc.freenode.net/#javabreak");
//            URL url = new URL("irc://irc.mozilla.org/firefox");
        con = url.openConnection();
        con.addRequestProperty("channel", "#javabreak");
        con.addRequestProperty("channel", "#test");
        con.setRequestProperty("content-encoding", "JIS");
     }

    public void run() throws IOException {
        con.connect();
        String contentType = con.getContentType();
        log.log(Level.INFO, "contentType: {0}", contentType);
        String enc = con.getContentEncoding();
        log.log(Level.INFO, "encoding: {0}", enc);
        in = new BufferedReader(new InputStreamReader(con.getInputStream(), enc));
        out = new PrintStream(con.getOutputStream(), true, enc);
        while (true) {
//            Object obj = con.getContent();
            String line = in.readLine();
            if (line == null) break;

    //                log.log(Level.INFO, "type: {0}", obj.getClass().getName());
            onMessage(line);
            // TODO ユーザーの入力受付
        }
        con = null;
    }

    public void onMessage(String message) {
        log.log(Level.INFO, "reply: {0}", message);
        if (message.startsWith("PING")) {
            String pong = message.replaceFirst("PING", "PONG");
            log.log(Level.INFO, "pong: {0}", pong);
//            out.println(pong);
            postMessage(pong);
        }
    }
    
    public void postMessage(String message) {
        out.println(message);
    }

    public void onError(Exception e) {
        log.log(Level.SEVERE, "IRCエラー", e);
    }
}
