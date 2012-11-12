/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.tarchan.irc.bot;

import com.mac.tarchan.irc.client.IRC;
import com.mac.tarchan.irc.client.IRCClient;
import com.mac.tarchan.irc.client.IRCEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tarchan
 */
public class SampleClient
{
    public static void main(String[] args)
    {
	try {
	    SampleClient handler = new SampleClient();
	    String host = args[0];
	    int port = Integer.parseInt(args[1]);
	    String nick = args[2];
	    String pass = args[3];
	    String encoding = args[4];
	    IRCClient client = IRCClient.createClient(host, port, nick, pass, encoding);
	    client.addHandler(handler);
	    client.start();
	} catch (IOException ex) {
	    Logger.getLogger(SampleClient.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    @IRC("001")
    public void welcome(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("join")
    public void join(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("part")
    public void part(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("quit")
    public void quit(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("mode")
    public void mode(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("topic")
    public void topic(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("332")
    public void topic332(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("353")
    public void nickStart(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("366")
    public void nickEnd(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("433")
    public void onNickConflict(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("nick")
    public void nick(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("error")
    public void error(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("ping")
    public void ping(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }

    @IRC("privmsg")
    public void privmsg(IRCEvent event)
    {
        Logger.getLogger(SampleClient.class.getName()).log(Level.INFO, event.toString());
    }
}
