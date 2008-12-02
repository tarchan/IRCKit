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
import java.net.MalformedURLException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 * IRCConsole
 * 
 * @author tarchan
 */
public class IRCConsole
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		IRCConsole console = new IRCConsole();
		console.createChatWindow();
//		console.test();
	}

	/**
	 * チャットウインドウを作成します。
	 * 
	 * @return チャットウインドウのインスタンス
	 */
	public Window createChatWindow()
	{
		// 表示エリア
		final JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(320, 240));
		final StyledDocument doc = textPane.getStyledDocument();
		final String LF = System.getProperty("line.separator");

		// スクロールパネル
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// 入力フィールド
		final JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					// 入力テキストを取得
					String str = evt.getActionCommand();

					// 入力フィールドをクリア
					textField.setText("");

					// 表示エリアに1行追加
					AttributeSet style = null;
					doc.insertString(doc.getLength(), str + LF, style);
//					textPane.setCaretPosition(doc.getLength());
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
		});

		// メインパネル
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.add(scrollPane, BorderLayout.CENTER);
		mainPane.add(textField, BorderLayout.SOUTH);

		// ウインドウ
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainPane);
		frame.pack();
		frame.setVisible(true);

		return frame;
	}

	/**
	 * IRCKitの接続テスト
	 */
	public void test()
	{
		System.out.println("Welcome to IRCKit!");
		try
		{
			IRCClient irc = new IRCClient();
			irc.setUseSystemProxies(true);
			irc.registerHandler(new IRCMessageHandler()
			{
				/**
				 * 受信したメッセージを表示します。
				 * 
				 * @param msg メッセージ
				 */
				public void privmsg(IRCMessage msg)
				{
					// TODO 受信したメッセージを表示
				}
			});
			Properties prof = irc.createDefaultProperties();
			prof.setProperty("irc.real.name", "たーちゃん");
			prof.list(System.out);
			irc.registerNetwork("tokyo", "irc://irc.tokyo.wide.ad.jp:6667", "tarchan", "");
//			irc.registerNetwork("tokyo", "http://irc.mozilla.org:6667", "tarchan", "");
			irc.join("tokyo", "#dameTunes", "");
			irc.privmsg("tokyo", "#dameTunes", "テスト");
			irc.quit();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
}
