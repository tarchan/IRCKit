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
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tarchan
 */
public class Shell {
    static final Logger log = Logger.getLogger(Shell.class.getName());
    private URLConnection con;
    private BufferedReader in;
    private PrintStream out;
    private BufferedReader buf;
    private String target;

    public static void main(String[] args) {
        try {
//            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s%n%4$s: %5$s%6$s%n");
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %2$s %4$s: %5$s%6$s%n");
            new Shell().input();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "IRCサーバーにアクセスできません。", ex);
        }
    }

    public Shell() throws IOException {
        log.info("shell init.");
//        System.setProperty("java.net.useSystemProxies", "true");
//            URL.setURLStreamHandlerFactory(new IrcURLStreamHandlerFactory());
        URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
            @Override
            public ContentHandler createContentHandler(String mimetype) {
                log.log(Level.INFO, "mimetype: {0}", mimetype);
                return null;
            }
        });
        System.setProperty("java.protocol.handler.pkgs", "com.mac.tarchan");
//        System.setProperty("java.content.handler.pkgs", "com.mac.tarchan");
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
        String type = con.getContentType();
        log.log(Level.INFO, "type: {0}", type);
        Object content = con.getContent();
        log.log(Level.INFO, "content: {0}", content);
        content = con.getContent();
        log.log(Level.INFO, "content2: {0}", content);
        content = con.getContent(new Class[] {BufferedReader.class});
        log.log(Level.INFO, "content3: {0}", content);
     }

    public void input() throws IOException {
        String enc = "SJIS";
        log.log(Level.INFO, "encoding: {0}", enc);
        buf = new BufferedReader(new InputStreamReader(System.in, enc));
        target = "#javabreak";
        while (true) {
            System.out.print(target + ": ");
            String line = buf.readLine();
            log.log(Level.INFO, "input: {0}", line);
            if (line.startsWith("/")) {
                if (line.equals("/exit")) {
                    break;
                }
            } else {
                // TODO IRCサーバーに送信
            }
        }
        log.info("shell exit.");
    }

    public void run() throws IOException {
        log.info("shell connecting...");
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
            con.setIfModifiedSince(System.currentTimeMillis());
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
