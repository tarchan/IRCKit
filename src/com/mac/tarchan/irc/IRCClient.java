/*
 * IRCClient.java
 * IRCKit
 * 
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mac.tarchan.irc.util.KanaInputFilter;

/**
 * IRCクライアントを実装します。
 */
public class IRCClient
{
	/** ログ */
	private static final Log log = LogFactory.getLog(IRCClient.class);

	/** ホスト名 */
	protected String host;

	/** ポート番号 */
	protected int port;

	/** ニックネーム */
	protected String nick;

	/** パスワード */
	protected String pass;

	/** 接続モード (if the bit 2 is set, the user mode 'w' will be set and if the bit 3 is set, the user mode 'i' will be set.) */
	protected int mode;

	/** 文字コード */
	protected String encoding;

	/** 入出力ソケット */
	protected Socket socket;

	/** メッセージキュー */
	protected ExecutorService messageQueue = Executors.newFixedThreadPool(2);

	/** メッセージハンドラ */
	protected ArrayList<IRCHandler> handlers = new ArrayList<IRCHandler>();

	/**
	 * IRCClient を構築します。
	 * 
	 * @param host ホスト名
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @param encoding 文字コード
	 */
	protected IRCClient(String host, int port, String nick, String pass, String encoding)
	{
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.pass = pass;
		this.encoding = encoding;
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホスト名
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @return IRCClient
	 */
	public static IRCClient createClient(String host, int port, String nick)
	{
		return createClient(host, port, nick, null);
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホスト名
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @return IRCクライアント
	 */
	public static IRCClient createClient(String host, int port, String nick, String pass)
	{
		return createClient(host, port, nick, pass, "JIS");
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホスト名
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @param encoding 文字コード
	 * @return IRCクライアント
	 */
	public static IRCClient createClient(String host, int port, String nick, String pass, String encoding)
	{
		IRCClient client = new IRCClient(host, port, nick, pass, encoding);
		// TODO ホストアドレス毎にキャッシュする
		return client;
	}

	/**
	 * ホスト名を返します。
	 * 
	 * @return ホスト名
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * ポート番号を返します。
	 * 
	 * @return ポート番号
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * ニックネームを返します。
	 * 
	 * @return ニックネーム
	 */
	public String getUserNick()
	{
		return nick;
	}

	/**
	 * 文字コードを返します。
	 * 
	 * @return 文字コード
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * すべてのコマンドを受け入れるメッセージハンドラを追加します。
	 * 
	 * @param handler メッセージハンドラ
	 * @return IRCクライアント
	 */
	public IRCClient on(IRCHandler handler)
	{
		handlers.add(handler);
		return this;
	}

	/**
	 * 指定されたコマンドのメッセージハンドラを追加します。
	 * 
	 * @param command コマンド
	 * @param handler メッセージハンドラ
	 * @return IRCクライアント
	 */
	public IRCClient on(String command, IRCHandler handler)
	{
		final String _command = command.toUpperCase();
		final IRCHandler _handler = handler;
		return on(new IRCHandler()
		{
			public void onMessage(IRCEvent event)
			{
				if (event.getMessage().getCommand().equals(_command)) _handler.onMessage(event);
			}
		});
	}

	/**
	 * IRCサーバに接続します。
	 * 
	 * @return IRCクライアント
	 * @throws IOException IRCサーバに接続できない場合
	 */
	public IRCClient connect() throws IOException
	{
//		InetAddress inet = InetAddress.getByName(host);
//		socket = new Socket(host, port);
//		log.info("connect: " + inet);
////		new Thread(new InputListener(this)).start();
//		messageQueue.execute(new InputTask(this));
		connect(host, port);
//		out = new PrintStream(socket.getOutputStream(), true);
//		if (pass != null && pass.trim().length() != 0) sendMessage("PASS %s", pass);
//		sendMessage("NICK %s", nick);
//		sendMessage("USER %s %d %s :%s", nick, mode, host, nick);
		login(nick, nick, nick, mode, pass);
//		sendMessage("USER %s %s bla :%s", nick, host, nick);
//		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		while (true)
//		{
//			String line = in.readLine();
//			if (line == null) break;
//			System.out.println(new String(line.getBytes(), "JIS"));
//		}
//		log.info("connected: " + host);
//		socket.close();
		return this;
	}

	/**
	 * IRCサーバに接続します。
	 * 
	 * @param host ホスト名
	 * @param port ポート番号
	 * @return IRCクライアント
	 * @throws IOException IRCサーバに接続できない場合
	 */
	public IRCClient connect(String host, int port) throws IOException
	{
		InetAddress inet = InetAddress.getByName(host);
		socket = new Socket(host, port);
		log.info("接続します。: " + inet);
//		new Thread(new InputListener(this)).start();
		messageQueue.execute(new InputTask(this));
		return this;
	}

	/**
	 * IRCサーバの接続をクローズします。
	 * 
	 * @return IRCクライアント
	 * @throws IOException IRCサーバの接続をクローズできない場合
	 */
	public IRCClient close() throws IOException
	{
		socket.close();
		messageQueue.shutdown();
		log.info("disconnected. " + socket.isConnected() + ", " + socket.isClosed());
		return this;
	}

	/**
	 * 入力ストリームを返します。
	 * 
	 * @return 入力ストリーム
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public InputStream getInputStream() throws IOException
	{
		return socket.getInputStream();
	}

	/**
	 * 出力ストリームを返します。
	 * 
	 * @return 出力ストリーム
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}

	/**
	 * 指定されたテキストを送信します。
	 * 
	 * @param text テキスト
	 * @return IRCクライアント
	 */
	public IRCClient sendMessage(String text)
	{
		if (text != null && text.trim().length() > 0) messageQueue.execute(new OutputTask(this, text));
		return this;
	}

	/**
	 * 指定されたコマンドを送信します。
	 * 
	 * @param command コマンド書式
	 * @param args コマンド引数
	 * @return IRCクライアント
	 */
	public IRCClient sendMessage(String command, Object... args)
	{
		return sendMessage(String.format(command, args));
	}

	/**
	 * IRCネットワークにログインします。
	 * 
	 * @param nick ニックネーム
	 * @param user ユーザ名
	 * @param real 本名
	 * @param mode 接続モード
	 * @param pass パスワード
	 * @return IRCクライアント
	 */
	public IRCClient login(String nick, String user, String real, int mode, String pass)
	{
		if (pass != null && pass.length() != 0) sendMessage("PASS %s", pass);
		sendMessage("NICK %s", nick);
		sendMessage("USER %s %d * :%s", user, mode, real);
		return this;
	}

	/**
	 * 新しいニックネームを設定します。
	 * 
	 * @param nick 新しいニックネーム
	 * @return IRCクライアント
	 */
	public IRCClient nick(String nick)
	{
		return sendMessage("NICK %s", nick);
	}

	/**
	 * 指定されたチャンネルに参加します。
	 * IRCサーバに JOIN コマンドを送信します。
	 * 
	 * @param channel チャンネル名
	 * @return IRCクライアント
	 */
	public IRCClient join(String channel)
	{
		return sendMessage("JOIN %s", channel);
	}

	/**
	 * 指定されたチャンネルを離脱します。
	 * IRCサーバに PART コマンドを送信します。
	 * 
	 * @param channel チャンネル名
	 * @return IRCクライアント
	 */
	public IRCClient part(String channel)
	{
		return sendMessage("PART %s", channel);
	}

	/**
	 * IRCサーバとの接続を継続します。
	 * IRCサーバに PONG コマンドを送信します。
	 * 
	 * @param payload ペイロード
	 * @return IRCクライアント
	 */
	public IRCClient pong(String payload)
	{
		return sendMessage("PONG :%s", payload);
	}

	/**
	 * 指定されたテキストを送信します。
	 * IRCサーバに PRIVMSG コマンドを送信します。
	 * 
	 * @param receiver テキストの宛先
	 * @param text テキスト
	 * @return IRCクライアント
	 */
	public IRCClient privmsg(String receiver, String text)
	{
		return sendMessage("PRIVMSG %s :%s", receiver, text);
	}

	/**
	 * 指定されたテキストを送信します。
	 * IRCサーバに NOTICE コマンドを送信します。
	 * 
	 * @param receiver テキストの宛先
	 * @param text テキスト
	 * @return IRCクライアント
	 */
	public IRCClient notice(String receiver, String text)
	{
		return sendMessage("NOTICE %s :%s", receiver, text);
	}

	/**
	 * CTCPクエリを送信します。
	 * 
	 * @param receiver テキストの宛先
	 * @param text テキスト
	 * @return IRCクライアント
	 * @see #privmsg(String, String)
	 */
	public IRCClient ctcpQuery(String receiver, String text)
	{
		return privmsg(receiver, IRCMessage.wrapCTCP(text));
	}

	/**
	 * CTCPリプライを送信します。
	 * 
	 * @param receiver テキストの宛先
	 * @param text テキスト
	 * @return IRCクライアント
	 * @see #notice(String, String)
	 */
	public IRCClient ctcpReply(String receiver, String text)
	{
		return notice(receiver, IRCMessage.wrapCTCP(text));
	}

	/**
	 * 指定されたメッセージを送信して、IRCサーバとの接続を終了します。
	 * IRCサーバに QUIT コマンドを送信します。
	 * 
	 * @param text QUITメッセージ
	 * @return IRCクライアント
	 */
	public IRCClient quit(String text)
	{
		return sendMessage("QUIT :%s", text);
	}

	/**
	 * 指定されたテキストを解析して、ハンドラに送信します。
	 * 
	 * @param text テキスト
	 */
	protected void fireMessage(String text)
	{
		try
		{
			IRCMessage message = new IRCMessage(text);
//			String encoding = "JIS";
////			System.out.println(new String(text.getBytes(), encoding));
//			IRCMessage message = new IRCMessage(text, encoding);
//			handler.onMessage(new IRCEvent(this, message));
//			System.out.println(message.toString());
			IRCEvent event = new IRCEvent(this, message);
			for (IRCHandler handler : handlers)
			{
				try
				{
					handler.onMessage(event);
				}
				catch (Throwable x)
				{
					log.error("IRCハンドラの実行中にエラーが発生しました。", x);
					x.printStackTrace();
				}
			}
		}
		catch (Throwable x)
		{
			log.error("IRCメッセージの処理を中止しました。: " + text, x);
		}
	}
}

/**
 * InputTask
 */
class InputTask implements Runnable
{
	IRCClient irc;

	BufferedReader in;

//	String encoding;

	InputTask(IRCClient irc) throws IOException
	{
		this.irc = irc;
//		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//		String encoding = "JIS";
		String encoding = irc.getEncoding();
//		in = new BufferedReader(new InputStreamReader(client.getInputStream(), encoding));
		in = new BufferedReader(new InputStreamReader(new KanaInputFilter(irc.getInputStream()), encoding));
	}

	public void run()
	{
		try
		{
			while (true)
			{
				String line = in.readLine();
				if (line == null) break;
//				System.out.println(new String(line.getBytes(), encoding));
				irc.fireMessage(line);
				Thread.yield();
			}
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
		finally
		{
			try
			{
				irc.close();
			}
			catch (IOException x)
			{
				x.printStackTrace();
			}
		}
	}
}

/**
 * OutputTask
 */
class OutputTask implements Runnable
{
	/** ログ */
	private static final Log log = LogFactory.getLog(OutputTask.class);

	IRCClient irc;

	String text;

	PrintStream out;

	OutputTask(IRCClient irc, String text)
	{
		this.irc = irc;
		this.text = text;
	}

	public void run()
	{
		try
		{
			log.info(text);
//			client.out.println(text);
			out = new PrintStream(irc.getOutputStream(), true, "JIS");
			out.println(text);
//			out.close();
		}
		catch (Throwable x)
		{
			throw new RuntimeException("送信エラー: " + text, x);
		}
	}
}
