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
 * IRCクライアントを実装します。
 * 
 * @author tarchan
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
			setProperty("irc.user.name", System.getProperty("user.name", ""));
			setProperty("irc.user.pass", "");
			setProperty("irc.user.mode", "12");
			setProperty("irc.user.icon", "");
			setProperty("irc.nick.name", System.getProperty("user.name", ""));
			setProperty("irc.real.name", "IRCKit 0.2");
		}
	};

	/** 環境設定 */
	private Properties props = new Properties(DEFAULTS);

	/** 改行コード */
	private static final String CRLF = "\r\n";

	/** 入力ストリーム */
	private InputStream in;

	/** 出力ストリーム */
	private PrintStream out;

	/** メッセージキュー */
	private ExecutorService postQueue = Executors.newSingleThreadExecutor();

	/** メッセージハンドラ */
	private HashMap<String, ArrayList<IRCMessageHandler>> handlerMap = new HashMap<String, ArrayList<IRCMessageHandler>>();

	static
	{
		// IRCプロトコルハンドラを設定
		setProtocolHandlerPackage("com.mac.tarchan");

		// システムプロキシーを使用
		setUseSystemProxies(true);
	}

	/**
	 * IRCクライアントを構築します。
	 */
	public IRCClient()
	{
		addAllHandlers(new PingPong());
		addAllHandlers(new AutoJoin());
		addAllHandlers(new AutoNick());
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
	 * @see URL#URL(String, String, int, String)
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
	 * @return このオブジェクト
	 * @throws IOException 入力エラーが発生した場合
	 * @see Properties#load(InputStream)
	 * @see Properties#loadFromXML(InputStream)
	 */
	public IRCClient load(String name) throws IOException
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

		return this;
	}

	/**
	 * 指定されたキーのプロパティーを設定します。
	 * 
	 * @param key キー
	 * @param value プロパティー
	 * @return このオブジェクト
	 */
	public IRCClient setProperty(String key, String value)
	{
		props.setProperty(key, value);
		return this;
	}

	/**
	 * ニックネームを設定します。
	 * 
	 * @param nickname ニックネーム
	 * @return このオブジェクト
	 */
	public IRCClient setNickname(String nickname)
	{
		props.setProperty("irc.nick.name", nickname);
		return this;
	}

	/**
	 * 文字コードを設定します。
	 * 
	 * @param encoding 文字コード
	 * @return このオブジェクト
	 */
	public IRCClient setEncoding(String encoding)
	{
		props.setProperty("irc.encoding", encoding);
		return this;
	}

	/**
	 * IRCに接続します。
	 * 
	 * @param host ホスト
	 * @param port ポート
	 * @return このオブジェクト
	 * @throws IOException 接続できない場合
	 */
	public IRCClient open(String host, int port) throws IOException
	{
		props.setProperty("irc.host", host);
		props.setProperty("irc.port", String.valueOf(port));
		open();
		return this;
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
	 * 環境設定を返します。
	 * 
	 * @return 環境設定
	 */
	public Properties getProperties()
	{
		return props;
	}

	/**
	 * メッセージハンドラを追加します。
	 * 
	 * @param command コマンド
	 * @param handler メッセージハンドラ
	 * @return このオブジェクト
	 */
	public IRCClient addHandler(String command, IRCMessageHandler handler)
	{
		command = command.toUpperCase();
		ArrayList<IRCMessageHandler> handlerChain = handlerMap.get(command);
		if (handlerChain == null)
		{
			handlerChain = new ArrayList<IRCMessageHandler>();
			handlerMap.put(command, handlerChain);
		}
		handlerChain.add(handler);
		return this;
	}

	/**
	 * 注釈されたメッセージハンドラをすべて追加します。
	 * 
	 * @param obj オブジェクト
	 * @return このオブジェクト
	 * @see Reply
	 */
	public IRCClient addAllHandlers(Object obj)
	{
		ArrayList<IRCMessageHandler> handlerChain = new ArrayList<IRCMessageHandler>();
		for (Method m : obj.getClass().getMethods())
		{
			if (m.isAnnotationPresent(Reply.class))
			{
				Reply reply = m.getAnnotation(Reply.class);
//				System.out.format("method: %s: %s: %s\n", m, reply, m.getName());
				IRCMessageHandler handler = EventHandler.create(IRCMessageHandler.class, obj, m.getName(), "");
				addHandler(reply.value(), handler);
				handlerChain.add(handler);
//				System.out.format("handler: %s: %s\n", reply.value(), handler.getClass());
			}
		}
		return this;
	}

	/**
	 * メッセージハンドラを削除します。
	 * 
	 * @param command コマンド
	 * @param handler メッセージハンドラ
	 */
	public void removeHandler(String command, IRCMessageHandler handler)
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
	 * すべてのメッセージハンドラを削除します。
	 * 
	 * @param handler メッセージハンドラ
	 */
	public void removeAllHandlers(IRCMessageHandler handler)
	{
		for (String command : handlerMap.keySet())
		{
			removeHandler(command, handler);
		}
	}

	/**
	 * IRCサーバーにログインします。
	 * 
	 * @throws IOException 入出力エラーが発生した場合
	 */
	public void open() throws IOException
	{
		String href = getProperty("irc.url");
		String host = getProperty("irc.host");
		String port = getProperty("irc.port");
//		System.out.format("[OPEN] %s\n", href);
//		System.out.format("[OPEN] %s\n", host);
//		System.out.format("[OPEN] %s\n", port);
		if (isEmpty(href)) href = String.format("irc://%s:%s", host, port);
//		System.out.format("[OPEN] %s\n", href);
		URL url = new URL(href);
		String channel = url.getPath();
//		System.out.format("[OPEN] ref=%s\n", ref);
		if (!isEmpty(channel)) setProperty("irc.channel", channel);
//		System.out.format("[OPEN] %s\n", url);
		URLConnection con = url.openConnection();
		con.connect();
//		System.out.format("[OPEN] %s\n", con);

		String encoding = getProperty("irc.encoding");
		in = con.getInputStream();
		if (!isEmpty(encoding))
		{
			out = new PrintStream(con.getOutputStream(), true, encoding);
		}
		else
		{
			out = new PrintStream(con.getOutputStream(), true);
		}
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
	 * IRCサーバーをログアウトします。
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

//		System.out.format("[POST] %s\n", text);
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
		fireHandler(msg.getCommand(), msg);
		fireHandler("ALL", msg);
	}

	/**
	 * メッセージハンドラを呼び出します。
	 * 
	 * @param command コマンド
	 * @param msg IRCメッセージ
	 */
	protected void fireHandler(String command, IRCMessage msg)
	{
		ArrayList<IRCMessageHandler> handlerChain = handlerMap.get(command);
		if (handlerChain == null) return;

		for (IRCMessageHandler handler : handlerChain)
		{
			try
			{
				handler.onMessage(msg);
			}
			catch (Exception x)
			{
				x.printStackTrace();
			}
		}
	}

	/**
	 * メッセージを作成します。
	 * 
	 * @param str 文字列
	 * @return メッセージ
	 */
	protected IRCMessage createMessage(String str)
	{
		IRCMessage msg = IRCMessage.valueOf(str, this);
		String encoding = getProperty("irc.encoding");
		msg.setEncoding(encoding);
		return msg;
	}

	/**
	 * デバッグのために現在のスレッドを表示します。
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

	/**
	 * IRCサーバーからの入力をポーリングします。
	 */
	private static class PollMessage implements Runnable
	{
		private IRCClient irc;

		private BufferedReader in;

		public PollMessage(IRCClient irc)
		{
			this.irc = irc;
			this.in = new BufferedReader(new InputStreamReader(irc.in));
		}

		public void run()
		{
			while (true)
			{
				try
				{
					String str = in.readLine();
					if (str == null) break;

					irc.onMessage(str);
					Thread.yield();
				}
				catch (IOException x)
				{
					error(x);
					break;
				}
			}
			System.err.println("Disconnected.");
		}

		public void error(Exception x)
		{
			x.printStackTrace();
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
	 * 接続を継続するために PONG を送信します。
	 */
	public static class PingPong
	{
		/**
		 * 接続を継続するために PONG を送信します。
		 * 
		 * @param msg IRCメッセージ
		 */
		@Reply("PING")
		public void onPing(IRCMessage msg)
		{
			IRCClient irc = (IRCClient)msg.getSource();
			String server = msg.getTrail();
			irc.postMessage(String.format("PONG :%s", server));
		}
	}

	/**
	 * IRCサーバーにログインしたら、自動的に JOIN します。
	 */
	public static class AutoJoin
	{
		/**
		 * 指定されたチャンネルに JOIN します。
		 * 
		 * @param msg IRCメッセージ
		 */
		@Reply("001")
		public void onWelcome(IRCMessage msg)
		{
			IRCClient irc = (IRCClient)msg.getSource();
			String channel = irc.getProperty("irc.channel");
			if (!isEmpty(channel))
			{
				irc.postMessage(String.format("JOIN %s", channel));
			}
		}

		/**
		 * ニックネームを変更します。
		 * 
		 * @param msg IRCメッセージ
		 */
		@Reply("433")
		public void onNicknameInUse(IRCMessage msg)
		{
			IRCClient irc = (IRCClient)msg.getSource();
			String oldNick = msg.getParam(1);
			String newNick = String.format("%s_", oldNick).substring(1);
//			System.out.format("nick=%s -> %s\n", oldNick, newNick);
			irc.postMessage(String.format("NICK %s", newNick));
		}
	}

	/**
	 * ニックネームの変更を追跡します。
	 */
	public static class AutoNick
	{
		/**
		 * ログインしたときのニックネームを取得します。
		 * 
		 * @param msg IRCメッセージ
		 */
		@Reply("001")
		public void onWelcome(IRCMessage msg)
		{
			IRCClient irc = msg.getClient();
			System.out.println("WELCOME: " + msg.toString());
			String nick = msg.getParam(0);
			irc.setProperty("irc.nick.name", nick);
		}

		/**
		 * 変更したニックネームを取得します。
		 * 
		 * @param msg IRCメッセージ
		 */
		@Reply("NICK")
		public void onNick(IRCMessage msg)
		{
			IRCClient irc = msg.getClient();
			System.out.println("NICK: " + msg.toString());
			String oldNick = msg.getPrefix().getNick();
			String newNick = msg.getTrail();
			System.out.println(String.format("NICK: %s -> %s", oldNick, newNick));
			if (irc.getProperty("irc.nick.name").equals(oldNick))
			{
				irc.setProperty("irc.nick.name", newNick);
			}
		}
	}
}
