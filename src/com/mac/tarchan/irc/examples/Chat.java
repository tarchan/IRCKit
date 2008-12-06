/*
 * Chat.java
 * IRCKit
 *
 * Created by tarchan on 2008/12/06.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.examples;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCMessage;

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
	protected Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

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
				if (!str.isEmpty()) printLine(str);
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
	}

	/**
	 * 指定されたテキストを表示します。
	 * 
	 * @param line テキスト
	 */
	public void printLine(final String line)
	{
//		SwingUtilities.invokeLater(new Runnable()
//		{
//			public void run()
			{
				try
				{
					doc.insertString(doc.getLength(), line + NL, style);
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
//		});
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Object doInBackground() throws Exception
	{
		System.out.println("doInBackground");
		String encoding = "ISO-2022-JP";
		final IRCClient irc = new IRCClient();
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
//					irc.privmsg("tokyo", "#javabreak", str);
				}
			}
		});

		while (true)
		{
			String line = irc.readLine("tokyo");
			if (line == null) break;

			IRCMessage reply = new IRCMessage(irc, line);
//			reply.setEncoding(encoding);
			publish(reply);
		}

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
			printLine(reply.getMessage("ISO-2022-JP"));
		}
	}
}
