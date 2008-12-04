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
//		registerAction("020", "pleasewait");
//		registerAction("433", "NICKNAMEINUSE");
//		registerAction("451", "NOTREGISTERED");
		registerAction("PING", "ping");
		registerAction("PRIVMSG", "privmsg");
//		registerAction("ERROR", "error");
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
			error(e);
		}
		catch (NoSuchMethodException e)
		{
			error(e);
		}
	}

	/**
	 * @see com.mac.tarchan.irc.client.IRCMessageListener#reply(com.mac.tarchan.irc.client.IRCMessage)
	 */
	public void reply(IRCMessage reply)
	{
		try
		{
			String key = reply.getCommand();
			Method m = actions.get(key);
			if (m != null) m.invoke(this, reply);
			else error(new NullPointerException(key));

		}
		catch (IllegalArgumentException e)
		{
			error(e);
		}
		catch (IllegalAccessException e)
		{
			error(e);
		}
		catch (InvocationTargetException e)
		{
			error(e);
		}
	}

	/**
	 * 例外を受け取ります。
	 * 
	 * @param e 例外
	 */
	public void error(Exception e)
	{
		System.err.println(e.toString());
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
