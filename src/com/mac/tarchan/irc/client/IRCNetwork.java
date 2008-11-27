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
import java.net.URL;
import java.net.URLConnection;
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

	/**
	 * 
	 * @param channel
	 */
	public void join(String channel)
	{
		join(channel, "");
	}

	/**
	 * 
	 * @param channel
	 * @param key
	 */
	public void join(String channel, String key)
	{
		out.join(channel, key);
	}

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
}
