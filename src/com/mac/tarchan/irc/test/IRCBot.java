package com.mac.tarchan.irc.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.mac.tarchan.irc.IRCClient;
import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;
import com.mac.tarchan.irc.IRCMessage;

/**
 * IRCClient Test
 */
public class IRCBot implements IRCHandler
{
	/**
	 * @param args <ホストアドレス> <ポート番号>
	 */
	public static void main(String[] args)
	{
		try
		{
			// irc.livedoor.ne.jp、irc6.livedoor.ne.jp、125.6.255.10
			String host = "irc.livedoor.ne.jp";
			int port = 6667;
			String nick = "mybot";
			String[] channles = {"#javabreak"};
			new IRCBot(host, port, nick ,channles);
		}
		catch (IOException x)
		{
			throw new RuntimeException("サーバーに接続できません。", x);
		}
	}

	/**
	 * IRCBot
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param channles チャンネルリスト
	 * @throws IOException サーバーに接続できない場合
	 */
	public IRCBot(String host, int port, String nick, String[] channles) throws IOException
	{
		IRCClient irc = IRCClient.createClient(host, port, nick)
			.on("001", this)
			.on("privmsg", this)
			.on("notice", this)
			.on("ping", this)
			.connect();
		System.out.println("接続: " + irc);
		if (channles != null)
		{
			for (String channel : channles)
			{
				irc.join(channel);
			}
		}
		decodeKana("ﾃｽﾄ");
		decodeKana("ﾃｽﾄ1");
		decodeKana("ﾃｽﾄだ");
		decodeKana("漢字のﾃｽﾄですけど");
	}

	public void onMessage(IRCEvent event)
	{
		IRCMessage message = event.getMessage();
		System.out.println("メッセージ: " + message);
//		message.getServer().send("me, too.");
		IRCClient client = event.getClient();
		client.postMessage("privmsg", "me, too.");
	}

	static byte ESC = 0x1b;

	void decodeKana(String text)
	{
		try
		{
			byte[] data = text.getBytes("JIS");
//			byte[] data = text.getBytes();
			for (int i = 0; i < data.length; i++)
			{
				byte b = data[i];
				if (b == ESC)
				{
					if (i + 2 < data.length && data[i + 1] == '(' && data[i + 2] == 'I')
					{
						System.out.printf("|I ");
					}
					else if (i + 2 < data.length && data[i + 1] == '(' && data[i + 2] == 'J')
					{
						System.out.printf("|J ");
					}
					else
					{
						System.out.printf("| ");
					}
				}
				System.out.printf("%02X ", b);
			}
			System.out.println();
			String newText = new String(data, "JIS");
			System.out.printf("%s -> %s, %s%n", text, newText, text.equals(newText));
		}
		catch (UnsupportedEncodingException x)
		{
			x.printStackTrace();
		}
	}
}
