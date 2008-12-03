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
	 * メッセージを処理します。
	 * 
	 * @param msg メッセージ
	 */
	public void reply(IRCMessage msg);

	/**
	 * 例外を処理します。
	 * 
	 * @param e 例外
	 */
	public void error(Exception e);
}
