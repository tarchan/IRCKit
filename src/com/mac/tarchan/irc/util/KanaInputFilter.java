/*
 * KanaInputFilter.java
 * IRCKit
 *
 * Created by tarchan on 2011/06/17.
 * Copyright (c) 2011 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mac.tarchan.irc.IRCClient;

/**
 * 入力ストリームに、半角カナを修正する機能を追加します。
 * 入力ストリームの文字コードが JIS ではない場合は、使用できません。
 * 
 * @see IRCClient
 */
public class KanaInputFilter extends FilterInputStream
{
	/**
	 * KanaInputFilter を構築します。
	 * 
	 * @param in 入力ストリーム
	 */
	public KanaInputFilter(InputStream in)
	{
		super(in);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
//		System.out.printf("readRange%d:%d:%d%n", off, len, b.length);
		int eof = super.read(b, off, len);
		shiftKana(b, off, len);
		return eof;
	}

//	/** 半角カナにマッチする正規表現 */
//	private static Pattern KANA = Pattern.compile("(\\x1b\\(J)(.*?)(\\x1b(\\(B|\\(J|\\$@|\\$B))");
//	private static Pattern KANA = Pattern.compile("(\\x1b\\x28\\x4a)([\\xa1-\\xdf]*)(\\x1b\\x28\\x42)");
//	private static Pattern KANA = Pattern.compile("(?<=\\x1b\\(J)(.*)");

	/** エスケープコード */
	protected static final byte ESC = 0x1b;

	/**
	 * 	指定されたバイト配列の半角カナを修正します。
	 * 
	 * @param data バイト配列
	 * @param off データの開始位置
	 * @param len データの長さ
	 * @throws IOException 不正なデータの場合
	 * @see <a href="http://d.hatena.ne.jp/tarchan/20070308">Windowsから送られてくる半角カナを修正する。</a>
	 */
	void shiftKana(byte[] data, int off, int len) throws IOException
	{
		boolean shiftKana = false;
		int startKana = 0;
		for (int i = 0; i < data.length; i++)
		{
			byte b0 = data[i];
			if (b0 == ESC)
			{
				startKana = i;
				// 「[ESC] ( J」かどうか判定
				if (i + 2 < data.length && data[i + 1] == '(' && data[i + 2] == 'J')
				{
					// 「[ESC] ( I」に修正
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
			if (shiftKana)
			{
				if ((b0 & 0x80) != 0)
				{
					b0 = (byte)(b0 - 0x80);
					data[i] = b0;
				}
			}
			if ((b0 & 0x80) != 0)
			{
				int endKana = data.length;
				for (int j = i; j < data.length; j++)
				{
					if (data[j] == ESC)
					{
						endKana = j;
						break;
					}
				}
				String str = new String(data, startKana, endKana - startKana, "JIS");
//				throw new IOException(String.format("不正な文字です。: %s (%02X)", str, b0));
				System.err.println(String.format("不正な文字です。: %s (%02X)", str, b0));
				data[i] = (byte)(b0 - 0x80);
			}
		}
//		System.out.println();
	}
}
