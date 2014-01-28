package com.mac.tarchan.irc;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 *
 * @author tarchan
 */
public class text extends ContentHandler {
    static final Logger log = Logger.getLogger(text.class.getName());

    public text() {
        log.info("new ContentHandler");
    }

    @Override
    public Object getContent(URLConnection urlc) throws IOException {
        log.info("host=" + urlc.getURL().getHost());
        log.info("file=" + urlc.getURL().getFile());
        return "" + urlc;
    }
}
