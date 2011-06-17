package com.mac.tarchan.ircbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.mac.tarchan.irc.IRCClient;
import com.mac.tarchan.irc.IRCEvent;
import com.mac.tarchan.irc.IRCHandler;
import com.mac.tarchan.irc.IRCMessage;

/**
 * IRCClient Test
 */
public class IRCBot implements IRCHandler
{
	String[] channels;

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
			String pass = null;
			String[] channles = {"#javabreak"};
			new IRCBot(host, port, nick, pass, channles);
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
	 * @param pass パスワード
	 * @param channels チャンネルリスト
	 * @throws IOException サーバーに接続できない場合
	 */
	public IRCBot(String host, int port, String nick, String pass, String[] channels) throws IOException
	{
		this.channels = channels;
		IRCClient irc = IRCClient.createClient(host, port, nick, pass)
			.on(this)
//			.on("001", this)
//			.on("privmsg", this)
//			.on("notice", this)
//			.on("ping", this)
			.connect();
		System.out.println("接続: " + irc);
	}

	void join(IRCClient irc)
	{
		if (channels != null)
		{
			for (String channel : channels)
			{
				irc.join(channel);
			}
		}
	}

	void readLines(IRCClient irc) throws IOException
	{
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		while (true)
		{
			String line = buf.readLine();
			System.out.println("input: " + line);
			String chan = "#javabreak";
			irc.postMessage(String.format("PRIVMSG %s :%s", chan, new String(line.getBytes(), "JIS")));
		}
	}

//	// '^PING (?P<payload>.*)'
//	Pattern ping_re = Pattern.compile("^PING :(.*)");
//
//	// ':(?P<nick>.*?)!\S+\s+?PRIVMSG\s+#(?P<channel>[-\w]+)\s+:(?P<message>[^\n\r]+)'
//	Pattern chanmsg_re = Pattern.compile(":(.*?)!\\S+\\s+?PRIVMSG\\s+(#?[-\\w]+)\\s+:(.+)");

	public void onMessage(IRCEvent event)
	{
		IRCMessage message = event.getMessage();
//		System.out.println("メッセージ: " + message);
//		message.getServer().send("me, too.");
		IRCClient irc = event.getClient();
//		client.postMessage("privmsg", "me, too.");

		String command = message.getCommand();
		if (command.equals("PRIVMSG"))
		{
			// privmsg
//			String nick = message.getPrefix();
//			String chan = message.getParam(0);
//			String msg = message.getTrailing();
//			if (!chan.equals(irc.getNick()))
//			{
//				irc.privmsg(chan, msg);
//			}
//			else
//			{
//				irc.privmsg(nick, msg);
//			}
		}
		else if (command.equals("PING"))
		{
			// ping
			String payload = message.getTrailing();
			irc.pong(payload);
		}
		else if (command.equals("ERROR"))
		{
			// error
		}
		else if (command.equals("001"))
		{
			// welcome
			join(irc);
		}

//		String text = message.toString();
//		Matcher chanmsg_re_m = chanmsg_re.matcher(text);
//		Matcher ping_re_m = ping_re.matcher(text);
//		if (chanmsg_re_m.find())
//		{
//			String nick = chanmsg_re_m.group(1);
//			String chan = chanmsg_re_m.group(2);
//			String msg = chanmsg_re_m.group(3);
//			System.out.printf("%s %s :%s%n", nick, chan, msg);
//			if (!chan.equals(irc.getNick()))
//			{
//				irc.privmsg(chan, msg);
//			}
//			else
//			{
//				irc.privmsg(nick, msg);
//			}
//		}
//		else if (ping_re_m.find())
//		{
//			String payload = ping_re_m.group(1);
////			System.out.println("PING " + payload);
//			irc.pong(payload);
//		}
	}
}
