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
import android.widget.TextView;
import android.widget.Toast;

import com.company.async_encryptions.GM;
import com.company.async_encryptions.RSA;

import org.w3c.dom.Text;

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
    private EditText server;
    private TextView rsa_output;
    private Button connect, generate_rsa, rsa_mode, gm_mode;

    public static final int PORT = 32000;

    public static final int CONNECTED = 1000;
    public static final int NOT_CONNECTED = 1001;
    public static final int CONNECTING_TROUBLES = 1002;
    public static final int SERVER_ERROR = 1003;
    public static final int RSA_UPDATED = 1004;
    public static final int GM_UPDATED = 1005;
    public static final int INIT = 1006;

    private Handler messageHandler;

    private static GM gm;
    private static RSA rsa;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String sessionKey;

    private enum AsyncMode {
        NONE,
        RSA,
        GM
    }

    private static AsyncMode mode = AsyncMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // System.out.println("I'm here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentification_layout);

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {

                switch (inputMessage.what) {
                    case INIT:
                        rsa_mode.setEnabled(false);
                        gm_mode.setEnabled(false);
                        connect.setEnabled(true);
                        generate_rsa.setEnabled(true);
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
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
                    case RSA_UPDATED:
                        generate_rsa.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "RSA keys updated", Toast.LENGTH_LONG).show();
                        break;
                    case GM_UPDATED:
                        generate_rsa.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "GM keys updated", Toast.LENGTH_LONG).show();
                        break;
                    case CONNECTING_TROUBLES:
                    case SERVER_ERROR:

                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;


                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };


        rsa_mode = findViewById(R.id.rsa_mode);
        gm_mode = findViewById(R.id.gm_mode);

        rsa_output = findViewById(R.id.rsa);

        server = findViewById(R.id.server);
        connect = findViewById(R.id.connect);

        generate_rsa = findViewById(R.id.generate_rsa);

        editText_login_username = findViewById(R.id.editText_login_username);
        editText_login_password = findViewById(R.id.editText_login_password);
        button_login_login = findViewById(R.id.button_login_login);


        switch (mode) {
            case GM:
                rsa_mode.setBackgroundColor(1);
                init();
                break;
            case RSA:
                gm_mode.setBackgroundColor(1);
                init();
                break;
            case NONE:
                connect.setEnabled(false);
        }

        rsa_mode.setOnClickListener(view -> {
            try{
                gm_mode.setBackgroundColor(1);
                generateRSA();
                init();

            } catch (Exception e) {}
        });

        gm_mode.setOnClickListener(view -> {
            try{
                rsa_mode.setBackgroundColor(1);
                generateGM();
                init();

            } catch (Exception e) {}
        });

        /**
         * THIS IS THE PLACE WHERE WE CAN SET UP ENCRYPTION ALGORITHM!!!
         */

        //if (gm == null) generateGM(); else displayGM();
        //if (rsa == null) generateRSA(); else displayRSA();



        server.setText("10.0.2.2");

        connect.setOnClickListener(view -> {
            view.setEnabled(false);
            Thread thread1 = new Thread(new Start());
            thread1.start();

            // acquired session key

        });

        generate_rsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generate_rsa.setEnabled(false);

                switch (mode) {
                    case GM:
                        generateGM();
                        displayGM();
                        break;

                    case RSA:
                    default:
                        generateRSA();
                        displayRSA();
                }
            }
        });

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
        generate_rsa.setEnabled(false);
    }

    void init() {
        if (mode.equals(AsyncMode.RSA)) displayRSA();
        else displayGM();
        SocketHandler.init();
        Message message = messageHandler.obtainMessage(INIT, "Client has no connection");
        message.sendToTarget();
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

                getSessionKey();
                // get encrypted session key
                // decrypt session key using private key



            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(NOT_CONNECTED, e.toString());
                message.sendToTarget();
            }
        }
    }

    private void displayRSA() {
        String msg = "Keys generated with RSA\nPrivate Key: " + rsa.getPrivateKey().substring(0, 20)
                + "...\n" + "Public Key: " + rsa.getPublicKey().substring(0, 20) + "...";
        rsa_output.clearComposingText();
        rsa_output.setText(msg);
    }

    private void  displayGM() {
        String msg = "Keys generated with GM\nPrivate Key(p, q): " + gm.getPrivateKey() +
                "\n" + "Public Key(n, a): " + gm.getPublicKey();
        rsa_output.clearComposingText();
        rsa_output.setText(msg);
    }

    private void generateRSA() {

        // Generate new RSA
        rsa = new RSA();
        mode = AsyncMode.RSA;

        Message message = messageHandler.obtainMessage(RSA_UPDATED);
        message.sendToTarget();
    }

    private void generateGM() {

        // Generate new GM
        gm = new GM();
        mode = AsyncMode.GM;

        Message message = messageHandler.obtainMessage(GM_UPDATED);
        message.sendToTarget();
    }


    private void getSessionKey() throws IOException, ClassNotFoundException {
        switch (mode) {
            case GM:
                ConnUtill.sendMsg(out, Command.CommandType.REQUEST_ACCESS_WITH_GM,
                    gm.getPublicKey());
                break;

            case RSA:
                ConnUtill.sendMsg(out, Command.CommandType.REQUEST_ACCESS_WITH_RSA,
                        rsa.getPublicKey());
                break;


            default:
                Message message = messageHandler.obtainMessage(CONNECTING_TROUBLES,
                        "Neither RSA nor GM mode is specified");
                message.sendToTarget();
                return;

        }

        Command cmd_key = (Command) in.readObject();
        if (cmd_key.getCommandType().equals(Command.CommandType.ERROR)) {
            throw new IOException("Server responded with ERROR message: " + cmd_key.getParam());
        }

        switch (mode) {
            case GM:
                SocketHandler.setSessionKey(gm.decrypt(cmd_key.getParam()));
                break;
            case RSA:
                SocketHandler.setSessionKey(rsa.decrypt(cmd_key.getParam()));
        }

        sessionKey = SocketHandler.getSessionKey();



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
                isValidCredentials = apiAuthenticationClient.execute(sessionKey);
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
        Intent intent = new Intent(this, AppActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }



}


