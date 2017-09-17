package com.gmail.ynakamura027.ppicalc;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class HistoryListActivity extends ListActivity {
	
	private ArrayList<DisplayProperty> m_dps;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		ArrayList<DisplayProperty> list;

		list = (ArrayList<DisplayProperty>)getIntent().getSerializableExtra("DisplayPropertyList");

		m_dps = new ArrayList<DisplayProperty>();

		// 逆順でリストに入れる
		int i=list.size()-1;
		while( i >= 0 ){
			Log.d("onCreate", "list - " + list.get(i) + " --> m_dps");
			m_dps.add(list.get(i));
			i--;
		}

		// リスト作成
		ArrayAdapter<DisplayProperty> adapter = new ArrayAdapter<DisplayProperty>(
				this, android.R.layout.simple_list_item_1, (DisplayProperty[])m_dps.toArray(new DisplayProperty[0]));

		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView listView, View v, int position, long id){
		super.onListItemClick(listView, v, position, id);
		
		DisplayProperty dp = (DisplayProperty) listView.getItemAtPosition(position);
		Log.d("onListItemClick", dp.toString());
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		//i.putExtra("Log", listView.getItemAtPosition(position).toString());
		i.putExtra("DisplayProperty", dp);
		setResult(RESULT_OK, i);
		finish();
	}
}
