/*
 * IRCFactory.java
 * IRCKit
 *
 * Created by tarchan on 2008/03/19.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 名前とIRCNetwork オブジェクトの関連付けマップを管理します。
 * 
 * @since 1.0
 * @author tarchan
 */
public class IRCFactory
{
	/** ロガー */
	private static final Log log = LogFactory.getLog(IRCFactory.class);

	/** 名前とIRCNetwork オブジェクトの関連付けマップ */
	private static Map<String, IRCNetwork> networkMap = new HashMap<String, IRCNetwork>();

	/**
	 * 指定した名前に関連付けられた一意の IRCNetwork オブジェクトを取得します。
	 * 
	 * @param name ネットワーク名
	 * @return 名前に関連付けられた IRCNetwork オブジェクト
	 */
	public static IRCNetwork getNetwork(String name)
	{
		IRCNetwork network = networkMap.get(name);
		if (network == null)
		{
			network = new IRCNetwork(name);
			networkMap.put(name, network);
		}
		log.info("get network: " + network);

		return network;
	}
}
