/*
 * IRCMessageFilter.java
 * IRCKit
 * 
 * Created by tarchan on 2011/07/13.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

/**
 * IRCメッセージのフィルタです。
 */
public interface IRCMessageFilter
{
	/**
	 * 指定されたIRCメッセージが処理する必要があるかどうかを判定します。
	 * 
	 * @param message IRCメッセージ
	 * @return 処理する必要がある場合は true
	 */
	public boolean accept(IRCMessage message);
}
