/*
 * IRCBot.java
 * IRCKit
 * 
 * Created by tarchan on 2011/06/16.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.util;

import java.io.IOException;

import com.mac.tarchan.irc.IRCClient;
import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;
import com.mac.tarchan.irc.IRCMessage;

/**
 * IRCメッセージを受け取る抽象アダプタクラスです。
 * このクラスはIRCボットの作成を容易にするためのものです。
 * IRCEvent リスナーを作成するには、このクラスを拡張して関係のあるイベントに対するメソッドをオーバーライドします。
 */
public abstract class IRCBotAdapter
{
	/** IRCクライアント */
	private IRCClient irc;

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
//			.on("privmsg", HandlerBuilder.create(this, "onMessage", "message"))
			.on("privmsg", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (!message.isCTCP())
					{
						IRCBotAdapter.this.onMessage(message);
					}
					else
					{
						IRCBotAdapter.this.onCtcpQuery(message);
					}
				}
			})
			.on("notice", new IRCHandler()
			{
				@Override
				public void onMessage(IRCEvent event)
				{
					IRCMessage message = event.getMessage();
					if (!message.isCTCP())
					{
						IRCBotAdapter.this.onNotice(message);
					}
					else
					{
						IRCBotAdapter.this.onCtcpReply(message);
					}
				}
			})
			.on("ping", HandlerBuilder.create(this, "onPing", "message.trailing"))
			.on("error", HandlerBuilder.create(this, "onError", "message.trailing"))
			.connect();
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
		getIRC().sendMessage("NICK %s", newNick);
	}

	/**
	 * 指定されたニックネームがユーザ自身かどうか判定します。
	 * 
	 * @param nick ニックネーム
	 * @return ユーザのニックネームの場合は true
	 * @see IRCClient#getNick()
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
		getIRC().pong(text);
	}

	/**
	 * エラーメッセージを受け取ったときに呼び出されます。
	 * 
	 * @param text エラーメッセージ
	 */
	public void onError(String text)
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
