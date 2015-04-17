package com.example.calle.nowchat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ChatActivity extends ActionBarActivity {

    //Socket for connection
    Socket kkSocket;

    //Socket outputstream
    PrintWriter out;

    //Socket inputstream
    BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Log.d("SocketThread", "Pre thread start");
        new Thread(new SocketThread()).start();
        //Log.d("SocketThread", "Post thread start");

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Find text editor
                            EditText et = ((EditText) findViewById(R.id.editText));

                            //Get text from editbox
                            String fromUser = et.getText().toString();
                            Log.d("OnSendClick", "Client: " + fromUser);



                            //System.out.println("Client: " + fromUser);
                            if(fromUser != null){
                                out.println(fromUser);
                                Log.d("OnSendClick", "Message sent");
                                //Empty text editor
                                et.setText("");
                            }else{
                                Log.d("OnSendClick", "No message");
                            }


                        }
                        catch(Exception e){
                            Log.e("SocketException", "in onClickListener for sendButton", e);
                        }
                    }
                }).start();

            }
        });

        //new Thread(new ServerListenerThread()).start();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Add text to the chat
     * @param text
     */
    private void addChatItem(String text){
        try{
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chat_item_list);

            // Add textview 1
            TextView textView1 = new TextView(this);
            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView1.setText(text);
            textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
            textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            linearLayout.addView(textView1);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * Initiates the socket and the input- and outputstreams
     */
    private class SocketThread implements Runnable{

        //Internet address to connect to
        private final String INET_ADDRESS = "192.168.1.96";

        //Port to connect to
        private final int PORT = 8080;

        @Override
        public void run() {
            try {
                Log.d("SocketThread", "Starting up");
                kkSocket = new Socket(INET_ADDRESS, PORT);
                out = new PrintWriter(kkSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(kkSocket.getInputStream()));
                Log.d("SocketThread", "Socket started, launching listener");

                new Thread(new ServerListenerThread()).start();

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Listens to the socketinputstream for messages, when a message is received updates the chatview with message
     */
    private class ServerListenerThread implements Runnable{

        @Override
        public void run() {
            try {
                String fromServer;

                //Listen to server
                while ((fromServer = in.readLine()) != null) {
                    Log.d("ChatUpdaterThread", "Server: " + fromServer);
                    //System.out.println("Server: " + fromServer);

                    //If non empty message, update chat
                    if (fromServer != null) {

                        //Update chat
                        findViewById(R.id.textView).post(new ChatUpdaterThread(fromServer));
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the chat with a new chatitem
     */
    private class ChatUpdaterThread implements Runnable{

        private String message;

        public ChatUpdaterThread(String message){
            this.message=message;
        }

        @Override
        public void run() {
            ((TextView) findViewById(R.id.textView)).setText(message);
            addChatItem(message);
        }
    }
}
