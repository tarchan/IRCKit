/*
 * EchoBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/30.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.ircbot;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mac.tarchan.irc.IRCMessage;
import com.mac.tarchan.irc.util.IRCBotAdapter;

/**
 * EchoBot
 */
public class EchoBot extends IRCBotAdapter
{
	/** ログ */
	private static final Log log = LogFactory.getLog(EchoBot.class);

	/** チャンネル */
	private String[] channels;

	/**
	 * IRCサーバに接続します。
	 * 
	 * @param args <ホストアドレス> <ポート番号> <ニックネーム> <チャンネル名>
	 */
	public static void main(String[] args)
	{
		// irc.livedoor.ne.jp、irc6.livedoor.ne.jp、125.6.255.10
		String host = "irc.livedoor.ne.jp";
		int port = 6667;
		String nick = "mybot";
		String pass = "";
		String[] channles = {"#javabreak"};
		try
		{
			new EchoBot(channles).login(host, port, nick, pass);
		}
		catch (IOException x)
		{
			throw new RuntimeException("IRCネットワークにログインできません。", x);
		}
	}

	/**
	 * EchoBot
	 * 
	 * @param channels 自動的に参加するチャンネル
	 */
	public EchoBot(String[] channels)
	{
		this.channels = channels;
	}

	@Override
	public void onStart()
	{
		log.info("接続しました。");
		for (String channel : channels)
		{
			getIRC().join(channel);
		}
	}

	@Override
	public void onMessage(IRCMessage message)
	{
		String nick = message.getPrefix();
		String chan = message.getParam(0);
		String text = message.getTrailing();
		if (text.matches(".*hi.*"))
		{
			getIRC().privmsg(chan, String.format("hi %s!", nick));
		}
		if (text.matches(".*time.*"))
		{
			getIRC().privmsg(chan, String.format("%tT now!", System.currentTimeMillis()));
		}
		if (text.matches(".*date.*"))
		{
			getIRC().privmsg(chan, String.format("%tF now!", System.currentTimeMillis()));
		}
		if (text.matches(".*bye.*"))
		{
			getIRC().quit("サヨウナラ");
		}
//		if (message.isCTCP())
//		{
////			System.out.println("CTCP3: " + text);
//			int i = 0;
//			for (String ctcp : message.splitCTCP())
//			{
//				System.out.printf("CTCP[%s]=%s%n", i++, ctcp);
//				if (ctcp.contains("PING"))
//				{
//					getIRC().ctcpReply(nick, ctcp);
//				}
//				else
//				{
//					getIRC().ctcpQuery(nick, ctcp);
//				}
//			}
//		}
	}

	@Override
	public void onCtcpQuery(IRCMessage message)
	{
		String nick = message.getPrefix();
//		String chan = message.getParam(0);
		String text = message.getTrailing();
		log.debug(String.format("CTCP: %s: %s", nick, text));
		int i = 0;
		for (String ctcp : message.splitCTCP())
		{
			log.debug(String.format("CTCP[%s]=%s%n", i++, ctcp));
			if (ctcp.contains("PING"))
			{
				getIRC().ctcpReply(nick, ctcp);
			}
			else
			{
				getIRC().ctcpQuery(nick, ctcp);
			}
		}
	}
}
