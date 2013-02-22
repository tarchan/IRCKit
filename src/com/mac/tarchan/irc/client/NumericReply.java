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
    public static final String RPL_YOURHOST = "002";
    public static final String RPL_CREATED = "003";
    public static final String RPL_MYINFO = "004";
    public static final String RPL_ISUPPORT = "005";
    public static final String RPL_BOUNCE = "010";
    public static final String RPL_MAP = "015";
    public static final String RPL_MAPEND = "017";
    public static final String RPL_MAPSTART = "018";
    public static final String RPL_HELLO = "020";
    public static final String RPL_YOURID = "042";
    public static final String RPL_SAVENICK = "043";
    public static final String RPL_TOPIC = "332";
    public static final String RPL_NAMREPLY = "353";
    public static final String RPL_ENDOFNAMES = "366";
    public static final String ERR_NICKNAMEINUSE = "433";

    public static boolean isServerClientCommand(String reply) {
        return "000".compareTo(reply) <= 0 && "199".compareTo(reply) >= 0;
    }

    public static boolean isServerCommand(String reply) {
        return "200".compareTo(reply) <= 0 && "399".compareTo(reply) >= 0;
    }

    public static boolean isError(String reply) {
        return "400".compareTo(reply) <= 0 && "599".compareTo(reply) >= 0;
    }

    private NumericReply() {
    }
}
