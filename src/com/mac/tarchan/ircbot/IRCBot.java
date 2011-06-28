/*
 * IRCBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.ircbot;

import java.io.IOException;

import com.mac.tarchan.irc.IRCClient;
import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;
import com.mac.tarchan.irc.IRCMessage;
import com.mac.tarchan.irc.util.HandlerBuilder;

/**
 * IRCClient Test
 */
public class IRCBot implements IRCHandler
{
	private String[] channels;

	/**
	 * IRCサーバに接続します。
	 * 
	 * @param args <ホストアドレス> <ポート番号> <ニックネーム> <チャンネル名>
	 */
	public static void main(String[] args)
	{
		try
		{
			// irc.livedoor.ne.jp、irc6.livedoor.ne.jp、125.6.255.10
			String host = "irc.livedoor.ne.jp";
			int port = 6667;
			String nick = "mybot";
			String pass = "";
			String[] channles = {"#javabreak"};
			new IRCBot(host, port, nick, pass, channles);
		}
		catch (IOException x)
		{
			throw new RuntimeException("IRCサーバに接続できません。", x);
		}
	}

	/**
	 * IRCBot
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @param channels チャンネルリスト
	 * @throws IOException IRCサーバに接続できない場合
	 */
	public IRCBot(String host, int port, String nick, String pass, String[] channels) throws IOException
	{
		this.channels = channels;
		IRCClient irc = IRCClient.createClient(host, port, nick, pass)
//			.on(this)
			.on("001", HandlerBuilder.create(this, "ready", "client"))
			.on("privmsg", HandlerBuilder.create(this, "privmsg", ""))
			.on("error", HandlerBuilder.create(this, "error", "message.trailing"))
//			.on("notice", this)
//			.on("ping", this)
			.connect();
//		irc.on("ping", HandlerBuilder.create(irc, "pong", "message.trailing"));
		System.out.println("接続: " + irc);
	}

	/**
	 * 接続したIRCネットワークのチャンネルに参加します。
	 * 
	 * @param irc IRCクライアント
	 */
	public void ready(IRCClient irc)
	{
		if (channels != null)
		{
			for (String channel : channels)
			{
				irc.join(channel);
			}
		}
	}

	public void privmsg(IRCEvent event)
	{
		IRCMessage message = event.getMessage();
		IRCClient irc = event.getClient();

		String nick = message.getPrefix();
		String chan = message.getParam(0);
		String msg = message.getTrailing();
		if (msg.matches(".*hi.*"))
		{
			irc.notice(chan, String.format("hi %s!", nick));
		}
		if (msg.matches(".*time.*"))
		{
			irc.notice(chan, String.format("%tT now!", System.currentTimeMillis()));
		}
		if (msg.matches(".*date.*"))
		{
			irc.notice(chan, String.format("%tF now!", System.currentTimeMillis()));
		}
		if (msg.matches(".*bye.*"))
		{
			irc.quit("サヨウナラ");
		}
	}

	public void error(String text)
	{
		System.err.println(text);
	}

//	private void join(IRCClient irc)
//	{
//		if (channels != null)
//		{
//			for (String channel : channels)
//			{
//				irc.join(channel);
//			}
//		}
//	}

//	void readLines(IRCClient irc) throws IOException
//	{
//		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
//		while (true)
//		{
//			String line = buf.readLine();
//			System.out.println("input: " + line);
//			String chan = "#javabreak";
//			irc.postMessage(String.format("PRIVMSG %s :%s", chan, new String(line.getBytes(), "JIS")));
//		}
//	}

	public void onMessage(IRCEvent event)
	{
		IRCMessage message = event.getMessage();
		IRCClient irc = event.getClient();

		String command = message.getCommand();
		if (command.equals("PRIVMSG"))
		{
			// privmsg
			String nick = message.getPrefix();
			String chan = message.getParam(0);
			String msg = message.getTrailing();
			if (msg.matches(".*hi.*"))
			{
				irc.privmsg(chan, String.format("hi %s!", nick));
			}
			if (msg.matches(".*time.*"))
			{
				irc.privmsg(chan, String.format("%tT now!", System.currentTimeMillis()));
			}
			if (msg.matches(".*date.*"))
			{
				irc.privmsg(chan, String.format("%tF now!", System.currentTimeMillis()));
			}
			if (msg.matches(".*bye.*"))
			{
				irc.quit("サヨウナラ");
			}
		}
//		else if (command.equals("PING"))
//		{
//			// ping
//			String payload = message.getTrailing();
//			irc.pong(payload);
//		}
		else if (command.equals("ERROR"))
		{
			// error
			throw new RuntimeException("IRCエラー: " + message);
		}
//		else if (command.equals("001"))
//		{
//			// welcome
//			join(irc);
//		}
	}
}
