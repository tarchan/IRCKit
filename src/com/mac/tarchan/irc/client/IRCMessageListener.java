/*
 * IRCMessageListener.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * IRCMessageListener
 * 
 * @author tarchan
 */
public interface IRCMessageListener
{
	/**
	 * リプライメッセージを受け取ります。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void reply(IRCMessage reply);
}
