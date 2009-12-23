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
package com.mac.tarchan.irc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCMessage;
import com.mac.tarchan.irc.client.IRCName;
import com.mac.tarchan.irc.client.Reply;

/**
 * Shell
 * 
 * @author tarchan
 */
public class Shell
{
	/**
	 * Shell を実行します。
	 * 
	 * @param args URL または、設定ファイル名
	 */
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Shell [<url> | <properties name>]");
			System.exit(1);
		}

		try
		{
			new Shell(args[0]);
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
	}

	/**
	 * Shell を構築して、IRCに接続します。
	 * 
	 * @param name 設定ファイル名
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public Shell(String name) throws IOException
	{
		IRCClient irc = new IRCClient().addAllHandlers(this);
		if (new File(name).exists()) irc.load(name);
		else irc.setProperty("irc.url", name);
		irc.open();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//		String currentNick = irc.getProperty("irc.nick.name");
		String currentChannel = irc.getProperty("irc.channel");
loop:	while (true)
		{
			Thread.yield();
			String str = in.readLine();
			if (str == null) break loop;

			if (str.matches("/\\S+.*"))
			{
				// コマンド入力
				String cmd = str.substring(1);
				System.out.format(">>> %s\n", cmd);
				irc.postMessage(cmd);
			}
			else if (str.matches("#\\S+"))
			{
				// チャンネル切り替え
				IRCName ch = new IRCName(str);
				System.out.format("[%s]\n", ch);
			}
			else if (str.trim().length() > 0)
			{
				// メッセージ入力
				System.out.format("[ECHO] %s\n", str);
				irc.postMessage(String.format("PRIVMSG %s :%s", currentChannel, str));
			}
			else
			{
				// 何もしない
			}
		}
		in.close();
	}

	/**
	 * すべてのコマンドを表示します。
	 * 
	 * @param msg IRCメッセージ
	 */
	@Reply("ALL")
	public void log(IRCMessage msg)
	{
		System.out.format("[%s] %s\n", msg.getCommand(), msg.getMessage());
	}

	/**
	 * トピックを表示します。
	 * 
	 * @param msg IRCメッセージ
	 */
	@Reply("332")
	public void onTopic(IRCMessage msg)
	{
		String channel = msg.getParam(1);
		String topic = msg.getTrail();
		System.out.format("%s のトピックは %s です。\n", channel, topic);
	}

	/**
	 * トピックを誰がいつ変更したのか表示します。
	 * 
	 * @param msg IRCメッセージ
	 */
	@Reply("333")
	public void onTopicWhoTime(IRCMessage msg)
	{
		String channel = msg.getParam(1);
		String who = msg.getParam(2);
		Date when = new Date(Long.valueOf(msg.getParam(3)) * 1000);
		System.out.format("%s のトピックは %s が %tF(%3$ta) に変更しました。\n", channel, who, when);
	}

	/** NAMES */
	ArrayList<String> names;

	/**
	 * @param msg IRCメッセージ
	 */
	@Reply("353")
	public void onNames(IRCMessage msg)
	{
		// [353] :sendak.freenode.net 353 archan_ @ #java-ja :archan_ kenan_ tarchan nori090b quabbin sugyan yamashiro coco1ban rt-bot nakm mercysluck ryogrid hsegawa wozozo hidek___ Yappo_ log-bot yoshiori otsune yuguiaway 
//		String channel = msg.getParam(2);
		String[] token = msg.getTrail().split(" ");
		if (names == null)
		{
			names = new ArrayList<String>();
		}
		names.addAll(Arrays.asList(token));
	}

	/**
	 * @param msg IRCメッセージ
	 */
	@Reply("366")
	public void endOfNames(IRCMessage msg)
	{
		// [366] :sendak.freenode.net 366 archan_ #java-ja :End of /NAMES list.
		String channel = msg.getParam(1);
		System.out.format("チャンネル %s は %d人います。", channel, names.size());
		this.names = null;
	}
}
