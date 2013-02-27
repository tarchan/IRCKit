/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCEvent;
import com.mac.tarchan.irc.client.IRCMessage;
import static com.mac.tarchan.irc.client.NumericReply.*;
import com.mac.tarchan.irc.client.Reply;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 必要最低限の機能を実装したボットです。
 *
 * @author Takashi Ogura <tarchan at mac.com>
 */
public class SimpleBot {

    private static final Logger log = Logger.getLogger(SimpleBot.class.getName());

    @Reply(value = RPL_HELLO, property = "message.trail")
    public void hello(String trail) {
        log.log(Level.INFO, "しばらくお待ちください。: {0}", trail);
    }

    @Reply(RPL_WELCOME)
    public void welcome(IRCEvent e) {
        String trail = e.getMessage().getTrail();
        log.log(Level.INFO, "IRCに接続しました。: {0}", trail);
//        IRCClient irc = e.getClient();
//        irc.join(target.getText());
    }

    @Reply("ping")
    public void ping(IRCEvent e) {
        IRCClient irc = e.getClient();
        String trail = e.getMessage().getTrail();
        log.log(Level.INFO, "IRC接続を継続します。: {0}", trail);
        irc.pong(trail);
    }

    @Reply("nick")
    public void nick(IRCEvent e) {
        IRCClient irc = e.getClient();
        IRCMessage msg = e.getMessage();
        String oldNick = msg.getPrefix().getNick();
        String newNick = msg.getTrail();
        log.log(Level.INFO, "ニックネーム変更: {0} -> {1} ({2})", new Object[]{oldNick, newNick, irc.getUserNick().equals(oldNick)});
        if (irc.getUserNick().equals(oldNick)) {
            irc.setUserNick(newNick);
        }
    }
}
