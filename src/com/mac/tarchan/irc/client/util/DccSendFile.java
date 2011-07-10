/*
 * DccSendFile.java
 * IRCKit
 * 
 * Created by tarchan on 2011/07/09.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client.util;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DccSendFile
 */
public class DccSendFile
{
	/** ログ */
	private static final Log log = LogFactory.getLog(DccSendFile.class);

	/** ファイル名 */
	protected String file;

	/** IPアドレス */
	protected byte[] addr;

	/** ポート番号 */
	protected int port;

	/** ファイルサイズ */
	protected long size;

	/**
	 * DCC SENDメッセージからファイルを構築します。
	 * 
	 * @param text DCC SENDメッセージ
	 * @throws IOException 指定されたIPアドレスのサーバが見つからない場合
	 */
	public DccSendFile(String text) throws IOException
	{
		if (!text.startsWith("DCC SEND")) throw new IllegalArgumentException("DCC SENDではありません。: " + text);

		String[] params = text.substring("DCC SEND ".length()).split(" ");
		file = params[0];
		addr = new BigInteger(params[1]).toByteArray();
		InetAddress inet = InetAddress.getByAddress(addr);
		port = Integer.parseInt(params[2]);
		size = Long.parseLong(params[3]);
		log.info(String.format("%s %,d bytes %s %s", file, size, inet, port));
	}

	/**
	 * ファイル名を返します。
	 * 
	 * @return ファイル名
	 */
	public String getName()
	{
		return file;
	}

	/**
	 * 指定されたファイルに保存します。
	 * 
	 * @param savefile ファイル
	 */
	public void save(File savefile)
	{
		log.info("ファイルを保存します。: " + savefile);
	}
}
