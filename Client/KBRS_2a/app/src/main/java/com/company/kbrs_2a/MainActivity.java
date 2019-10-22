package com.company.kbrs_2a;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import com.company.async_encryptions.GM;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int CONNECTED = 1000;
    public static final int NOT_CONNECTED = 1001;
    public static final int CONNECTING_TROUBLES = 1002;
    public static final int SERVER_ERROR = 1003;
    public static final int GOTTEN = 1004;
    public static final int RSA_UPDATED = 1005;

    public static final int PORT = 32000;

    private EditText server, content;
    private Button connect, get, rsa;
    private ListView text;

    private Handler messageHandler;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GM gm;

    private String selectedBook = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {

                switch (inputMessage.what) {
                    case CONNECTED:
                        text.setEnabled(true);
                        get.setEnabled(true);
                        rsa.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                        break;
                    case NOT_CONNECTED:
                        connect.setEnabled(true);
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
                    case CONNECTING_TROUBLES:
                        get.setEnabled(true);
                        content.setText("");
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
                    case SERVER_ERROR:
                        get.setEnabled(true);
                        content.setText("");
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
                    case RSA_UPDATED:
                        rsa.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "RSA keys updated", Toast.LENGTH_LONG).show();
                        break;
                    case GOTTEN:
                        get.setEnabled(true);
                        content.setText((String)inputMessage.obj);

                        break;
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        server = findViewById(R.id.server);
        server.setText("10.0.2.2");
        connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                Thread thread1 = new Thread(new Start());
                thread1.start();
            }
        });

        rsa = findViewById(R.id.rsa);
        rsa.setEnabled(false);
        rsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rsa.setEnabled(false);
                Thread thread1 = new Thread(new Rsa());
                thread1.start();
            }
        });


        text = findViewById(R.id.text);
        text.setEnabled(false);
        text.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        text.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedBook = (String)text.getItemAtPosition(position);
            }});
        get = findViewById(R.id.get);
        get.setEnabled(false);

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBook != null) {
                    view.setEnabled(false);
                    Thread thread1 = new Thread(new GetText(selectedBook));
                    thread1.start();
                } else {
                    Toast.makeText(getApplicationContext(), "Select book", Toast.LENGTH_LONG).show();
                }
            }
        });

        content = findViewById(R.id.content);



        String[] books = {"atlas_shrugged", "pride_and_prejudice", "world_without_end"};
        List<String> initialList = Arrays.asList(books);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, initialList );
        text.setAdapter(adapter);


    }

    class Start implements Runnable {
        public void run() {
            try {
                socket = new Socket(server.getText().toString(), PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                Message message = messageHandler.obtainMessage(CONNECTED);
                message.sendToTarget();

                generateRSA();
            } catch (IOException e) {
                Message message = messageHandler.obtainMessage(NOT_CONNECTED, e.toString());
                message.sendToTarget();
            }
        }
    }

    class Rsa implements Runnable {
        public void run() {
            try {
                generateRSA();
                Message message = messageHandler.obtainMessage(RSA_UPDATED);
                message.sendToTarget();
            } catch (IOException e) {
                Message message = messageHandler.obtainMessage(NOT_CONNECTED, e.toString());
                message.sendToTarget();
            }
        }
    }


    class GetText implements Runnable {
        private String name;

        public GetText(String name) {
            this.name = name;
        }

        public void run() {
            try {
                ConnUtill.sendMsg(out, Command.CommandType.REQUEST_TEXT, name);

                Command cmd_file = (Command) in.readObject();
                if (cmd_file.getCommandType().equals(Command.CommandType.ERROR)) {
                    throw new Exception("Server responded with ERROR message: " + cmd_file.getParam());
                }

                // Receive encoded session key from server
                Command cmd_key = (Command) in.readObject();
                if (cmd_key.getCommandType().equals(Command.CommandType.ERROR)) {
                    throw new Exception("Server responded with ERROR message: " + cmd_key.getParam());
                }

                byte[] encodedText = cmd_file.get_byteParam();
                System.out.println(encodedText);

                File outputDir = MainActivity.this.getCacheDir();
                File outputFile = File.createTempFile("encrypted", "txt", outputDir);
                File txtFile = File.createTempFile("decrypted", "txt", outputDir);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
                bos.write(encodedText);
                bos.flush();
                bos.close();


                String decryptedSessionKey = gm.decrypt(cmd_key.getParam());
                System.out.println(decryptedSessionKey);
                FileCipher task = new FileCipher(outputFile.getPath(),
                        txtFile.getPath(), decryptedSessionKey, false, OperationMode.Mode.CFB);

                task.cryptFile();
                String contents = new String(Files.readAllBytes(Paths.get(txtFile.getPath())));

                Message message = messageHandler.obtainMessage(GOTTEN, contents);
                message.sendToTarget();

            } catch (IOException | ClassNotFoundException e) {
                Message message = messageHandler.obtainMessage(CONNECTING_TROUBLES, e.toString());
                message.sendToTarget();
            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(SERVER_ERROR, e.toString());
                message.sendToTarget();
            }
        }
    }


    private void generateRSA() throws IOException {

        // Generate new RSA
        //rsa = new RSA();
        gm = new GM();

        // Send RSA public key to the server
        ConnUtill.sendMsg(out, Command.CommandType.UPDATE_RSA,
                gm.getPublicKey());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
