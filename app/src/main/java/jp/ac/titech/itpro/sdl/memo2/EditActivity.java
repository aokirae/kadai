package jp.ac.titech.itpro.sdl.memo2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


public class EditActivity extends AppCompatActivity implements TextWatcher{

    // 保存ファイル名
    String mFileName = "";
    // 保存なしフラグ
    boolean mNotSave = false;

    // 入力されていた内容とその日付
    List<String> preContent = new ArrayList<String>();
    List<String> preDate = new ArrayList<String>();

    // 変更された内容
    List<String> nextContent;
    List<String> nextDate = new ArrayList<String>();

    // 日時
    TextView eTxtDate;

    // 改行コード
    String sep = System.lineSeparator();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);

        // タイトルと内容入力用のEditTextを取得
        EditText eTxtTitle = (EditText)findViewById(R.id.eTxtTitle);
        EditText eTxtContent = (EditText)findViewById(R.id.eTxtContent);
        eTxtDate = (TextView)findViewById(R.id.eTxtDate);


        // TextWatcherを登録
        eTxtContent.addTextChangedListener(this);

        // メイン画面から情報を受け取りEditTextに設定
        //(情報がない場合(新規作成の場合)は設定しない)
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        if (name != null){
            mFileName = name;
            eTxtTitle.setText(intent.getStringExtra("TITLE"));
            eTxtContent.setText(intent.getStringExtra("CONTENT"));
            eTxtDate.setText(intent.getStringExtra("DATE"));

            // 編集画面を開いた時の内容を保存する
            preContent = Arrays.asList(eTxtContent.toString().split(sep));
            preDate = Arrays.asList(eTxtDate.getText().toString().split(sep));

            Log.d("EditActivityon","preDateSize:" + preDate.size());
            Log.d("EditActivityon", "preDate:" + eTxtDate.getText().toString());
            nextDate = new ArrayList<String>(preDate);
        }

    }

    @Override
    protected void onPause(){
        super.onPause();

        //[削除]で画面を閉じるときは保存しない
        if (mNotSave){
            return;
        }

        // タイトル,内容を取得
        EditText eTxtTitle = (EditText)findViewById(R.id.eTxtTitle);
        EditText eTxtContent = (EditText)findViewById(R.id.eTxtContent);
        TextView eTxtDate = (TextView)findViewById(R.id.eTxtDate);
        String title = eTxtTitle.getText().toString();
        String content = eTxtContent.getText().toString();
        String eDate = eTxtDate.getText().toString();

        // タイトル,内容が空白の場合、保存しない
        if (title.isEmpty() && content.isEmpty()){
            Toast.makeText(this, R.string.msg_destruction, Toast.LENGTH_SHORT).show();
            return ;
        }


        // ファイル名を生成
        // ファイル名 : yyyyMMdd_HHmmssSSS.txt
        // すでに保存されているファイルはそのままのファイル名とする
        if (mFileName.isEmpty()){
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.JAPAN);
            mFileName = sdf.format(date) + ".txt";
        }

        // contentにdateを結合する
        List<String> tempContent = Arrays.asList(content.split(sep));
        List<String> tempDate = Arrays.asList(eDate.split(sep));
        String dateContent = "";
        for (int i = 0; i < tempContent.size(); i++){
            dateContent = dateContent + tempDate.get(i) + sep;
            dateContent = dateContent + tempContent.get(i) + sep;
        }


        // 保存
        OutputStream out = null;
        PrintWriter writer = null;
        try {
            out = this.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
            // タイトル書き込み
            writer.println(title);
            // 内容書き込み
            writer.print(dateContent);
            writer.close();
            out.close();
        }
        catch (Exception e){
            Toast.makeText(this,"File save error!", Toast.LENGTH_LONG).show();
        }
    }

    // メニュー生成
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.action_del){
            // [削除]選択処理
            // ファイル削除
            if (!mFileName.isEmpty()){
                if (this.deleteFile((mFileName))){
                    Toast.makeText(this,R.string.msg_del, Toast.LENGTH_SHORT).show();
                }
            }
            // 保存せずに画面を閉じる
            mNotSave = true;
            this.finish();
            Intent intent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TextWatcherのやつ
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after){
    }

    @Override
    public void onTextChanged(CharSequence s,int start, int before, int count){
    }

    @Override
    public void afterTextChanged(Editable s){
        // 現在の文字列をList(改行区切り)に変換
        nextContent = Arrays.asList(s.toString().split(sep));
        // 日時を取得
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH", Locale.JAPAN);
        String nowDate = sdf.format(date);

        // 差分を取得
        Patch<String> diff = DiffUtils.diff(preContent,nextContent);


        List<Delta<String>> deltas = diff.getDeltas();
        int line = 0;
        for (Delta<String> delta : deltas){
            Delta.TYPE type = delta.getType();
            Chunk<String> oc = delta.getOriginal();
            Chunk<String> rc = delta.getRevised();
            Log.d("afterTextChanged","contentSize:"+nextContent.size());
            Log.d("afterTextChanged", "dateSize:"+nextDate.size());
            switch (type){
                case DELETE:
                    Log.d("afterTextChanged","DELETE");
                    nextDate.remove(oc.getPosition()+line);
                    line--;
                    break;
                case CHANGE:
                    Log.d("afterTextChanged","CHANGE");
                    nextDate.set(rc.getPosition()+line, nowDate);
                    break;
                case INSERT:
                    Log.d("afterTextChanged","INSERT");
                    nextDate.add(rc.getPosition()+line, nowDate);
                    line++;
                    break;
            }
        }


        String setNextDate = "";
        for (int i = 0; i < nextDate.size(); i++){
            setNextDate = setNextDate + nextDate.get(i) + sep;
        }
        eTxtDate.setText(setNextDate);
        preContent = nextContent;

    }

}
