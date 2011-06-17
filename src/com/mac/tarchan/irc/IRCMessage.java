package com.mac.tarchan.irc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IRCメッセージを生成します。
 * 
 * @see <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html#c2.3.1">RFC1459: Internet Relay Chat Protocol (IRC)</a>
 */
public class IRCMessage
{
	String text;

	String prefix;

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
			prefix = prefix_m.group(1);
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
			System.out.printf("(%s):%s/%s/:%s%n", command, prefix, middle, trailing);
		}
		else
		{
			throw new IllegalArgumentException("メッセージ形式が不明です。: " + text);
		}
	}

	/**
	 * プレフィックスを返します。
	 * 
	 * @return プレフィックス
	 */
	public String getPrefix()
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
	 * トレーラーを返します。
	 * 
	 * @return トレーラー
	 */
	public String getTrailing()
	{
		return trailing;
	}

	@Override
	public String toString()
	{
		return text;
	}
}
