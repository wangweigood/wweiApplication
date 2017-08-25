package cn.itcast.tabnavtest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.smartwebee.android.blespp.BleSppActivity;
import com.smartwebee.android.blespp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uidp5437 on 2017/7/7.
 */
public class PlaceholderFragment extends Fragment {

    String[] items = new String[]{
            "删除该数据", "修改该数据", "删除整行数据", "插入一行数据"
    };
    int current;
    int numColumn;
    List<Map<String, Object>> listItems;
    SimpleAdapter simpleAdapter;
    Map<String, Object> map;
    int newI;
    GridView gridView;

    public PlaceholderFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        gridView = (GridView) inflater.inflate(R.layout.grid, null);

        String[] str = new String[1200];
        String[] arr = {

                "Index", "Freq", "PI", "PI Conf", "PS Name", "PTY"
        };

        numColumn = arr.length;
        gridView.setNumColumns(arr.length);

        listItems = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            str[i] = arr[i];
        }

        for (int i = 0; i < arr.length; i++) {

            Map<String, Object> listItem = new HashMap<>();
            listItem.put("name", str[i]);
            listItems.add(listItem);
        }
        for (int i = 1; i < str.length / arr.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("name", i + "");
            listItems.add(listItem);

            if (BleSppActivity.bundle != null) {
                map = new HashMap<>();
                map.put("name", BleSppActivity.bundle.getString("frequency" + i));
                listItems.add(map);

                map = new HashMap<>();
                map.put("name", BleSppActivity.bundle.getString("PI" + i));
                listItems.add(map);

                map = new HashMap<>();
                map.put("name", BleSppActivity.bundle.getString("confidence" + i));
                listItems.add(map);

                map = new HashMap<>();
                map.put("name", BleSppActivity.bundle.getString("psName" + i));
                listItems.add(map);

                map = new HashMap<>();
                map.put("name", BleSppActivity.bundle.getString("pty" + i));
                listItems.add(map);

            }

            while (listItems.size() % arr.length != 0) {
                map = new HashMap<>();
                map.put("name", "");
                listItems.add(map);
            }
        }


        simpleAdapter = new SimpleAdapter(getContext(), listItems, R.layout.special2, new String[]{"name"}, new int[]{R.id.text});
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                current = i;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("请选择你的操作")
                        .setIcon(R.drawable.tools).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                newI = i;
                            }
                        });

                setPositiveButton(builder);
                setNegativeButton(builder).create().show();

            }
        });


        return gridView;
    }

    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {

        return builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (newI) {

                    case 0:
                        map = new HashMap<>();
                        map.put("name", "");
                        listItems.set(current, map);
                        simpleAdapter.notifyDataSetChanged();
                        newI = 0;
                        break;
                    case 1:
                        map = new HashMap<>();
                        map.put("name", "5437");
                        listItems.set(current, map);
                        simpleAdapter.notifyDataSetChanged();
                        newI = 0;
                        break;
                    case 2:
                        map = new HashMap<>();
                        map.put("name", "");
                        while (current % numColumn != 0) {
                            current--;
                        }
                        for (int t = 0; t < numColumn; t++) {

                            listItems.set(current + t, map);
                        }
                        simpleAdapter.notifyDataSetChanged();
                        newI = 0;
                        break;
                    case 3:

                        while (current % numColumn != 0) {
                            current--;
                        }
                        for (int t = 0; t < numColumn; t++) {
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("name", t + "");
                            listItems.add(current + t, map2);
                        }
                        simpleAdapter.notifyDataSetChanged();
                        newI = 0;
                        break;

                }

            }

        });
    }

    private AlertDialog.Builder setNegativeButton(AlertDialog.Builder builder) {
        return builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }


}

