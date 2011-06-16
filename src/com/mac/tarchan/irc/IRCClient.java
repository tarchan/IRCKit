package com.mac.tarchan.irc;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	PrintStream out;

	ExecutorService messageQueue = Executors.newFixedThreadPool(2);

	ArrayList<IRCHandler> handlers = new ArrayList<IRCHandler>();

	/**
	 * IRCClient
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

	public static IRCClient createClient(String host, int port, String nick, String pass)
	{
		IRCClient client = new IRCClient(host, port, nick, pass);
		// TODO Auto-generated method stub
		return client;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

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
	 * すべてのコマンドを受けるハンドラを追加します。
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
		out = new PrintStream(socket.getOutputStream(), true);
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

	public InputStream getInputStream() throws IOException
	{
		return socket.getInputStream();
	}

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
		return postMessage(String.format("%S: %s", command, args));
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
	 * 
	 * @param channel チャンネル名
	 * @return IRCClient
	 */
	public IRCClient join(String channel)
	{
		return postMessage(String.format("JOIN %s", channel));
	}

	public IRCClient pong(String payload)
	{
		return postMessage(String.format("PONG :%s", payload));
	}

	public IRCClient privmsg(String receiver, String text)
	{
//		try
		{
//			text = new String(text.getBytes("UTF-8"), "ISO-2022-JP");
			return postMessage(String.format("PRIVMSG %s :%s", receiver, text));
		}
//		catch (UnsupportedEncodingException x)
//		{
//			throw new RuntimeException("文字コードを変更できません。", x);
//		}
	}

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
				handler.onMessage(event);
			}
		}
		catch (Throwable x)
		{
			x.printStackTrace();
		}
	}
}

class KanaFilter extends FilterInputStream
{
	protected KanaFilter(InputStream in)
	{
		super(in);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
//		System.out.printf("readRange%d:%d:%d%n", off, len, b.length);
		int eof = super.read(b, off, len);
		escapeKana(b, off, len);
		return eof;
	}

	static final byte ESC = 0x1b;

	void escapeKana(byte[] data, int off, int len) throws IOException
	{
		boolean shiftKana = false;
//		int startKana = 0;
		for (int i = 0; i < data.length; i++)
		{
			byte b0 = data[i];
			if (b0 == ESC)
			{
				if (i + 2 < data.length && data[i + 1] == '(' && data[i + 2] == 'J')
				{
//					System.out.printf("|J ");
					shiftKana = true;
//					startKana = i;
					data[i + 2] = 'I';
				}
				else
				{
//					System.out.printf("| ");
//					if (shiftKana)
//					{
////						System.out.printf("%d-%d%n", startKana, i - startKana);
//						String kana = new String(data, startKana, i - startKana, "JIS");
//						System.out.printf("「%s」", kana);
//					}
					shiftKana = false;
				}
			}
//			if (shiftKana) System.out.printf("%02X ", b0);
			if ((b0 & 0x80) != 0)
			{
				if (shiftKana)
				{
					b0 = (byte)(b0 - 0x80);
					data[i] = b0;
				}
			}
			if ((b0 & 0x80) != 0)
			{
				throw new RuntimeException(String.format("不正な文字です。 (%02X)", b0));
			}
		}
//		System.out.println();
	}
}

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
		in = new BufferedReader(new InputStreamReader(new KanaFilter(client.getInputStream()), encoding));
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				String line = in.readLine();
				if (line == null) break;
//				System.out.println(new String(line.getBytes(), encoding));
//				line = escapeKana(line);
//				line = decodeKana(line);
				client.fireMessage(line);
				Thread.yield();
			}
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
	}

	static final byte ESC = 0x1b;

	String decodeKana(String text)
	{
		try
		{
			byte[] data = text.getBytes("JIS");
			// byte[] data = text.getBytes();
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
			return newText;
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
		return text;
	}

	/** 半角カナにマッチする正規表現 */
//	private static Pattern KANA = Pattern.compile("(\\x1b\\(J)(.*?)(\\x1b(\\(B|\\(J|\\$@|\\$B))");
//	private static Pattern KANA = Pattern.compile("(\\x1b\\x28\\x4a)([\\xa1-\\xdf]*)(\\x1b\\x28\\x42)");
	private static Pattern KANA = Pattern.compile("(?<=\\x1b\\(J)(.*)");

	/**
	 * 
	 * @param input テキスト
	 * @return 
	 * @see <a href="http://d.hatena.ne.jp/tarchan/20070308">Windowsから送られてくる半角カナに対応</a>
	 */
	String escapeKana(String input)
	{
//		System.out.println("escapeKana=" + input);
		StringBuffer buf = new StringBuffer();
		Matcher kana = KANA.matcher(input);
		while (kana.find())
		{
			String sub = kana.group();
			System.out.println("kana=" + sub);
			kana.appendReplacement(buf, sub);
		}
		kana.appendTail(buf);
		return buf.toString();
	}
}

class OutputTask implements Runnable
{
	IRCClient client;

	String text;

	OutputTask(IRCClient client, String text)
	{
		this.client = client;
		this.text = text;
	}

	@Override
	public void run()
	{
		try
		{
			System.out.println("send: " + text);
//			client.out.println(text);
			PrintStream out = new PrintStream(client.getOutputStream(), true, "JIS");
			out.println(text);
//			out.close();
		}
		catch (Throwable x)
		{
			throw new RuntimeException("送信エラー: " + text, x);
		}
	}
}
