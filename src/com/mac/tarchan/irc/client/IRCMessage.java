/*
 * IRCMessage.java
 * IRCKit
 *
 * Created by tarchan on 2005/05/19.
 * Copyright (c) 2005 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRCメッセージを構文解析します。
 * 
 * <ul>
 * <li><a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html" charset="EUC-JP">RFC1459: Internet Relay Chat Protocol (IRC)</a></li>
 * <li><a href="http://www.irchelp.org/irchelp/rfc/index.html">RFC: Internet Relay Chat Protocol</a>
 * <ul>
 * <li><a href="http://www.irchelp.org/irchelp/rfc/rfc.html">RFC 1459: Internet Relay Chat Protocol</a></li>
 * <li><a href="http://www.irchelp.org/irchelp/rfc/ctcpspec.html">The Client-To-Client Protocol (CTCP)</a></li>
 * <li><a href="http://www.irchelp.org/irchelp/rfc/dccspec.html">A description of the DCC protocol</a></li>
 * </ul>
 * </li>
 * </ul>
 * 
 * <pre>
 * message ::= [':'&lt;prefix&gt; &lt;SPACE&gt;] &lt;command&gt; &lt;params&gt; &lt;crlf&gt;
 * prefix ::= &lt;servername&gt; | &lt;nick&gt; ['!'&lt;user&gt;]['@'&lt;host&gt;]
 * command ::= &lt;letter&gt; {&lt;letter&gt;} | &lt;number&gt; &lt;number&gt; &lt;number&gt;
 * params ::= &lt;SPACE&gt;[':'&lt;trailing&gt; | &lt;middle&gt; &lt;params&gt;]
 * </pre>
 *
 * @author tarchan
 */
public class IRCMessage extends EventObject
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(IRCMessage.class);

	/** アドレス区切り */
	public static final String ADDRESS_DELIMITER = "[!@]";

	/** CTCPメッセージ */
//	private static final char CTCP = 0x01;
//	private static final String CTCP_DELIMITER = String.valueOf(CTCP);
	private static final String CTCP_DELIMITER = "\1";

	/** ボールド表示 */
	private static final String BOLD_DELIMITER = "\2";

	/** カラー表示*/
	private static final String COLOR_DELIMITER = "\3";

//	/** 反転表示または斜体表示 */
//	private static final String REVERSE_DELIMITER = "\0x16";
//
//	/** アンダーライン表示 */
//	private static final String UNDERLINE_DELIMITER = "\0x1f";
//
//	/** 使用禁止 */
//	private static final String NO_USE = "[\0x00\0x0d\0x0a]";	

	/** 接続が登録されて、すべてのIRCネットワークに認知されたということを表します。 */
	public static final int RPL_WELCOME = 001;

	/** サービスがうまく登録されると、サーバからサービスに送られます。 */
	public static final int RPL_YOURESERVICE = 383;

//	/**
//	 * クライアント-サーバ接続に使われる番号です。
//	 * クライアント-サーバ接続に使われるリプライは、001から099です。
//	 */
//	private static final int CONNECT_REPLY = 001;
//
//	/**
//	 * コマンドリプライを表す番号です。
//	 * コマンドの結果生成されるリプライは、200から399です。
//	 */
//	private static final int COMMAND_REPLY = 200;
//
//	/**
//	 * エラーリプライを表す番号です。
//	 * エラーリプライは、400から599です。
//	 */
//	private static final int ERROR_REPLY = 400;
//
//	/**
//	 * ニューメリックリプライ以外を表す番号です。
//	 */
//	private static final int UNKNOWN_REPLY = -1;

	/** 入力メッセージ */
	private String _msg;

	/** 入力時刻 */
	private long _when;

	// メッセージ解析結果
	/** プレフィックス */
	private String _prefix;

	/** コマンド */
	private String _command;

	/** パラメータ配列 */
	private String _middle[];

	/** トレーラ */
	private String _trailing;

	/** サーバ名(prefix) */
	private String _server;

	/** ニックネーム(prefix) */
	private String _nick;

	/** ユーザ名(prefix) */
	private String _user;

	/** ホスト名 (prefix) */
	private String _host;

	/**
	 * IRCメッセージを解析して IRCMessage を作成します。
	 * 
	 * @param source メッセージのソース
	 * @param message メッセージ
	 */
	public IRCMessage(IRCNetwork source, String message)
	{
		this(source, message, System.currentTimeMillis());
	}

	/**
	 * IRCメッセージを解析して IRCMessage を作成します。
	 * 
	 * @param source メッセージのソース
	 * @param message メッセージ
	 * @param when メッセージ作成日時
	 */
	public IRCMessage(IRCNetwork source, String message, long when)
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
		if (message == null) throw new NullPointerException("message　is null");

		// 疑似BNFによるメッセージを構文解析する
		parse(message);
	}

	/**
	 * メッセージが発生したタイムスタンプを設定します。
	 * 
	 * @param when タイムスタンプ
	 */
	protected void setWhen(long when)
	{
		_when = when;
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
		_msg = message;

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
			log.warn(message);
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
				_trailing = params.substring(1);
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
	 * 入力ソースを返します。
	 * 
	 * @return 入力ソース
	 */
	public IRCNetwork getNetwork()
	{
		return (IRCNetwork)getSource();
	}

	/**
	 * 入力メッセージを返します。
	 * 
	 * @return 入力メッセージ
	 */
	public String getMessage()
	{
		return _msg;
	}

	/**
	 * 入力メッセージを指定したエンコーディングで変換して返します。
	 * 
	 * @param encoding 文字エンコーディング
	 * @return 入力メッセージ
	 */
	public String getMessage(String encoding)
	{
		return decode(_msg, encoding);
	}

	/**
	 * 入力時刻を返します。
	 * 
	 * @return 入力時刻
	 */
	public long getWhen()
	{
		return _when;
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

	/** ニューメリックリプライのパターン */
	private Pattern NUMERIC_REPLY_PATTERN = Pattern.compile("[0-9][0-9][0-9]");

	/**
	 * コマンドがニューメリックリプライかどうかを判定します。
	 * ニューメリックリプライは、送信元のプレフィックスと3桁の数字とリプライのターゲットからなる一つのメッセージとして送られ「なければなりません」。
	 * 
	 * @return コマンドが数字の場合は true、そうでない場合は false
	 */
	public boolean isNumelic()
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
	public int getNumelic()
	{
		if (isNumelic())
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
	 * 指定したインデックス以降のパラメータ配列を返します。
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
	 * 指定したインデックス以降のパラメータリストを返します。
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
	public String getTrailing()
	{
		return _trailing;
	}

	/**
	 * 指定した文字エンコーディングで変換したトレーラを返します。
	 * 
	 * @param encoding 文字エンコーディング
	 * @return トレーラ
	 */
	public String getTrailing(String encoding)
	{
		return decode(_trailing, encoding);
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

		// 半角カナ対応
		input = escapeKana(input);

		Charset charset = Charset.forName(encoding);
		CharBuffer cb = charset.decode(ByteBuffer.wrap(getRowBytes(input)));
		return cb.toString();
	}

	/** 半角カナにマッチする正規表現 */
	private static Pattern KANA = Pattern.compile("(\\x1b\\(J)(.*?)(\\x1b(\\(B|\\(J|\\$@|\\$B))");
//	private static Pattern KANA = Pattern.compile("(\\x1b\\x28\\x4a)([\\xa1-\\xdf]*)(\\x1b\\x28\\x42)");
//	private static Pattern KANA = Pattern.compile("(\\x1b\\(J)(?!\\x1b(\\(B|\\(J|\\$@|\\$B))+(\\x1b(\\(B|\\(J|\\$@|\\$B))");

	/**
	 * 不正な半角かなを正規のかな表現に変換します。
	 * 
	 * @param input 不正な半角かな表現を含む文字列
	 * @return 正規のかな表現に変換した文字列
	 */
	private static String escapeKana(String input)
	{
		if (input == null) return null;

//		System.out.println("kana=" + input + ",[" + toHexString(getRowBytes(input)) + "]");
		StringBuffer sb = new StringBuffer();
		Matcher kana = KANA.matcher(input);
		int count = 0;
		while (kana.find())
		{
			String g = kana.group();
			byte[] b = getRowBytes(g);
			System.out.println("kana" + (count++) + "=" + g + ",[" + toHexString(b) + "]");
			int end = b.length - 3;
			// [ESC](J -> [ESC](I
			b[2] = 'I';
			for (int i = 3; i < end; i++)
			{
				// 0xa1 -> 0x21
				b[i] = (byte)(b[i] - (0xa1 - 0x21));
			}
//			System.out.println("[" + toHexString(b) + "]");
//			g = "X";
			g = new String(b);
			kana.appendReplacement(sb, g);
		}
		kana.appendTail(sb);
		input = sb.toString();

//		String g = null;
//		if (kana.find()) g = kana.group();
//		System.out.println("row=" + input + ",[" + g + "]");
		return input;
	}

	/**
	 * バイト配列の文字表現を返します。
	 * 
	 * @param b バイト配列
	 * @return バイト配列の文字表現
	 */
	private static String toHexString(byte[] b)
	{
		StringBuilder sb = new StringBuilder();
		for (byte c : b)
		{
			sb.append(String.format("%02X ", c));
		}
		return sb.toString();
	}

	/**
	 * バイト配列を返します。
	 * 
	 * @param input 文字列
	 * @return バイト配列
	 */
	private static byte[] getRowBytes(String input)
	{
		if (input == null) return null;

		try
		{
			byte[] b = input.getBytes("ISO-8859-1");
			return b;
		}
		catch (UnsupportedEncodingException x)
		{
			throw new IllegalStateException(x);
		}
	}

	/**
	 * 指定した文字エンコーディングで文字列を変換します。
	 * 
	 * @param input 文字列
	 * @param encoding 文字エンコーディング
	 * @return 文字列
	 */
	public static String encode(String input, String encoding)
	{
		if (input == null) return null;

		Charset charset = Charset.forName(encoding);
//		CharsetEncoder encorder = charset.newEncoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
//		ByteBuffer bb = encorder.encode(CharBuffer.wrap(input));
		ByteBuffer bb = charset.encode(input);
		String output = new String(bb.array());
		return output;
	}

	/**
	 * ターゲットを返します。
	 * 
	 * @return ターゲット
	 */
	public String getTarget()
	{
		return getParam(0);
	}

	/**
	 * メッセージターゲットを返します。
	 * 
	 * @return メッセージターゲット
	 */
	public String getMessageTarget()
	{
		return getParam(1);
	}

	/**
	 * メッセージソースを返します。
	 * 
	 * @return メッセージソース
	 */
	public String getMessageSource()
	{
		return getParam(2);
	}

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
	 * CTCPメッセージかどうかを判定します。
	 * 
	 * @return CTCPメッセージの場合は true
	 */
	public boolean isCTCP()
	{
//		return getTrailing().startsWith(CTCP_DELIMITER);
//		String trail = getTrailing();
//		if (trail.length() == 0) return false;
//		char ch = trail.charAt(0);
//		System.out.println("char=0x" + Integer.toHexString(ch) + "," + trail.startsWith(CTCP_DELIMITER) + "," + BOLD_DELIMITER + ";");
//		return Character.isISOControl(ch);
		return getTrailing().contains(CTCP_DELIMITER);
	}

	/**
	 * ボールドスタイルかどうかを判定します。
	 * 
	 * @return ボールドスタイルの場合は true
	 */
	public boolean isBold()
	{
		return getTrailing().contains(BOLD_DELIMITER);
	}

	/**
	 * カラー指定かどうかを判定します。
	 * 
	 * @return カラー指定の場合は true
	 */
	public boolean isColor()
	{
		return getTrailing().contains(COLOR_DELIMITER);
	}

	/**
	 * CTCPメッセージを分割します。
	 * 
	 * @param input CTCPを含む文字列
	 * @return CTCPを分割した配列
	 */
	public static String[] splitCTCP(String input)
	{
		return input.split(CTCP_DELIMITER);
	}

	/**
	 * CTCPメッセージに変換します。
	 * 
	 * @param input プレーンな文字列
	 * @return CTCP文字列
	 */
	public static String quoteCTCP(String input)
	{
		return CTCP_DELIMITER + input + CTCP_DELIMITER;
	}

	/**
	 * パラメータとトレーラを合成した文字列を返します。
	 * 
	 * @return パラメータとトレーラの合成文字列
	 */
	public String toParamString()
	{
		List<String> list = new ArrayList<String>(Arrays.asList(_middle));
		list = list.subList(1, list.size());
		if (_trailing != null) list.add(_trailing);

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
	 * 入力メッセージを返します。
	 * 
	 * @return 入力メッセージ
	 */
	public String toString()
	{
//		return "(" + _command + ")" + _msg;
//		return "(" + _command + ")," + _prefix + "," + Arrays.toString(_middle) + "," + _trailing;
//		return getClass().getName() + "[" + _command + ", " + _prefix + ", " + Arrays.toString(_middle) + ", " + _trailing + "]";
		return _msg;
	}
}
