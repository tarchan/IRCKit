/*
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.util.EventListener;

/**
 * IRCHandler
 */
public interface IRCHandler extends EventListener
{
	/**
	 * IRCイベントを受信します。
	 * 
	 * @param event IRCイベント
	 */
	public void onMessage(IRCEvent event);
}
