/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import com.mac.tarchan.irc.client.IRC;
import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCEvent;

/**
 *
 * @author tarchan
 */
public class SampleClient
{
    public static void main(String[] args)
    {
        SampleClient handler = new SampleClient();
        // TODO アノテーションでイベントハンドラを定義する
	IRCClient client = new IRCClient();
	client.addHandler(handler);
//	client.login();
    }
    
    @IRC("001")
    public void welcome(IRCEvent event)
    {
        
    }

    @IRC("join")
    public void join(IRCEvent event)
    {
        
    }

    @IRC("part")
    public void part(IRCEvent event)
    {
        
    }

    @IRC("quit")
    public void quit(IRCEvent event)
    {
        
    }

    @IRC("mode")
    public void mode(IRCEvent event)
    {
        
    }

    @IRC("topic")
    public void topic(IRCEvent event)
    {
        
    }

    @IRC("332")
    public void topic332(IRCEvent event)
    {
        
    }

    @IRC("353")
    public void nickStart(IRCEvent event)
    {
        
    }

    @IRC("366")
    public void nickEnd(IRCEvent event)
    {
        
    }

    @IRC("433")
    public void onNickConflict(IRCEvent event)
    {
        
    }

    @IRC("nick")
    public void nick(IRCEvent event)
    {
        
    }

    @IRC("error")
    public void error(IRCEvent event)
    {
        
    }

    @IRC("ping")
    public void ping(IRCEvent event)
    {
        
    }

    @IRC("privmsg")
    public void privmsg(IRCEvent event)
    {
        
    }
}
