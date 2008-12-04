/*
 * IRCMessageAdapter.java
 * IRCKit
 *
 * Created by tarchan on 2008/12/04.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * IRCMessageAdapter
 * 
 * @author tarchan
 */
public class IRCMessageAdapter implements IRCMessageListener
{
	/** IRCコマンドに対応するアクション */
	protected HashMap<String, Method> actions = new HashMap<String, Method>();

	/**
	 * IRCメッセージアダプタを構築します。
	 */
	public IRCMessageAdapter()
	{
		registerAction("001", "welcome");
		registerAction("PING", "ping");
		registerAction("PRIVMSG", "privmsg");
	}

	/**
	 * 指定されたアクションをを登録します。
	 * 
	 * @param key IRCコマンド
	 * @param action アクション
	 */
	protected void registerAction(String key, String action)
	{
		try
		{
			Class<? extends IRCMessageListener> c = getClass();
			Method m = c.getMethod(action, IRCMessage.class);
			actions.put(key, m);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see com.mac.tarchan.irc.client.IRCMessageListener#reply(com.mac.tarchan.irc.client.IRCMessage)
	 */
	public void reply(IRCMessage reply)
	{
		try
		{
			String cmd = reply.getCommand();
			Method m = actions.get(cmd);
			if (m != null) m.invoke(this, reply);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * IRCネットワークに接続した場合に受け取ります。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void welcome(IRCMessage reply)
	{
	}

	/**
	 * PINGメッセージを受け取ります。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void ping(IRCMessage reply)
	{
	}

	/**
	 * チャットメッセージを受け取ります。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void privmsg(IRCMessage reply)
	{
	}
}
