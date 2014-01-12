/*
 *  Copyright (c) 2009 tarchan. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY TARCHAN ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 *  EVENT SHALL TARCHAN OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are
 *  those of the authors and should not be interpreted as representing official
 *  policies, either expressed or implied, of tarchan.
 */
package com.mac.tarchan.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IRCサーバーへのURL接続です。 IRC URL 構文は次のとおりです。
 * <pre>irc://{host}:{port}</pre>
 *
 * @author tarchan
 * @see Handler#openConnection(URL)
 */
public class IrcURLConnection extends URLConnection {
    static final Logger logger = Logger.getLogger(IrcURLConnection.class.getName());

    /**
     * 入出力ソケット
     */
    private Socket socket;
    /**
     * 入力ストリーム
     */
    protected BufferedReader in;
    /**
     * 出力ストリーム
     */
    protected PrintStream out;
    /**
     * 文字コード
     */
    protected String encoding;

    /**
     * IRC 接続を構築します。
     *
     * @param url URL
     */
    protected IrcURLConnection(URL url) {
        super(url);
    }

    /**
     * 通信リンクを確立します。
     *
     * @throws java.io.IOException
     */
    @Override
    public void connect() throws IOException {
        // 接続済みの場合は何もしない
        if (connected) {
            return;
        }

        encoding = this.getRequestProperty("content-encoding");

        // デフォルトポートを設定
        int port = url.getPort();
        if (port == -1) port = url.getDefaultPort();

        // 接続する
        socket = new Socket(url.getHost(), port);
        socket.setSoTimeout(5 * 60 * 1000);
//		System.out.format("[CON] %s\n", socket);

        // ログイン
        login();

        // JOIN
        String file = url.getFile();
        logger.log(Level.INFO, "file=" + file);
        if (file != null) {
            postMessage("JOIN " + file);
        }

        // 接続済みにする
        connected = true;
    }

    public void login() {
        String[] userInfo = url.getUserInfo() != null ? url.getUserInfo().split(":") : new String[]{};
        String nick = userInfo.length >= 1 ? userInfo[0] : null;
        if (nick == null) nick = System.getProperty("user.name");
        String pass = userInfo.length >= 2 ? userInfo[1] : null;
        logger.log(Level.INFO, "nick=" + nick);
        logger.log(Level.INFO, "pass=" + pass);

        if (pass != null) {
            postMessage("PASS " + pass);
        }
        postMessage("NICK " + nick);

        String user = getRequestProperty("user");
        if (user == null) user = nick;
        String mode = getRequestProperty("mode");
        if (mode == null) mode = "0";
        String real = getRequestProperty("real");
        if (real == null) real = "IRCKit 2.1";
        postMessage("USER %s %d * :%s", user, Integer.parseInt(mode), real);
    }

    /**
     * 入力ストリームを返します。
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return socket != null ? socket.getInputStream() : null;
    }

    @Override
    public String getContentEncoding() {
//        return super.getContentEncoding();
//        return this.getRequestProperty("content-encoding");
        return encoding;
    }

    @Override
    public Object getContent() throws IOException {
//        return getContentHandler().getContent(this);
        if (in == null) {
            String enc = getContentEncoding();
            logger.log(Level.INFO, "encoding: " + enc);
            in = new BufferedReader(new InputStreamReader(getInputStream(), enc));
        }
        String line = in.readLine();
        return line;
    }

    /**
     * 出力ストリームを返します。
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket != null ? socket.getOutputStream() : null;
    }

    protected void prepareOutput() throws IOException {
        if (out == null) {
            out = new PrintStream(getOutputStream(), true, getContentEncoding());
        }
    }

    public IrcURLConnection postMessage(String text) {
        try {
            logger.log(Level.INFO, text);
            prepareOutput();
            out.println(text);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "メッセージを送信できません。: " + text, ex);
        }
        return this;
    }

    public IrcURLConnection postMessage(String format, Object... args) {
        return postMessage(String.format(format, args));
    }
}
