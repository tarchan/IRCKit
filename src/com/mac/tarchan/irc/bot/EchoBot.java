/*
 * EchoBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/30.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.bot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mac.tarchan.irc.client.IRCMessage;
import com.mac.tarchan.irc.client.IRCPrefix;
import com.mac.tarchan.irc.client.util.BotAdapter;
import com.mac.tarchan.irc.client.util.DccSendFile;

/**
 * EchoBot
 */
public class EchoBot extends BotAdapter
{
	/** ログ */
	private static final Log log = LogFactory.getLog(EchoBot.class);

	/** チャンネル */
	private String[] channels;

	/**
	 * IRCネットワークにログインします。
	 * 
	 * @param args <ホスト名> <ポート番号> <ニックネーム> <パスワード> [<チャンネル名> ...]
	 */
	public static void main(String[] args)
	{
		try
		{
			log.debug("args=" + Arrays.toString(args));
			if (args.length < 4)
			{
				System.out.println("Usage: EchoBot <ホスト名> <ポート番号> <ニックネーム> <パスワード> <チャンネル名>");
				throw new IllegalArgumentException("引数が不足しています。: " + args.length);
			}
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			String nick = args[2];
			String pass = args[3];
			String[] channles = Arrays.asList(args).subList(4, args.length).toArray(new String[]{});
			log.debug("channles=" + Arrays.toString(channles));
			new EchoBot(channles).login(host, port, nick, pass);
		}
		catch (Throwable x)
		{
			throw new RuntimeException("IRCネットワークにログインできません。", x);
		}
	}

	/**
	 * EchoBot
	 * 
	 * @param channels 最初に参加するチャンネル
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
			irc.join(channel);
		}
	}

	@Override
	public void onPing(String trail)
	{
		log.info("接続確認: " + trail);
		super.onPing(trail);
	}

	@Override
	public void onError(String trail)
	{
		log.error(trail);
	}

	@Override
	public void onJoin(String channel, IRCPrefix prefix)
	{
		log.info(String.format("%3$s has joined %1$s (%2$s)", channel, prefix, prefix.getNick()));
	}

	@Override
	public void onPart(String channel, IRCPrefix prefix)
	{
		log.info(String.format("%3$s has left channel %1$s (%2$s)", channel, prefix, prefix.getNick()));
	}

	@Override
	public void onQuit(String trail, IRCPrefix prefix)
	{
		log.info(String.format("%3$s has left IRC %1$s (%2$s)", trail, prefix, prefix.getNick()));
	}

	@Override
	public void onTopic(String channel, String topic, long when)
	{
		log.info(String.format("%1$s has set topic %2$s", channel, topic));
	}

	@Override
	public void onNames(String channel, String[] names, long when)
	{
		log.info(String.format("%1$s (%3$s) names %2$s", channel, Arrays.asList(names), names.length));
	}

	@Override
	public void onChannelMode(String channel, String mode)
	{
		log.info(String.format("%1$s has changed channel mode %2$s", channel, mode));
	}

	@Override
	public void onUserMode(String channel, String mode, String nick)
	{
		log.info(String.format("%1$s/%3$s has changed user mode %2$s", channel, mode, nick));
	}

	@Override
	public void onMessage(IRCMessage message)
	{
		String nick = message.getPrefix().getNick();
		if (isUserNick(nick))
		{
			log.info("self: " + message.getTrail());
		}
		else
		{
			log.info("other: " + message.getTrail());
		}
//		String chan = message.getParam0();
//		String text = message.getTrailing();
//		log.debug("channel: " + chan);
//		if (text.matches(".*hi.*"))
//		{
//			irc.privmsg(chan, String.format("hi %s!", nick));
//		}
//		if (text.matches(".*time.*"))
//		{
//			irc.privmsg(chan, String.format("%tT now!", System.currentTimeMillis()));
//		}
//		if (text.matches(".*date.*"))
//		{
//			irc.privmsg(chan, String.format("%tF now!", System.currentTimeMillis()));
//		}
//		if (text.matches(".*bye.*"))
//		{
//			irc.quit("サヨウナラ");
//		}
	}

	@Override
	public void onDirectMessage(IRCMessage message)
	{
		log.info("DM: " + message);
	}

	@Override
	public void onDccSend(String trail, IRCPrefix prefix)
	{
		try
		{
			// TODO DCC SEND
//			String[] params = trail.substring("DCC SEND ".length()).split(" ");
//			String file = params[0];
//			byte[] addr = new BigInteger(params[1]).toByteArray();
//			InetAddress inet = InetAddress.getByAddress(addr);
//			int port = Integer.parseInt(params[2]);
//			long size = Long.parseLong(params[3]);
//			log.info(String.format("%s %,d bytes %s %s", file, size, inet, port));
			DccSendFile dccfile = new DccSendFile(trail);
			File savefile = new File("dcc/" + prefix.getNick(), dccfile.getName());
			dccfile.save(savefile);
		}
		catch (IOException x)
		{
			log.error(x);
		}
	}
}
