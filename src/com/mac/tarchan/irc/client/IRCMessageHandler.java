/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.util.EventListener;

/**
 * IRCメッセージを受け取るためのリスナーインターフェースです。
 * 
 * @author tarchan
 * @see IRCMessage
 * @see IRCClient#addHandler(String, IRCMessageHandler)
*/
public interface IRCMessageHandler extends EventListener
{
	/**
	 * IRCメッセージを受け取ります。
	 * 
	 * @param msg IRCメッセージ
	 */
	public void onMessage(IRCMessage msg);
}
