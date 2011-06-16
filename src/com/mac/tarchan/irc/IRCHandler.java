package com.mac.tarchan.irc;

/**
 * IRCHandler
 */
public interface IRCHandler
{
	/**
	 * IRCメッセージを受信します。
	 * 
	 * @param event IRCイベント
	 */
	public void onMessage(IRCEvent event);
}
