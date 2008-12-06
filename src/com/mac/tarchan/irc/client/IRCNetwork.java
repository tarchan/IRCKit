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

//	/** プロパティー */
//	private Properties properties;

	/** ロケール */
	private Locale locale = new Locale("jp");

	/** 入力 */
	public BufferedReader in0;

	/** 出力 */
	public IRCWriter out0;

	/**
	 * URL コネクションハンドラに com.mac.tarchan.net.irc.Hnadler を設定します。
	 * irc スキーマを実装します。
	 */
	static
	{
		System.setProperty("java.protocol.handler.pkgs", "com.mac.tarchan.net");
	}

//	/**
//	 * 
//	 */
//	private IRCNetwork()
//	{
//		Properties defaults = new Properties();
//		defaults.setProperty("irc.encoding", "ISO-2022-JP");
//		defaults.setProperty("irc.user", System.getProperty("user.name"));
//
//		properties = new Properties(defaults);
//		log.debug("defaults=" + defaults);
//		log.debug("properties=" + properties);
//	}
//
//	/**
//	 * IRCNetwork オブジェクトを構築します。
//	 * 
//	 * @param name IRC ネットワークの名前
//	 */
//	public IRCNetwork(String name)
//	{
//		this();
//		this.name = name;
//	}

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
	 * @deprecated
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
	 * @deprecated
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
		in0 = new BufferedReader(new InputStreamReader(c.getInputStream(), enc));
		out0 = IRCWriter.getWriter(new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), enc)));

		return this;
	}

	/**
	 * IRC ネットワークに接続します。
	 * 
	 * @param config 接続プロパティー
	 * @return このオブジェクトへの参照
	 * @throws IOException 接続時にエラーが発生した場合
	 * @deprecated
	 */
	public IRCNetwork connect(Properties config) throws IOException
	{
		return this;
	}

	/** エンコーディング */
	protected String encoding;

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
		this.encoding = encoding;
		return this;
	}

	/**
	 * Reader オブジェクトを返します。
	 * 
	 * @return Reader オブジェクト
	 * @deprecated
	 */
	public Reader reader()
	{
		return in0;
	}

	/**
	 * PrintWriter オブジェクトを返します。
	 * 
	 * @return PrintWriter オブジェクト
	 * @deprecated
	 */
	public PrintWriter writer()
	{
		return out0;
	}

	/**
	 * 1行読み込みます。
	 * 
	 * @return 1行の文字列
	 * @deprecated
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
	 * @deprecated
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
	 * @deprecated
	 */
	public IRCNetwork printLine(String format, Object... args)
	{
		out0.printf(format, args);
		out0.println();
		return this;
	}

	/**
	 * 
	 * @param format
	 * @param args
	 * @return このオブジェクトへの参照
	 * @deprecated
	 */
	public IRCNetwork printBreak(String format, Object... args)
	{
		out0.printf(format, args);
		out0.printf("\r");
		return this;
	}

	/**
	 * 
	 * @param ch
	 * @param msg
	 * @return このオブジェクトへの参照
	 * @deprecated
	 */
	public IRCNetwork printMessage(String ch, String msg)
	{
		out0.printf("PRIVMSG %s %s", ch, msg);
		out0.println();
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
		out0.quit(message);
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

	/** JOIN (<channel> *(","<channel>) [<key> *(","<key>)]) / "0" */
	public static final String JOIN = "JOIN %s %s";

	/** PRIVMSG <msgtarget> <text to be sent> */
	public static final String PRIVMSG = "PRIVMSG %s :%s";

	/** ネットワークグループ */
	protected static final HashMap<String, IRCNetwork> groups = new HashMap<String, IRCNetwork>();

	/** クライアント */
	protected IRCClient client;

	/** URL */
	protected URL url;

	/**
	 * 指定された IRC ネットワークを構築します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param address サーバアドレス
	 * @throws IllegalArgumentException 指定された文字列が RFC 2396 に違反する場合
	 * @throws MalformedURLException URL のプロトコルハンドラが見つからなかった場合、または URL の構築中にその他の何らかのエラーが発生した場合
	 */
	public IRCNetwork(String groupName, String address) throws MalformedURLException
	{
		this.name = groupName;
		URI uri = URI.create(address);
		url = uri.toURL();
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
		def.setProperty("irc.encoding", "UTF-8");

		return new Properties(def);
	}

	/**
	 * IRCクライアントを設定します。
	 * 
	 * @param client IRCクライアント
	 */
	public void setClient(IRCClient client)
	{
		this.client = client;
	}

	/**
	 * 指定された IRC ネットワークを登録します。
	 * 
	 * @param groupName IRC ネットワーク名
	 * @param address サーバアドレス
	 * @throws IllegalArgumentException 指定された文字列が RFC 2396 に違反する場合
	 * @throws MalformedURLException URL のプロトコルハンドラが見つからなかった場合、または URL の構築中にその他の何らかのエラーが発生した場合
	 */
	public static void registerNetwork(String groupName, String address) throws MalformedURLException
	{
		IRCNetwork network = new IRCNetwork(groupName, address);
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

	/** 入力ストリーム */
	protected BufferedReader in;

	/** 出力ストリーム */
	protected PrintWriter out;

	/**
	 * IRC ネットワークにログインします。
	 * 
	 * @param username ユーザ名
	 * @param password パスワード
	 * @param mode 接続モード
	 * @param realname 本名
	 * @param nickname ニックネーム
	 * @throws IOException 接続エラーが発生した場合
	 */
	protected void login(String username, String password, int mode, String realname, String nickname) throws IOException
	{
		// TODO IRCサーバに接続
		System.out.println("connect to " + url);
		final URLConnection conn = url.openConnection();
		System.out.println("conn=" + conn);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		// TODO IRCサーバにログイン
		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), encoding), true);
		if (password.trim().length() > 0) out.printf(PASS + CRLF, password);
//		out.flush();
		out.printf(NICK + CRLF, nickname);
//		out.flush();
		out.printf(USER + CRLF, username, mode, realname);
//		out.flush();
//		new Thread(new Runnable()
//		{
//			public void run()
//			{
//				try
//				{
//					while (true)
//					{
//						String line = in.readLine();
//						System.out.println("IRC: " + line);
//						if (line == null) continue;
//						if (line.startsWith("PING"))
//						{
//							String[] ping = line.split(":");
//							System.out.println("ping-pong at " + new Date() + "/" + ping[1]);
//							out.printf(PONG + CRLF, ping[1]);
//							out.flush();
//						}
//
//						IRCMessage msg = new IRCMessage(this, line);
//						client.reply(msg);
//
//						if (line.startsWith("ERROR")) break;
//					}
//					in.close();
//					out.close();
//					System.out.println("bye!");
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}).start();
	}

	/**
	 * IRC ネットワークにログインします。
	 * 
	 * @param prof ユーザプロパティー
	 */
	public void login(Properties prof)
	{
		try
		{
			String username = prof.getProperty("irc.user.name");
			String password = prof.getProperty("irc.user.password");
			int mode = Integer.parseInt(prof.getProperty("irc.user.mode"));
			String realname = prof.getProperty("irc.real.name");
			String nickname = prof.getProperty("irc.nick.name");
			String encoding = prof.getProperty("irc.encoding");
			setEncoding(encoding);

			login(username, password, mode, realname, nickname);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 1行ゲットします。
	 * 
	 * @return 文字列
	 * @throws IOException 入力エラーが発生した場合
	 */
	public String get() throws IOException
	{
		return in.readLine();
	}

	/**
	 * 指定されたコマンドをIRCネットワークに送信します。
	 * 
	 * @param command コマンド
	 */
	public void put(String command)
	{
		System.out.println("put: " + command);
		out.printf("%s" + CRLF, command);
	}

	/**
	 * 指定されたチャンネルに参加します。
	 * 
	 * @param channelName チャンネル名
	 * @param keyword 秘密のキーワード
	 */
	public void join(String channelName, String keyword)
	{
		String cmd = String.format(JOIN, channelName, keyword);
		put(cmd);
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
		String cmd = String.format(PRIVMSG, channelName, message);
		put(cmd);
	}
}
