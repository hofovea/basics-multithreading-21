package com.artemchep.basics_multithreading;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artemchep.basics_multithreading.cipher.CipherUtil;
import com.artemchep.basics_multithreading.cipher.CypherManager;
import com.artemchep.basics_multithreading.cipher.CypherTask;
import com.artemchep.basics_multithreading.cipher.ICypher;
import com.artemchep.basics_multithreading.domain.Message;
import com.artemchep.basics_multithreading.domain.WithMillis;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<WithMillis<Message>> mList = new ArrayList<>();

    private MessageAdapter mAdapter = new MessageAdapter(mList);

    private final CypherManager<CypherTask> manager = new CypherManager<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        showWelcomeDialog();
        manager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.stop();
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
                .setMessage("What are you going to need for this task: Thread, Handler.\n" +
                        "\n" +
                        "1. The main thread should never be blocked.\n" +
                        "2. Messages should be processed sequentially.\n" +
                        "3. The elapsed time SHOULD include the time message spent in the queue.")
                .show();
    }

    public void onPushBtnClick(View view) {
        Message message = Message.generate();
        insert(new WithMillis<>(message));
    }

    @UiThread
    public void insert(final WithMillis<Message> message) {
        mList.add(message);
        mAdapter.notifyItemInserted(mList.size() - 1);

        CypherTask task = new CypherTask(message.value.plainText, System.currentTimeMillis());
        task.setCypherCallback(new ICypher() {
            @Override
            public void updateUICallback(String cypheredText, long executionTime) {
                final Message messageNew = message.value.copy(cypheredText); // call with cyphered text
                final WithMillis<Message> messageNewWithMillis = new WithMillis<>(messageNew, executionTime);  //call with elapsed time
                update(messageNewWithMillis);
            }
        });
        manager.postTask(task);

        // final Message messageNew = message.value.copy(CipherUtil.encrypt(message.value.plainText));
        // TODO: Start processing the message (please use CipherUtil#encrypt(...)) here.
        //       After it has been processed, send it to the #update(...) method.

        // How it should look for the end user? Uncomment if you want to see. Please note that
        // you should not use poor decor view to send messages to UI thread.
//        getWindow().getDecorView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final Message messageNew = message.value.copy("sample :)"); // call with cyphered text
//                final WithMillis<Message> messageNewWithMillis = new WithMillis<>(messageNew, CipherUtil.WORK_MILLIS);  //call with elapsed time
//                update(messageNewWithMillis);
//            }
//        }, CipherUtil.WORK_MILLIS);
    }

    @UiThread
    public void update(final WithMillis<Message> message) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).value.key.equals(message.value.key)) {
                mList.set(i, message);
                mAdapter.notifyItemChanged(i);
                return;
            }
        }

        throw new IllegalStateException();
    }

}
