/*
 * IRCMessageListener.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * リプライメッセージを受け取るためのリスナーインターフェースです。
 * 
 * @see IRCMessage
 * @see IRCMessageAdapter
 */
public interface IRCMessageListener
{
	/**
	 * リプライメッセージを受信したときに呼び出されます。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void reply(IRCMessage reply);
}
