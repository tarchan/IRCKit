/*
 * Copyright (c) 2009 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.beans.EventHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IRCClient
 */
public class IRCClient
{
	/** デフォルトの環境設定 */
	private static final Properties DEFAULTS = new Properties()
	{
		{
			setProperty("irc.host", "localhost");
			setProperty("irc.port", "6667");
			setProperty("irc.channel", "");
			setProperty("irc.encoding", "UTF-8");
			setProperty("irc.user.name", "");
			setProperty("irc.user.pass", "");
			setProperty("irc.user.mode", "12");
			setProperty("irc.user.icon", "");
			setProperty("irc.nick.name", "");
			setProperty("irc.real.name", "");
		}
	};

	/** 環境設定 */
	protected Properties props = new Properties(DEFAULTS);

	/** 改行コード */
	public static final String CRLF = "\r\n";

	/** 入力ストリーム */
	protected InputStream in;

	/** 出力ストリーム */
	protected PrintStream out;

//	/** メッセージキュー */
//	protected Queue<String> queue;

//	/** 入力キュー */
//	protected ScheduledExecutorService inQueue = Executors.newScheduledThreadPool(1);

	/** メッセージキュー */
	protected ExecutorService postQueue = Executors.newSingleThreadExecutor();

	/** メッセージハンドラ */
	protected HashMap<String, ArrayList<IRCMessageHandler>> handlerMap = new HashMap<String, ArrayList<IRCMessageHandler>>();

	static
	{
		// IRCプロトコルハンドラを設定
		setProtocolHandlerPackage("com.mac.tarchan");

		// システムプロキシーを使用
		setUseSystemProxies(true);
	}

	/**
	 * IRCClient
	 */
	public IRCClient()
	{
		addMessageHandler("PING", EventHandler.create(IRCMessageHandler.class, this, "ping", "trail"));
	}

	/**
	 * 空文字列かどうかを判定します。
	 * 
	 * @param str 文字列
	 * @return 空文字列の場合は true
	 */
	private static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}

	/**
	 * プロトコルハンドラのパッケージを設定します。
	 * 
	 * @param name パッケージ名
	 */
	public static void setProtocolHandlerPackage(String name)
	{
		System.setProperty("java.protocol.handler.pkgs", name);
	}

	/**
	 * システムプロキシを使用するかどうかを設定します。
	 * 
	 * http://java.sun.com/javase/ja/6/docs/ja/technotes/guides/net/proxies.html
	 * 
	 * @param useSystemProxies システムプロキシを使用する場合は true
	 */
	public static void setUseSystemProxies(boolean useSystemProxies)
	{
		System.setProperty("java.net.useSystemProxies", Boolean.valueOf(useSystemProxies).toString());
	}

	/**
	 * 環境設定を読み込みます。
	 * 
	 * @param name 設定ファイル名
	 * @throws IOException 入力エラーが発生した場合
	 */
	public void load(String name) throws IOException
	{
		props = new Properties(DEFAULTS);
		File file = new File(name);
		FileInputStream in = new FileInputStream(file);
		try
		{
			if (name.endsWith(".properties"))
			{
				props.load(in);
			}
			else if (name.endsWith(".xml") || name.endsWith(".plist"))
			{
				props.loadFromXML(in);
			}
			else
			{
				throw new IllegalArgumentException(name);
			}
		}
		catch (IOException x)
		{
			throw x;
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * 環境設定を返します。
	 * 
	 * @return 環境設定
	 */
	public Properties getProperties()
	{
		return props;
	}

	/**
	 * 指定されたキーのプロパティーを設定します。
	 * 
	 * @param key キー
	 * @param value プロパティー
	 */
	public void setProperty(String key, String value)
	{
		props.setProperty(key, value);
	}

	/**
	 * 指定されたキーのプロパティーを返します。
	 * 
	 * @param key キー
	 * @return プロパティー
	 */
	public String getProperty(String key)
	{
		return props.getProperty(key);
	}

	/**
	 * メッセージハンドラを追加します。
	 * 
	 * @param command コマンド
	 * @param handler メッセージハンドラ
	 */
	public void addMessageHandler(String command, IRCMessageHandler handler)
	{
		command = command.toUpperCase();
		ArrayList<IRCMessageHandler> handlerChain = handlerMap.get(command);
		if (handlerChain == null)
		{
			handlerChain = new ArrayList<IRCMessageHandler>();
			handlerMap.put(command, handlerChain);
		}
		handlerChain.add(handler);
	}

	/**
	 * @param obj オブジェクト
	 * @return メッセージハンドラの配列
	 * @see Reply
	 */
	public IRCMessageHandler[] addMessageHandlerAll(Object obj)
	{
		ArrayList<IRCMessageHandler> handlerChain = new ArrayList<IRCMessageHandler>();
		for (Method m : obj.getClass().getMethods())
		{
			if (m.isAnnotationPresent(Reply.class))
			{
				Reply reply = m.getAnnotation(Reply.class);
//				System.out.format("method: %s: %s: %s\n", m, reply, m.getName());
				IRCMessageHandler handler = EventHandler.create(IRCMessageHandler.class, obj, m.getName(), "");
				addMessageHandler(reply.value(), handler);
				handlerChain.add(handler);
				System.out.format("handler: %s: %s\n", reply.value(), handler);
			}
		}
		return handlerChain.toArray(new IRCMessageHandler[handlerChain.size()]);
	}

	/**
	 * メッセージハンドラを削除します。
	 * 
	 * @param command コマンド
	 * @param handler メッセージハンドラ
	 */
	public void removeMessageHandler(String command, IRCMessageHandler handler)
	{
		command = command.toUpperCase();
		ArrayList<IRCMessageHandler> handlerChain = handlerMap.get(command);
		if (handlerChain != null)
		{
			handlerChain.remove(handler);
			if (handlerChain.size() == 0) handlerMap.remove(command);
		}
	}

	/**
	 * IRCサーバーにログインします。
	 * 
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public void login() throws IOException
	{
		String host = getProperty("irc.host");
		String port = getProperty("irc.port");
		URL url = new URL(String.format("irc://%s:%s", host, port));
		System.out.format("[LOGIN] %s\n", url);
		URLConnection con = url.openConnection();
		con.connect();
		System.out.format("[LOGIN] %s\n", con);

		String encoding = getProperty("irc.encoding");
		in = con.getInputStream();
		out = new PrintStream(con.getOutputStream(), true, encoding);
//		queue = new ConcurrentLinkedQueue<String>();

		new Thread(new PollMessage(this)).start();
//		new Thread(new OutputWriter(this)).start();
//		inQueue.execute(new InputReader(this));
//		inQueue.scheduleWithFixedDelay(new UpdateMessage(this), 0, 1, TimeUnit.MILLISECONDS);

		String user = getProperty("irc.user.name");
		String pass = getProperty("irc.user.pass");
		String nick = getProperty("irc.nick.name");
		String real = getProperty("irc.real.name");
		int mode = Integer.valueOf(getProperty("irc.user.mode"));

		if (!isEmpty(pass)) postMessage(String.format("PASS %s", pass));
		postMessage(String.format("NICK %s", nick));
		postMessage(String.format("USER %s %d * :%s", user, mode, real));
	}

	/**
	 * 接続を継続するために PONG を送信します。
	 * 
	 * @param server サーバ名
	 */
	public void ping(String server)
	{
		postMessage(String.format("PONG :%s", server));
	}

	/**
	 * IRCをログアウトします。
	 * 
	 * @param msg QUITメッセージ
	 */
	public void quit(String msg)
	{
		postMessage(String.format("QUIT :%s", msg));
	}

	/**
	 * メッセージを送信します。
	 * 
	 * @param text メッセージ
	 */
	public void postMessage(String text)
	{
		if (out == null || isEmpty(text)) return;

		System.out.format("[POST] %s\n", text);
//		out.print(str);
//		out.print(CRLF);
		postQueue.execute(new PostMessage(out, text));
	}

	/**
	 * メッセージを受信します。
	 * 
	 * @param text メッセージ
	 */
	public void onMessage(String text)
	{
		IRCMessage msg = createMessage(text);
//		try
//		{
//			text = new String(text.getBytes(), encoding);
//		}
//		catch (UnsupportedEncodingException x)
//		{
//			x.printStackTrace();
//		}
//		System.out.format("[GET][%s] %s\n", msg.getCommand(), text);
//		if (handler != null) handler.onMessage(msg);
		fireHandler(msg.getCommand(), msg);
		fireHandler("ALL", msg);
	}

	private void fireHandler(String command, IRCMessage msg)
	{
		ArrayList<IRCMessageHandler> handlerChain = handlerMap.get(command);
		if (handlerChain == null) return;

		for (IRCMessageHandler handler : handlerChain)
		{
			handler.onMessage(msg);
		}
	}

	/**
	 * メッセージを作成します。
	 * 
	 * @param str 文字列
	 * @return メッセージ
	 */
	public IRCMessage createMessage(String str)
	{
		IRCMessage msg = IRCMessage.valueOf(str, this);
		String encoding = getProperty("irc.encoding");
		msg.setEncoding(encoding);
		return msg;
	}

	/**
	 * IRCサーバーからの入力を読み込みます。
	 */
	private static class PollMessage implements Runnable
	{
		private IRCClient irc;

//		private int count;

		private PollMessage(IRCClient irc)
		{
			this.irc = irc;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(irc.in));
			while (true)
			{
				try
				{
//					System.out.println("count: " + (++count));
//					Thread.sleep(20);
					String str = input.readLine();
					if (str == null) break;

					irc.onMessage(str);
//					Thread.yield();
				}
				catch (IOException x)
				{
					x.printStackTrace();
				}
//				catch (InterruptedException x)
//				{
//					x.printStackTrace();
//				}
			}
			try
			{
				input.close();
				irc.in.close();
//				irc.out.close();
			}
			catch (IOException x)
			{
				x.printStackTrace();
			}
		}
	}

	/**
	 * IRCサーバーにメッセージを出力します。
	 */
	private static class PostMessage implements Runnable
	{
		/** 出力ストリーム */
		private PrintStream out;

		/** テキスト */
		private String text;

		/**
		 * @param out 出力ストリーム
		 * @param text テキスト
		 */
		private PostMessage(PrintStream out, String text)
		{
			this.out = out;
			this.text = text;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
//			System.out.println("[QUEUE] " + Thread.currentThread());
//			listThread();
			out.print(text);
			out.print(CRLF);
		}
	}

	/**
	 * listThread
	 */
	public static void listThread()
	{
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		while (true)
		{
			ThreadGroup parent = group.getParent();
			if (parent == null)
			{
				break;
			}
			else
			{
				group = parent;
			}
		}
		group.list();
	}
}
