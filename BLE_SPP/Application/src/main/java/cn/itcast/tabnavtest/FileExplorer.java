package cn.itcast.tabnavtest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import android.widget.Toast;

import com.smartwebee.android.blespp.BluetoothLeService;
import com.smartwebee.android.blespp.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uidp5437 on 2017/8/28.
 */

public class FileExplorer extends Activity {
    ListView listView;
    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    File sdCardDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_main);
        // 获取列出全部文件的ListView
        listView = (ListView) findViewById(R.id.list);

        sdCardDir = Environment.getExternalStorageDirectory();
        File root = null;
        try {
            root = new File(sdCardDir.getCanonicalPath() + "/BLE_SPP");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (root != null) {
            currentParent = root;
            currentFiles = root.listFiles();
            // 使用当前目录下的全部文件、文件夹来填充ListView
            inflateListView(currentFiles);
        }
        // 为ListView的列表项的单击事件绑定监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0) {
                    try {
                        if (!currentParent.getCanonicalPath()
                                .equals(sdCardDir.getCanonicalPath())) {
                            // 获取上一级目录
                            currentParent = currentParent.getParentFile();
                            // 列出当前目录下所有文件
                            currentFiles = currentParent.listFiles();
                            // 再次更新ListView
                            inflateListView(currentFiles);
                            return;
                        } else {
                            Toast.makeText(FileExplorer.this, "已在sdCard根目录!!!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {


                    if (currentFiles[position - 1].isFile()) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromFile(currentFiles[position - 1]);
                        intent.setDataAndType(uri, "text/plain");
                        startActivity(intent);
                        return;
                    }

                    File[] tmp = currentFiles[position - 1].listFiles();
                    if (tmp == null || tmp.length == 0) {
                        Toast.makeText(FileExplorer.this
                                , "当前路径不可访问或该路径下没有文件",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                        currentParent = currentFiles[position - 1]; // ②
                        // 保存当前的父文件夹内的全部文件和文件夹
                        currentFiles = tmp;
                        // 再次更新ListView
                        inflateListView(currentFiles);
                    }
                }
            }
        });

    }

    private void inflateListView(File[] files)  // ①
    {
        // 创建一个List集合，List集合的元素是Map
        List<Map<String, Object>> listItems =
                new ArrayList<Map<String, Object>>();

        Map<String, Object> map =
                new HashMap<String, Object>();
        map.put("icon", R.drawable.folder);
        map.put("fileName", "...");
        listItems.add(map);

        for (int i = 0; i < files.length; i++) {
            Map<String, Object> listItem =
                    new HashMap<String, Object>();
            // 如果当前File是文件夹，使用folder图标；否则使用file图标
            if (files[i].isDirectory()) {
                listItem.put("icon", R.drawable.folder);
            } else {
                listItem.put("icon", R.drawable.file);
            }
            listItem.put("fileName", files[i].getName());
            // 添加List项
            listItems.add(listItem);
        }
        // 创建一个SimpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(this
                , listItems, R.layout.explorer_line
                , new String[]{"icon", "fileName"}
                , new int[]{R.id.icon, R.id.file_name});
        // 为ListView设置Adapter
        listView.setAdapter(simpleAdapter);
        try {

            getActionBar().setTitle(currentParent.getCanonicalPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_explorer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.web:
                Intent intent = new Intent();
                String data = "http://122.152.195.124:8080/FileUploadAndDownLoad/index.jsp";
                Uri uri = Uri.parse(data);
                intent.setData(uri);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
