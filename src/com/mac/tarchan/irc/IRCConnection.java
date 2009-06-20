/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * IRC 接続です。
 * 
 * @see Handler#openConnection(URL)
 */
public class IRCConnection extends URLConnection
{
	/** ソケット */
	protected Socket socket;

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
		if (port < 0) port = url.getDefaultPort();
		socket = new Socket(url.getHost(), port);
		System.out.format("[CON] %s\n", socket);

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
