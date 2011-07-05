/*
 * IRCMessage.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRCメッセージを生成します。
 * 
 * @see <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html#c2.3.1">疑似BNFによるメッセージ形式</a>
 */
public class IRCMessage
{
	/** ログ */
	private static final Log log = LogFactory.getLog(IRCMessage.class);

	/** CTCPメッセージの区切り文字 */
	public static final String CTCP = "\u0001";

	/** ニュメリックリプライのパターン */
	protected Pattern NUMERIC_REPLY_PATTERN = Pattern.compile("\\d{3}");

	/** オリジナルテキスト */
	protected String text;

	/** ニックネーム */
	protected String nick;

	/** メッセージの作成時間 */
	protected long when;

	/** プレフィックス */
	protected IRCPrefix prefix;

	/** コマンド */
	protected String command;

	/** 分割前のパラメータ */
	protected String middle;

	/** パラメータ配列 */
	protected String[] params;

	/** トレーラー */
	protected String trail;

	/** 疑似BNFによるメッセージ形式 */
//	protected static final Pattern IRC_PBNF = Pattern.compile("(?::([^ ]+) )?([^ ]+)([^:]+)(?::(.+))?");
	protected static final Pattern IRC_PBNF = Pattern.compile("(?::([^ ]+) )?([^ ]+)(.*)");

	/**
	 * 指定されたテキストからIRCメッセージを構築します。
	 * メッセージの作成時間は現在になります。
	 * 
	 * @param text テキスト
	 * @param nick ニックネーム
	 */
	public IRCMessage(String text, String nick)
	{
		this(text, nick, System.currentTimeMillis());
	}

	/**
	 * 指定されたテキストからIRCメッセージを構築します。
	 * 
	 * @param text テキスト
	 * @param nick ニックネーム
	 * @param when メッセージの作成時間
	 */
	public IRCMessage(String text, String nick, long when)
	{
		this.text = text;
		this.nick = nick;
		this.when = when;
		parse();
	}

	/**
	 * IRCメッセージを解析します。
	 */
	protected void parse()
	{
		Matcher prefix_m = IRC_PBNF.matcher(text);
		if (prefix_m.find())
		{
			prefix = new IRCPrefix(prefix_m.group(1), getWhen());
			command = prefix_m.group(2);
			middle = prefix_m.group(3);
			int pos = middle.indexOf(" :");
			if (pos >= 0)
			{
				trail = middle.substring(pos + 2);
				middle = middle.substring(0, pos);
			}
			middle = middle.trim();
			params = middle.split(" ");
			log.debug(String.format("(%s):%s/%s/:%s", command, prefix, middle, trail));
		}
		else
		{
			throw new IllegalArgumentException("メッセージ形式が不正です。: " + text);
		}
	}

	/**
	 * メッセージの作成時間を返します。
	 * 
	 * @return メッセージの作成時間
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
	 * パラメータの数を返します。
	 * 
	 * @return パラメータの数
	 */
	public int getParamsCount()
	{
		return params.length;
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
	 * 2番目のパラメータを返します。
	 * 
	 * @return 2番目のパラメータ
	 */
	public String getParam2()
	{
		return getParam(2);
	}

	/**
	 * トレーラーを返します。
	 * 
	 * @return トレーラー
	 */
	public String getTrail()
	{
		return trail;
	}

	/**
	 * ニュメリックリプライかどうか判定します。
	 * 
	 * @return ニュメリックリプライの場合は true
	 */
	public boolean isNumericReply()
	{
		return NUMERIC_REPLY_PATTERN.matcher(getCommand()).matches();
	}

	/**
	 * ダイレクトメッセージかどうか判定します。
	 * 
	 * @return ダイレクトメッセージの場合は true
	 */
	public boolean isDirectMessage()
	{
		return getParam0().equals(nick);
	}

	/**
	 * CTCPメッセージかどうか判定します。
	 * 
	 * @return CTCPメッセージの場合は true
	 */
	public boolean isCTCP()
	{
		return trail != null && trail.contains(CTCP);
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
			return trail.substring(1).split(CTCP);
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
