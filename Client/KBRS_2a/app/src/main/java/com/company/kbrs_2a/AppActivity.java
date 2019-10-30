package com.company.kbrs_2a;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;
import main.java.com.company.idea_cipher.modes.FileCipher;
import main.java.com.company.idea_cipher.modes.OperationMode;
import main.java.com.company.utils.Command;
import main.java.com.company.utils.ConnUtill;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.FieldPosition;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class AppActivity extends AppCompatActivity {

    public static final int CONNECTING_TROUBLES = 1002;
    public static final int SERVER_ERROR = 1003;
    public static final int GOTTEN = 1004;
    public static final int SENT_TEXT_TO_SERVER = 1005;

    private EditText content;
    private Button  get, send;
    private ListView text;

    private Handler messageHandler;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String sessionKey;
    private final int sessionKeyTimeOut =25000;
    private CountDownTimer timer =  new CountDownTimer(sessionKeyTimeOut, 1000) {
        @Override
        public void onTick(long l) {

        }
        public void onFinish() {
            Toast.makeText(getApplicationContext(), "Session Time Expired", Toast.LENGTH_LONG).show();
            goToMainActivity();
        }
    };

    private String selectedBook = null;
    private String fixedSelectedBook = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {

                switch (inputMessage.what) {
                    case CONNECTING_TROUBLES:
                    case SERVER_ERROR:
                        get.setEnabled(true);
                        content.setText("");
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                        break;
                    case GOTTEN:
                        get.setEnabled(true);
                        content.setText((String)inputMessage.obj);

                        break;

                    case SENT_TEXT_TO_SERVER:
                        send.setEnabled(true);
                        Toast.makeText(getApplicationContext(), (String)inputMessage.obj, Toast.LENGTH_LONG).show();
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };


        socket = SocketHandler.getSocket();
        in = SocketHandler.getInputStream();
        out = SocketHandler.getOutputStream();
        sessionKey = SocketHandler.getSessionKey();

        text = findViewById(R.id.text);
        text.setEnabled(true);
        text.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        text.setOnItemClickListener((parent, view, position, id) -> {
            timer.cancel();
            timer.start();
            selectedBook = (String)text.getItemAtPosition(position);
        });

        get = findViewById(R.id.get);
        get.setEnabled(true);

        get.setOnClickListener(view -> {
            timer.cancel();
            timer.start();

            if (selectedBook != null) {
                view.setEnabled(false);
                Thread thread1 = new Thread(new GetText(selectedBook));
                thread1.start();
            } else {
                Toast.makeText(getApplicationContext(), "Select book", Toast.LENGTH_SHORT).show();
            }
        });

        send = findViewById(R.id.send);
        send.setEnabled(true);

        send.setOnClickListener(view -> {
            timer.cancel();
            timer.start();

            if (fixedSelectedBook != null) {
                view.setEnabled(false);
                Thread thread1 = new Thread(new SendText(fixedSelectedBook));
                thread1.start();
            } else {
                Toast.makeText(getApplicationContext(), "Select book", Toast.LENGTH_SHORT).show();
            }
        });

        content = findViewById(R.id.content);

        String[] books = {"atlas_shrugged", "pride_and_prejudice", "world_without_end"};
        List<String> initialList = Arrays.asList(books);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, initialList );
        text.setAdapter(adapter);

        timer.start();
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

                byte[] encodedText = cmd_file.get_byteParam();
                System.out.println(encodedText);

                File outputDir = AppActivity.this.getCacheDir();
                File outputFile = File.createTempFile("encrypted", "txt", outputDir);
                File txtFile = File.createTempFile("decrypted", "txt", outputDir);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
                bos.write(encodedText);
                bos.flush();
                bos.close();

                FileCipher task = new FileCipher(outputFile.getPath(),
                        txtFile.getPath(), sessionKey, false, OperationMode.Mode.CFB);

                task.cryptFile();
                String contents = new String(Files.readAllBytes(Paths.get(txtFile.getPath())));

                Message message = messageHandler.obtainMessage(GOTTEN, contents);
                message.sendToTarget();

                // Fix the name of received text
                fixedSelectedBook = name;

                outputFile.deleteOnExit();
                txtFile.deleteOnExit();

            } catch (IOException | ClassNotFoundException e) {
                Message message = messageHandler.obtainMessage(CONNECTING_TROUBLES, e.toString());
                message.sendToTarget();
            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(SERVER_ERROR, e.toString());
                message.sendToTarget();
            }
        }
    }

    class SendText implements Runnable {
        private String name;
        private final SimpleDateFormat format = new SimpleDateFormat("yyyy-M-dd_HH:mm:ss");

        public SendText(String name) {
            this.name = name;
        }

        public void run() {
            try {
                Date now = new Date();
                StringBuffer dateName  = new StringBuffer();
                dateName.append("text_decrypted_");
                format.format(now, dateName, new FieldPosition(15));
                
                ConnUtill.sendMsg(out, Command.CommandType.SAVE_TEXT_INIT, dateName.toString());
                Command cmdResponse = (Command) in.readObject();
                if (cmdResponse.getCommandType().equals(Command.CommandType.ERROR)) {
                    Message message = messageHandler.obtainMessage(SENT_TEXT_TO_SERVER, cmdResponse.getParam());
                    message.sendToTarget();
                    return;
                }

                byte[] modifiedText = content.getText().toString().getBytes();

                File outputDir = AppActivity.this.getCacheDir();
                File modifiedFile = File.createTempFile("decrypted_new", "txt", outputDir);
                File encodedFile = File.createTempFile("encrypted_new", "txt", outputDir);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(modifiedFile));
                bos.write(modifiedText);
                bos.flush();
                bos.close();

                FileCipher task = new FileCipher(modifiedFile.getPath(),
                        encodedFile.getPath(), sessionKey, true, OperationMode.Mode.CFB);

                task.cryptFile();
                byte[] contents = Files.readAllBytes(Paths.get(encodedFile.getPath()));
                ConnUtill.sendMsg(out, Command.CommandType.SAVE_TEXT_PROCEED, contents);

                cmdResponse = (Command) in.readObject();
                Message message = messageHandler.obtainMessage(SENT_TEXT_TO_SERVER, cmdResponse.getParam());
                message.sendToTarget();

                modifiedFile.deleteOnExit();
                encodedFile.deleteOnExit();


            } catch (IOException e) {
                Message message = messageHandler.obtainMessage(CONNECTING_TROUBLES, e.toString());
                message.sendToTarget();
            } catch (Exception e) {
                Message message = messageHandler.obtainMessage(SERVER_ERROR, e.toString());
                message.sendToTarget();
            }
        }
    }

    private void goToMainActivity() {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
