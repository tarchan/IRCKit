/*
 *  Copyright (c) 2009 tarchan. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY TARCHAN ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 *  EVENT SHALL TARCHAN OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are
 *  those of the authors and should not be interpreted as representing official
 *  policies, either expressed or implied, of tarchan.
 */
package com.mac.tarchan.irc.client;

import java.io.Serializable;
import java.util.Locale;

/**
 * IRCの名前空間を表します。
 * ニックネーム、チャンネル名、サーバー名を判定します。
 * 
 * @author tarchan
 */
public class IRCName implements Serializable
{
	private static final long serialVersionUID = -5303819010167276281L;

	/** アドレス区切り */
	protected static final String LONGNAME_DELIMITER = "[!@]";

	/** ロケール */
	protected Locale locale;

	/** 名前 */
	protected String name;

	/** ニックネーム */
	protected String nick;

	/** ユーザー名 */
	protected String user;

	/** ホスト名 */
	protected String host;

	/**
	 * 指定されたロケールの名前を構築します。
	 * 
	 * @param name 名前
	 * @param locale ロケール
	 */
	public IRCName(String name, Locale locale)
	{
		this.name = name;
		this.locale = locale;
		parse();
	}

	/**
	 * デフォルトロケールの名前を構築します。
	 * 
	 * @param name 名前
	 */
	public IRCName(String name)
	{
		this(name, Locale.getDefault());
	}

	/**
	 * チャンネル名かどうか判定します。
	 * 
	 * @return チャンネル名の場合は true
	 */
	public boolean isChannel()
	{
		return name.matches("[&#%]\\S+");
	}

	/**
	 * ロングネームを解析します。
	 */
	private void parse()
	{
		String[] token = name.split(LONGNAME_DELIMITER);
		if (token.length == 1)
		{
			nick = token[0];
			host = token[0];
		}
		else
		{
			nick = token[0];
			user = token[1];
			host = token[2];
		}
	}

	/**
	 * ニックネームを返します。
	 * 
	 * @return ニックネーム
	 */
	public String getNick()
	{
		return nick;
	}

	/**
	 * ユーザー名を返します。
	 * 
	 * @return ユーザー名
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * ホスト名を返します。
	 * 
	 * @return ホスト名
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * ローカル名を返します。
	 * 
	 * @return ローカル名
	 */
	public String getLocalName()
	{
		return locale.getCountry();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return name;
	}
}
