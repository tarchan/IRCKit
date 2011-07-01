/*
 * EchoBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/30.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.ircbot;

import java.io.IOException;

import com.mac.tarchan.irc.IRCMessage;

/**
 * EchoBot
 */
public class EchoBot extends IRCBot
{
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
		for (String channel : channels)
		{
			getClient().join(channel);
		}
	}

	@Override
	public void onMessage(IRCMessage message)
	{
		String nick = message.getPrefix();
		String chan = message.getParam(0);
		String msg = message.getTrailing();
		if (msg.matches(".*hi.*"))
		{
			getClient().privmsg(chan, String.format("hi %s!", nick));
		}
		if (msg.matches(".*time.*"))
		{
			getClient().privmsg(chan, String.format("%tT now!", System.currentTimeMillis()));
		}
		if (msg.matches(".*date.*"))
		{
			getClient().privmsg(chan, String.format("%tF now!", System.currentTimeMillis()));
		}
		if (msg.matches(".*bye.*"))
		{
			getClient().quit("サヨウナラ");
		}
	}
}
