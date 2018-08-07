package com.myscrap.xmppresources;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by ms3 SampleXMPP.
 */

public class ConnectionClass
{
    private XMPPConnection connection = null;

    private static ConnectionClass instance = null;

    public synchronized static ConnectionClass getInstance()
    {
        if(instance==null)
        {
            instance = new ConnectionClass();
        }
        return instance;
    }

    public void setConnection(XMPPConnection connection)
    {
        this.connection = connection;
    }

    public XMPPConnection getConnection()
    {
        return this.connection;
    }
}
