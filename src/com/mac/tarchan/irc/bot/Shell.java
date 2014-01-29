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
//    private BufferedReader buf;
    private String target;
    private String charset = "JIS";

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
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in, enc));
        target = "#javabreak";
        while (true) {
            try {
                System.out.print(target + "> ");
                String line = buf.readLine();
    //            log.log(Level.INFO, "input: {0}", line);
                if (line.startsWith("/")) {
                    String[] args = line.split(" ");
                    String cmd = args[0].toLowerCase();
                    // /exit exit to shell
                    if (cmd.startsWith("/e")) {
                        close();
                        System.err.println("えんいー");
                        break;
                    // /help print help
                    } else if (cmd.startsWith("/h")) {
                        help();
                    // /open <host>:<port> connect to server
                    } else if (cmd.startsWith("/o")) {
                        String host = args[1];
                        con = open(host, charset);
                    // /quit <message> disconnect server
                    } else if (cmd.startsWith("/q")) {
                        if (args.length >= 2) {
                            String message = args[1];
                            quit(message);
                        } else {
                            quit();
                        }
                    // /join <channnel>,<key> switch to channnel
                    } else if (cmd.startsWith("/j")) {
                        for (int i = 1; i < args.length; i++) {
                            String target = args[i];
                            join(target);
                            this.target = target;
                        }
                    // /part <channel> part channel
                    } else if (cmd.startsWith("/p")) {
                        part(target);
                        target = null;
                        // TODO 他のチャンネルに移動する
                    } else {
                        System.err.println("unknown command: " + args[0]);
                    }
                    // /notice <message> send notice message
                } else {
                    // TODO IRCサーバーに送信
                }
            } catch (RuntimeException e) {
                log.log(Level.SEVERE, "エラー", e);
            }
        }
    }
    
    public void help() {
        System.out.println("IRC shell help message.");
        System.out.println("  /help print this message");
        System.out.println("  /open <nick>:<pass>@<host>:<port> connect to server");
        System.out.println("  /quit <message> disconnect server");
        System.out.println("  /join <channnel>,<key> switch to channnel");
        System.out.println("  /part <channel> part channel");
        System.out.println("  /notice <message> send notice message");
        System.out.println("  <message> send normal message");
    }

    public URLConnection open(String host, String charset) {
        try {
            URL url = new URL("irc", host, null);
            URLConnection con = url.openConnection();
            con.setRequestProperty("content-encoding", charset);
            con.connect();
            return con;
        } catch (IOException ex) {
            throw new RuntimeException("サーバーに接続できません。: " + host, ex);
        }
    }

    public void close() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
    }

    public void quit() {
        postMessage("quit");
    }

    public void quit(String message) {
        postMessage("quit :" + message);
    }

    public void join(String channel) {
        postMessage("join " + channel);
    }

    public void join(String channel, String key) {
        postMessage(String.format("join %s %s", channel, key));
    }

    public void leaveAll() {
        postMessage("join 0");
    }

    public void part(String target) {
        postMessage("part :" + target);
    }

    public void nick(String nickname) {
        postMessage("nick " + nickname);
    }

    public void mode(String target, String mode) {
        postMessage(String.format("mode %s %s", target, mode));
    }

    public void topic(String channel, String topic) {
        postMessage(String.format("topic %s :%s", channel, topic));
    }

    public void topic(String channel) {
        postMessage(String.format("topic %s", channel));
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
