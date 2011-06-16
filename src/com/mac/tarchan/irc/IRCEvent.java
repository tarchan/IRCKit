package com.mac.tarchan.irc;

import java.util.EventObject;

/**
 * IRCEvent
 */
@SuppressWarnings("serial")
public class IRCEvent extends EventObject
{
	/** クライアント */
	protected IRCClient source;

	/** メッセージ */
	protected IRCMessage message;

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
