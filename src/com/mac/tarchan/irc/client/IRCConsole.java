/*
 * IRCConsole.java
 * IRCKit
 *
 * Created by tarchan on 2008/12/02.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Flushable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Formatter;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * IRCConsole
 * 
 * @author tarchan
 */
public class IRCConsole implements Appendable, Flushable
{
	/**
	 * チャットコンソールを起動します。
	 * 
	 * @param args 引数
	 */
	public static void main(String[] args)
	{
		IRCConsole console = new IRCConsole();
		console.createChatWindow();
		console.test();
	}

	/** 改行コード */
	protected static final String NL = System.getProperty("line.separator");

	/** 表示エリア */
	protected JTextPane textPane;

	/** ドキュメント */
	protected StyledDocument doc;

	/** 表示スタイル */
	protected Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

	/**
	 * チャットウインドウを作成します。
	 * 
	 * @return チャットウインドウのインスタンス
	 */
	public Window createChatWindow()
	{
		// 表示エリア
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(320, 240));
		doc = textPane.getStyledDocument();

		// スクロールパネル
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2));

//		final JButton sendButton = new JButton("Send");

		// 入力フィールド
		final JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				// 入力テキストを取得
				String str = evt.getActionCommand();

				// 入力フィールドをクリア
				textField.setText("");

				// 表示エリアに1行追加
				if (!str.isEmpty()) putCommand(str);
			}
		});

		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.LINE_AXIS));
		inputPane.add(Box.createHorizontalGlue());
		inputPane.add(textField);
//		inputPane.add(sendButton);
		inputPane.add(Box.createHorizontalStrut(11));

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

		return frame;
	}

	/**
	 * 指定された文字列を表示エリアに追加します。
	 * 
	 * @param str 文字列
	 */
	public void appendLine(String str)
	{
		try
		{
			append(str + NL);
			flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * コマンドを送信します。
	 * 
	 * @param command コマンド
	 */
	public void putCommand(String command)
	{
		// 表示エリアに追加
		appendLine(command);

		// IRC ネットワークに送信
		if (command.startsWith("/"))
		{
			// コマンド
			irc.put("tokyo", command.substring(1));
		}
		else
		{
			// メッセージ送信
		}
	}

	/** IRC クライアント */
	protected IRCClient irc;

	/**
	 * IRCKitの接続テスト
	 */
	public void test()
	{
		final Formatter formatter = new Formatter(this);
		formatter.format("Welcome to IRCKit!" + NL);
		try
		{
			irc = new IRCClient();
			irc.setUseSystemProxies(true);
			irc.registerHandler(new IRCMessageHandler()
			{
				public void reply(IRCMessage msg)
				{
					// TODO メッセージを表示
					String encoding = "ISO-2022-JP";
					String nick = msg.getNick();
					String text = msg.getTrailing(encoding);
					formatter.format("(%s) %s" + NL, nick, text);
					flush();
				}

				public void error(Exception e)
				{
					// エラーを表示
					e.printStackTrace();
				}
			});
			Properties prof = irc.createDefaultProperties();
			prof.setProperty("irc.real.name", "たーちゃん");
			prof.setProperty("irc.encoding", "ISO-2022-JP");
			prof.list(System.out);
			irc.registerNetwork("tokyo", "irc://irc.tokyo.wide.ad.jp:6667");
//			irc.registerNetwork("tokyo", "irc://irc.mozilla.org:6667");
			irc.login("tokyo", prof);
			irc.join("tokyo", "#javabreak", "");
			irc.privmsg("tokyo", "#javabreak", "テスト");
			irc.quit();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see java.lang.Appendable#append(java.lang.CharSequence)
	 */
	public Appendable append(CharSequence csq) throws IOException
	{
		try
		{
			doc.insertString(doc.getLength(), csq.toString(), style);
		}
		catch (BadLocationException e)
		{
			throw new IOException(e);
		}

		return this;
	}

	/**
	 * @see java.lang.Appendable#append(char)
	 */
	public Appendable append(char c) throws IOException
	{
		return append(String.valueOf(c));
	}

	/**
	 * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
	 */
	public Appendable append(CharSequence csq, int start, int end) throws IOException
	{
		return append(csq.subSequence(start, end));
	}

	/**
	 * @see java.io.Flushable#flush()
	 */
	public void flush()
	{
		textPane.setCaretPosition(doc.getLength());
	}
}
