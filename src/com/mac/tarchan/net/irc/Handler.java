/*
 * Handler.java
 * IRCKit
 *
 * Created by tarchan on Aug 07, 2006.
 * Copyright (c) 2006 tarchan. All rights reserved.
 */
package com.mac.tarchan.net.irc;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IRCプロトコルの接続を確立します。
 * システムプロパティ java.protocol.handler.pkgs に com.mac.tarchan.net を加えておくと、
 * 自動的に {@link IRCConnection} のインスタンスがロードされます。
 *
 * @since 1.0
 * @author tarchan
 * @see URL#URL(String, String, int, String)
 * @see IRCConnection
 */
public class Handler extends URLStreamHandler
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(Handler.class);

	/**
	 * IRCプロトコルのデフォルトのポートを返します。(6667)
	 * 
	 * @return IRCプロトコルのデフォルのトポート
	 */
	@Override
	protected int getDefaultPort()
	{
		log.debug("get default port: 6667");
		return 6667;
	}

	/**
	 * IRCプロトコルの接続を確立します。
	 * 
	 * @return URL への URLConnection オブジェクト
	 * @see IRCConnection
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException
	{
		log.debug("open connection: " + url);

		URLConnection con = new IRCConnection(url);
		
		// バージョン文字列を設定
		con.setRequestProperty(IRCConnection.IRC_REAL, "IRCCore 2.0");

		// ニックネームとパスワードを設定
		String userInfo = url.getUserInfo();
		String[] params = userInfo != null ? userInfo.split(":") : new String[]{};
		switch (params.length)
		{
		case 2:
			con.setRequestProperty(IRCConnection.IRC_NICK, params[0]);
			con.setRequestProperty(IRCConnection.IRC_PASS, params[1]);
			break;
		case 1:
			con.setRequestProperty(IRCConnection.IRC_NICK, params[0]);
			break;
		case 0:
		default:
			break;
		}

		return con;
	}

	/**
	 * URL の文字列表現を構文解析し、IRC URL に変換します。
	 *
	 * <p>
	 * IRC URL の構文は、次のとおりです。
	 * </p>
	 * <pre>
	 * irc://[&lt;user&gt;[':'&lt;pass&gt;]'@']&lt;host&gt;[':'&lt;port&gt;]['/#'&lt;channel&gt;]
	 * </pre>
	 *
	 * @param url 仕様構文解析の結果を受け取る URL
	 * @param spec 構文解析する必要のある URL を表す String
	 * @param start 構文解析の開始位置を表す文字インデックス。これはプロトコル名の確定を表す「:」(存在する場合) の直後にくる
	 * @param limit 構文解析の終了位置を表す文字の位置。これは文字列の終わりか、「#」文字 (存在する場合) の位置である。シャープ記号よりもあとの情報はすべてアンカーを表す
	 */
	@Override
	protected void parseURL(URL url, String spec, int start, int limit)
	{
		log.debug("parse url: " + url + "," + spec + "," + start + "," + limit);
/*
		System.out.println(">>> parse");
		System.out.println("url=" + url);
		System.out.println("spec=" + spec);
		System.out.println("start=" + start);
		System.out.println("limit=" + limit);
		String ch = spec.substring(start);
		System.out.println("ch=" + ch);
		System.out.println("---");
*/

		// http として構文解析する
		super.parseURL(url, spec, start, limit);
//		System.out.println("url1=" + url);

		String protocol = url.getProtocol();
		String host = url.getHost();
		int port = url.getPort();
		String auth = url.getAuthority();
		String userinfo = url.getUserInfo();
		String path = url.getPath();
		String query = url.getQuery();
		String ref = url.getRef();

//		System.out.println(new StringBuffer()
//			.append("spec=" + spec)
//			.toString());
//		System.out.println(new StringBuffer()
//			.append("->protocol=" + protocol)
//			.append(",host=" + host)
//			.append(",port=" + port)
//			.append(",auth=" + auth)
//			.append(",userinfo=" + userinfo)
//			.append(",path=" + path)
//			.append(",query=" + query)
//			.append(",ref=" + ref)
//			.toString());

		if (port == -1) port = getDefaultPort();
//		if (userinfo == null) userinfo = "";
//		if (ref == null) ref = "";

//		// チャンネル名を再設定する
//		// path = path[?query][#ref]
//		String channel = path;
////		StringBuffer buf = new StringBuffer();
////		buf.append(path);
//		if (query != null)
//		{
////			buf.append("?");
////			buf.append(query);
//			channel += "?" + query;
//		}
//		if (ref != null)
//		{
////			buf.append("#");
////			buf.append(ref);
//			channel += "#" + ref;
//		}

		setURL(url, protocol, host, port, auth, userinfo, path, query, ref);
//		setURL(url, protocol, host, port, auth, userinfo, channel, null, null);
//		System.out.println("url2=" + url);

//		System.out.println("<<<");
	}
}
