package com.mac.tarchan.irc;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * IRCMessage
 * 
 * @see <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html#c2.3.1">RFC1459: Internet Relay Chat Protocol (IRC)</a>
 */
public class IRCMessage
{
	String text;

	String encoding;

	String prefix;

	String command;

	String middle;

	String trailing;

	String[] params;

	/**
	 * IRCMessage
	 * 
	 * @param text テキスト
	 */
	public IRCMessage(String text)
	{
		this.text = text;
		parse();
	}

	public IRCMessage(String text, String encoding) throws UnsupportedEncodingException
	{
		this.text = new String(text.getBytes(), encoding);
		this.encoding = encoding;
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

	public String getPrefix()
	{
		return prefix;
	}

	public String getCommand()
	{
		return command;
	}

	public String[] getParams()
	{
		return params;
	}

	public String getParam(int index)
	{
		if (index < 0 || index >= params.length) throw new ArrayIndexOutOfBoundsException("パラメータが見つかりません。: " + index);
		return params[index];
	}

	public String getTrailing()
	{
		return trailing;
	}

	/**
	 * データを返します。
	 * 
	 * @return データ
	 */
	public byte[] getData()
	{
		return text.getBytes();
	}

	@Override
	public String toString()
	{
		return text;
	}
}
