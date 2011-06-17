/*
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.util.EventListener;

/**
 * IRCイベントを受け取るインターフェースです。
 * 
 * @see IRCEvent
 */
public interface IRCHandler extends EventListener
{
	/**
	 * IRCメッセージを受信すると呼び出されます。
	 * 
	 * @param event IRCイベント
	 */
	public void onMessage(IRCEvent event);
}
