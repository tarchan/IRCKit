/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.net.URLStreamHandler;

/**
 * Handler
 */
public class Handler extends URLStreamHandler
{
	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException
	{
		return new IRCConnection(url);
	}

	/**
	 * @see java.net.URLStreamHandler#getDefaultPort()
	 */
	@Override
	protected int getDefaultPort()
	{
		return 6667;
	}
}
