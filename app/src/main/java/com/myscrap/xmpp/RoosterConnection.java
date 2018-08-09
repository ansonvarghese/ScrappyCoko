package com.myscrap.xmpp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.myscrap.HomeActivity;
import com.myscrap.R;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.application.AppController;
import com.myscrap.logger.Log;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmppdata.ChatMessagesTable;
import com.myscrap.xmppmodel.XMPPChatMessageModel;
import com.myscrap.xmppresources.ChatMessagesModel;
import com.myscrap.xmppresources.Constant;
import com.myscrap.xmppresources.ReplyExtension;
import com.myscrap.xmppresources.Support;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.UUID;


public class RoosterConnection implements ConnectionListener
{

    private static final String LOGTAG = "Rock";

    private  final Context mApplicationContext;
    private   String mUsername;
    private   String mPassword;
    private   String mServiceName;
    private XMPPTCPConnection mConnection;
    private ConnectionState mConnectionState;
    private PingManager pingManager;
    private ChatManager chatManager;





    String userJid ;
    String friendsJid ;
    String userId ;
    String friendsId ;
    String userName ;
    String friendsName ;
    String userImage ;
    String friendsImage ;
    String userColor ;
    String friendsColor ;
    String msgId = null;



    public static enum ConnectionState
    {
        OFFLINE,CONNECTING,ONLINE
    }

    public ConnectionState getmConnectionState() {
        return mConnectionState;
    }

    public void setmConnectionState(ConnectionState mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    public String getConnectionStateString()
    {
        switch ( mConnectionState)
        {
            case OFFLINE:
                return  "Offline";

            case CONNECTING:
                return  "Connecting...";

            case ONLINE:
                return  "Online";

            default:
                return  "Offline";
        }

    }

    private void updateActivitiesOfConnectionStateChange( ConnectionState mConnectionState)
    {
        ConnectionState connectionState = mConnectionState;
        String status;
        switch ( mConnectionState)
        {
            case OFFLINE:
                status = "Offline";
                break;
            case CONNECTING:
                status = "Connecting...";
                break;
            case ONLINE:
                status = "Online";
                break;
            default:
                status = "Offline";
                break;
        }

        Intent i = new Intent(Constant.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        i.putExtra(Constant.UI_CONNECTION_STATUS_CHANGE,status);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);



    }

    public RoosterConnection(Context mApplicationContext)
    {
        Log.d(LOGTAG,"RoosterConnection Constructor called.");
        this.mApplicationContext = mApplicationContext;
    }

    public void connect() throws IOException,XMPPException,SmackException
    {



        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
        gatherCredentials();

        XMPPTCPConnectionConfiguration connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(mServiceName)
                .setHost(mServiceName)
                .setResource("MYSCRAP")
                //Was facing this issue
                // https://discourse.igniterealtime.org/t/connection-with-ssl-fails-with-java-security-keystoreexception-jks-not-found/62566
                .setKeystoreType(null) //This line seems to get rid of the problem
                .setSendPresence(true)
                .setDebuggerEnabled(true)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(true).build();


        SmackConfiguration.DEBUG = true;
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        mConnection = new XMPPTCPConnection(connectionConfiguration);
        mConnection.setUseStreamManagement(true);
        mConnection.setUseStreamManagementResumption(true);
        mConnection.setPreferredResumptionTime(5);
        mConnection.addConnectionListener(this);


        ProviderManager.addExtensionProvider(ReplyExtension.ELEMENT, ReplyExtension.NAMESPACE, new ReplyExtension.Provider());

            if (!mConnection.isConnected())
            {
                try
                {
                    mConnection.connect();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            if (mUsername != null  && mPassword != null &&  !mUsername.equalsIgnoreCase("") && !mPassword.equalsIgnoreCase(""))
            {
                    try
                    {
                        mConnection.login(mUsername, mPassword);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
            }





        chatManager = ChatManager.getInstanceFor(mConnection);
        chatManager.addIncomingListener(new IncomingChatMessageListener(){
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat)
            {

                String messageSource = message.getFrom().toString();
                String contactJid="";
                if ( messageSource.contains("/"))
                {
                    contactJid = messageSource.split("/")[0];
                }
                else
                {
                    contactJid=messageSource;
                }



                ExtensionElement packetExtension =  message.getExtension(ReplyExtension.NAMESPACE);
                ReplyExtension repExt = (ReplyExtension)packetExtension;
                if(repExt!=null)
                {
                    userId = repExt.getuId();
                    friendsId = repExt.getfId();
                    userImage = repExt.getuImage();
                    friendsImage = repExt.getfImage();
                    userName = repExt.getuName();
                    friendsName = repExt.getfName();
                    userColor = repExt.getuColor();
                    friendsColor = repExt.getfColor();

                }
                else
                {
                    Log.d(LOGTAG, "we are geting null");
                }



                msgId =  message.getStanzaId();
                userJid = Support.getJidFromName(userId, userName);
                friendsJid = Support.getJidFromName(friendsId, friendsName);




                /*ChatMessagesModel.get(mApplicationContext).addMessage(new XMPPChatMessageModel(msgId, friendsJid, userJid,
                        userId, userName,userImage, message.getBody(), "receive", String.valueOf(System.currentTimeMillis()), "", userColor));*/

                ChatMessagesTable chatMessagesTable = new ChatMessagesTable(mApplicationContext);

                String loginStatus = UserUtils.getLoginStatus(mApplicationContext);
                if (!chatMessagesTable.isChatAvailable(message.getStanzaId()) && !loginStatus.equalsIgnoreCase("0"))
                {

                    ChatMessagesModel.get(mApplicationContext).addMessage(new XMPPChatMessageModel(message.getStanzaId(), contactJid.split("@")[0],
                            friendsId, friendsName, friendsImage, message.getBody(), "receive", String.valueOf(System.currentTimeMillis()), "0", friendsColor));


                    // messages Broadcasting to XMPPChatRoomActivity
                    //If the view (XMPPChatRoomActivity) is visible, inform it so it can do necessary adjustments
                    Intent intent = new Intent(Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
                    intent.setPackage(mApplicationContext.getPackageName());
                    mApplicationContext.sendBroadcast(intent);


                        Intent notificationIntent = new Intent(mApplicationContext, XMPPChatRoomActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.FRIENDS_JID, contactJid.split("@")[0]);
                        bundle.putString(Constant.FRIENDS_ID, friendsId);
                        bundle.putString(Constant.FRIENDS_NAME, friendsName);
                        bundle.putString(Constant.FRIENDS_URL, friendsImage);
                        bundle.putString(Constant.FRIENDS_COLOR, friendsColor);
                        notificationIntent.putExtras(bundle);
                        showNotifications(friendsName, message.getBody(), notificationIntent);


                }

            }
        });



        // do it later
        chatManager.addOutgoingListener(new OutgoingChatMessageListener() {
            @Override
            public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat)
            {

                String userJid = null;
                String friendsJid = null;
                String userId = null;
                String friendsId = null;
                String userName = null;
                String friendsName = null;
                String userImage = null;
                String friendsImage = null;
                String userColor = null;
                String friendsColor = null;
                String msgId = null;


                ExtensionElement packetExtension =  message.getExtension(ReplyExtension.NAMESPACE);
                ReplyExtension repExt = (ReplyExtension)packetExtension;
                if(repExt!=null)
                {

                   /* friendsId = repExt.getuId();
                    userId = repExt.getfId();
                    friendsImage = repExt.getuImage();
                    userImage = repExt.getfImage();
                    friendsName = repExt.getuName();
                    userName = repExt.getfName();
                    friendsColor = repExt.getuColor();
                    userColor = repExt.getfColor();*/


                    userId = repExt.getuId();
                    friendsId = repExt.getfId();
                    userImage = repExt.getuImage();
                    friendsImage = repExt.getfImage();
                    userName = repExt.getuName();
                    friendsName = repExt.getfName();
                    userColor = repExt.getuColor();
                    friendsColor = repExt.getfColor();


                }
                else
                {
                    Log.d(LOGTAG, "we are geting null");
                }

                msgId =  message.getStanzaId();


                ChatMessagesModel.get(mApplicationContext).addMessage(new XMPPChatMessageModel(msgId,to.toString().split("@")[0],
                        userId, userName,userImage, message.getBody(), "send", String.valueOf(System.currentTimeMillis()), "0", userColor));

                Intent intent = new Intent(Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
                intent.setPackage(mApplicationContext.getPackageName());
                mApplicationContext.sendBroadcast(intent);

            }
        });



        // Add reconnect manager
        ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
        ReconnectionManager.setEnabledPerDefault(true);


        ServerPingWithAlarmManager.getInstanceFor(mConnection).setEnabled(true);
        pingManager = PingManager.getInstanceFor(mConnection);
        pingManager.setPingInterval(10);
        try
        {
            pingManager.pingMyServer();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }



        // Add ping manager here
        PingManager.getInstanceFor(mConnection).registerPingFailedListener(new PingFailedListener() {
            @Override public void pingFailed()
            {

                disconnect();

                try
                {
                    mConnection.connect();
                    if (mUsername != null && mPassword != null && !mUsername.equalsIgnoreCase("") && !mPassword.equalsIgnoreCase(""))
                    {
                        mConnection.login(mUsername, mPassword);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (XMPPException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (SmackException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }





    public void showNotifications(String title,String message,Intent intent)
    {
        if (title != null && message != null && intent != null)
        {
            String NOTIFICATION_CHANNEL_ID = "myscrap";
            Bitmap bitmap;
            int smallIcon = R.mipmap.noti;
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //  intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP);
            final PendingIntent resultPendingIntent = PendingIntent.getActivity(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mApplicationContext, NOTIFICATION_CHANNEL_ID);
            final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            bitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(), R.mipmap.ic_launcher);
            Notification notification;
            notification = mBuilder.setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setLights(Color.WHITE, 5000, 5000)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setLargeIcon(bitmap)
                    .setSmallIcon(smallIcon)
                    .setContentText(message)
                    .build();

            NotificationManager notificationManager = (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    int importance = NotificationManager.IMPORTANCE_DEFAULT; //high to def
                    NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, title, importance);
                    mChannel.setDescription("");
                    mChannel.enableLights(true);
                    mChannel.setLightColor(ContextCompat.getColor(mApplicationContext, R.color
                            .colorPrimary));
                    notificationManager.createNotificationChannel(mChannel);
                }
                notificationManager.notify(0, notification);
            }

          // update badger when notification comes
            try
            {
                HomeActivity.notification();
            }
            catch (Exception exp)
            {

            }
        }

    }




    public void disconnect ()
    {
        Log.d(LOGTAG,"Disconnecting from server "+ mServiceName);
        if (mConnection != null)
        {
            mConnection.disconnect();
        }
    }







    public void sendMessage ( String body,String userJid,String friendsJid, String userId,String friendsId, String username,
                              String friendsName,String userImage, String friendsImage, String userColor, String friendsColor)
    {
        if (mConnection != null)
        {

            if (mConnection.isConnected() && mConnection.isAuthenticated())
            {

                EntityBareJid jid = null;
                try
                {
                    jid = JidCreate.entityBareFrom(friendsJid + "@s192-169-189-223.secureserver.net"); //put @
                }
                catch (XmppStringprepException e)
                {
                    e.printStackTrace();
                }


                ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
                String uniqueID = UUID.randomUUID().toString();
                Chat chat = chatManager.chatWith(jid);

                Message message = new Message(jid, Message.Type.chat);
                message.setBody(body);
                message.setStanzaId(uniqueID);

                //adding custom reply extension
                ReplyExtension repExt = new ReplyExtension();
                repExt.setuId(friendsId);
                repExt.setfId(userId);
                repExt.setuName(friendsName);
                repExt.setfName(username);
                repExt.setuImage(friendsImage);
                repExt.setfImage(userImage);
                repExt.setuColor(friendsColor);
                repExt.setfColor(userColor);
                message.addExtension(repExt);


                // send message
                try
                {
                    chat.send(message);
                }
                catch (SmackException.NotConnectedException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

            }
            else
                {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();
            }
        }

    }





    private void gatherCredentials()
    {

        mUsername = UserUtils.getUserJid(AppController.getInstance());
        mPassword = UserUtils.getUserPassword(AppController.getInstance());
        mServiceName = "myscrap.com";

    }


    private void notifyUiForConnectionError()
    {
        Intent i = new Intent(Constant.BroadCastMessages.UI_CONNECTION_ERROR);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(LOGTAG,"Sent the broadcast for connection Error");
    }



    @Override
    public void connected(XMPPConnection connection)
    {
        Log.d(LOGTAG,"Connected");
    }



    @Override
    public void authenticated(XMPPConnection connection, boolean resumed)
    {
        Log.d(LOGTAG,"Sent the broadcast that we are authenticated");

    }



    @Override
    public void connectionClosed()
    {
        Log.d(LOGTAG,"connectionClosed");
        notifyUiForConnectionError();


        if (CheckNetworkConnection.isConnectionAvailable(mApplicationContext))
        {
            if (mUsername != null && mPassword != null  && !mUsername.equalsIgnoreCase("") && !mPassword.equalsIgnoreCase(""))
            {
                        try
                        {
                            mConnection.login(mUsername, mPassword);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        catch (SmackException e)
                        {
                            e.printStackTrace();
                        }
                        catch (XMPPException e)
                        {
                            e.printStackTrace();
                        }

            }

        }


    }

    @Override
    public void connectionClosedOnError(Exception e)
    {

        if (CheckNetworkConnection.isConnectionAvailable(mApplicationContext))
        {
            if (mUsername != null &&  mPassword != null && !mUsername.equalsIgnoreCase("") && !mPassword.equalsIgnoreCase(""))
            {
                        try
                        {
                            mConnection.login(mUsername, mPassword);
                        }
                        catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (SmackException e1) {
                            e1.printStackTrace();
                        } catch (XMPPException e1) {
                            e1.printStackTrace();
                        }

            }

        }
        Log.d(LOGTAG,"connectionClosedOnError");
            notifyUiForConnectionError();

    }



    @Override
    public void reconnectionSuccessful()
    {
        Log.d(LOGTAG,"reconnectionSuccessful");
    }



    @Override
    public void reconnectingIn(int seconds)
    {
        Log.d(LOGTAG,"Reconnecting in " + seconds + "seconds");
    }



    @Override
    public void reconnectionFailed(Exception e)
    {
        Log.d(LOGTAG,"reconnectionFailed");
    }




}
