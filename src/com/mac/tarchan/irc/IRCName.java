/*
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IRCメッセージのプレフィックスを、サーバ名、ニックネーム、ユーザ名、ホスト名に分割する機能を提供します。
 */
public class IRCName
{
	/** プレフィックス形式 */
	protected static Pattern prefixPattern = Pattern.compile("([^!]+)(!.+)?(@.+)?");

	/**
	 * プレフィックスから、サーバ名またはニックネームのみを返します。
	 * 
	 * @param prefix プレフィックス
	 * @return サーバ名またはニックネーム
	 */
	public static String getSimpleName(String prefix)
	{
		Matcher m = prefixPattern.matcher(prefix);
		if (m.find())
		{
			return m.group(1);
		}
		else
		{
			throw new IllegalArgumentException("プレフィックスが不正です。: " + prefix);
		}
	}
}
