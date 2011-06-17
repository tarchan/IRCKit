package com.mac.tarchan.irc;

import java.util.EventObject;

/**
 * IRCEvent
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
	 * IRCEvent
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
