/*
 * IRCClient.java
 * IRCKit
 *
 * Created by tarchan on 2008/03/26.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @since 1.0
 * @author tarchan
 */
public class IRCClient
{
	/** メッセージハンドラ */
	protected ArrayList<IRCMessageHandler> handlers = new ArrayList<IRCMessageHandler>();

	/**
	 * システムプロキシを使用するかどうかを設定します。
	 * 
	 * @param useSystemProxies システムプロキシを使用する場合は true
	 */
	public void setUseSystemProxies(boolean useSystemProxies)
	{
		String ver = System.getProperty("java.version");
		System.out.println("java.version=" + ver);
		System.setProperty("java.net.useSystemProxies", Boolean.valueOf(useSystemProxies).toString());
	}

	/**
	 * デフォルトのプロパティーを返します。
	 * 
	 * @return デフォルトのプロパティー
	 */
	public Properties createDefaultProperties()
	{
		return IRCNetwork.createDefaultProperties();
	}

	/**
	 * 指定されたメッセージハンドラを登録します。
	 * 
	 * @param handler メッセージハンドラ
	 */
	public void registerHandler(IRCMessageHandler handler)
	{
		handlers.add(handler);
	}

	/**
	 * 指定された IRC ネットワークを登録します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param address サーバアドレス
	 * @param username ユーザ名
	 * @param password パスワード
	 * @throws IllegalArgumentException 指定された文字列が RFC 2396 に違反する場合
	 * @throws MalformedURLException URL のプロトコルハンドラが見つからなかった場合、または URL の構築中にその他の何らかのエラーが発生した場合
	 */
	public void registerNetwork(String groupName, String address, String username, String password) throws MalformedURLException
	{
		IRCNetwork.register(groupName, address, username, password);
		IRCNetwork.find(groupName).setClient(this);
	}

	/**
	 * 指定された IRC ネットワークのチャンネルに参加します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param channelName チャンネル名
	 * @param keyword キーワード
	 */
	public void join(String groupName, String channelName, String keyword)
	{
		IRCNetwork.find(groupName).join(channelName, keyword);
	}

	/**
	 * すべての IRC ネットワークを切断します。
	 */
	public void quit()
	{
		IRCNetwork.quitAll();
	}

	/**
	 * 指定されたチャンネルにメッセージを送信します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param channelName チャンネル名
	 * @param message メッセージ
	 */
	public void privmsg(String groupName, String channelName, String message)
	{
		// TODO チャンネルにメッセージを送信
		IRCNetwork.find(groupName).privmsg(channelName, message);
	}

	/**
	 * リプライメッセージを処理します。
	 * 
	 * @param msg リプライメッセージ
	 */
	public void reply(IRCMessage msg)
	{
		for (IRCMessageHandler handle : handlers)
		{
			handle.reply(msg);
		}
	}
}
