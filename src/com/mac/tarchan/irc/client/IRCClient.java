/*
 * IRCClient.java
 * IRCKit
 *
 * Created by tarchan on 2008/03/26.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * @since 1.0
 * @author tarchan
 */
public class IRCClient
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Welcome to IRCKit!");
		try
		{
			IRCClient irc = new IRCClient();
			irc.setUseSystemProxies(true);
			irc.registerHandler(new IRCMessageHandler()
			{
				/**
				 * 受信したメッセージを表示します。
				 * 
				 * @param msg メッセージ
				 */
				public void privmsg(IRCMessage msg)
				{
					// TODO 受信したメッセージを表示
				}
			});
//			irc.registerNetwork("tokyo", "http://irc.tokyo.wide.ad.jp:6667", "tarchan", "");
			irc.registerNetwork("tokyo", "http://irc.mozilla.org:6667", "tarchan", "");
			irc.join("tokyo", "#dameTunes", "");
			irc.privmsg("tokyo", "#dameTunes", "テスト");
			irc.quit();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}



//		try
//		{
//			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//			while (true)
//			{
//				String input = in.readLine();
//				System.out.println("echo: " + input);
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}

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
}
