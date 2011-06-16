package com.mac.tarchan.irc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * IRCClient
 */
public class IRCClient
{
	String host;

	int port;

	String nick;

	/**
	 * IRCClient
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 */
	protected IRCClient(String host, int port, String nick)
	{
		this.host = host;
		this.port = port;
		this.nick = nick;
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @return IRCClient
	 */
	public static IRCClient createClient(String host, int port, String nick)
	{
		IRCClient client = new IRCClient(host, port, nick);
		// TODO ホストアドレス毎にキャッシュ
		return client;
	}

	/**
	 * 指定されたコマンドのハンドラを追加します。
	 * 
	 * @param command コマンド
	 * @param handler ハンドラ
	 * @return IRCClient オブジェクト
	 */
	public IRCClient on(String command, IRCHandler handler)
	{
//		command = command.toUpperCase();
		IRCMessage message = new IRCMessage(command);
//		handler.onMessage(IRCMessage.createMessage(this, command));
		handler.onMessage(new IRCEvent(this, message));
		return this;
	}

	/**
	 * すべてのコマンドを受けるハンドラを追加します。
	 * 
	 * @param handler ハンドラ
	 * @return IRCClient オブジェクト
	 */
	public IRCClient on(IRCHandler handler)
	{
		return this;
	}

	/**
	 * 指定されたコマンドのハンドラを追加します。
	 * 
	 * @param handler ハンドラ
	 * @return IRCClient オブジェクト
	 */
	public IRCClient on(Object handler)
	{
		// TODO アノテーションでハンドラを指定
		return this;
	}

	/**
	 * サーバーに接続します。
	 * 
	 * @return IRCClient
	 * @throws IOException サーバーに接続できない場合
	 */
	public IRCClient connect() throws IOException
	{
		InetAddress inet = InetAddress.getByName(host);
		System.out.printf("connect: %s:%s%n", inet, port);
		return this;
	}

	/**
	 * ログインユーザの現在のニックネームを返します。
	 * 
	 * @return ニックネーム
	 */
	public String getNick()
	{
		return nick;
	}

	/**
	 * 指定されたコマンドを送信します。
	 * 
	 * @param command コマンド名
	 * @param args コマンド引数
	 * @return IRCClient
	 */
	public IRCClient postMessage(String command, String... args)
	{
		return postMessage(String.format("%S: %s", command, Arrays.toString(args)));
	}

	/**
	 * 指定されたテキストを送信します。
	 * 
	 * @param text テキスト
	 * @return IRCClient
	 */
	public IRCClient postMessage(String text)
	{
		System.out.println("send: " + text);
		return this;
	}

	/**
	 * 指定されたチャンネルに入ります。
	 * 
	 * @param channel チャンネル名
	 * @return IRCClient
	 */
	public IRCClient join(String channel)
	{
		return postMessage(String.format("%S: %s", "join", channel));
	}
}
