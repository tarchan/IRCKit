/*
 * IRCMessage.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRCメッセージを生成します。
 * 
 * @see <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html#c2.3.1">RFC1459: Internet Relay Chat Protocol (IRC)</a>
 */
public class IRCMessage
{
	/** ログ */
	private static final Log log = LogFactory.getLog(IRCMessage.class);

	/** CTCPメッセージの区切り文字 */
	public static final String CTCP = "\u0001";

	String text;

	long when;

	IRCPrefix prefix;

	String command;

	String middle;

	String trailing;

	String[] params;

	/**
	 * IRCMessage を構築します。
	 * 
	 * @param text テキスト
	 * @throws IllegalArgumentException メッセージ形式が不明の場合
	 */
	public IRCMessage(String text)
	{
		this.text = text;
		this.when = System.currentTimeMillis();
		parse();
	}

	/** 疑似BNFによるメッセージ形式 */
//	static final Pattern IRC_PBNF = Pattern.compile("(?::([^ ]+) )?([^ ]+)([^:]+)(?::(.+))?");
	static final Pattern IRC_PBNF = Pattern.compile("(?::([^ ]+) )?([^ ]+)(.*)");

	void parse()
	{
		Matcher prefix_m = IRC_PBNF.matcher(text);
		if (prefix_m.find())
		{
			prefix = new IRCPrefix(prefix_m.group(1));
			command = prefix_m.group(2);
			middle = prefix_m.group(3);
			int pos = middle.indexOf(" :");
			if (pos >= 0)
			{
				trailing = middle.substring(pos + 2);
				middle = middle.substring(0, pos);
			}
			middle = middle.trim();
			params = middle.split(" ");
			log.debug(String.format("(%s):%s/%s/:%s", command, prefix, middle, trailing));
		}
		else
		{
			throw new IllegalArgumentException("メッセージ形式が不明です。: " + text);
		}
	}

	/**
	 * メッセージを作成した時間を返します。
	 * 
	 * @return メッセージを作成した時間
	 */
	public long getWhen()
	{
		return when;
	}

	/**
	 * プレフィックスを返します。
	 * 
	 * @return プレフィックス
	 */
	public IRCPrefix getPrefix()
	{
		return prefix;
	}

	/**
	 * コマンドを返します。
	 * 
	 * @return コマンド
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * パラメータリストを返します。
	 * 
	 * @return パラメータリスト
	 */
	public String[] getParams()
	{
		return params;
	}

	/**
	 * 指定されたインデックスのパラメータを返します。
	 * 
	 * @param index インデックス
	 * @return 指定されたインデックスのパラメータ
	 */
	public String getParam(int index)
	{
		if (index < 0 || index >= params.length) throw new ArrayIndexOutOfBoundsException("パラメータが見つかりません。: " + index);
		return params[index];
	}

	/**
	 * 0番目のパラメータを返します。
	 * 
	 * @return 0番目のパラメータ
	 */
	public String getParam0()
	{
		return getParam(0);
	}

	/**
	 * 1番目のパラメータを返します。
	 * 
	 * @return 1番目のパラメータ
	 */
	public String getParam1()
	{
		return getParam(1);
	}

	/**
	 * トレーラーを返します。
	 * 
	 * @return トレーラー
	 */
	public String getTrailing()
	{
		return trailing;
	}

	/**
	 * CTCPメッセージかどうか判定します。
	 * 
	 * @return CTCPメッセージの場合は true
	 */
	public boolean isCTCP()
	{
		return trailing != null && trailing.contains(CTCP);
	}

	/**
	 * CTCPメッセージを区切り文字で分割します。
	 * 
	 * @return CTCPメッセージの配列
	 * @see #CTCP
	 */
	public String[] splitCTCP()
	{
		if (isCTCP())
		{
			return trailing.substring(1).split(CTCP);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 指定されたテキストをCTCPメッセージに変換します。
	 * 
	 * @param text テキスト
	 * @return CTCPメッセージ
	 * @see #CTCP
	 */
	public static String wrapCTCP(String text)
	{
		return String.format("%1$s%2$s%1$s", CTCP, text);
	}

	@Override
	public String toString()
	{
		return text;
	}
}
