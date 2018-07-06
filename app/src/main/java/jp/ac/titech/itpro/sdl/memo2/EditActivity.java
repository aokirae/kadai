package jp.ac.titech.itpro.sdl.memo2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    // 保存ファイル名
    String mFileName = "";
    // 保存なしフラグ
    boolean mNotSave = false;
    // 保存ファイル日時(MM,dd,HH)
    String mDay = "";
    String preTitle = "";
    String preText = "";
    String preDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);

        // タイトルと内容入力用のEditTextを取得
        EditText eTxtTitle = (EditText)findViewById(R.id.eTxtTitle);
        EditText eTxtContent = (EditText)findViewById(R.id.eTxtContent);

        // メイン画面から情報を受け取りEditTextに設定
        //(情報がない場合(新規作成の場合)は設定しない)
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        if (name != null){
            mFileName = name;
            eTxtTitle.setText(intent.getStringExtra("TITLE"));
            eTxtContent.setText(intent.getStringExtra("CONTENT"));
            preTitle = eTxtTitle.getText().toString();
            preText = eTxtContent.getText().toString();
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
            SimpleDateFormat fullsdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.JAPAN);
            SimpleDateFormat sdf = new SimpleDateFormat("MM,dd,HH", Locale.JAPAN);
            mFileName = fullsdf.format(date) + ".txt";
            mDay = sdf.format(date);
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
            writer.print(content);
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

}
