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
	@Deprecated
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
	 */
	public IRCBot()
	{
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
	@Deprecated
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
	 * IRCネットワークにログインします。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @throws IOException IRCネットワークにログインできない場合
	 */
	public void login(String host, int port, String nick, String pass) throws IOException
	{
		if (irc != null) irc.quit("再接続します。");

		irc = IRCClient.createClient(host, port, nick, pass)
				.on("001", HandlerBuilder.create(this, "onStart"))
				.connect();
	}

	/**
	 * 接続したIRCネットワークのチャンネルに参加します。
	 * 
	 * @param irc IRCクライアント
	 */
	@Deprecated
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

	/**
	 * 受信したメッセージに対応したコマンドを返信します。
	 * 
	 * @param event IRCイベント
	 */
	@Deprecated
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

	/**
	 * エラーメッセージを表示します。
	 * 
	 * @param text エラーメッセージ
	 */
	@Deprecated
	public void error(String text)
	{
		System.err.println("IRCエラー: " + text);
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

	@Deprecated
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

	/** IRCクライアント */
	private IRCClient irc;

	/**
	 * 接続中のIRCクライアントを返します。
	 * 
	 * @return IRCクライアント
	 * @throws NullPointerException IRCネットワークに接続していない場合
	 */
	protected IRCClient getClient()
	{
		if (irc != null)
		{
			return irc;
		}
		else
		{
			throw new NullPointerException("IRCネットワークに接続していません。");
		}
	}

	/**
	 * 新しいニックネームを設定します。
	 * 
	 * @param newNick 新しいニックネーム
	 */
	public void setUserNick(String newNick)
	{
		getClient().sendMessage("NICK %s", newNick);
	}

	/**
	 * 指定されたニックネームがユーザ自身かどうか判定します。
	 * 
	 * @param nick ニックネーム
	 * @return ユーザのニックネームの場合は true
	 */
	public boolean isUserNick(String nick)
	{
		String userNick = irc.getNick();
		return userNick.equals(nick);
	}

	/**
	 * IRCネットワークの接続が確立したときに呼び出されます。
	 */
	public void onStart()
	{
	}

	/**
	 * ニックネームが衝突したときに呼び出されます。
	 * ニックネームの変更ができませんでした。
	 * 接続前の場合は直ちに新しいニックネームを設定する必要があります。
	 * 
	 * @param conflictNick 衝突したニックネーム
	 */
	public void onNickConflict(String conflictNick)
	{
	}

	/**
	 * ニックネームが変更されたときに呼び出されます。
	 * 
	 * @param oldNick 古いニックネーム
	 * @param newNick 新しいニックネーム
	 */
	public void onNickChanged(String oldNick, String newNick)
	{
	}

	/**
	 * IRCネットワークの接続を継続します。
	 * 自動継続したくないときは、このメソッドをオーバーライドしてください。
	 * 
	 * @param text テキスト
	 */
	public void onPing(String text)
	{
		getClient().pong(text);
	}

	/**
	 * チャンネルに参加したときに呼び出されます。
	 */
	public void onJoin()
	{
	}

	/**
	 * チャンネルを離脱したときに呼び出されます。
	 */
	public void onPart()
	{
	}

	/**
	 * 終了したときに呼び出されます。
	 */
	public void onQuit()
	{
	}

	/**
	 * ユーザーのモードが変更されたときに呼び出されます。
	 */
	public void onUserMode()
	{
	}

	/**
	 * チャンネルのモードが変更されたときに呼び出されます。
	 */
	public void onChannelMode()
	{
	}

	/**
	 * メッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onMessage(IRCMessage message)
	{
	}

	/**
	 * プライベートメッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onPrivateMessage(IRCMessage message)
	{
	}

	/**
	 * お知らせメッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onNotice(IRCMessage message)
	{
	}

	/**
	 * CTCP問い合わせメッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onCtcpQuery(IRCMessage message)
	{
	}

	/**
	 * CTCPお知らせメッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onCtcpReply(IRCMessage message)
	{
	}

	/**
	 * ファイル送信メッセージが届いたときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onDccSend(IRCMessage message)
	{
	}

	/**
	 * IRCネットワークが切断したときに呼び出されます。
	 */
	public void onStop()
	{
	}

	/**
	 * IRCネットワークの再接続を止めたときに呼び出されます。
	 */
	public void onDestroy()
	{
	}
}
