package test;

import com.mac.tarchan.irc.client.IRCMessage;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

public class IRCMessageTest {

    @Test
    public void testAdd() {
        IRCMessage msg = new IRCMessage("", "nick");
        assertThat(msg.getCommand(), is("LOGIN"));
    }
}
