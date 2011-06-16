/*
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IRCName
 */
public class IRCName
{
	static Pattern namePattern = Pattern.compile("([^!]+)(!.*)?");

	public static String getSimpleName(String name)
	{
		Matcher m = namePattern.matcher(name);
		if (m.find())
		{
			return m.group(1);
		}
		else
		{
			throw new IllegalArgumentException("IRCネームが不正です。: " + name);
		}
	}
}
