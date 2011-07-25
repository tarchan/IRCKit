/*
 * IRCMessage.java
 * IRCKit
 *
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.util.ArrayList;
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
	protected Prefix prefix;

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
			prefix = new Prefix(this, prefix_m.group(1));
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
	public Prefix getPrefix()
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
	 * ニュメリックリプライの番号を返します。
	 * 
	 * @return ニュメリックリプライの番号
	 */
	public int getNumber()
	{
		return Integer.valueOf(command);
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
		if (!isCTCP()) return null;

		return trail.substring(1).split(CTCP);
	}

	/**
	 * CTCPメッセージの配列を返します。
	 * 
	 * @return CTCPメッセージの配列
	 * @see CTCP
	 */
	public CTCP[] toCTCPArray()
	{
		if (!isCTCP()) return null;

		ArrayList<CTCP> list = new ArrayList<CTCP>();
		for (String text : splitCTCP())
		{
			list.add(new CTCP(this, text));
		}
		return list.toArray(new CTCP[]{});
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

	/**
	 * IRCメッセージのプレフィックスを、サーバ名、ニックネーム、ユーザ名、ホスト名に分割する機能を提供します。
	 */
	public static class Prefix
	{
		/** プレフィックス形式 */
		protected static Pattern prefixPattern = Pattern.compile("([^!]+)(!.+)?(@.+)?");

		/** 親のIRCメッセージ */
		protected IRCMessage message;

		/** プレフィックス */
		protected String prefix;

		/** メッセージ作成時間 */
		protected long when;

		/** ニックネーム */
		protected String nick;

		/** ユーザ名 */
		protected String user;

		/** ホスト名 */
		protected String host;

		/**
		 * プレフィックスを、サーバ名、ニックネーム、ユーザ名、ホスト名に分割します。
		 * 
		 * @param prefix プレフィックス
		 */
		@Deprecated
		public Prefix(String prefix)
		{
			this(null, prefix);
			this.when = System.currentTimeMillis();
		}

		/**
		 * プレフィックスを、サーバ名、ニックネーム、ユーザ名、ホスト名に分割します。
		 * 
		 * @param parent IRCメッセージ
		 * @param prefix プレフィックス
		 */
		public Prefix(IRCMessage parent, String prefix)
		{
			if (parent != null)
			{
				this.message = parent;
				this.when = message.getWhen();
			}
			if (prefix != null)
			{
				this.prefix = prefix;
				Matcher m = prefixPattern.matcher(prefix);
				if (m.find())
				{
					nick = m.group(1);
					user = m.group(2);
					host = m.group(3);
				}
				else
				{
					throw new IllegalArgumentException("プレフィックスが不正です。: " + prefix);
				}
			}
		}

		/**
		 * 親のIRCメッセージを返します。
		 * 
		 * @return 親のIRCメッセージ
		 */
		public IRCMessage getMessage()
		{
			return message;
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
		 * メッセージの作成時間を返します。
		 * 
		 * @return メッセージの作成時間
		 */
		public long getWhen()
		{
			return when;
		}

		/**
		 * サーバ名またはニックネームを返します。
		 * 
		 * @return サーバ名またはニックネーム
		 */
		public String getNick()
		{
			return nick;
		}

		/**
		 * ユーザ名を返します。
		 * 
		 * @return ユーザ名
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

		@Override
		public String toString()
		{
			return prefix;
		}
	}

	/**
	 * CTCPメッセージ
	 */
	public static class CTCP
	{
		/** DCCメッセージのプレフィックス */
		public static final String DCC = "DCC";

		/** CTCP PING */
		public static final String PING = "PING";

		/** CTCP TIME */
		public static final String TIME = "TIME";

		/** CTCP VERSION */
		public static final String VERSION = "VERSION";

		/** CTCP USERINFO */
		public static final String USERINFO = "USERINFO";

		/** CTCP CLIENTINFO */
		public static final String CLIENTINFO = "CLIENTINFO";

		/** CTCP ACTION */
		public static final String ACTION = "ACTION";

		/** CTCP FINGER */
		public static final String FINGER = "FINGER";

		/** CTCP DCC SEND */
		public static final String DCC_SEND = "DCC SEND";

		/** CTCP DCC CHAT */
		public static final String DCC_CHAT = "DCC CHAT";

		/** IRCメッセージ */
		protected IRCMessage message;

		/** CTCPメッセージ */
		protected String text;

		/** CTCPコマンド */
		protected String command;

		/** CTCPパラメータ */
		protected String param;

		/**
		 * CTCPメッセージを作成します。
		 * 
		 * @param message IRCメッセージ
		 * @param text CTCPメッセージ
		 */
		public CTCP(IRCMessage message, String text)
		{
			try
			{
				this.message = message;
				this.text = text;
				String[] span = text.split(" ", 2);
				command = span[0].toUpperCase();
				param = span[1];
				if (command.equals(DCC))
				{
					span = param.split(" ", 2);
					command = command + " " + span[0].toUpperCase();
					param = span[1];
				}
			}
			catch (Exception x)
			{
				throw new IllegalArgumentException("CTCPメッセージが不正です。: " + text, x);
			}
		}

		/**
		 * 親のIRCメッセージを返します。
		 * 
		 * @return 親のIRCメッセージ
		 */
		public IRCMessage getMessage()
		{
			return message;
		}

		/**
		 * CTCPコマンドを返します。
		 * 
		 * @return CTCPコマンド
		 */
		public String getCommand()
		{
			return command;
		}

		/**
		 * CTCPパラメータを返します。
		 * 
		 * @return CTCPパラメータ
		 */
		public String getParam()
		{
			return param;
		}

		/**
		 * CTCPメッセージの文字列表現を返します。
		 */
		@Override
		public String toString()
		{
			return text;
		}
	}
}
