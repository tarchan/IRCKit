/*
 * Chat.java
 * IRCKit
 *
 * Created by tarchan on 2008/12/06.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCMessage;
import com.mac.tarchan.irc.client.IRCMessageAdapter;
import com.mac.tarchan.irc.client.IRCMessageListener;

/**
 * Chat
 */
public class Chat extends SwingWorker<Object, IRCMessage>
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final Chat chat = new Chat();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				chat.createAndShowWindow();
				chat.setStyle("system");
				chat.printLine("Welcome to IRCKit!");
			}
		});
		chat.execute();
		System.out.println("done.");
	}

	/** 改行コード */
	protected static final String NL = System.getProperty("line.separator");

	/** 表示エリア */
	protected JTextPane textPane;

	/** ドキュメント */
	protected StyledDocument doc;

	/** 表示スタイル */
//	protected Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

	/** 入力フィールド */
	protected JTextField textField;

	/**
	 * チャットウインドウを表示します。
	 */
	public void createAndShowWindow()
	{
		// 表示エリア
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(320, 240));
		doc = textPane.getStyledDocument();

		initStyle(doc);

		// スクロールパネル
		JScrollPane scrollPane = new JScrollPane(textPane);
//		System.out.println("scrollPane=" + scrollPane.getBorder().getBorderInsets(textPane));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2));

//		final JButton sendButton = new JButton("Send");

		// 入力フィールド
		textField = new JTextField();
		textField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				// 入力テキストを取得
				String str = evt.getActionCommand();

				// 入力フィールドをクリア
				textField.setText("");

				// 表示エリアに1行追加
//				System.out.println(str);
				if (!str.isEmpty())
				{
					setStyle("text");
					printLine(str);
				}
			}
		});
		textField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				// 入力テキストを取得
				String str = evt.getActionCommand();

				// メッセージを送信
				System.out.println(str);
				if (!str.isEmpty())
				{
					putCommand(str);
				}
			}
		});

		// 入力パネル
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.LINE_AXIS));
		inputPane.add(Box.createHorizontalGlue());
		inputPane.add(textField);
//		inputPane.add(sendButton);
//		inputPane.add(Box.createHorizontalStrut(11));
		inputPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 11));
//		System.out.println("inputPanel: " + inputPane.getBorder());

		// メインパネル
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.add(scrollPane, BorderLayout.CENTER);
		mainPane.add(inputPane, BorderLayout.SOUTH);

		// ウインドウ
		JFrame frame = new JFrame("チャット");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainPane);
		frame.pack();
		frame.setVisible(true);

		// フォーカスを設定
		textField.requestFocusInWindow();
	}

	/**
	 * ドキュメントのスタルを初期化します。
	 * 
	 * @param doc ドキュメント
	 */
	protected void initStyle(StyledDocument doc)
	{
//		String fontFamily = "Monaco";
//		int fontSize = 9;
		String fontFamily = "Lucida Grande";
		int fontSize = 12;
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		// 標準テキスト
		Style text = doc.addStyle("text", def);
		StyleConstants.setFontFamily(text, fontFamily);
		StyleConstants.setFontSize(text, fontSize);

		// システムテキスト
		Style system = doc.addStyle("system", text);
		StyleConstants.setForeground(system, Color.GREEN.darker());
		StyleConstants.setItalic(system, true);

		// エラーテキスト
		Style error = doc.addStyle("error", text);
		StyleConstants.setForeground(error, Color.RED);
		StyleConstants.setBold(error, true);
	}

	/**
	 * 指定されたスタイルを表示エリアに設定します。
	 * 
	 * @param styleName スタイル
	 */
	public void setStyle(final String styleName)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				doc.setLogicalStyle(doc.getLength(), doc.getStyle(styleName));
			}
		});
	}

	/**
	 * 指定されたテキストを表示します。
	 * 
	 * @param line テキスト
	 */
	public void printLine(final String line)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					System.out.println("print: " + Thread.currentThread() + ": " + line);
					doc.insertString(doc.getLength(), line + NL, null);
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/** IRCクライアント */
	protected IRCClient irc = new IRCClient();

	/**
	 * コマンドを送信します。
	 * 
	 * @param str IRCコマンド
	 */
	public void putCommand(final String str)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("put: " + Thread.currentThread() + ": " + str);
				synchronized (irc)
				{
					irc.privmsg("tokyo", "#javabreak", str);
				}
			}
		});
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Object doInBackground() throws Exception
	{
		System.out.println("doInBackground");
		setStyle("error");
		printLine("Hello");
		final String encoding = "ISO-2022-JP";
//		final IRCClient irc = new IRCClient();
		irc.setUseSystemProxies(true);
		Properties prof = irc.createDefaultProperties();
		prof.setProperty("irc.real.name", "たーちゃん");
		prof.setProperty("irc.encoding", encoding);
		prof.list(System.out);
		irc.registerNetwork("tokyo", "irc://irc.tokyo.wide.ad.jp:6667");
//		irc.registerNetwork("tokyo", "irc://irc.mozilla.org:6667");
		irc.login("tokyo", prof);
		irc.join("tokyo", "#javabreak", "");
		irc.privmsg("tokyo", "#javabreak", "テスト");

		setStyle("error");
		printLine("Hello");

		setStyle("error");
		printLine("Hello");
		irc.registerHandler(new IRCMessageAdapter());
//		irc.registerHandler(new ChatAdapter(this));
//		irc.registerHandler(handler);
//		irc.registerHandler(new IRCMessageAdapter()
//		{
//			/** エンコーディング */
//			protected String encoding = "ISO-2022-JP";
//
//			/**
//			 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#privmsg(com.mac.tarchan.irc.client.IRCMessage)
//			 */
//			@Override
//			public void privmsg(IRCMessage reply)
//			{
//				// メッセージを表示
//				Date date = new Date(reply.getWhen());
//				String nick = reply.getNick();
//				String text = reply.getTrailing(encoding);
//				String str = String.format("%tR (%s) %s", date, nick, text);
//				printLine(str);
//			}
//
//			/**
//			 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#welcome(com.mac.tarchan.irc.client.IRCMessage)
//			 */
//			@Override
//			public void welcome(IRCMessage reply)
//			{
//				printLine(reply.getTrailing(encoding));
//			}
//			
//		});

		while (true)
		{
			IRCMessage reply = null;
			synchronized (irc)
			{
				reply = irc.get("tokyo");
			}
			if (reply == null) break;

//			reply.setEncoding(encoding);
			publish(reply);
		}

		System.out.println("quit");
		irc.quit();

		return null;
	}

	/**
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<IRCMessage> chunks)
	{
		System.out.println("process");
		for (IRCMessage reply : chunks)
		{
//			System.out.println();
//			setStyle("text");
//			printLine(reply.getMessage("ISO-2022-JP"));
			irc.reply(reply);
		}
	}

	/** リプライメッセージハンドラ */
	static class ChatAdapter extends IRCMessageAdapter
	{
		/** チャット */
		protected Chat chat;

		/** エンコーディング */
		protected String encoding = "ISO-2022-JP";

		/**
		 * チャットアダプタ
		 * 
		 * @param chat チャット
		 */
		public ChatAdapter(Chat chat)
		{
			this.chat = chat;
		}

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#privmsg(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void privmsg(IRCMessage reply)
		{
			// メッセージを表示
			Date date = new Date(reply.getWhen());
			String nick = reply.getNick();
			String text = reply.getTrailing(encoding);
			String str = String.format("%tR (%s) %s", date, nick, text);
			chat.printLine(str);
		}

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#welcome(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void welcome(IRCMessage reply)
		{
			chat.printLine(reply.getTrailing(encoding));
		}

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#ping(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void ping(IRCMessage reply)
		{
			super.ping(reply);
			String trail = reply.getTrailing();
			String pong = String.format("PONG %s", trail);
			chat.setStyle("system");
			chat.printLine(pong);
		}
	}

	/** リプライメッセージハンドラ */
	protected IRCMessageListener handler = new IRCMessageAdapter()
	{
		protected String encoding = "ISO-2022-JP";

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#privmsg(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void privmsg(IRCMessage reply)
		{
			// メッセージを表示
			Date date = new Date(reply.getWhen());
			String nick = reply.getNick();
			String text = reply.getTrailing(encoding);
			String str = String.format("%tR (%s) %s", date, nick, text);
			printLine(str);
		}

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#welcome(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void welcome(IRCMessage reply)
		{
			printLine(reply.getTrailing(encoding));
		}

		/**
		 * @see com.mac.tarchan.irc.client.IRCMessageAdapter#ping(com.mac.tarchan.irc.client.IRCMessage)
		 */
		@Override
		public void ping(IRCMessage reply)
		{
			super.ping(reply);
			String trail = reply.getTrailing();
			String pong = String.format("PONG %s", trail);
			setStyle("system");
			printLine(pong);
		}
	};
}
