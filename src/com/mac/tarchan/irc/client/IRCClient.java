/*
 * IRCClient.java
 * IRCKit
 * 
 * Created by tarchan on 2008/11/27.
 * Copyright (c) 2008 tarchan. All rights reserved.
 */
package com.mac.tarchan.irc.client;

import com.mac.tarchan.irc.client.util.KanaInputFilter;
import java.awt.EventQueue;
import java.beans.EventHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IRCクライアントを実装します。
 *
 * @see <a href="http://www.irchelp.org/irchelp/rfc/rfc.html">RFC 1459 - Internet Relay Chat Protocol</a>
 * @see <a href="http://www.haun.org/kent/lib/rfc1459-irc-ja.html">RFC 1459 - 日本語訳</a>
 * @see <a href="http://www.faqs.org/rfcs/rfc2812.html">RFC 2812 - Internet Relay Chat: Client Protocol</a>
 * @see <a href="http://jbpe.tripod.com/rfcj/rfc2812.j.sjis.txt">RFC 2812 - 日本語訳</a>
 */
public class IRCClient {

    public static final int UserModeNode = 0;
    public static final int UserModeInvisible = 4;
    public static final int UserModeWallops = 8;
    /**
     * ログ
     */
    private static final Logger log = Logger.getLogger(IRCClient.class.getName());
    /**
     * ホスト名
     */
    protected String host;
    /**
     * ポート番号
     */
    protected int port;
    /**
     * ニックネーム
     */
    protected String nick;
    /**
     * パスワード
     */
    protected String pass;
    /**
     * 接続モード (if the bit 2 is set, the user mode 'w' will be set and if the bit 3 is set, the user mode 'i' will be
     * set.)
     */
    protected int mode;
    /**
     * 文字コード
     */
    protected String encoding;
    /**
     * 入出力ソケット
     */
    protected Socket socket;
    /**
     * タスクキュー
     */
    protected ExecutorService taskQueue = Executors.newFixedThreadPool(2);
    /**
     * メッセージハンドラ
     */
    protected ArrayList<IRCHandler> handlers = new ArrayList<IRCHandler>();

    protected IRCClient() {
    }

    /**
     * IRCClient を構築します。
     *
     * @param host ホスト名
     * @param port ポート番号
     * @param nick ニックネーム
     * @param pass パスワード
     * @param encoding 文字コード
     */
    protected IRCClient(String host, int port, String nick, String pass, String encoding) {
        this.host = host;
        this.port = port;
        this.nick = nick;
        this.pass = pass;
        this.encoding = encoding;
    }

    public static IRCClient createClient(Object handler) {
        IRCClient irc = new IRCClient();
        irc.addHandler(handler);
        return irc;
    }

    /**
     * IRCClient を作成します。
     *
     * @param host ホスト名
     * @param port ポート番号
     * @param nick ニックネーム
     * @return IRCClient
     */
    public static IRCClient createClient(String host, int port, String nick) {
        return createClient(host, port, nick, null);
    }

    /**
     * IRCClient を作成します。
     *
     * @param host ホスト名
     * @param port ポート番号
     * @param nick ニックネーム
     * @param pass パスワード
     * @return IRCクライアント
     */
    public static IRCClient createClient(String host, int port, String nick, String pass) {
        return createClient(host, port, nick, pass, "JIS");
    }

    /**
     * IRCClient を作成します。
     *
     * @param host ホスト名
     * @param port ポート番号
     * @param nick ニックネーム
     * @param pass パスワード
     * @param encoding 文字コード
     * @return IRCクライアント
     */
    public static IRCClient createClient(String host, int port, String nick, String pass, String encoding) {
        return new IRCClient(host, port, nick, pass, encoding);
    }

    /**
     * ホスト名を返します。
     *
     * @return ホスト名
     */
    public String getHost() {
        return host;
    }

    /**
     * ポート番号を返します。
     *
     * @return ポート番号
     */
    public int getPort() {
        return port;
    }

    /**
     * ニックネームを返します。
     *
     * @return ニックネーム
     */
    public String getUserNick() {
        return nick;
    }

    /**
     * ユーザのニックネームを設定します。
     *
     * @param nick ニックネーム
     * @return IRCクライアント
     */
    public IRCClient setUserNick(String nick) {
        this.nick = nick;
        return this;
    }

    /**
     * 文字コードを返します。
     *
     * @return 文字コード
     */
    public String getEncoding() {
        return encoding;
    }

    public void addHandler(Object target) {
//        final Object target = handler;
        Class clazz = target.getClass();
//	dump("クラス", clazz.getDeclaredAnnotations());
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
//	    dump("メソッド", m.getDeclaredAnnotations());
            Reply reply = method.getAnnotation(Reply.class);
//	    if (a != null) dump("メソッド", new Annotation[]{a});
            if (reply != null) {
//		System.out.format("メソッド: %s, %s, %s%n", irc, irc.annotationType(), irc.value());
                log.log(Level.INFO, "イベントハンドラを追加します。: {0} [{1}]", new Object[]{reply, method});
//                final Method mf = m;
                if (reply.value().length == 0) {
                    IRCHandler handler = EventHandler.create(IRCHandler.class, target, method.getName(), reply.property());
                    on(handler);
                }
                for (String cmd : reply.value()) {
                    IRCHandler handler = EventHandler.create(IRCHandler.class, target, method.getName(), reply.property());
                    on(cmd, handler);
                }
            }
        }
    }

    /**
     * すべてのコマンドを受け入れるメッセージハンドラを追加します。
     *
     * @param handler メッセージハンドラ
     * @return IRCクライアント
     */
    public IRCClient on(IRCHandler handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * 指定されたフィルタに含まれるメッセージハンドラを追加します。
     *
     * @param filter フィルタ
     * @param handler メッセージハンドラ
     * @return IRCクライアント
     */
    public IRCClient on(final IRCMessageFilter filter, final IRCHandler handler) {
        return on(new IRCHandler() {
            public void onMessage(IRCEvent event) {
                if (filter.accept(event.getMessage())) {
                    handler.onMessage(event);
                }
            }
        });
    }

    /**
     * 指定されたコマンドのメッセージハンドラを追加します。
     *
     * @param command コマンド
     * @param handler メッセージハンドラ
     * @return IRCクライアント
     */
    public IRCClient on(String command, IRCHandler handler) {
        final String _command = command.toUpperCase();
        final IRCMessageFilter filter = new IRCMessageFilter() {
            @Override
            public boolean accept(IRCMessage message) {
                return message.getCommand().equals(_command);
            }
        };
        return on(filter, handler);
    }

    /**
     * IRCサーバに接続します。
     *
     * @return IRCクライアント
     * @throws IOException IRCサーバに接続できない場合
     */
    public IRCClient start() throws IOException {
        return connect(host, port).login(nick, nick, nick, mode, pass).start(encoding);
    }

    /**
     * 入力ストリームをオープンして、イベントループを開始します。
     *
     * @param encoding 文字コード
     * @return IRCクライアント
     * @throws IOException 入力ストリームをオープンできない場合
     * @see #fireMessage(String)
     */
    protected IRCClient start(String encoding) throws IOException {
        log.info("イベントループを開始します。");
        this.encoding = encoding;
        taskQueue.execute(new InputTask(this, encoding));
        return this;
    }

    /**
     * IRCサーバに接続します。
     *
     * @param host ホスト名
     * @param port ポート番号
     * @return IRCクライアント
     * @throws IOException IRCサーバに接続できない場合
     */
    protected IRCClient connect(String host, int port) throws IOException {
        InetAddress inet = InetAddress.getByName(host);
        socket = new Socket(host, port);
        log.log(Level.INFO, "接続します。: {0}", inet);
//		new Thread(new InputListener(this)).start();
//		messageQueue.execute(new InputTask(this, encoding));
        return this;
    }

    public IRCClient connect(String host, int port, String encoding) throws IOException {
        connect(host, port);
//        login(nick, null, null, mode, pass);
        start(encoding);
        return this;
    }

    /**
     * IRCサーバの接続をクローズします。
     *
     * @return IRCクライアント
     * @throws IOException IRCサーバの接続をクローズできない場合
     */
    public IRCClient close() throws IOException {
        socket.close();
        taskQueue.shutdown();
        log.info("disconnected. " + socket.isConnected() + ", " + socket.isClosed());
        return this;
    }

    /**
     * 接続がクローズしているかどうか判定します。
     *
     * @return 接続がクローズしている場合は true
     */
    public boolean isClosed() {
        return socket != null && socket.isClosed();
    }

    /**
     * 入力ストリームを返します。
     *
     * @return 入力ストリーム
     * @throws IOException 入出力エラーが発生した場合
     */
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * 出力ストリームを返します。
     *
     * @return 出力ストリーム
     * @throws IOException 入出力エラーが発生した場合
     */
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * 指定されたテキストを送信します。
     *
     * @param text テキスト
     * @return IRCクライアント
     */
    public IRCClient postMessage(String text) {
        // TODO タスクキューが落ちてたら再起動する
        if (text != null && text.trim().length() > 0 && !taskQueue.isShutdown()) {
            taskQueue.execute(new OutputTask(this, text));
        }
        return this;
    }

    /**
     * 指定されたコマンドを送信します。
     *
     * @param command コマンド書式
     * @param args コマンド引数
     * @return IRCクライアント
     */
    public IRCClient postMessage(String command, Object... args) {
        return postMessage(String.format(command, args));
    }

    /**
     * IRCネットワークにログインします。
     *
     * @param nick ニックネーム
     * @param user ユーザ名
     * @param real 本名
     * @param mode 接続モード
     * @param pass パスワード
     * @return IRCクライアント
     */
    public IRCClient login(String nick, String user, String real, int mode, String pass) {
        log.log(Level.INFO, "ログインします。: {0}", nick);
        if (pass != null && pass.length() != 0) {
            pass(pass);
        }
        nick(nick);
        user(user, mode, real);
        return this;
    }

    public IRCClient pass(String pass) {
        return postMessage("PASS %s", pass);
    }

    public IRCClient user(String user, int mode, String real) {
        return postMessage("USER %s %d * :%s", user, mode, real);
    }

    /**
     * 指定されたニックネームに変更します。 他のクライアントが使っているニックネームと同一であることが判明した場合は、無視されます。
     *
     * @param nick ニックネーム
     * @return IRCクライアント
     */
    public IRCClient nick(String nick) {
        return postMessage("NICK %s", nick);
    }

    /**
     * クライアントのセッションを、デフォルトメッセージと共に終了します。
     *
     * @return IRCクライアント
     */
    public IRCClient quit() {
        return postMessage("QUIT");
    }

    /**
     * クライアントのセッションを、終了メッセージと共に終了します。
     *
     * @param text 終了メッセージ
     * @return IRCクライアント
     */
    public IRCClient quit(String text) {
        return postMessage("QUIT :%s", text);
    }

    /**
     * 指定されたチャンネルに参加します。
     *
     * @param channel チャンネル名
     * @return IRCクライアント
     */
    public IRCClient join(String channel) {
        return postMessage("JOIN %s", channel);
    }

    /**
     * 指定されたチャンネルに参加します。 キーワードが設定されているチャンネルに入るときは、キーワードを正しく入力しなくてはいけません。
     *
     * @param channel チャンネル名
     * @param keyword キーワード
     * @return IRCクライアント
     */
    public IRCClient join(String channel, String keyword) {
        return postMessage("JOIN %s %s", channel, keyword);
    }

    /**
     * 指定されたチャンネルから離脱します。
     *
     * @param channel チャンネル名
     * @return IRCクライアント
     */
    public IRCClient part(String channel) {
        return postMessage("PART %s", channel);
    }

    /**
     * 指定されたチャンネルから離脱メッセージと共に離脱します。
     *
     * @param channel チャンネル名
     * @param text 離脱メッセージ
     * @return IRCクライアント
     */
    public IRCClient part(String channel, String text) {
        return postMessage("PART %s :%s", channel, text);
    }

    /**
     * 指定されたチャンネル上のユーザモードを変更します。
     *
     * @param channel チャンネル名
     * @param mode モード
     * @param nick ニックネーム
     * @return IRCクライアント
     */
    public IRCClient mode(String channel, String mode, String nick) {
        return postMessage("MODE %s %s %s", channel, mode, nick);
    }

    /**
     * 指定されたチャンネルのトピックを取得します。
     *
     * @param channel チャンネル名
     * @return IRCクライアント
     */
    public IRCClient topic(String channel) {
        return postMessage("TOPIC %s", channel);
    }

    /**
     * 指定されたチャンネルのトピックを変更します。
     *
     * @param channel チャンネル名
     * @param topic トピック
     * @return IRCクライアント
     */
    public IRCClient topic(String channel, String topic) {
        return postMessage("TOPIC %s :%s", channel, topic);
    }

    /**
     * 指定されたチャンネルの情報を取得します。
     *
     * @param channel チャンネル名
     * @return IRCクライアント
     */
    public IRCClient list(String channel) {
        return postMessage("LIST %s", channel);
    }

    /**
     * 指定されたチャンネルにクライアントを招待します。
     *
     * @param nick ニックネーム
     * @param channel チャンネル名
     * @return IRCクライアント
     */
    public IRCClient invite(String nick, String channel) {
        return postMessage("INVITE %s %s", nick, channel);
    }

    /**
     * 指定されたチャンネルからクライアントを追放します。
     *
     * @param channel チャンネル名
     * @param user クライアント
     * @param text 追放メッセージ
     * @return IRCクライアント
     */
    public IRCClient kick(String channel, String user, String text) {
        return postMessage("KICK %s %s :%s", channel, user, text);
    }

    /**
     * 指定されたターゲットにテキストを送信します。
     *
     * @param target チャンネル名またはニックネーム
     * @param text テキスト
     * @return IRCクライアント
     */
    public IRCClient privmsg(String target, String text) {
        return postMessage("PRIVMSG %s :%s", target, text);
    }

    /**
     * 指定されたターゲットにテキストを送信します。
     *
     * @param target チャンネル名またはニックネーム
     * @param text テキスト
     * @return IRCクライアント
     */
    public IRCClient notice(String target, String text) {
        return postMessage("NOTICE %s :%s", target, text);
    }

    /**
     * CTCPクエリを送信します。
     *
     * @param target チャンネル名またはニックネーム
     * @param text CTCPクエリ
     * @return IRCクライアント
     * @see #privmsg(String, String)
     */
    public IRCClient ctcp(String target, String text) {
        return privmsg(target, IRCMessage.wrapCTCP(text));
    }

    /**
     * CTCPリプライを送信します。
     *
     * @param target チャンネル名またはニックネーム
     * @param text CTCPリプライ
     * @return IRCクライアント
     * @see #notice(String, String)
     */
    public IRCClient ctcpReply(String target, String text) {
        return notice(target, IRCMessage.wrapCTCP(text));
    }

    /**
     * IRCサーバとの接続を継続します。
     *
     * @param server サーバ名
     * @return IRCクライアント
     */
    public IRCClient pong(String server) {
        return postMessage("PONG :%s", server);
    }

    /**
     * 不在メッセージを設定します。
     *
     * @param text 不在メッセージ
     * @return IRCクライアント
     */
    public IRCClient away(String text) {
        return postMessage("AWAY :%s", text);
    }

    /**
     * 不在メッセージを解除します。
     *
     * @return IRCクライアント
     */
    public IRCClient away() {
        return postMessage("AWAY");
    }

    /**
     * 指定されたテキストを解析して、ハンドラに送信します。
     *
     * @param text テキスト
     */
    protected void fireMessage(String text) {
        try {
            IRCMessage message = new IRCMessage(text, getUserNick());
//			String encoding = "JIS";
////			System.out.println(new String(text.getBytes(), encoding));
//			IRCMessage message = new IRCMessage(text, encoding);
//			handler.onMessage(new IRCEvent(this, message));
//			System.out.println(message.toString());
            final IRCEvent event = new IRCEvent(this, message);
            for (final IRCHandler handler : handlers) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handler.onMessage(event);
                        } catch (Throwable ex) {
                            fireError(new RuntimeException("IRCメッセージハンドラを中止しました。: " + event, ex));
                        }
                    }
                });
            }
        } catch (Throwable ex) {
            fireError(new RuntimeException("IRCメッセージが不正です。: " + text, ex));
        }
    }

    /**
     * 入出力タスクで例外が発生したときに呼び出されます。
     *
     * @param x 例外
     */
    protected void fireError(Throwable x) {
        log.log(Level.SEVERE, "IRCクライアントでエラーが発生しました。", x);
    }
}

/**
 * 入力タスク
 */
class InputTask implements Runnable {

    /**
     * IRCクライアント
     */
    private IRCClient irc;
    /**
     * 入力ストリーム
     */
    private BufferedReader in;

    /**
     * 入力ストリームがクローズされるまで読み続ける入力タスクを構築します。
     *
     * @param irc IRCクライアント
     * @param encoding 文字コード
     * @throws IOException 入力ストリームをオープンできない場合
     */
    public InputTask(IRCClient irc, String encoding) throws IOException {
        this.irc = irc;
        in = new BufferedReader(new InputStreamReader(new KanaInputFilter(irc.getInputStream()), encoding));
    }

    public void run() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                irc.fireMessage(line);
                Thread.yield();
            }
        } catch (IOException ex) {
            irc.fireError(new IOException("入力ストリームを読み込めません。", ex));
        } finally {
            try {
                irc.close();
            } catch (IOException ex) {
                irc.fireError(new IOException("入力ストリームをクローズできません。", ex));
            }
        }
    }
}

/**
 * 出力タスク
 */
class OutputTask implements Runnable {

    /**
     * ログ
     */
    private static final Logger log = Logger.getLogger(OutputTask.class.getName());
    /**
     * IRCクライアント
     */
    private IRCClient irc;
    /**
     * 送信するテキスト
     */
    private String text;
    /**
     * 出力ストリーム
     */
    private PrintStream out;

    /**
     * 指定されたテキストをひとつ送信するタスクを構築します。
     *
     * @param irc IRCクライアント
     * @param text 送信するテキスト
     */
    public OutputTask(IRCClient irc, String text) {
        this.irc = irc;
        this.text = text;
    }

    public void run() {
        try {
            log.info(text);
            out = new PrintStream(irc.getOutputStream(), true, irc.getEncoding());
            out.println(text);
//			out.close();
        } catch (IOException ex) {
            irc.fireError(new IOException("指定されたテキストを送信できません。: " + text, ex));
//			throw new RuntimeException("送信エラー: " + text, x);
        }
    }
}
