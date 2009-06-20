/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * IRCMessageListener
 */
public interface IRCMessageHandler
{
	/**
	 * メッセージを受け取ります。
	 * 
	 * @param msg メッセージ
	 */
	public void onMessage(IRCMessage msg);
}
