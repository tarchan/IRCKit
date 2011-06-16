package com.mac.tarchan.irc;

import java.util.EventListener;

/**
 * IRCHandler
 */
public interface IRCHandler extends EventListener
{
	/**
	 * IRCメッセージを受信します。
	 * 
	 * @param event IRCイベント
	 */
	public void onMessage(IRCEvent event);
}
