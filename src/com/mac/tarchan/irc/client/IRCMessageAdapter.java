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
 * リプライメッセージを受け取るためのアダプタクラスです。
 * このクラスは、リスナーオブジェクトの作成を容易にするために、よく使われる空のメソッドを提供します。
 * IRCMessage リスナーを作成するには、このクラスを拡張して、必要なIRCコマンド用のメソッドを登録します。
 * 拡張したクラスを使ってリスナーオブジェクトを作成してから、registerAction メソッドを使って必要なメソッドを登録するだけで済みます。
 * 
 * @see IRCMessage
 * @see IRCMessageListener
 */
public class IRCMessageAdapter implements IRCMessageListener
{
	/** IRCコマンドに対応するアクション */
	protected HashMap<String, Method> actions = new HashMap<String, Method>();

	/** イベントの消費を表します。 */
	protected boolean consumed;

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
	 * IRCコマンドに対応するアクションをを登録します。
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
	 * リプライメッセージが消費されたかどうかを返します。
	 * 
	 * @return リプライメッセージが消費された場合は true
	 */
	protected boolean isConsumed()
	{
		return consumed;
	}

	/**
	 * リプライメッセージが消費されたかどうかを設定します。
	 * 
	 * @param consumed リプライメッセージが消費された場合は true
	 */
	protected void setConsumed(boolean consumed)
	{
		this.consumed = consumed;
	}

	/**
	 * リプライメッセージを受信したときに呼び出されます。
	 * 
	 * @param reply リプライメッセージ
	 * @see com.mac.tarchan.irc.client.IRCMessageListener#reply(com.mac.tarchan.irc.client.IRCMessage)
	 */
	public void reply(IRCMessage reply)
	{
		try
		{
			setConsumed(false);
			String key = reply.getCommand();
			Method m = actions.get(key);
			if (m != null)
			{
				m.invoke(this, reply);
				setConsumed(true);
			}
		}
		catch (IllegalArgumentException e)
		{
			error(e);
		}
		catch (IllegalAccessException e)
		{
			System.err.println("error: " + reply);
			error(e);
		}
		catch (InvocationTargetException e)
		{
			error(e);
		}
	}

	/**
	 * 例外が発生したときに呼び出されます。
	 * 
	 * @param e 例外
	 */
	public void error(Exception e)
	{
		System.err.println(e.toString());
	}

	/**
	 * IRCネットワークに接続したときに呼び出されます。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void welcome(IRCMessage reply)
	{
	}

	/**
	 * PINGメッセージを受信したときに呼び出されます。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void ping(IRCMessage reply)
	{
		IRCNetwork network = reply.getNetwork();
		String trail = reply.getTrailing();
		String pong = String.format("PONG %s", trail);
		network.put(pong);
	}

	/**
	 * チャットメッセージを受信したときに呼び出されます。
	 * 
	 * @param reply リプライメッセージ
	 */
	public void privmsg(IRCMessage reply)
	{
		String nick = reply.getNick();
		String text = reply.getTrailing("ISO-2022-JP");
		String str = String.format("(%s) %s", nick, text);
		System.out.println(str);
	}
}
