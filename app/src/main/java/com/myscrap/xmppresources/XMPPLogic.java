package com.myscrap.xmppresources;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by ms3 SampleXMPP.
 */

public class XMPPLogic
{
    private XMPPTCPConnection connection = null;

    private static XMPPLogic instance = null;

    public synchronized static XMPPLogic getInstance()
    {
        if(instance==null){
            instance = new XMPPLogic();
        }
        return instance;
    }

    public void setConnection(XMPPTCPConnection connection){
        this.connection = connection;
    }

    public XMPPTCPConnection getConnection() {
        return this.connection;
    }
}
