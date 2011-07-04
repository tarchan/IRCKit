/*
 * IRCEvent.java
 * IRCKit
 *
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.util.EventObject;

/**
 * IRCメッセージの受信イベントです。
 * 
 * @see IRCClient
 * @see IRCMessage
 * @see IRCHandler
 */
public class IRCEvent extends EventObject
{
	/** serialVersionUID */
	private static final long serialVersionUID = 2880861890798748708L;

	/** クライアント */
	transient protected IRCClient source;

	/** メッセージ */
	transient protected IRCMessage message;

	/**
	 * IRCEvent を構築します。
	 * 
	 * @param source {@link IRCClient}
	 * @param message {@link IRCMessage}
	 */
	public IRCEvent(IRCClient source, IRCMessage message)
	{
		super(source);
		this.source = source;
		this.message = message;
	}

	/**
	 * IRCClient を返します。
	 * 
	 * @return {@link IRCClient}
	 */
	public IRCClient getClient()
	{
		return source;
	}

	/**
	 * IRCMessage を返します。
	 * 
	 * @return {@link IRCMessage}
	 */
	public IRCMessage getMessage()
	{
		return message;
	}
}
