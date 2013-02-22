/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.client;

/**
 * ニューメリックリプライを定義します。
 *
 * @author Takashi Ogura <tarchan at mac.com>
 * @see <a href="https://www.alien.net.au/irc/irc2numerics.html">IRC/2 Numeric List</a>
 */
public class NumericReply {

    public static final String RPL_WELCOME = "001";
    public static final String RPL_TOPIC = "332";
    public static final String RPL_NAMREPLY = "353";
    public static final String RPL_ENDOFNAMES = "366";
    public static final String ERR_NICKNAMEINUSE = "433";

    private NumericReply() {
    }
}
