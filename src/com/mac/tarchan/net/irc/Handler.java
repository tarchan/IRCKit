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
package com.mac.tarchan.net.irc;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.net.URLStreamHandler;

/**
 * IRC接続のプロトコルハンドラです。
 * 
 * @author tarchan
 * @see URL#URL(String, String, int, String)
 */
public class Handler extends URLStreamHandler
{
	/**
	 * IRC接続をオープンします。
	 * 
	 * @see URLStreamHandler#openConnection(java.net.URL)
	 * @see IRCConnection
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException
	{
		return new IRCConnection(url);
	}

	/**
	 * IRC接続のデフォルトのポート番号を返します。
	 * 
	 * @see URLStreamHandler#getDefaultPort()
	 */
	@Override
	protected int getDefaultPort()
	{
		return 6667;
	}
}
