/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mac.tarchan.irc.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tarchan
 */
public class ShellAdapter {
    public static void main(String[] args) {
        try {
            URL url = new URL("irc://irc.ircnet.ne.jp/#javabreak");
            URLConnection con = url.openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                while (true) {
                    String line = in.readLine();
                    if (line == null) break;
                    
                    System.out.println(line);
                }
            } catch (IOException ex) {
                Logger.getLogger(ShellAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ShellAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
