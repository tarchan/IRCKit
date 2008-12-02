/*
 * IRCNetwork.java
 * IRCKit
 *
 * Created by tarchan on 2008/03/19.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.mac.tarchan.net.irc.IRCConnection;
import com.mac.tarchan.net.irc.IRCWriter;

/**
 * @since 1.0
 * @author tarchan
 */
public class IRCNetwork
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(IRCNetwork.class);

	/** IRC ネットワークの名前 */
	private String name;

	/** プロパティー */
	private Properties properties;

	/** ロケール */
	private Locale locale = new Locale("jp");

	/** 入力 */
	public BufferedReader in;

	/** 出力 */
	public IRCWriter out;

	/**
	 * URL コネクションハンドラに com.mac.tarchan.net.irc.Hnadler を設定します。
	 * irc スキーマを実装します。
	 */
	static
	{
		System.setProperty("java.protocol.handler.pkgs", "com.mac.tarchan.net");
	}

	/**
	 * 
	 */
	private IRCNetwork()
	{
		Properties defaults = new Properties();
		defaults.setProperty("irc.encoding", "ISO-2022-JP");
		defaults.setProperty("irc.user", System.getProperty("user.name"));

		properties = new Properties(defaults);
		log.debug("defaults=" + defaults);
		log.debug("properties=" + properties);
	}

	/**
	 * IRCNetwork オブジェクトを構築します。
	 * 
	 * @param name IRC ネットワークの名前
	 */
	public IRCNetwork(String name)
	{
		this();
		this.name = name;
	}

	/**
	 * IRC ネットワークの名前を返します。
	 * 
	 * @return IRC ネットワークの名前
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * ロケールを返します。
	 * 
	 * @return ロケール
	 */
	public Locale locale()
	{
		return locale;
	}

	/**
	 * この IRC ネットワークのメッセージを受け取るオブジェクトを設定します。
	 * @param client メッセージを受け取るオブジェクト
	 * @return IRCNetwork オブジェクト
	 */
	public IRCNetwork register(Object client)
	{
		return this;
	}

	/**
	 * 指定された URL に接続します。
	 * 
	 * @param url IRC接続を表す URL
	 * @param user 接続ユーザ名
	 * @param pass 接続パスワード
	 * @return このオブジェクトへの参照
	 * @throws IOException 接続時にエラーが発生した場合
	 */
	public IRCNetwork connect(String url, String user, String pass) throws IOException
	{
		URL u = new URL(url);
		URLConnection c = u.openConnection();
//		c.setRequestProperty(IRCConnection.IRC_NICK, user);
		c.setRequestProperty(IRCConnection.IRC_USER, user);
		c.setRequestProperty(IRCConnection.IRC_PASS, pass);
		log.debug("connect: " + c + ", " + c.getClass().getName());
		c.connect();
//		IRCConnection con = (IRCConnection)c;
//		in = con.reader();
//		out = con.writer();
		String enc = "ISO-2022-JP";
		in = new BufferedReader(new InputStreamReader(c.getInputStream(), enc));
		out = IRCWriter.getWriter(new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), enc)));

		return this;
	}

	/**
	 * IRC ネットワークに接続します。
	 * 
	 * @param config 接続プロパティー
	 * @return このオブジェクトへの参照
	 * @throws IOException 接続時にエラーが発生した場合
	 */
	public IRCNetwork connect(Properties config) throws IOException
	{
		return this;
	}

	/**
	 * エンコーディングを設定します。
	 * 
	 * @param encoding エンコーディング
	 * @return このオブジェクトへの参照
	 * @see #reader()
	 * @see #writer()
	 */
	public IRCNetwork setEncoding(String encoding)
	{
		return this;
	}

	/**
	 * Reader オブジェクトを返します。
	 * 
	 * @return Reader オブジェクト
	 */
	public Reader reader()
	{
		return in;
	}

	/**
	 * PrintWriter オブジェクトを返します。
	 * 
	 * @return PrintWriter オブジェクト
	 */
	public PrintWriter writer()
	{
		return out;
	}

	/**
	 * 1 行読み込みます。
	 * 
	 * @return 1 行の文字列
	 */
	public String readLine()
	{
		try
		{
			return in.readLine();
		}
		catch (IOException e)
		{
			log.error("read line error!", e);
			return null;
		}
	}

	/**
	 * 1 行読み込んで IRCMessage に変換します。
	 * 
	 * @return IRCMessage オブジェクト
	 */
	public IRCMessage readMessage()
	{
		String line = readLine();
		log.trace(line);
		return line != null ? new IRCMessage(this, line) : null;
	}

	/**
	 * 1 行書き込みます。
	 * 
	 * @param format 書式文字列
	 * @param args 引数
	 * @return このオブジェクトへの参照
	 */
	public IRCNetwork printLine(String format, Object... args)
	{
		out.printf(format, args);
		out.println();
		return this;
	}

	/**
	 * 
	 * @param format
	 * @param args
	 * @return このオブジェクトへの参照
	 */
	public IRCNetwork printBreak(String format, Object... args)
	{
		out.printf(format, args);
		out.printf("\r");
		return this;
	}

	/**
	 * 
	 * @param ch
	 * @param msg
	 * @return このオブジェクトへの参照
	 */
	public IRCNetwork printMessage(String ch, String msg)
	{
		out.printf("PRIVMSG %s %s", ch, msg);
		out.println();
		return this;
	}

//	/**
//	 * 
//	 * @param channel
//	 */
//	public void join(String channel)
//	{
//		join(channel, "");
//	}
//
//	/**
//	 * 
//	 * @param channel
//	 * @param key
//	 */
//	public void join(String channel, String key)
//	{
//		out.join(channel, key);
//	}

	/**
	 * この IRC ネットワークとの接続を切断します。
	 * 
	 * @param message 終了のメッセージ
	 */
	public void quit(String message)
	{
		log.debug("quit: " + message);
		out.quit(message);
	}

	/**
	 * IRCNetwork の文字列表現を返します。
	 * 
	 * @return IRCNetwork の文字列表現
	 */
	public String toString()
	{
		return "irc network: " + name;
	}

	/** ネットワークグループ */
	protected static final HashMap<String, IRCNetwork> groups = new HashMap<String, IRCNetwork>();

	/** URL */
	protected URL url;

	/** ユーザプロパティー */
	protected Properties prof;

	/** ユーザ名 */
	protected String username;

	/** パスワード */
	protected String password;

	/**
	 * 指定された IRC ネットワークを構築します。
	 * 
	 * @param address サーバアドレス
	 * @param prof ユーザプロパティー
	 * @throws IllegalArgumentException 指定された文字列が RFC 2396 に違反する場合
	 * @throws MalformedURLException URL のプロトコルハンドラが見つからなかった場合、または URL の構築中にその他の何らかのエラーが発生した場合
	 */
	public IRCNetwork(String address, Properties prof) throws MalformedURLException
	{
		URI uri = URI.create(address);
		url = uri.toURL();
		this.prof = prof;
		System.out.println("host=" + url.getHost() + ", port=" + url.getPort());
	}

	/**
	 * デフォルトのプロパティーを返します。
	 * 
	 * @return デフォルトのプロパティー
	 */
	public static Properties createDefaultProperties()
	{
		Properties def = new Properties();
		String username = System.getProperty("user.name");
		def.setProperty("irc.user.name", username);
		def.setProperty("irc.user.password", "");
		def.setProperty("irc.user.mode", "0");
		def.setProperty("irc.nick.name", username);
		def.setProperty("irc.real.name", username);

		return new Properties(def);
	}

	/**
	 * 指定された IRC ネットワークを登録します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param address サーバアドレス
	 * @param username ユーザ名
	 * @param password パスワード
	 * @throws IllegalArgumentException 指定された文字列が RFC 2396 に違反する場合
	 * @throws MalformedURLException URL のプロトコルハンドラが見つからなかった場合、または URL の構築中にその他の何らかのエラーが発生した場合
	 */
	public static void register(String groupName, String address, String username, String password) throws MalformedURLException
	{
		Properties prof = createDefaultProperties();
		prof.setProperty("irc.user.name", username);
		prof.setProperty("irc.real.name", username);
		prof.setProperty("irc.nick.name", username);
		prof.setProperty("irc.user.password", password);
		IRCNetwork network = new IRCNetwork(address, prof);
		groups.put(groupName, network);
	}

	/**
	 * 指定された IRC ネットワークを検索します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @return 指定されたグループの IRC ネットワーク
	 */
	public static IRCNetwork find(String groupName)
	{
		return groups.get(groupName);
	}

	/** 改行コード */
	private static final String CRLF = "\r\n";

	/** PASS <password> */
	public static final String PASS = "PASS %s";

	/** NICK <nickname> */
	public static final String NICK = "NICK %s";

	/** USER <user> <mode> <unused> <realname> */
	public static final String USER = "USER %s %d * :%s";

	/** PONG <server1> [<server2>] */
	public static final String PONG = "PONG %s";

	/**
	 * IRC ネットワークを接続します。
	 * 
	 * @throws IOException 
	 */
	public void login() throws IOException
	{
		String username = prof.getProperty("irc.user.name");
		String password = prof.getProperty("irc.user.password");
		int mode = Integer.parseInt(prof.getProperty("irc.user.mode"));
		String realname = prof.getProperty("irc.real.name");
		String nickname = prof.getProperty("irc.nick.name");
		login(username, password, mode, realname, nickname);
	}

	/**
	 * IRC ネットワークを接続します。
	 * 
	 * @param username ユーザ名
	 * @param password パスワード
	 * @param mode 接続モード
	 * @param realname 本名
	 * @param nickname ニックネーム
	 * @throws IOException 接続エラーが発生した場合
	 */
	public void login(String username, String password, int mode, String realname, String nickname) throws IOException
	{
		// TODO IRCサーバに接続
		System.out.println("connect to " + url);
		URLConnection conn = url.openConnection();
		System.out.println("conn=" + conn);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		// TODO IRCサーバにログイン
		PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "ISO-2022-JP"), true);
		if (password.trim().length() > 0) out.printf(PASS + CRLF, password);
		out.flush();
		out.printf(NICK + CRLF, username);
		out.flush();
		out.printf(USER + CRLF, username, 0, username);
		out.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		while (true)
		{
			String line = in.readLine();
//			IRCMessage msg = new IRCMessage(this, line);
			System.out.println("IRC: " + IRCMessage.decode(line, "ISO-2022-JP"));
			if (line == null) continue;
			if (line.startsWith("PING"))
			{
				String[] ping = line.split(":");
				System.out.println("ping-pong at " + new Date() + "/" + ping[1]);
				out.printf(PONG + CRLF, ping[1]);
				out.flush();
			}
			if (line.startsWith("ERROR")) break;
		}
		in.close();
		out.close();
		System.out.println("bye!");
	}

	/**
	 * 指定されたチャンネルに参加します。
	 * 
	 * @param channelName チャンネル名
	 * @param keyword 秘密のキーワード
	 */
	public void join(String channelName, String keyword)
	{
		try
		{
			login();
			// TODO IRCサーバにjoinコマンドを送信
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * IRC ネットワークを切断します。
	 */
	public void quit()
	{
		// TODO IRCサーバにquitコマンドを送信
	}

	/**
	 * すべての IRC ネットワークを切断します。
	 */
	public static void quitAll()
	{
		for (IRCNetwork network : groups.values())
		{
			network.quit();
		}
	}

	/**
	 * 指定されたチャンネルにメッセージを送信します。
	 * 
	 * @param channelName チャンネル名
	 * @param message メッセージ
	 */
	public void privmsg(String channelName, String message)
	{
		// TODO IRCサーバにprivmsgコマンドを送信
	}
}
