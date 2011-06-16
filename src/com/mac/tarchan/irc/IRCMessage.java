package com.mac.tarchan.irc;


/**
 * IRCMessage
 */
public class IRCMessage
{
	String text;

	/**
	 * IRCMessage
	 * 
	 * @param text テキスト
	 */
	public IRCMessage(String text)
	{
		this.text = text;
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
