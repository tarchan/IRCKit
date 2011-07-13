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
					IRCPrefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String text = message.getTrail();
					if (message.isCTCP())
					{
						for (String ctcp : message.splitCTCP())
						{
							String[] span = ctcp.split(" ", 2);
							String command = span[0].toUpperCase();
							String param = span[1];
							BotAdapter.this.onCtcp(prefix, channel, command, param);
						}
					}
					else if (message.isDirectMessage())
					{
						BotAdapter.this.onDirectMessage(prefix, channel, text);
					}
					else
					{
						BotAdapter.this.onMessage(prefix, channel, text);
					}
				}
			})
			.on("notice", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					IRCPrefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String text = message.getTrail();
					if (message.isCTCP())
					{
						BotAdapter.this.onCtcpReply(prefix, channel, message.splitCTCP());
					}
					else
					{
						BotAdapter.this.onNotice(prefix, channel, text);
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
					BotAdapter.this.onJoin(prefix, channel);
				}
			})
			.on("part", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					IRCPrefix prefix = message.getPrefix();
					String channel = message.getParam1();
					String text = message.getTrail();
					BotAdapter.this.onPart(prefix, channel, text);
				}
			})
			.on("quit", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String text = message.getTrail();
					IRCPrefix prefix = message.getPrefix();
					BotAdapter.this.onQuit(prefix, text);
					if (text.equals("Killed"))
					{
						BotAdapter.this.onKilled(prefix, text);
					}
				}
			})
			.on("mode", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					IRCPrefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String mode = message.getParam1();
					if (message.getParamsCount() < 3)
					{
						// TODO チャンネルモード
						// (MODE):tarcMac21!~tarchan@124x38x70x51.ap124.ftth.ucom.ne.jp/#javabreak +p-s/:null
						BotAdapter.this.onMode(prefix, channel, mode);
					}
					else
					{
						// TODO ユーザーモードが複数指定された場合は分解して呼び出す
						// (MODE):tarcMac21!~tarchan@124x38x70x51.ap124.ftth.ucom.ne.jp/#javabreak +oo mybot bot2/:null
						String nick = message.getParam2();
						BotAdapter.this.onMode(prefix, channel, mode, nick);
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
					IRCPrefix prefix = message.getPrefix();
					String oldNick = message.getPrefix().getNick();
					String newNick = message.getTrail();
//					long when = message.getWhen();
					try
					{
						BotAdapter.this.onNick(prefix, newNick);
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

//	/**
//	 * 新しいニックネームを設定します。
//	 * 
//	 * @param newNick 新しいニックネーム
//	 */
//	public void setUserNick(String newNick)
//	{
//		irc.nick(newNick);
//	}

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
	 * @param prefix プレフィックス
	 * @param newNick 新しいニックネーム
	 */
	public void onNick(IRCPrefix prefix, String newNick)
	{
	}

	/**
	 * ニックネームリストが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param names ニックネームリスト
	 * @param when メッセージ作成時間
	 */
	public void onNames(String channel, String[] names, long when)
	{
	}

	/**
	 * トピックが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param topic トピック
	 * @param when メッセージ作成時間
	 */
	public void onTopic(String channel, String topic, long when)
	{
	}

	/**
	 * チャンネルのモードが変更されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param mode チャンネルモード
	 */
	public void onMode(IRCPrefix prefix, String channel, String mode)
	{
	}

	/**
	 * ユーザのモードが変更されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param mode ユーザモード
	 * @param nick ニックネーム
	 */
	public void onMode(IRCPrefix prefix, String channel, String mode, String nick)
	{
	}

	/**
	 * チャンネルに招待されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 */
	public void onInvite(IRCPrefix prefix, String channel)
	{
		// TODO INVITE
	}

	/**
	 * チャンネルに参加したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 */
	public void onJoin(IRCPrefix prefix, String channel)
	{
	}

	/**
	 * チャンネルを離脱したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text 離脱メッセージ
	 */
	public void onPart(IRCPrefix prefix, String channel, String text)
	{
	}

	/**
	 * チャンネルを追放されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param target 追放したオペレータ
	 * @param text 追放メッセージ
	 */
	public void onKick(IRCPrefix prefix, String channel, String target, String text)
	{
		// TODO KICK
	}

	/**
	 * 終了したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param text 終了メッセージ
	 */
	public void onQuit(IRCPrefix prefix, String text)
	{
	}

	/**
	 * 	ニックネームが衝突してサーバから強制的に排除されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param text 終了メッセージ
	 * @see <a href="http://yoshino.tripod.com/73th/data/irccode.htm#quitmessage">server が付加する Quit Message</a>
	 */
	public void onKilled(IRCPrefix prefix, String text)
	{
	}

	/**
	 * IRCネットワークの接続を確認するメッセージを受け取ったときに呼び出されます。
	 * デフォルトの実装は、IRCネットワークの接続を継続します。
	 * 自動継続したくないときは、このメソッドをオーバーライドしてください。
	 * 
	 * @param text テキスト
	 * @see IRCClient#pong(String)
	 */
	public void onPing(String text)
	{
		if (autoPingPong) irc.pong(text);
	}

	/**
	 * エラーメッセージを受け取ったときに呼び出されます。
	 * IRCネットワークが切断したときは {@link #onStop()} を呼び出します。
	 * 
	 * @param text エラーメッセージ
	 * @see IRCClient#isClosed()
	 * @see #onStop()
	 */
	public void onError(String text)
	{
		if (irc.isClosed()) onStop();
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
	 * メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text テキスト
	 */
	public void onMessage(IRCPrefix prefix, String channel, String text)
	{
	}

	/**
	 * ダイレクトメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param text テキスト
	 */
	public void onDirectMessage(IRCPrefix prefix, String target, String text)
	{
	}

	/**
	 * お知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text テキスト
	 */
	public void onNotice(IRCPrefix prefix, String channel, String text)
	{
	}

	/**
	 * CTCPお知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text CTCPリプライ
	 */
	public void onCtcpReply(IRCPrefix prefix, String channel, String[] text)
	{
	}

	/**
	 * CTCP問い合わせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param command CTCPコマンド
	 * @param param CTCPパラメータ
	 * @see #onCtcpPing(String, IRCPrefix)
	 * @see #onCtcpTime(String, IRCPrefix)
	 * @see #onCtcpVersion(String, IRCPrefix)
	 * @see #onCtcpUserInfo(String, IRCPrefix)
	 * @see #onCtcpClientInfo(String, IRCPrefix)
	 * @see #onDccSend(String, IRCPrefix)
	 */
	public void onCtcp(IRCPrefix prefix, String channel, String command, String param)
	{
		if (command.equals("PING"))
		{
			onCtcpPing(param, prefix);
		}
		else if (command.equals("TIME"))
		{
			onCtcpTime(param, prefix);
		}
		else if (command.equals("VERSION"))
		{
			onCtcpVersion(param, prefix);
		}
		else if (command.equals("USERINFO"))
		{
			onCtcpUserInfo(param, prefix);
		}
		else if (command.equals("CLIENTINFO"))
		{
			onCtcpClientInfo(param, prefix);
		}
		else if (command.equals("ACTION"))
		{
			onCtcpAction(param, prefix);
		}
		else if (command.equals("DCC SEND"))
		{
			// TODO DCC
			onDccSend(param, prefix);
		}
		else
		{
			// ignore
		}
	}

	/**
	 * CTCP PINGを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix プレフィックス
	 */
	public void onCtcpPing(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), trail);
	}

	/**
	 * CTCP TIMEを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix プレフィックス
	 */
	public void onCtcpTime(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), String.format(Locale.ENGLISH, "TIME %tc", System.currentTimeMillis()));
	}

	/**
	 * CTCP VERSIONを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix プレフィックス
	 */
	public void onCtcpVersion(String trail, IRCPrefix prefix)
	{
		irc.ctcpReply(prefix.getNick(), "VERSION IRCKit for Java");
	}

	/**
	 * CTCP USERINFOを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix プレフィックス
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
	 * CTCP ACTIONを受け取ったときに呼び出されます。
	 * 
	 * @param trail CTCPメッセージ
	 * @param prefix プレフィックス
	 */
	public void onCtcpAction(String trail, IRCPrefix prefix)
	{
		// TODO ACTION
	}

	/**
	 * DCC SENDを受け取ったときに呼び出されます。
	 * ファイル送信メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param trail テキスト
	 * @param prefix プレフィックス
	 */
	public void onDccSend(String trail, IRCPrefix prefix)
	{
	}
}
