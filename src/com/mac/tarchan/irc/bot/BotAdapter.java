/*
 * BotAdapter.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.bot;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCEvent;
import com.mac.tarchan.irc.client.IRCHandler;
import com.mac.tarchan.irc.client.IRCMessage;
import com.mac.tarchan.irc.client.IRCMessage.CTCP;
import com.mac.tarchan.irc.client.IRCMessage.Prefix;
import com.mac.tarchan.irc.client.util.HandlerBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

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
					Prefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String text = message.getTrail();
					if (message.isCTCP())
					{
						for (CTCP ctcp : message.toCTCPArray())
						{
							BotAdapter.this.onCtcp(prefix, channel, ctcp);
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
					Prefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String text = message.getTrail();
					if (message.isCTCP())
					{
						for (CTCP ctcp : message.toCTCPArray())
						{
							BotAdapter.this.onCtcpReply(prefix, channel, ctcp);
						}
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
					Prefix prefix = message.getPrefix();
					BotAdapter.this.onJoin(prefix, channel);
				}
			})
			.on("part", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					Prefix prefix = message.getPrefix();
					String channel = message.getParam0();
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
					Prefix prefix = message.getPrefix();
					String text = message.getTrail();
					BotAdapter.this.onQuit(prefix, text);
					if (text.startsWith("Killed"))
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
					Prefix prefix = message.getPrefix();
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
					Prefix prefix = message.getPrefix();
					String channel = message.getParam1();
					String[] names = nicklist.toArray(new String[]{});
					nicklist.clear();
					BotAdapter.this.onNames(prefix, channel, names);
				}
			})
			.on("topic", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					Prefix prefix = message.getPrefix();
					String channel = message.getParam0();
					String topic = message.getTrail();
					BotAdapter.this.onTopic(prefix, channel, topic);
				}
			})
			.on("332", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					Prefix prefix = message.getPrefix();
					String channel = message.getParam1();
					String topic = message.getTrail();
					BotAdapter.this.onTopic(prefix, channel, topic);
				}
			})
			.on("nick", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					Prefix prefix = message.getPrefix();
					String oldNick = prefix.getNick();
					String newNick = message.getTrail();
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
			.on("error", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					BotAdapter.this.onError(message.getPrefix(), message.getTrail());
				}
			})
			.on("ping", HandlerBuilder.create(this, "onPing", "message.trail"))
//			.on("error", HandlerBuilder.create(this, "onError", "message.trail"))
			.on("001", HandlerBuilder.create(this, "onStart"))
			.on("433", HandlerBuilder.create(this, "onNickConflict", "message.param1"))
			.on(new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (message.isNumericReply()) BotAdapter.this.onNumericReply(message.getPrefix(), message.getNumber(), message.getTrail());
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
	 * IRCネットワークが切断したときは {@link #onStop(Prefix)} を呼び出します。
	 * 
	 * @param prefix プレフィックス
	 * @param text エラーメッセージ
	 * @see IRCClient#isClosed()
	 * @see #onStop(Prefix)
	 */
	public void onError(Prefix prefix, String text)
	{
		if (irc.isClosed()) onStop(prefix);
	}

	/**
	 * IRCネットワークが切断したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @see #onRestart()
	 * @see #onDestroy()
	 */
	public void onStop(Prefix prefix)
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
	public void onNick(Prefix prefix, String newNick)
	{
	}

	/**
	 * ニックネームリストが変更されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param names ニックネームリスト
	 */
	public void onNames(Prefix prefix, String channel, String[] names)
	{
	}

	/**
	 * トピックが変更されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param topic トピック
	 */
	public void onTopic(Prefix prefix, String channel, String topic)
	{
	}

	/**
	 * チャンネルのモードが変更されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param mode チャンネルモード
	 */
	public void onMode(Prefix prefix, String channel, String mode)
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
	public void onMode(Prefix prefix, String channel, String mode, String nick)
	{
	}

	/**
	 * チャンネルに招待されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 */
	public void onInvite(Prefix prefix, String channel)
	{
		// TODO INVITE
	}

	/**
	 * チャンネルに参加したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 */
	public void onJoin(Prefix prefix, String channel)
	{
	}

	/**
	 * チャンネルを離脱したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text 離脱メッセージ
	 */
	public void onPart(Prefix prefix, String channel, String text)
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
	public void onKick(Prefix prefix, String channel, String target, String text)
	{
		// TODO KICK
	}

	/**
	 * 終了したときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param text 終了メッセージ
	 */
	public void onQuit(Prefix prefix, String text)
	{
	}

	/**
	 * 	ニックネームが衝突してサーバから強制的に排除されたときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param text 終了メッセージ
	 * @see <a href="http://yoshino.tripod.com/73th/data/irccode.htm#quitmessage">server が付加する Quit Message</a>
	 */
	public void onKilled(Prefix prefix, String text)
	{
	}

	/**
	 * ニュメリックリプライを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param number ニュメリックリプライの番号
	 * @param text ニュメリックリプライのメッセージ
	 */
	public void onNumericReply(Prefix prefix, int number, String text)
	{
	}

	/**
	 * メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text テキスト
	 */
	public void onMessage(Prefix prefix, String channel, String text)
	{
	}

	/**
	 * ダイレクトメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param text テキスト
	 */
	public void onDirectMessage(Prefix prefix, String target, String text)
	{
	}

	/**
	 * お知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param channel チャンネル名
	 * @param text テキスト
	 */
	public void onNotice(Prefix prefix, String channel, String text)
	{
	}

	/**
	 * CTCPお知らせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpReply(Prefix prefix, String target, CTCP ctcp)
	{
	}

	/**
	 * CTCP問い合わせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 * @see #onCtcpPing(Prefix, String, CTCP)
	 * @see #onCtcpTime(Prefix, String, CTCP)
	 * @see #onCtcpVersion(Prefix, String, CTCP)
	 * @see #onCtcpUserInfo(Prefix, String, CTCP)
	 * @see #onCtcpClientInfo(Prefix, String, CTCP)
	 * @see #onCtcpAction(Prefix, String, CTCP)
	 * @see #onDccSend(Prefix, String, CTCP)
	 */
	public void onCtcp(Prefix prefix, String target, CTCP ctcp)
	{
		String command = ctcp.getCommand();
		if (command.equals(CTCP.PING))
		{
			onCtcpPing(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.VERSION))
		{
			onCtcpVersion(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.TIME))
		{
			onCtcpTime(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.CLIENTINFO))
		{
			onCtcpClientInfo(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.FINGER))
		{
			onCtcpFinger(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.USERINFO))
		{
			onCtcpUserInfo(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.ACTION))
		{
			onCtcpAction(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.DCC_SEND))
		{
			onDccSend(prefix, target, ctcp);
		}
		else if (command.equals(CTCP.DCC_CHAT))
		{
			onDccChat(prefix, target, ctcp);
		}
		else
		{
			// ignore
		}
	}

	/**
	 * CTCP PINGを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpPing(Prefix prefix, String target, CTCP ctcp)
	{
		irc.ctcpReply(prefix.getNick(), ctcp.toString());
	}

	/**
	 * CTCP VERSIONを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpVersion(Prefix prefix, String target, CTCP ctcp)
	{
		irc.ctcpReply(prefix.getNick(), "VERSION IRCKit for Java");
	}

	/**
	 * CTCP TIMEを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpTime(Prefix prefix, String target, CTCP ctcp)
	{
		irc.ctcpReply(prefix.getNick(), String.format(Locale.ENGLISH, "TIME %tc", System.currentTimeMillis()));
	}

	/**
	 * CTCP CLIENTINFOを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpClientInfo(Prefix prefix, String target, CTCP ctcp)
	{
		irc.ctcpReply(prefix.getNick(), "CLIENTINFO PING VERSION TIME CLIENTINFO FINGER USERINFO ACTION DCC");
	}

	/**
	 * CTCP FINGERを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpFinger(Prefix prefix, String target, CTCP ctcp)
	{
	}

	/**
	 * CTCP USERINFOを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpUserInfo(Prefix prefix, String target, CTCP ctcp)
	{
		irc.ctcpReply(prefix.getNick(), "USERINFO " + irc.getUserNick());
	}

	/**
	 * CTCP ACTIONを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onCtcpAction(Prefix prefix, String target, CTCP ctcp)
	{
	}

	/**
	 * DCC SENDを受け取ったときに呼び出されます。
	 * ファイル送信メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onDccSend(Prefix prefix, String target, CTCP ctcp)
	{
	}

	/**
	 * DCC CHATを受け取ったときに呼び出されます。
	 * 
	 * @param prefix プレフィックス
	 * @param target 対象ニックネーム
	 * @param ctcp CTCPメッセージ
	 */
	public void onDccChat(Prefix prefix, String target, CTCP ctcp)
	{
	}
}
