/*
 * BotAdapter.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCEvent;
import com.mac.tarchan.irc.client.IRCHandler;
import com.mac.tarchan.irc.client.IRCMessage;
import com.mac.tarchan.irc.client.IRCPrefix;

/**
 * IRCメッセージを受け取る抽象アダプタクラスです。
 * IRCボットを作成するには、このクラスを拡張して関係のあるイベントに対するメソッドをオーバーライドします。
 * 
 * @see HandlerBuilder
 */
public abstract class BotAdapter
{
	/** IRCクライアント */
	protected IRCClient irc;

	/** ニックネームリスト */
	protected ArrayList<String> nicklist = new ArrayList<String>();

	/** 自動ニックネーム */
	protected boolean autoNickname = true;

	/** 自動継続 */
	protected boolean autoPingPong = true;

	/** 自動再接続 */
	protected boolean autoRecconection = true;

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
		login(host, port, nick, pass, "JIS");
	}

	/**
	 * IRCネットワークにログインします。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @param encoding 文字コード
	 * @throws IOException IRCネットワークにログインできない場合
	 */
	public void login(String host, int port, String nick, String pass, String encoding) throws IOException
	{
		if (irc != null) irc.quit("再接続します。");

		irc = IRCClient.createClient(host, port, nick, pass, encoding)
//			.on("privmsg", HandlerBuilder.create(this, "onMessage", "message"))
			.on("privmsg", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (message.isCTCP())
					{
						BotAdapter.this.onCtcpQuery(message);
					}
					else if (message.isDirectMessage())
					{
						BotAdapter.this.onDirectMessage(message);
					}
					else
					{
						BotAdapter.this.onMessage(message);
					}
				}
			})
			.on("notice", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (message.isCTCP())
					{
						BotAdapter.this.onCtcpReply(message);
					}
					else
					{
						BotAdapter.this.onNotice(message);
					}
				}
			})
			.on("join", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getTrail();
					IRCPrefix prefix = message.getPrefix();
					BotAdapter.this.onJoin(channel, prefix);
				}
			})
			.on("part", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam1();
					IRCPrefix prefix = message.getPrefix();
					BotAdapter.this.onPart(channel, prefix);
				}
			})
			.on("quit", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String trail = message.getTrail();
					IRCPrefix prefix = message.getPrefix();
					BotAdapter.this.onQuit(trail, prefix);
					if (trail.equals("Killed"))
					{
						BotAdapter.this.onKilled(trail, prefix);
					}
				}
			})
			.on("mode", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam0();
					String mode = message.getParam1();
					if (message.getParamsCount() < 3)
					{
						BotAdapter.this.onChannelMode(channel, mode);
					}
					else
					{
						String nick = message.getParam2();
						BotAdapter.this.onUserMode(channel, mode, nick);
					}
				}
			})
			.on("353", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String[] names = message.getTrail().split(" ");
					nicklist.addAll(Arrays.asList(names));
				}
			})
			.on("366", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					long when = message.getWhen();
					String channel = message.getParam1();
					String[] names = nicklist.toArray(new String[]{});
					nicklist.clear();
					BotAdapter.this.onNames(channel, names, when);
				}
			})
			.on("topic", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam0();
					String topic = message.getTrail();
					long when = message.getWhen();
					BotAdapter.this.onTopic(channel, topic, when);
				}
			})
			.on("332", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam1();
					String topic = message.getTrail();
					long when = message.getWhen();
					BotAdapter.this.onTopic(channel, topic, when);
				}
			})
			.on("nick", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String oldNick = message.getPrefix().getNick();
					String newNick = message.getTrail();
					long when = message.getWhen();
					try
					{
						BotAdapter.this.onNick(oldNick, newNick, when);
					}
					finally
					{
						if (isUserNick(oldNick)) irc.setUserNick(newNick);
					}
				}
			})
			.on("ping", HandlerBuilder.create(this, "onPing", "message.trail"))
			.on("error", HandlerBuilder.create(this, "onError", "message.trail"))
			.on("001", HandlerBuilder.create(this, "onStart"))
			.on("433", HandlerBuilder.create(this, "onNickConflict", "message.param1"))
			.on(new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (message.isNumericReply()) BotAdapter.this.onNumericReply(message);
				}
			})
			.start();
	}

	/**
	 * 接続中のIRCクライアントを返します。
	 * 
	 * @return IRCクライアント
	 * @throws NullPointerException IRCネットワークに接続していない場合
	 */
	public IRCClient getIRC()
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
		irc.nick(newNick);
	}

	/**
	 * 指定されたニックネームがユーザ自身かどうか判定します。
	 * 
	 * @param nick ニックネーム
	 * @return ユーザのニックネームの場合は true
	 * @see IRCClient#getUserNick()
	 */
	public boolean isUserNick(String nick)
	{
		String userNick = irc.getUserNick();
		return userNick.equals(nick);
	}

	/**
	 * 指定されたメッセージがダイレクトメッセージかどうか判定します。
	 * 
	 * @param message メッセージ
	 * @return ダイレクトメッセージの場合は true
	 */
	@Deprecated
	public boolean isDirectMessage(IRCMessage message)
	{
//		String channel = message.getParam0();
//		return isUserNick(channel);
		return message.isDirectMessage();
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
		if (autoNickname) irc.nick(conflictNick + "_");
	}

	/**
	 * ニックネームが変更されたときに呼び出されます。
	 * 
	 * @param oldNick 古いニックネーム
	 * @param newNick 新しいニックネーム
	 */
	public void onNick(String oldNick, String newNick, long when)
	{
	}

	/**
	 * ニックネームリストが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param names ニックネームリスト
	 */
	public void onNames(String channel, String[] names, long when)
	{
	}

	/**
	 * トピックが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param topic トピック
	 */
	public void onTopic(String channel, String topic, long when)
	{
	}

	/**
	 * チャンネルのモードが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param mode チャンネルモード
	 */
	public void onChannelMode(String channel, String mode)
	{
	}

	/**
	 * ユーザのモードが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param mode ユーザモード
	 * @param nick ニックネーム
	 */
	public void onUserMode(String channel, String mode, String nick)
	{
	}

	/**
	 * チャンネルに参加したときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param prefix ユーザ名
	 */
	public void onJoin(String channel, IRCPrefix prefix)
	{
	}

	/**
	 * チャンネルを離脱したときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param prefix ユーザ名
	 */
	public void onPart(String channel, IRCPrefix prefix)
	{
	}

	/**
	 * 終了したときに呼び出されます。
	 * 
	 * @param trail 終了メッセージ
	 * @param prefix ユーザ名
	 */
	public void onQuit(String trail, IRCPrefix prefix)
	{
	}

	/**
	 * 	ニックネームが衝突してサーバから強制的に排除されたときに呼び出されます。
	 * 
	 * @param trail 終了メッセージ
	 * @param prefix ユーザ名
	 * @see <a href="http://yoshino.tripod.com/73th/data/irccode.htm#quitmessage">server が付加する Quit Message</a>
	 */
	public void onKilled(String trail, IRCPrefix prefix)
	{
	}

	/**
	 * IRCネットワークの接続を確認するメッセージを受け取ったときに呼び出されます。
	 * デフォルトの実装は、IRCネットワークの接続を継続します。
	 * 自動継続したくないときは、このメソッドをオーバーライドしてください。
	 * 
	 * @param trail テキスト
	 * @see IRCClient#pong(String)
	 */
	public void onPing(String trail)
	{
		if (autoPingPong) irc.pong(trail);
	}

	/**
	 * エラーメッセージを受け取ったときに呼び出されます。
	 * IRCネットワークが切断したときは {@link #onStop()} を呼び出します。
	 * 
	 * @param trail エラーメッセージ
	 * @see IRCClient#isClosed()
	 * @see #onStop()
	 */
	public void onError(String trail)
	{
		if (irc.isClosed()) onStop();
	}

	/**
	 * IRCネットワークが切断したときに呼び出されます。
	 * 
	 * @see #onRestart()
	 * @see #onDestroy()
	 */
	public void onStop()
	{
		if (autoRecconection)
		{
			onRestart();
		}
		else
		{
			onDestroy();
		}
	}

	/**
	 * IRCネットワークに再接続するときに呼び出されます。
	 * 
	 * @see IRCClient#start()
	 * @see #onStart()
	 */
	public void onRestart()
	{
		try
		{
			irc.start();
		}
		catch (IOException x)
		{
			throw new RuntimeException("IRCネットワークに再接続できません。", x);
		}
	}

	/**
	 * IRCネットワークの再接続を止めたときに呼び出されます。
	 */
	public void onDestroy()
	{
	}

	/**
	 * メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onMessage(IRCMessage message)
	{
	}

	/**
	 * ダイレクトメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onDirectMessage(IRCMessage message)
	{
	}

	/**
	 * お知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onNotice(IRCMessage message)
	{
	}

	/**
	 * ニュメリックリプライを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onNumericReply(IRCMessage message)
	{
	}

	/**
	 * CTCPお知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onCtcpReply(IRCMessage message)
	{
	}

	/**
	 * CTCP問い合わせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 * @see #onCtcpPing(String, IRCPrefix)
	 * @see #onCtcpTime(String, IRCPrefix)
	 * @see #onCtcpVersion(String, IRCPrefix)
	 * @see #onCtcpUserInfo(String, IRCPrefix)
	 * @see #onCtcpClientInfo(String, IRCPrefix)
	 * @see #onDccSend(String, IRCPrefix)
	 */
	public void onCtcpQuery(IRCMessage message)
	{
		IRCPrefix prefix = message.getPrefix();
		for (String ctcp : message.splitCTCP())
		{
			if (ctcp.startsWith("PING"))
			{
				onCtcpPing(ctcp, prefix);
			}
			else if (ctcp.startsWith("TIME"))
			{
				onCtcpTime(ctcp, prefix);
			}
			else if (ctcp.startsWith("VERSION"))
			{
				onCtcpVersion(ctcp, prefix);
			}
			else if (ctcp.startsWith("USERINFO"))
			{
				onCtcpUserInfo(ctcp, prefix);
			}
			else if (ctcp.startsWith("CLIENTINFO"))
			{
				onCtcpClientInfo(ctcp, prefix);
			}
			else if (ctcp.startsWith("DCC SEND"))
			{
				onDccSend(ctcp, prefix);
			}
			else
			{
				// ignore
			}
		}
	}

	/**
	 * CTCP PINGを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix ユーザ名
	 */
	public void onCtcpPing(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), trail);
	}

	/**
	 * CTCP TIMEを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix ユーザ名
	 */
	public void onCtcpTime(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), String.format(Locale.ENGLISH, "TIME %tc", System.currentTimeMillis()));
	}

	/**
	 * CTCP VERSIONを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix ユーザ名
	 */
	public void onCtcpVersion(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), "VERSION IRCKit for Java");
	}

	/**
	 * CTCP USERINFOを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix ユーザ名
	 */
	public void onCtcpUserInfo(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), "USERINFO " + irc.getUserNick());
	}

	/**
	 * CTCP CLIENTINFOを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix ユーザ名
	 */
	public void onCtcpClientInfo(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), "CLIENTINFO PING TIME VERSION USERINFO CLIENTINFO DCC");
	}

	/**
	 * DCC SENDを受け取ったときに呼び出されます。
	 * ファイル送信メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param trail テキスト
	 * @param prefix ユーザ名
	 * @see #onCtcpQuery(IRCMessage)
	 */
	public void onDccSend(String trail, IRCPrefix prefix)
	{
	}
}
