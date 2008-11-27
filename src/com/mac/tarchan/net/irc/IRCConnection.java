/*
 * IRCConnection.java
 * IRCKit
 *
 * Created by tarchan on Aug 07, 2006.
 * Copyright (c) 2006 tarchan. All rights reserved.
 */
package com.mac.tarchan.net.irc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRC 特有の機能をサポートする URLConnection です。
 * 詳細は、<a href="http://www.alt-r.com/lib/rfc2812j.html">「"IRC Client Protocol" [IRC-CLIENT]」</a>を参照してください。
 * 
 * <p>
 * IRC URL の構文は、次のとおりです。
 * </p>
 * <pre>
 * irc://[&lt;user&gt;[:&lt;pass&gt;]@]&lt;host&gt;[:&lt;port&gt;][/#&lt;channel&gt;]
 * </pre>
 *
 * @since 1.0
 * @author tarchan
 * @see #connect()
 * @see #getInputStream()
 * @see #getOutputStream()
 */
public class IRCConnection extends URLConnection
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(IRCConnection.class);

	/** ユーザーの本名 */
	public static final String IRC_REAL = "irc.real";

	/** ユーザーのニックネーム */
	public static final String IRC_NICK = "irc.nick";

	/** 接続ユーザー名 */
	public static final String IRC_USER = "irc.user";

	/** 接続パスワード */
	public static final String IRC_PASS = "irc.pass";

	/** 接続モード */
	public static final String IRC_MODE = "irc.mode";

	/** 文字エンコーディング */
	public static final String IRC_ENCODING = "irc.encoding";

	/** 切断時に送信するメッセージ */
	public static final String IRC_QUIT = "irc.quit";

	/** ソケットチャンネル */
	protected SocketChannel socket;

	/** 接続プロパティー */
	protected Properties properties = new Properties();

	/**
	 * 指定した URL に新しい IRCConnection を作成します。
	 * 
	 * @param url URL
	 */
	public IRCConnection(URL url)
	{
		super(url);
		log.debug("init: " + url);

		// URL 接続を入出力に使うことを許可します。
		setDoInput(true);
		setDoOutput(true);

		// ユーザとの対話処理を許可します。
		setAllowUserInteraction(true);

		// デフォルトの文字エンコーディングを設定
		setRequestProperty(IRC_ENCODING, "ISO-2022-JP");

		// デフォルトの本名にバージョン文字列を設定
		setRequestProperty(IRC_REAL, "IRCKit 1.0");

		// デフォルトのニックネームとユーザ名を設定
		String username = System.getProperty("user.name", "anonymous");
		setRequestProperty(IRC_NICK, username);
		setRequestProperty(IRC_USER, username);
	}

	/**
	 * IRCネットワーク接続プロパティーを設定します。
	 * 
	 * <p>
	 * まず、固定の値と URL で指定された値で初期化します。
	 * このプロパティーのセットには、常に次のキーの値が含まれます。
	 * </p>
	 * 
	 * <table>
	 * <tr><th>キー</th><th>対応する値の説明</th></tr>
	 * <tr><td>irc.real</td><td>ユーザーの本名</td></tr>
	 * <tr><td>irc.nick</td><td>ユーザーのニックネーム</td></tr>
	 * <tr><td>irc.user</td><td>ユーザー名</td></tr>
	 * <tr><td>irc.pass</td><td>ユーザーのパスワード</td></tr>
	 * <tr><td>irc.mode</td><td>ユーザーの接続モード</td></tr>
	 * <tr><td>irc.encoding</td><td>文字エンコーディング</td></tr>
	 * <tr><td>irc.quit</td><td>切断時に送信するメッセージ</td></tr>
	 * </table>
	 * 
	 * @param key プロパティーを識別するキーワード
	 * @param value キーワードに関連した値
	 * @see #getRequestProperty(String)
	 * @see #getRequestProperties()
	 */
	@Override
	public void setRequestProperty(String key, String value)
	{
		log.debug("key=" + key + ", value=" + value);
		if (value != null)
		{
			super.setRequestProperty(key, value);
			properties.put(key, value);
		}
	}

	/**
	 * 指定したプロパティーの値を返します。
	 */
	@Override
	public String getRequestProperty(String key)
	{
		return properties.getProperty(key);
	}

	/**
	 * システムプロパティー irc.encoding の値を返します。
	 * 
	 * @return URL が参照するデフォルトエンコーディング。不明の場合は null
	 * @see #IRC_ENCODING
	 * @see System#getProperty(String)
	 */
	@Override
	public String getContentEncoding()
	{
//		log.debug("get content encoding: " + IRC_ENCODING + "=" + getRequestProperty(IRC_ENCODING));
		return getRequestProperty(IRC_ENCODING);
//		return "ISO-2022-JP";
	}

//	@Override
//	public Object getContent()
//	{
//		try
//		{
//			return getInputStream();
//		}
//		catch (IOException x)
//		{
//			return null;
//		}
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public Object getContent(Class[] aclass)
//	{
//		for (int i = 0; i < aclass.length; i++)
//		{
//			if (aclass[i].isAssignableFrom(IRCMessage.class))
//			{
//				try
//				{
//					BufferedReader r = new BufferedReader(reader());
//					String line = r.readLine();
//					IRCMessage msg = new IRCMessage(this, line);
//					return msg;
//				}
//				catch (IOException x)
//				{
//					throw new RuntimeException(x);
//				}
//			}
//			if (aclass[i].isAssignableFrom(IRCNetwork.class))
//			{
//				IRCNetwork net = new IRCNetwork(url.toString());
//				return net;
//			}
//		}
//
//		return null;
//	}

//	/**
//	 * IRCネットワークにログインします。
//	 * 
//	 * @throws IOException 
//	 * @see #IRC_REAL
//	 * @see #IRC_NICK
//	 * @see #IRC_USER
//	 * @see #IRC_PASS
//	 * @see #IRC_MODE
//	 * @see #IRC_ENCODING
//	 * @see #setRequestProperty(String, String)
//	 */
//	protected void login() throws IOException
//	{
//		String real = getRequestProperty(IRC_REAL);
//		String nick = getRequestProperty(IRC_NICK);
//		String user = getRequestProperty(IRC_USER);
//		String pass = getRequestProperty(IRC_PASS);
//		String mode = getRequestProperty(IRC_MODE);
////		System.out.println("login=" + real + "," + nick + "," + user + "," + pass + "," + mode);
//
//		// 未定義のパラメータを補完する
////		if (pass == null) pass = "";
//		if (user == null || user.length() == 0) user = nick;
//		if (mode == null || mode.length() == 0) mode = "0";
//
//		int iMode = Integer.parseInt(mode);
//
//		log.info("irc.real: " + real);
//		log.info("irc.nick: " + nick);
//		log.info("irc.user: " + user);
//		log.info("irc.pass: " + pass);
//		log.info("irc.mode: " + mode);
//
////		IRCWriter out = writer();
////		System.out.println("out=" + out);
////		out.pass(pass);
////		out.nick(nick);
////		out.user(user, iMode, real);
////		out.flush();
//	}
//
//	/**
//	 * IRCネットワークをログアウトします。
//	 * 
//	 * @throws IOException 
//	 * @see #IRC_QUIT
//	 * @see #setRequestProperty(String, String)
//	 */
//	protected void logout() throws IOException
//	{
////		String quit = getRequestProperty(IRC_QUIT);
////		IRCWriter out = getWriter();
////		out.format(IRCWriter.QUIT, quit);
//	}

	/**
	 * IRCネットワークに接続します。
	 * @see URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException
	{
		if (connected) return;

		ProxySelector selector = ProxySelector.getDefault();
		try
		{
			List<Proxy> proxies = selector.select(url.toURI());
			for (Proxy proxy : proxies)
			{
				log.debug("proxy=" + proxy + "," + proxy.address());
			}
		}
		catch (URISyntaxException e)
		{
			log.error("URI Syntax Error", e);
			throw new IllegalArgumentException("URI Syntax Error", e);
		}

		log.debug("connect: " + url.getHost() + ":" + url.getPort());

//		System.out.println("url=" + url);
		InetSocketAddress remote = new InetSocketAddress(url.getHost(), url.getPort());
//		System.out.println("remote=" + remote);
		socket = SocketChannel.open(remote);
//		System.out.println("socket=" + socket);

//		login();

		connected = true;
	}

//	/**
//	 * IRCネットワークへの接続を無効にします。
//	 * 
//	 * @see #logout()
//	 */
//	public synchronized void disconnect()
//	{
//		try
//		{
//			logout();
//			socket.close();
//		}
//		catch (IOException x)
//		{
//			throw new RuntimeException(x);
//		}
//		finally
//		{
//			socket = null;
//		}
//	}

	/**
	 * 入力ストリームを返します。
	 */
	@Override
	public InputStream getInputStream() throws IOException
	{
		return connected ? Channels.newInputStream(socket) : null;
	}

	/**
	 * 出力ストリームを返します。
	 */
	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return connected ? Channels.newOutputStream(socket) : null;
	}

//	/**
//	 * 入力ストリームを返します。
//	 * 
//	 * @return 入力ストリーム。接続していない場合は null
//	 * @throws IOException 入力ストリームの作成中に入出力エラーが発生した場合
//	 * @see #getContentEncoding()
//	 */
//	public synchronized BufferedReader reader() throws IOException
//	{
//		return reader(getContentEncoding());
//	}
//
//	/**
//	 * 入力ストリームを返します。
//	 * 
//	 * @param encoding 文字エンコーディング
//	 * @return 入力ストリーム。接続していない場合は null
//	 * @throws IOException 入力ストリームの作成中に入出力エラーが発生した場合
//	 */
//	public synchronized BufferedReader reader(String encoding) throws IOException
//	{
//		log.debug("get reader: " + socket + ", " + encoding);
//		return new BufferedReader(Channels.newReader(socket, encoding));
//	}
//
//	/**
//	 * デフォルトの文字エンコーディングの出力ストリームを返します。
//	 * 
//	 * @return 出力ストリーム。接続していない場合は null
//	 * @throws IOException 出力ストリームの作成中に入出力エラーが発生した場合
//	 * @see #getContentEncoding()
//	 */
//	public synchronized IRCWriter writer() throws IOException
//	{
//		return writer(getContentEncoding());
//	}
//
//	/**
//	 * 指定した文字エンコーディングの出力ストリームを返します。
//	 * 
//	 * @param encoding 文字エンコーディング
//	 * @return 出力ストリーム。接続していない場合は null
//	 * @throws IOException 出力ストリームの作成中に入出力エラーが発生した場合
//	 */
//	public synchronized IRCWriter writer(String encoding) throws IOException
//	{
//		log.debug("get writer: " + socket + ", " + encoding);
////		return new IRCWriter(new BufferedWriter(Channels.newWriter(socket, encoding)));
//		return IRCWriter.getWriter(new BufferedWriter(Channels.newWriter(socket, encoding)));
//	}
}
