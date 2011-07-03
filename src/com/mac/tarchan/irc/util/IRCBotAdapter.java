/*
 * IRCBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.mac.tarchan.irc.IRCClient;
import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;
import com.mac.tarchan.irc.IRCMessage;
import com.mac.tarchan.irc.IRCPrefix;

/**
 * IRCメッセージを受け取る抽象アダプタクラスです。
 * このクラスはIRCボットの作成を容易にするためのものです。
 * IRCEvent リスナーを作成するには、このクラスを拡張して関係のあるイベントに対するメソッドをオーバーライドします。
 */
public abstract class IRCBotAdapter
{
	/** IRCクライアント */
	protected IRCClient irc;

	/** ニックネームリスト */
	ArrayList<String> nicklist = new ArrayList<String>();

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
						IRCBotAdapter.this.onCtcpQuery(message);
					}
					else if (isDM(message))
					{
						IRCBotAdapter.this.onDirectMessage(message);
					}
					else
					{
						IRCBotAdapter.this.onMessage(message);
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
						IRCBotAdapter.this.onCtcpReply(message);
					}
					else
					{
						IRCBotAdapter.this.onNotice(message);
					}
				}
			})
			.on("join", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getTrailing();
					IRCPrefix prefix = message.getPrefix();
					IRCBotAdapter.this.onJoin(channel, prefix);
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
					IRCBotAdapter.this.onPart(channel, prefix);
				}
			})
			.on("quit", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String text = message.getTrailing();
					IRCPrefix prefix = message.getPrefix();
					IRCBotAdapter.this.onQuit(prefix, text);
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
						IRCBotAdapter.this.onChannelMode(channel, mode);
					}
					else
					{
						String nick = message.getParam2();
						IRCBotAdapter.this.onUserMode(channel, mode, nick);
					}
				}
			})
			.on("353", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String[] names = message.getTrailing().split(" ");
					nicklist.addAll(Arrays.asList(names));
				}
			})
			.on("366", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam(1);
					String[] names = nicklist.toArray(new String[]{});
					nicklist.clear();
					IRCBotAdapter.this.onNames(channel, names);
				}
			})
			.on("topic", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam0();
					String topic = message.getTrailing();
					IRCBotAdapter.this.onTopic(channel, topic);
				}
			})
			.on("332", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String channel = message.getParam1();
					String topic = message.getTrailing();
					IRCBotAdapter.this.onTopic(channel, topic);
				}
			})
			.on("nick", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					String oldNick = message.getPrefix().getNick();
					String newNick = message.getTrailing();
					IRCBotAdapter.this.onNick(oldNick, newNick);
				}
			})
			.on("ping", HandlerBuilder.create(this, "onPing", "message.trailing"))
			.on("error", HandlerBuilder.create(this, "onError", "message.trailing"))
			.on("001", HandlerBuilder.create(this, "onStart"))
			.on("433", HandlerBuilder.create(this, "onNickConflict", "message.param1"))
			.on(new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (message.isNumelic()) IRCBotAdapter.this.onNumelicReply(message);
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
	public boolean isDM(IRCMessage message)
	{
		String channel = message.getParam0();
		return isUserNick(channel);
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
	public void onNick(String oldNick, String newNick)
	{
		if (isUserNick(oldNick)) irc.setUserNick(newNick);
	}

	/**
	 * チャンネルに参加したときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param prefix プレフィックス
	 */
	public void onJoin(String channel, IRCPrefix prefix)
	{
	}

	/**
	 * ニックネームリストが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param names ニックネームリスト
	 */
	public void onNames(String channel, String[] names)
	{
	}

	/**
	 * トピックが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param topic トピック
	 */
	public void onTopic(String channel, String topic)
	{
	}

	/**
	 * チャンネルを離脱したときに呼び出されます。
	 */
	public void onPart(String channel, IRCPrefix prefix)
	{
	}

	/**
	 * 終了したときに呼び出されます。
	 * 
	 * @param prefix ユーザー
	 * @param text 終了メッセージ
	 */
	public void onQuit(IRCPrefix prefix, String text)
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
	 * ユーザーのモードが変更されたときに呼び出されます。
	 * 
	 * @param channel チャンネル名
	 * @param mode チャンネルモード
	 * @param nick ニックネーム
	 */
	public void onUserMode(String channel, String mode, String nick)
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
	 * CTCP問い合わせメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onCtcpQuery(IRCMessage message)
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
	 * ニューメリックリプライを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onNumelicReply(IRCMessage message)
	{
	}

	/**
	 * ファイル送信メッセージを受け取ったときに呼び出されます。
	 * 
	 * @param message IRCメッセージ
	 */
	public void onDccSend(IRCMessage message)
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
		irc.pong(text);
	}

	/**
	 * エラーメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param text エラーメッセージ
	 */
	public void onError(String text)
	{
		// TODO 切断したときに onStop を呼び出す
	}

	/**
	 * IRCネットワークが切断したときに呼び出されます。
	 */
	public void onStop()
	{
		// TODO 切断したときに再接続する
	}

	/**
	 * IRCネットワークの再接続を止めたときに呼び出されます。
	 */
	public void onDestroy()
	{
	}
}
