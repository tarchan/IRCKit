/*
 * IRCConsole.java
 * IRCKit
 *
 * Created by tarchan on 2008/12/02.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.net.MalformedURLException;
import java.util.Properties;

/**
 * IRCConsole
 * 
 * @author tarchan
 */
public class IRCConsole
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new IRCConsole().test();
	}

	/**
	 * IRCKitの接続テスト
	 */
	public void test()
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
			Properties prof = irc.createDefaultProperties();
			prof.setProperty("irc.real.name", "たーちゃん");
			prof.list(System.out);
			irc.registerNetwork("tokyo", "irc://irc.tokyo.wide.ad.jp:6667", "tarchan", "");
//			irc.registerNetwork("tokyo", "http://irc.mozilla.org:6667", "tarchan", "");
			irc.join("tokyo", "#dameTunes", "");
			irc.privmsg("tokyo", "#dameTunes", "テスト");
			irc.quit();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
}
