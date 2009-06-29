/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.net.irc;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.net.URLStreamHandler;

/**
 * IRC接続のプロトコルハンドラです。
 * 
 * @author tarchan
 * @see URL#URL(String, String, int, String)
 */
public class Handler extends URLStreamHandler
{
	/**
	 * IRC接続をオープンします。
	 * 
	 * @see URLStreamHandler#openConnection(java.net.URL)
	 * @see IRCConnection
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException
	{
		return new IRCConnection(url);
	}

	/**
	 * IRC接続のデフォルトのポート番号を返します。
	 * 
	 * @see URLStreamHandler#getDefaultPort()
	 */
	@Override
	protected int getDefaultPort()
	{
		return 6667;
	}
}
