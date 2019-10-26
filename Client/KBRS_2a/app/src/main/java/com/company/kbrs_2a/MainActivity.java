package com.company.kbrs_2a;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.company.async_encryptions.GM;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

public class MainActivity  extends AppCompatActivity {
    private Button button_login_login;
    private EditText editText_login_username;
    private EditText editText_login_password;
    private String username;
    private String password;
    private String baseUrl;
    private EditText server, content;
    private Button connect;
    private ListView text;

    public static final int PORT = 32000;

    public static final int CONNECTED = 1000;
    public static final int NOT_CONNECTED = 1001;
    public static final int CONNECTING_TROUBLES = 1002;
    public static final int SERVER_ERROR = 1003;
    public static final int GOTTEN = 1004;
    private GM gm;
    public static final int RSA_UPDATED = 1005;

    private Handler messageHandler;
    private String sessionKey;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // System.out.println("I'm here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentification_layout);

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {

                switch (inputMessage.what) {
                    case CONNECTED:
                        editText_login_username.setEnabled(true);
                        editText_login_password.setEnabled(true);
                        button_login_login.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                        break;
                    case NOT_CONNECTED:
                        connect.setEnabled(true);
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
                    case CONNECTING_TROUBLES:
                    case SERVER_ERROR:

                        content.setText("");
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;


                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };


        // TODO: Replace this with your own IP address or URL.
        baseUrl = "10.0.2.2";

        editText_login_username = findViewById(R.id.editText_login_username);
        editText_login_password = findViewById(R.id.editText_login_password);

        server = findViewById(R.id.server);
        server.setText("10.0.2.2");
        connect = findViewById(R.id.connect);
        connect.setOnClickListener(view -> {
            view.setEnabled(false);
            Thread thread1 = new Thread(new Start());
            thread1.start();

            // acquired session key



        });
        button_login_login = findViewById(R.id.button_login_login);

        button_login_login.setOnClickListener(v -> {
            try {

                username = editText_login_username.getText().toString();
                password = editText_login_password.getText().toString();

                ApiAuthenticationClient apiAuthenticationClient =
                        new ApiAuthenticationClient( out, in, username, password);

                AsyncTask<Void, Void, String> execute = new ExecuteNetworkOperation(apiAuthenticationClient);
                execute.execute();
            } catch (Exception ex) {
            }
        });

        editText_login_username.setEnabled(false);
        editText_login_password.setEnabled(false);
        button_login_login.setEnabled(false);
    }

    class Start implements Runnable {
        public void run() {
            try {
                socket = new Socket(server.getText().toString(), PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                SocketHandler.setIn(in);
                SocketHandler.setOut(out);
                SocketHandler.setSocket(socket);
                Message message = messageHandler.obtainMessage(CONNECTED);
                message.sendToTarget();

                generateRSA();
                getSessionKey();
                // get encrypted session key
                // decrypt session key using private key



            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(NOT_CONNECTED, e.toString());
                message.sendToTarget();
            }
        }
    }

    private void generateRSA() throws IOException {

        // Generate new RSA
        //rsa = new RSA();
        gm = new GM();

        // Send RSA public key to the server
        ConnUtill.sendMsg(out, Command.CommandType.REQUEST_ACCESS,
                gm.getPublicKey());
    }


    private void getSessionKey() throws IOException, ClassNotFoundException {
        Command cmd_key = (Command) in.readObject();
        if (cmd_key.getCommandType().equals(Command.CommandType.ERROR)) {
            throw new IOException("Server responded with ERROR message: " + cmd_key.getParam());
        }

        SocketHandler.setSessionKey(gm.decrypt(cmd_key.getParam()));

    }

    public class ExecuteNetworkOperation extends AsyncTask<Void, Void, String> {

        private ApiAuthenticationClient apiAuthenticationClient;
        private String isValidCredentials;

        /**
         * Overload the constructor to pass objects to this class.
         */
        public ExecuteNetworkOperation(ApiAuthenticationClient apiAuthenticationClient) {
            this.apiAuthenticationClient = apiAuthenticationClient;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Display the progress bar.
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                isValidCredentials = apiAuthenticationClient.execute();
            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(SERVER_ERROR, e.toString());
                message.sendToTarget();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Hide the progress bar.
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            System.out.println("postprocessing almost done");
            // Login Success
            if (isValidCredentials.equals("true")) {
                goToSecondActivity();
            }
            // Login Failure
            else {
                Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Open a new activity window.
     */
    private void goToSecondActivity() {
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("password", password);
       // bundle.putString("socket", baseUrl);
        Intent intent = new Intent(this, AppActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }



}


