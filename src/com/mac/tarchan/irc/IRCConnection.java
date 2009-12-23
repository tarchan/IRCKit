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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * IRCサーバーへのURL接続です。
 * IRC URL 構文は次のとおりです。
 * <pre>irc://{host}:{port}</pre>
 * 
 * @author tarchan
 * @see Handler#openConnection(URL)
 */
public class IRCConnection extends URLConnection
{
	/** ソケット */
	private Socket socket;

	/**
	 * IRC 接続を構築します。
	 * 
	 * @param url URL
	 */
	protected IRCConnection(URL url)
	{
		super(url);
	}

	/**
	 * 通信リンクを確立します。
	 */
	@Override
	public void connect() throws IOException
	{
		// 接続済みの場合は何もしない
		if (connected) return;

		// 接続する
		int port = url.getPort();
		if (port == -1) port = url.getDefaultPort();
		socket = new Socket(url.getHost(), port);
		socket.setSoTimeout(5 * 60 * 1000);
//		System.out.format("[CON] %s\n", socket);

		// 接続済みにする
		connected = true;
	}

	/**
	 * 入力ストリームを返します。
	 */
	@Override
	public InputStream getInputStream() throws IOException
	{
		if (!connected) return null;
		return socket.getInputStream();
	}

	/**
	 * 出力ストリームを返します。
	 */
	@Override
	public OutputStream getOutputStream() throws IOException
	{
		if (!connected) return null;
		return socket.getOutputStream();
	}
}
