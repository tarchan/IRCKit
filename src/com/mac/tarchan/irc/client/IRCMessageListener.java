/*
 * IRCMessageListener.java
 * IRCKit
 *
 * Created by tarchan on 2008/04/04.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * @since 1.0
 * @author tarchan
 */
public interface IRCMessageListener
{
	/**
	 * メッセージを受け取ります。
	 * 
	 * @param message IRC メッセージ
	 */
	public void onMessage(IRCMessage message);

	/**
	 * エラーを受け取ります。
	 * 
	 * @param e 発生した例外
	 */
	public void onError(Exception e);
}
