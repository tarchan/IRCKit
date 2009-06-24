/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * IRCメッセージを構文解析します。
 * 疑似BNFによるメッセージ形式は次のとおりです。
 * <p
 * <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html#c2.3.1">RFC1459: Internet Relay Chat Protocol (IRC)</a>
 *</p>
 *
 * @author tarchan
 */
public class IRCMessage extends EventObject
{
	/** アドレス区切り */
	public static final String ADDRESS_DELIMITER = "[!@]";

	/** CTCPメッセージ */
	private static final String CTCP_DELIMITER = "\0x1";

	/** ボールド表示 */
	private static final String BOLD_DELIMITER = "\0x2";

	/** カラー表示*/
	private static final String COLOR_DELIMITER = "\0x3";

	/** 反転表示または斜体表示 */
	private static final String REVERSE_DELIMITER = "\0x16";

	/** アンダーライン表示 */
	private static final String UNDERLINE_DELIMITER = "\0x1f";

	/** ニューメリックリプライのパターン */
	private static final Pattern NUMERIC_REPLY_PATTERN = Pattern.compile("[0-9][0-9][0-9]");

	/** 接続が登録されて、すべてのIRCネットワークに認知されたということを表します。 */
	public static final int RPL_WELCOME = 001;

	/** サービスがうまく登録されると、サーバからサービスに送られます。 */
	public static final int RPL_YOURESERVICE = 383;

	/** 入力メッセージ */
	private String msg;

	/** 入力時刻 */
	private long when;

	// メッセージ解析結果
	/** プレフィックス */
	private String _prefix;

	/** コマンド */
	private String _command;

	/** パラメータ配列 */
	private String _middle[];

	/** トレーラ */
	private String _trail;

	/** サーバ名(prefix) */
	private String _server;

	/** ニックネーム(prefix) */
	private String _nick;

	/** ユーザ名(prefix) */
	private String _user;

	/** ホスト名 (prefix) */
	private String _host;

	/** 文字エンコーディング */
	private String encoding;

	/**
	 * IRCメッセージを解析して IRCMessage を作成します。
	 * 
	 * @param source メッセージのソース
	 * @param message メッセージ
	 */
	public IRCMessage(Object source, String message)
	{
		this(source, message, System.currentTimeMillis());
	}

	/**
	 * IRCメッセージを解析して IRCMessage を作成します。
	 * 
	 * @param source メッセージのソース
	 * @param message メッセージ
	 * @param when メッセージを受け取った時間
	 */
	public IRCMessage(Object source, String message, long when)
	{
		// オリジナルのパラメータを保存する
		super(source);
		setMessage(message);
		setWhen(when);

		// デバッグ出力
//		System.out.println(this);
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message 入力メッセージ
	 * @throws NullPointerException 入力メッセージが null の場合
	 * @throws IllegalArgumentException 入力メッセージが解析できない場合
	 */
	protected void setMessage(String message) throws NullPointerException, IllegalArgumentException
	{
		if (message == null) throw new NullPointerException("message");

		// 疑似BNFによるメッセージを構文解析する
		parse(message);
	}

	/**
	 * 入力時刻を設定します。
	 * 
	 * @param when 入力時刻
	 */
	protected void setWhen(long when)
	{
		this.when = when;
	}

	/**
	 * 疑似BNFによるメッセージを構文解析します。
	 *
	 * <pre>
	 * message = [':'<prefix> <SPACE>] <command> <params> <crlf>
	 * prefix = <servername> | <nick>['!'<user>]['@'<host>]
	 * params = <SPACE>[':'<trailing> | <middle> <params>]
	 * </pre>
	 *
	 * @param message メッセージ
	 */
	private void parse(String message)
	{
		this.msg = message;

		// message = [':'<prefix> <SPACE>] <command> <params> <crlf>
		String params = parseMessage(message);

		// params = <SPACE>[':'<trailing> | <middle> <params>]
		parseParams(params);
	}

	/**
	 * <message> を <prefix>、<command>、<params> に分解します。
	 *
	 * <pre>
	 * message = [':'<prefix> <SPACE>] <command> <params> <crlf>
	 * </pre>
	 * 
	 * @param message メッセージ
	 * @return <params>
	 */
	private String parseMessage(String message)
	{
		// ':' からはじまる場合は <prefix> を設定します。
		// <prefix>
		if (message.startsWith(":"))
		{
			String[] prefix = message.split(" ", 2);
			parsePrefix(prefix[0].substring(1));
			message = prefix[1];
		}

		// <command> と <params> を分解します。
		String[] token = message.split(" ", 2);
		// <command>
		_command = token[0];

		// ERRORコマンド対応
		if (_command.equals("ERROR:"))
		{
			_command = "ERROR";
			token[1] = ":" + token[1];
		}

		// <params>
		return token[1];
	}

	/**
	 * <prefix> を <servername> または <nick> に分解します。
	 *
	 * <pre>
	 * prefix = <servername> | <nick>['!'<user>]['@'<host>]
	 * </pre>
	 *
	 * @param prefix メッセージ
	 */
	private void parsePrefix(String prefix)
	{
		_prefix = prefix;
		String[] token = prefix.split(ADDRESS_DELIMITER);
		if (token.length == 1)
		{
			_server = token[0];
			_nick = token[0];
		}
		else
		{
			_nick = token[0];
			_user = token[1];
			_host = token[2];
		}

//		_prefix = prefix;
//		_server = prefix;
//		_nick = prefix;
//
//		String[] atmark = prefix.split("@", 2);
////		System.out.println("atmark=" + prefix + "," + atmark.length);
//		if (atmark.length >= 2)
//		{
//			_server = null;
//			prefix = atmark[0];
//			_host = atmark[1];
//		}
//
//		String[] exmark = prefix.split("!", 2);
////		System.out.println("exmark=" + prefix + "," + exmark.length);
//		if (atmark.length >= 2)
//		{
//			_server = null;
//			_nick = exmark[0];
//			_user = exmark[1];
//		}
	}

	/**
	 * <params> を <middle> と <trailing> に分解します。
	 *
	 * <pre>
	 * params = <SPACE>[':'<trailing> | <middle> <params>]
	 * </pre>
	 *
	 * @param params メッセージ
	 */
	private void parseParams(String params)
	{
		ArrayList<String> array = new ArrayList<String>();
		while (true)
		{
			// ':' からはじまる場合は <trailing> を設定して終了します。
			if (params.startsWith(":"))
			{
				_trail = params.substring(1);
//				System.out.println(array.size() + "=" + _trailing);
				break;
			}

			// そうでない場合は2分割して処理を続行します。
			String[] token = params.split(" ", 2);
//			System.out.println("div=" + token.length + "," + params);

			// 空文字でない場合はパラメータに追加します。
			if (token[0].length() > 0) array.add(token[0]);
//			System.out.println(array.size() + "=" + token[0]);

			// パラメータがなくなった場合は終了します。
			if (token.length <= 1) break;

			// 次に繰り越し
			params = token[1];
		}

		// パラメータリストを配列に設定します。
//		System.out.println("params=" + array);
		_middle = new String[array.size()];
		array.toArray(_middle);
	}

	/**
	 * 入力メッセージを返します。
	 * 
	 * @return 入力メッセージ
	 */
	public String getMessage()
	{
		return decode(msg, encoding);
	}

	/**
	 * 入力時刻を返します。
	 * 
	 * @return 入力時刻
	 */
	public long getWhen()
	{
		return when;
	}

	/**
	 * コマンドを返します。
	 * 
	 * @return コマンド
	 */
	public String getCommand()
	{
		return _command;
	}

	/**
	 * コマンドがニューメリックリプライかどうかを判定します。
	 * ニューメリックリプライは、送信元のプレフィックスと3桁の数字とリプライのターゲットからなる一つのメッセージとして送られ「なければなりません」。
	 * 
	 * @return コマンドが数字の場合は true、そうでない場合は false
	 */
	public boolean isNumelicReply()
	{
		return NUMERIC_REPLY_PATTERN.matcher(getCommand()).matches();
	}

	/**
	 * ニューメリックリプライを数字で返します。
	 * 001から099までのニューメリックは、クライアント-サーバ接続にのみ使われ、サーバ同士でやり取りされることはありません。
	 * コマンドの結果生成されるリプライは、200から399の間になります。
	 * エラーリプライは400から599です。
	 * 
	 * @return ニューメリックリプライまたは -1
	 */
	public int getNumber()
	{
		if (isNumelicReply())
		{
			return Integer.parseInt(getCommand());
		}
		else
		{
			return -1;
		}
	}

//	public int getType()
//	{
//		// メッセージタイプが未検査の場合は検査する
//		if (_type == 0)
//		{
//			int reply = getNumelic();
//			if (reply >= 400)
//			{
//				_type = ERROR_REPLY;
//			}
//			else if (reply >= 200)
//			{
//				_type = COMMAND_REPLY;
//			}
//			else if (reply >= 001)
//			{
//				_type = CONNECT_REPLY;
//			}
//			else
//			{
//				_type = UNKNOWN_REPLY;
//			}
//		}
//
//		return _type;
//	}

	/**
	 * プレフィックスを返します。
	 * 
	 * @return プレフィックス
	 */
	public String getPrefix()
	{
		return _prefix;
	}

	/**
	 * サーバ名を返します。
	 * 
	 * @return サーバ名
	 */
	public String getServer()
	{
		return _server;
	}

	/**
	 * ニックネームを返します。
	 * 
	 * @return ニックネーム
	 */
	public String getNick()
	{
		return _nick;
	}

	/**
	 * ユーザ名を返します。
	 * 
	 * @return ユーザ名
	 */
	public String getUser()
	{
		return _user;
	}

	/**
	 * ホストを返します。
	 * 
	 * @return ホストまたは null
	 */
	public String getHost()
	{
		return _host;
	}

	/**
	 * 指定した位置のパラメータを返します。
	 *
	 * @param position 正の数の場合は先頭からのインデックス、負の数の場合は最後からのインデックス
	 * @return 指定した位置のパラメータ。指定した位置にない場合は null
	 */
	public String getParam(int position)
	{
		// 位置を再計算
		int index = (position >= 0) ? position : position + _middle.length;
		// 指定した位置にない場合は null
		if (index < 0 || index >= _middle.length) return null;
		else return _middle[index];
	}

	/**
	 * すべてのパラメータ配列を返します。
	 * 
	 * @return パラメータ配列
	 */
	public String[] getParams()
	{
		return _middle.clone();
	}

	/**
	 * 指定したインデックスより後のパラメータ配列を返します。
	 * 
	 * @param start 開始インデックス
	 * @return パラメータ配列
	 */
	public String[] getParams(int start)
	{
		List<String> list = getParamsAsList(start);
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * 指定したインデックスより後のパラメータをリストで返します。
	 * 
	 * @param start 開始インデックス
	 * @return パラメータリスト
	 */
	public List<String> getParamsAsList(int start)
	{
		List<String> list = new ArrayList<String>(Arrays.asList(_middle));
		list = list.subList(start, list.size());
		return list;
	}

	/**
	 * トレーラを返します。
	 * 
	 * @return トレーラ
	 */
	public String getTrail()
	{
		return decode(_trail, encoding);
	}

	/**
	 * 指定した文字エンコーディングで文字列を変換します。
	 * 
	 * @param input 文字列
	 * @param encoding 文字エンコーディング
	 * @return 文字列
	 */
	public static String decode(String input, String encoding)
	{
		if (input == null) return null;
		if (encoding == null || encoding.length() == 0) return input;

//		// 半角カナ対応
//		input = escapeKana(input);

		try
		{
			input = new String(input.getBytes(), encoding);
			return input;
		}
		catch (UnsupportedEncodingException x)
		{
			return input;
		}
	}

//	public String getTarget()
//	{
//		return getParam(0);
//	}
//
//	public String getMessageTarget()
//	{
//		return getParam(1);
//	}
//
//	public String getMessageSource()
//	{
//		return getParam(2);
//	}

//	public boolean withCTCP()
//	{
//		return getTrailing().startsWith(CTCP_DELIMITER);
//	}
//
//	public String[] getCTCPs()
//	{
//		return getTrailing().substring(CTCP_DELIMITER.length()).split(CTCP_DELIMITER);
//	}

	/**
	 * CTCPを含むかどうか判定します。
	 * 
	 * @return CTCPを含む場合は true
	 */
	public boolean withCTCP()
	{
//		return getTrailing().startsWith(CTCP_DELIMITER);
//		String trail = getTrailing();
//		if (trail.length() == 0) return false;
//		char ch = trail.charAt(0);
//		System.out.println("char=0x" + Integer.toHexString(ch) + "," + trail.startsWith(CTCP_DELIMITER) + "," + BOLD_DELIMITER + ";");
//		return Character.isISOControl(ch);
		return getTrail().contains(CTCP_DELIMITER);
	}

	/**
	 * ボールド表示を含むかどうか判定します。
	 * 
	 * @return ボールド表示を含む場合は true
	 */
	public boolean withBold()
	{
		return getTrail().contains(BOLD_DELIMITER);
	}

	/**
	 * カラー表示を含むかどうか判定します。
	 * 
	 * @return カラー表示を含む場合は true
	 */
	public boolean withColor()
	{
		return getTrail().contains(COLOR_DELIMITER);
	}

	/**
	 * 反転表示または斜体表示を含むかどうか判定します。
	 * 
	 * @return 反転表示または斜体表示を含む場合は true
	 */
	public boolean withReverse()
	{
		return getTrail().contains(REVERSE_DELIMITER);
	}

	/**
	 * アンダーライン表示を含むかどうか判定します。
	 * 
	 * @return アンダーライン表示を含む場合は true
	 */
	public boolean withUnderline()
	{
		return getTrail().contains(UNDERLINE_DELIMITER);
	}

//	public static String[] splitCTCP(String input)
//	{
//		return input.split(CTCP_DELIMITER);
//	}
//
//	public static String quoteCTCP(String input)
//	{
//		return CTCP_DELIMITER + input + CTCP_DELIMITER;
//	}

	/**
	 * パラメータとトレーラを合成した文字列を返します。
	 * 
	 * @return パラメータとトレーラの合成文字列
	 */
	public String toParamString()
	{
		List<String> list = new ArrayList<String>(Arrays.asList(_middle));
		list = list.subList(1, list.size());
		if (_trail != null) list.add(_trail);

		StringBuffer buf = new StringBuffer();
		Iterator<String> it = list.iterator();
		while (it.hasNext())
		{
			buf.append(it.next());
			if (it.hasNext()) buf.append(" ");
		}

		return buf.toString();
	}

	/**
	 * メッセージのデバッグ文字列を返します。
	 * 
	 * @return デバッグ文字列
	 */
	public String toString()
	{
//		return "(" + _command + ")" + _msg;
//		return "(" + _command + ")," + _prefix + "," + Arrays.toString(_middle) + "," + _trailing;
//		return getClass().getName() + "[" + _command + ", " + _prefix + ", " + Arrays.toString(_middle) + ", " + _trail + "]";
		return String.format("[%s] %s %s %s", getCommand(), getPrefix(), Arrays.toString(_middle), getTrail());
	}

	/**
	 * メッセージを作成します。
	 * 
	 * @param str 文字列
	 * @param source ソース
	 * @return メッセージ
	 */
	public static IRCMessage valueOf(String str, Object source)
	{
		return new IRCMessage(source, str);
	}

	private boolean createAndInvoke(Object target, String name)
	{
		try
		{
			// ex. onPrivmsg
			Class<? extends Object> c = target.getClass();
			Method m = c.getMethod(name, IRCMessage.class);
			m.setAccessible(true);	// 必要?
			IRCMessage msg = this;
			m.invoke(target, msg);
			return true;
		}
		catch (SecurityException x)
		{
			return false;
		}
		catch (NoSuchMethodException x)
		{
			return false;
		}
		catch (IllegalArgumentException x)
		{
			return false;
		}
		catch (IllegalAccessException x)
		{
			return false;
		}
		catch (InvocationTargetException x)
		{
			return false;
		}
	}

	/**
	 * アッパーキャメルケースに変換します。
	 * c
	 * @param str 文字列
	 * @return アッパーキャメルケースの文字列
	 */
	private String toUpperCamelCase(String str)
	{
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}

	/**
	 * コマンドに対応するメソッドを実行します。
	 * 
	 * @param target ターゲットオブジェクト
	 * @return 実行できた場合は true
	 */
	public boolean invoke(Object target)
	{
		String name = "on" + toUpperCamelCase(getCommand());
		boolean success = createAndInvoke(target, name) || createAndInvoke(target, "onMessage");
		return success;
	}

	/**
	 * @param encoding 文字エンコーディング
	 */
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/** 色コード */
	public static final HashMap<Integer, Color> COLORS = new HashMap<Integer, Color>()
	{
		{
			put(1, Color.BLACK);			// 1 - Black
			put(2, new Color(0x000080));	// 2 - Navy Blue
			put(3, Color.GREEN);			// 3 - Green
			put(4, Color.RED);				// 4 - Red
			put(5, new Color(0xa52a2a));	// 5 - Brown
			put(6, new Color(0x800080));	// 6 - Purple
			put(7, new Color(0x808000));	// 7 - Olive
			put(8, Color.YELLOW);			// 8 - Yellow
			put(9, new Color(0x32cd32));	// 9 - Lime Green
			put(10, new Color(0x008080));	// 10 - Teal
			put(11, new Color(0x00ffff));	// 11 - Aqua Light
			put(12, new Color(0x4169e1));	// 12 - Royal Blue
			put(13, new Color(0xff69b4));	// 13 - Hot Pink
			put(14, Color.DARK_GRAY);		// 14 - Dark Gray
			put(15, Color.LIGHT_GRAY);		// 15 - Light Gray
			put(16, Color.WHITE);			// 16 - White
		}
	};
}
