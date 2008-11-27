/*
 * IRCWriter.java
 * IRCKit
 *
 * Created by tarchan on Aug 07, 2006.
 * Copyright (c) 2006 tarchan. All rights reserved.
 */
package com.mac.tarchan.net.irc;

import java.io.Flushable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRC向けに調整した出力バッファです。
 * 改行コードは常にCRLFを出力します。
 * 一定時間のうちにしきい値以上のバイト数を出力しません。
 *
 * @since 1.0
 * @author tarchan
 */
public class IRCWriter extends PrintWriter
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(IRCWriter.class);

//	/** PASS <password> */
//	public static final String PASS = "PASS %s";
//
//	/** NICK <nickname> */
//	public static final String NICK = "NICK %s";
//
//	/** USER <user> <mode> <unused> <realname> */
//	public static final String USER = "USER %s %d * :%s";
//
//	/** QUIT [<quit message>] */
//	public static final String QUIT = "QUIT :%s";
//
//	/** JOIN (<channel> *(","<channel>) [<key> *(","<key>)]) / "0" */
//	public static final String JOIN = "JOIN %s %s";
//
//	/** PART <channel> *(","<channel>) [:<part message>] */
//	public static final String PART = "PART %s :%s";
//
//	/** TOPIC <channel> [:<topic>] */
//	public static final String TOPIC = "TOPIC %s :%s";
//
//	/** INVITE <nickname> <channel> */
//	public static final String INVITE = "INVITE %s %s";
//
//	/** PRIVMSG <msgtarget> <text to be sent> */
//	public static final String PRIVMSG = "PRIVMSG %s :%s";
//
//	/** NOTICE <msgtarget> <text to be sent> */
//	public static final String NOTICE = "NOTICE %s :%s";
//
//	/** PONG <server1> [<server2>] */
//	public static final String PONG = "PONG %s";

	/** コマンドフォーマットリスト */
	private static final String RESOURCE_NAME = "com.mac.tarchan.net.irc.commands";

	/** コマンドキーワードのプレフィックス */
	private static final String COMMAND_PREFIX = "irc.command.";

//	private static final String REPLY_PREFIX = "irc.reply.";

	/** コマンドフォーマット */
	private HashMap<String, String> commands = new HashMap<String, String>();

	/**
	 * IRC行区切り 
	 */
	private static final String CRLF = "\r\n";

	/**
	 * 
	 * @param out
	 */
	public IRCWriter(Writer out)
	{
		super(out, true);

		Locale locale = new Locale("2.11.0", "jp8");
		configure(locale);

		String lineSeparator = System.getProperty("line.separator");
		System.out.println("lineSeparator=" + lineSeparator.length() + "," + lineSeparator);

		// フォーマット登録
//		setFormat("PASS", PASS);
//		setFormat("NICK", NICK);
//		setFormat("USER", USER);
//		setFormat("QUIT", QUIT);
//		setFormat("JOIN", JOIN);
//		setFormat("PART", PART);
//		setFormat("INVITE", INVITE);
//		setFormat("PRIVMSG", PRIVMSG);
//		setFormat("NOTICE", NOTICE);
//		setFormat("PONG", PONG);
	}

	/**
	 * IRCWriter オブジェクトを構築します。
	 * 
	 * @param out 文字出力ストリーム
	 * @return IRCWriter オブジェクト
	 */
	public static IRCWriter getWriter(Writer out)
	{
		System.setProperty("line.separator", CRLF);
		return new IRCWriter(out);
	}

	/**
	 * コマンドのフォーマットを設定します。
	 * 
	 * @param locale ロケール
	 * @return IRCWriter オブジェクト
	 */
	private IRCWriter configure(Locale locale)
	{
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME, locale);
		log.debug("bundle: " + bundle);
//		log.debug("bundle: " + bundle.getKeys());
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			String value = bundle.getString(key);
//			log.debug("bundle: " + key + "=" + value);
			if (key.startsWith(COMMAND_PREFIX))
			{
				key = key.substring(COMMAND_PREFIX.length()).toUpperCase();
				log.debug("put: " + key + "=" + value);
				addFormat(key, value);
			}
		}

		return this;
	}

	/**
	 * コマンドのフォーマットを追加します。
	 * 
	 * @param name コマンド名
	 * @param format コマンドのフォーマット
	 * @return IRCWriter オブジェクト
	 */
	private IRCWriter addFormat(String name, String format)
	{
		commands.put(name.toUpperCase(), format);
		return this;
	}

	/**
	 * コマンドのフォーマットを返します。
	 * 
	 * @param name コマンド名
	 * @return コマンドのフォーマット
	 */
	private String getFormat(String name)
	{
		String command = name.toUpperCase();
		return commands.containsKey(command) ? commands.get(command) : name;
	}

	/**
	 * PASS メッセージを出力します。
	 * 
	 * @param pass パスワード
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter pass(String pass)
	{
		// パスワードが null の場合は何もしない
		if (pass == null) return this;

		return printLine("PASS", pass);
	}

	/**
	 * NICK メッセージを出力します。
	 * 
	 * @param nick ニックネーム
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter nick(String nick)
	{
		return printLine("NICK", nick);
	}

	/**
	 * USER メッセージを出力します。
	 * 
	 * @param user ユーザ名
	 * @param mode ユーザの接続オプション
	 * @param real 本名
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter user(String user, int mode, String real)
	{
		return printLine("USER", user, mode, real);
	}

	/**
	 * QUIT メッセージを出力します。
	 * 
	 * @param message 終了メッセージ
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter quit(String message)
	{
		return printLine("QUIT", message != null ? message : "");
	}

	/**
	 * JOIN メッセージを出力します。
	 * 
	 * @param channel チャンネル名
	 * @param key キーワード
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter join(String channel, String key)
	{
		return printLine("JOIN", channel, key != null ? key : "");
	}

	/**
	 * PART メッセージを出力します。
	 * 
	 * @param channel チャンネル名
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter part(String channel)
	{
		return printLine("PART", channel);
	}

	/**
	 * MODE メッセージを出力します。
	 * 
	 * @param channel
	 * @param params
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter mode(String channel, String... params)
	{
		StringBuilder mode = new StringBuilder();
		for (String p : params)
		{
			mode.append(p);
			mode.append(" ");
		}

		return printLine("MODE", channel, mode.toString());
	}

	/**
	 * メッセージを出力します。
	 * 
	 * @param args
	 * @return このオブジェクトへの参照
	 */
	public IRCWriter send(String... args)
	{
		return printLine("");
	}

	/**
	 * 文字列を出力し、行を終了させます。
	 * 
	 * @param format 書式文字列
	 * @param args 引数
	 * @return このオブジェクトへの参照
	 * @see String#format(String, Object...)
	 * @see #println(String)
	 */
	public IRCWriter printLine(String format, Object... args)
	{
		format = getFormat(format);
		String str = String.format(format, args);
		log.trace(str);
		println(str);
		return this;
	}

//	public PrintWriter format(String format, Object... args)
//	{
//		println();
////		System.out.println("IRCWriter#format()=" + format);
////		format = getFormat(format) + CRLF;
//		format = getFormat(format);
//		String str = String.format(format, args);
////		super.format(format, args);
//		log.trace(str);
//		println(str);
////		System.out.format(format, args);
////		super.print(CRLF);
//		return this;
//	}

//	protected void newLine()
//	{
//		System.out.println("CRLF");
//		super.format(CRLF);
//	}

	/** 出力レギュレーション */
	private Regulatory reg = new Regulatory();

	/**
	 * このストリームをフラッシュします。
	 */
	public void flush()
	{
		// penalty wait
		// 制限時間に達したら一回休み
		if (reg.isLimitTime())
		{
			try
			{
				Thread.sleep(Regulatory.PENALTY_TIME);
			}
			catch (InterruptedException x)
			{
				x.printStackTrace();
			}
		}
		reg.addPenaltyTime();

		// super flush
		super.flush();
	}

//	/** 最大持ち時間 */
//	private static final int LIMIT_TIME = 10 * 1000;
//
//	/** 単位ペナルティ時間 */
//	private static final int PENALTY_TIME = 2 * 1000;
//
//	/** 合計ペナルティ時間 */
//	private long penaltyTime;
//
//	protected boolean isLimitTime()
//	{
//		// get current time
//		final long currentTime = System.currentTimeMillis();
//
//		// message timer >= current time, always
//		if (penaltyTime < currentTime) penaltyTime = currentTime;
//
//		// message timer - current time < limit time, always
//		long limitTime = penaltyTime - currentTime;
//		if (limitTime > LIMIT_TIME) limitTime = LIMIT_TIME;
//		final int limitPercent = 100 * (int)limitTime / LIMIT_TIME;
//
////		System.out.println("penalty: " + (penaltyTime - currentTime) + " ms," + limitPercent + " %");
//
//		return limitPercent >= 100;
//	}
//
//	protected void addPenaltyTime()
//	{
//		penaltyTime += PENALTY_TIME;
//	}
}

/**
 * 出力量を調整します。
 * 
 * @since 1.0
 * @author tarchan
 */
class Regulatory implements Flushable
{
	/** 最大持ち時間 */
	public static final int LIMIT_TIME = 10 * 1000;

	/** 単位ペナルティ時間 */
	public static final int PENALTY_TIME = 2 * 1000;

	/** 合計ペナルティ時間 */
	private long penaltyTime;

	/**
	 * 制限時間かどうかを判定します。
	 * 
	 * @return 制限時間の場合は true
	 */
	public boolean isLimitTime()
	{
		// get current time
		final long currentTime = System.currentTimeMillis();

		// message timer >= current time, always
		if (penaltyTime < currentTime) penaltyTime = currentTime;

		// message timer - current time < limit time, always
		long limitTime = penaltyTime - currentTime;
		if (limitTime > LIMIT_TIME) limitTime = LIMIT_TIME;
		final int limitPercent = 100 * (int)limitTime / LIMIT_TIME;

//		System.out.println("penalty: " + (penaltyTime - currentTime) + " ms," + limitPercent + " %");

		return limitPercent >= 100;
	}

	/**
	 * ペナルティ時間を追加します。
	 */
	public void addPenaltyTime()
	{
		penaltyTime += PENALTY_TIME;
	}

	/**
	 * @see java.io.Flushable#flush()
	 */
	public void flush() throws IOException
	{
		// TODO 自動生成されたメソッド・スタブ
		
	}
}
