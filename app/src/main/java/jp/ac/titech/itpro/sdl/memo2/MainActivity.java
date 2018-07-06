package jp.ac.titech.itpro.sdl.memo2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //ListView 用アダプタ
    SimpleAdapter mAdapter = null;
    // ListViewに設定するデータ
    List<Map<String,String>> mList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // ListView用アダプタのリストを生成
        mList = new ArrayList<Map<String, String>>();
        // ListView用アダプタを生成
        mAdapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String[]{"title","content"},
                new int[]{android.R.id.text1, android.R.id.text2});

        // ListViewにアダプタをセット
        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(mAdapter);



        //ListViewのアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 編集画面に渡すデータをセットし表示
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("NAME", mList.get(position).get("filename"));
                intent.putExtra("TITLE",mList.get(position).get("title"));
                intent.putExtra("CONTENT", mList.get(position).get("content"));
                startActivity(intent);
            }
        });

        // ListViewをコンテキストメニューに登録
        registerForContextMenu(list);
    }

    @Override
    protected void onResume(){
        super.onResume();

        // ListView用アダプタのデータをクリア
        mList.clear();

        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        // ファイル名の降順でそーと
        Arrays.sort(files, Collections.<File>reverseOrder());
        // .txtを取得し、ListView用アダプタのリストにセット
        for (int i = 0; i < files.length; i++){
            String fileName = files[i].getName();
            if (files[i].isFile() && fileName.endsWith(".txt")){
                String title = null;
                String content = null;
                // ファイルを読み込み
                try {
                    InputStream in = this.openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                    char[] buf = new char[(int)files[i].length()];
                    //タイトルを読み込み
                    title = reader.readLine();
                    //内容を読み込み
                    int num = reader.read(buf);
                    content = new String(buf,0,num);

                    reader.close();
                    in.close();
                }
                catch (Exception e){
                    Toast.makeText(this, "File read error!", Toast.LENGTH_LONG).show();
                }

                // ListView用のアダプタにデータをセット
                Map<String,String> map = new HashMap<String, String>();
                map.put("filename",fileName);
                map.put("title",title);
                map.put("content",content);
                mList.add(map);
            }
        }

        // ListViewのデータ変更を表示に反映
        mAdapter.notifyDataSetChanged();
    }

    // アイコン追加 ?
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.action_add){
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // コンテキストメニュー作成処理
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info){
        super.onCreateContextMenu(menu, view, info);
        getMenuInflater().inflate(R.menu.main_context,menu);
    }


    // コンテキストメニュー選択処理
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.context_del:
                // [削除]選択時の処理
                // ファイル削除
                if (this.deleteFile(mList.get(info.position).get("filename"))){
                    Toast.makeText(this, R.string.msg_del, Toast.LENGTH_SHORT).show();
                }
                // リストから削除
                mList.remove(info.position);
                //ListViewのデータ変更を表示に反映
                mAdapter.notifyDataSetChanged();
                break;
        }
        return false;
    }


}
