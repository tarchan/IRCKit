/*
 * HandlerBuilder.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/28.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.util;

import java.beans.EventHandler;

import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;

/**
 * HandlerBuilder
 */
public class HandlerBuilder
{
	/**
	 * 指定されたパラメータのメッセージハンドラを構築します。
	 * 
	 * @param target action を実行するオブジェクト
	 * @param action target オブジェクトのプロパティ名またはメソッド名
	 * @param eventPropertyName イベントのプロパティ名
	 * @return メッセージハンドラ
	 * @see IRCEvent
	 * @see EventHandler#create(Class, Object, String, String)
	 */
	public static IRCHandler create(Object target, String action, String eventPropertyName)
	{
		IRCHandler onMessage = EventHandler.create(IRCHandler.class, target, action, eventPropertyName, "onMessage");
		return onMessage;
	}

	/**
	 * 指定されたパラメータのメッセージハンドラを構築します。
	 * 対象のメソッドは引数があってはいけません。
	 * 
	 * @param target action を実行するオブジェクト
	 * @param action target オブジェクトのプロパティ名またはメソッド名
	 * @return メッセージハンドラ
	 */
	public static IRCHandler create(Object target, String action)
	{
		return create(target, action, null);
	}
}
