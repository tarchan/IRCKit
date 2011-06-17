package com.mac.tarchan.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mac.tarchan.irc.util.KanaInputFilter;

/**
 * IRCClient
 */
public class IRCClient
{
	String host;

	int port;

	String nick;

	String pass;

	Socket socket;

//	PrintStream out;

	ExecutorService messageQueue = Executors.newFixedThreadPool(2);

	ArrayList<IRCHandler> handlers = new ArrayList<IRCHandler>();

	/**
	 * IRCClient を構築します。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 */
	protected IRCClient(String host, int port, String nick, String pass)
	{
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.pass = pass;
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @return IRCClient
	 */
	public static IRCClient createClient(String host, int port, String nick)
	{
		return createClient(host, port, nick, null);
	}

	/**
	 * IRCClient を作成します。
	 * 
	 * @param host ホストアドレス
	 * @param port ポート番号
	 * @param nick ニックネーム
	 * @param pass パスワード
	 * @return IRCClient
	 */
	public static IRCClient createClient(String host, int port, String nick, String pass)
	{
		IRCClient client = new IRCClient(host, port, nick, pass);
		// TODO ホストアドレス毎にキャッシュする
		return client;
	}

	/**
	 * ホストアドレスを返します。
	 * 
	 * @return ホストアドレス
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * ポート番号を返します。
	 * 
	 * @return ポート番号
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * ニックネームを返します。
	 * 
	 * @return ニックネーム
	 */
	public String getNick()
	{
		return nick;
	}

	/**
	 * 指定されたコマンドのハンドラを追加します。
	 * 
	 * @param command コマンド
	 * @param handler ハンドラ
	 * @return IRCClient オブジェクト
	 */
	public IRCClient on(String command, IRCHandler handler)
	{
//		command = command.toUpperCase();
//		IRCMessage message = new IRCMessage(command);
//		handler.onMessage(IRCMessage.createMessage(this, command));
//		handler.onMessage(new IRCEvent(this, message));
		return this;
	}

	/**
	 * すべてのコマンドを受け入れるハンドラを追加します。
	 * 
	 * @param handler ハンドラ
	 * @return IRCClient オブジェクト
	 */
	public IRCClient on(IRCHandler handler)
	{
		handlers.add(handler);
		return this;
	}

//	/**
//	 * 指定されたコマンドのハンドラを追加します。
//	 * 
//	 * @param handler ハンドラ
//	 * @return IRCClient オブジェクト
//	 */
//	public IRCClient on(Object handler)
//	{
//		// TODO アノテーションでハンドラを指定
//		return this;
//	}

	/**
	 * サーバーに接続します。
	 * 
	 * @return IRCClient
	 * @throws IOException サーバーに接続できない場合
	 */
	public IRCClient connect() throws IOException
	{
		InetAddress inet = InetAddress.getByName(host);
		socket = new Socket(host, port);
		System.out.println("connect: " + inet);
//		new Thread(new InputListener(this)).start();
		messageQueue.execute(new InputTask(this));
//		out = new PrintStream(socket.getOutputStream(), true);
		if (pass != null && pass.trim().length() != 0)
		{
			postMessage(String.format("PASS %s", pass));
		}
		postMessage(String.format("NICK %s", nick));
//		postMessage(String.format("USER %s %d %s :%s", nick, 0, host, nick));
		postMessage(String.format("USER %s %s bla :%s", nick, host, nick));
//		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		while (true)
//		{
//			String line = in.readLine();
//			if (line == null) break;
//			System.out.println(new String(line.getBytes(), "JIS"));
//		}
		System.out.println("connected: " + host);
//		socket.close();
		return this;
	}

	/**
	 * 入力ストリームを返します。
	 * 
	 * @return 入力ストリーム
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public InputStream getInputStream() throws IOException
	{
		return socket.getInputStream();
	}

	/**
	 * 出力ストリームを返します。
	 * 
	 * @return 出力ストリーム
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}

	/**
	 * 指定されたコマンドを送信します。
	 * 
	 * @param command コマンド名
	 * @param args コマンド引数
	 * @return IRCClient
	 */
	public IRCClient postMessage(String command, String... args)
	{
		return postMessage(String.format("%S: %s", command, Arrays.toString(args)));
	}

	/**
	 * 指定されたテキストを送信します。
	 * 
	 * @param text テキスト
	 * @return IRCClient
	 */
	public IRCClient postMessage(String text)
	{
//		System.out.println("send: " + text);
//		if (out != null)
//		{
//			out.println(text);
//		}
		messageQueue.execute(new OutputTask(this, text));
		return this;
	}

	/**
	 * 指定されたチャンネルに入ります。
	 * サーバーに JOIN コマンドを送信します。
	 * 
	 * @param channel チャンネル名
	 * @return IRCClient
	 */
	public IRCClient join(String channel)
	{
		return postMessage(String.format("JOIN %s", channel));
	}

	// TODO part

	/**
	 * サーバーとの接続を継続します。
	 * サーバーに PONG コマンドを送信します。
	 * 
	 * @param payload ペイロード
	 * @return IRCClient
	 */
	public IRCClient pong(String payload)
	{
		return postMessage(String.format("PONG :%s", payload));
	}

	/**
	 * 指定されたテキストを送信します。
	 * 
	 * @param receiver テキストの宛先
	 * @param text テキスト
	 * @return IRCClient
	 */
	public IRCClient privmsg(String receiver, String text)
	{
		return postMessage(String.format("PRIVMSG %s :%s", receiver, text));
	}

	// TODO quit

	/**
	 * 指定されたテキストを解析して、ハンドラに送信します。
	 * 
	 * @param text テキスト
	 */
	protected void fireMessage(String text)
	{
		try
		{
			IRCMessage message = new IRCMessage(text);
//			String encoding = "JIS";
////			System.out.println(new String(text.getBytes(), encoding));
//			IRCMessage message = new IRCMessage(text, encoding);
//			handler.onMessage(new IRCEvent(this, message));
//			System.out.println(message.toString());
			IRCEvent event = new IRCEvent(this, message);
			for (IRCHandler handler : handlers)
			{
				try
				{
					handler.onMessage(event);
				}
				catch (Throwable x)
				{
					x.printStackTrace();
				}
			}
		}
		catch (Throwable x)
		{
			x.printStackTrace();
		}
	}
}

/**
 * InputTask
 */
class InputTask implements Runnable
{
	IRCClient client;

	BufferedReader in;

//	String encoding;

	InputTask(IRCClient client) throws IOException
	{
		this.client = client;
//		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String encoding = "JIS";
//		in = new BufferedReader(new InputStreamReader(client.getInputStream(), encoding));
		in = new BufferedReader(new InputStreamReader(new KanaInputFilter(client.getInputStream()), encoding));
	}

	public void run()
	{
		try
		{
			while (true)
			{
				String line = in.readLine();
				if (line == null) break;
//				System.out.println(new String(line.getBytes(), encoding));
				client.fireMessage(line);
				Thread.yield();
			}
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
	}
}

/**
 * OutputTask
 */
class OutputTask implements Runnable
{
	IRCClient client;

	String text;

	PrintStream out;

	OutputTask(IRCClient client, String text)
	{
		this.client = client;
		this.text = text;
	}

	public void run()
	{
		try
		{
			System.out.println("send: " + text);
//			client.out.println(text);
			out = new PrintStream(client.getOutputStream(), true, "JIS");
			out.println(text);
//			out.close();
		}
		catch (Throwable x)
		{
			throw new RuntimeException("送信エラー: " + text, x);
		}
	}
}
