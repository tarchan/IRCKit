/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * メソッド宣言がメッセージハンドラであることを示します。
 * 
 * @author tarchan
 * @see IRCMessageHandler#onMessage(IRCMessage)
 * @see IRCClient#addMessageHandlerAll(Object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reply
{
	/**
	 * 注釈を付けたメソッドで処理されるコマンドを示します。
	 * 
	 * @see IRCMessage#getCommand()
	 */
	String value();
}