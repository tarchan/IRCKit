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

	String file;

	byte[] addr;

	int port;

	long size;

	public DccSendFile(String trail) throws IOException
	{
		if (!trail.startsWith("DCC SEND")) throw new IllegalArgumentException("DCC SENDではありません。: " + trail);

		String[] params = trail.substring("DCC SEND ".length()).split(" ");
		file = params[0];
		addr = new BigInteger(params[1]).toByteArray();
		InetAddress inet = InetAddress.getByAddress(addr);
		port = Integer.parseInt(params[2]);
		size = Long.parseLong(params[3]);
		log.info(String.format("%s %,d bytes %s %s", file, size, inet, port));
	}

	public String getName()
	{
		return file;
	}

	public void save(File savefile)
	{
		log.info("ファイルを保存します。: " + savefile);
	}
}
