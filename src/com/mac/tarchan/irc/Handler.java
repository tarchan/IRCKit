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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.net.URLStreamHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IRC接続のプロトコルハンドラです。
 *
 * @author tarchan
 * @see URL#URL(String, String, int, String)
 */
public class Handler extends URLStreamHandler {

    static final Logger logger = Logger.getLogger(Handler.class.getName());

    /**
     * IRC接続をオープンします。
     *
     * @param url
     * @return
     * @throws java.io.IOException
     * @see URLStreamHandler#openConnection(java.net.URL)
     * @see IrcURLConnection
     */
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new IrcURLConnection(url);
    }

    /**
     * IRC接続のデフォルトのポート番号を返します。
     *
     * @return
     * @see URLStreamHandler#getDefaultPort()
     */
    @Override
    protected int getDefaultPort() {
        return 6667;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host");
        }

        // デフォルトポートを設定
        if (port == -1) {
            port = getDefaultPort();
        }

        // デフォルトニックネームを設定
        if (userInfo == null) {
            userInfo = System.getProperty("user.name");
        }

        // チャンネル名を設定
        if (ref != null) {
            path = "#" + ref;
        } else if (path.length() > 1) {
            path = "#" + path.substring(1);
        }

//            System.out.format("[URL] %s://%s:%s, %s, %s, %s, %s, %s\n", protocol, host, port, authority, userInfo, path, query, ref);
        logger.log(Level.INFO, "protocol=" + protocol);
        logger.log(Level.INFO, "host=" + host);
        logger.log(Level.INFO, "port=" + port);
        logger.log(Level.INFO, "authority=" + authority);
        logger.log(Level.INFO, "userInfo=" + userInfo);
        logger.log(Level.INFO, "path=" + path);
        logger.log(Level.INFO, "query=" + query);
        super.setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }
}
