/*
 * IRCMessageHandler.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * IRCMessageHandler
 * 
 * @author tarchan
 */
public interface IRCMessageHandler
{
	/**
	 * メッセージを表示します。
	 * 
	 * @param msg メッセージ
	 */
	public void privmsg(IRCMessage msg);
}
