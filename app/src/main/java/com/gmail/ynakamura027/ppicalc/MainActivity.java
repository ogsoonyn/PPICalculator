package com.gmail.ynakamura027.ppicalc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.Editable;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public class MainActivity extends Activity{

    private ArrayList<DisplayProperty> m_dplist;
	private int m_listsize_max;
	private String m_historyFilePath;
    private DisplayProperty m_curDp;

	private final int m_menu1 = Menu.FIRST,
				m_menu2 = Menu.FIRST+1,
				m_menu3 = Menu.FIRST+2;

	/*
	 *  呼び出したActivityから戻ってきた時に実行される
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d("onActivityResult", "request: " + requestCode + ", result: " + resultCode);
		
		if( resultCode == RESULT_OK ){
			// Test
			Log.d("onActivityResult", "result ok!");
	    	// 入力用ビュー
			
			DisplayProperty dptmp = (DisplayProperty) (data.getSerializableExtra("DisplayProperty"));
			
			Log.d("onActivityResult", "receive DisplayProperty ... " + dptmp.toString());
			
	    	EditText edittext_h=(EditText)findViewById(R.id.editText_Height);
	    	EditText edittext_w=(EditText)findViewById(R.id.editText_Width);
	    	EditText edittext_in=(EditText)findViewById(R.id.editText_Inch);
	    	EditText edittext_na=(EditText)findViewById(R.id.editText_Name);
	
	    	edittext_w.setText(Integer.toString(dptmp.getWidth()));
	    	edittext_h.setText(Integer.toString(dptmp.getHeight()));
	    	edittext_in.setText(Double.toString(dptmp.getInch()));
	    	edittext_na.setText(dptmp.getName());

		    // TODO: set radio button and seekbar from dptmp
            RadioGroup compGrp = (RadioGroup)findViewById(R.id.compRadioGroup);
            compGrp.check(R.id.none);

            RadioGroup dsiGrp = (RadioGroup)findViewById(R.id.dsilaneRadioGroup);
            dsiGrp.check(R.id.lane4);

            SeekBar frSeek = (SeekBar)findViewById(R.id.refreshSlider);
            frSeek.setProgress(dptmp.get_refresh());

            m_curDp = dptmp;

            //updateDisplayParams();
	    	showDisplayParams(m_curDp);
		}
	}
	
	/*
	 * onCreate
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button calcButton = (Button) findViewById(R.id.calcButton);
        Button historyButton = (Button) findViewById(R.id.historyButton);
        //m_edittext = (EditText)findViewById(R.id.editText_Inch);
        EditText edittext = (EditText) findViewById(R.id.editText_Name);
        RadioGroup dsilaneGrp = (RadioGroup)findViewById(R.id.dsilaneRadioGroup);
        RadioGroup compGrp = (RadioGroup)findViewById(R.id.compRadioGroup);
        SeekBar refreshSeek = (SeekBar)findViewById(R.id.refreshSlider);
        final TextView refreshText = (TextView)findViewById(R.id.refreshText);
        
        m_listsize_max = 100; // 暫定
        m_historyFilePath = Environment.getExternalStorageDirectory().getPath()+"/ppicalc_history.txt";
        //m_historyFilePath = "ppicalc_history.txt";
        m_curDp = new DisplayProperty();
        m_dplist = new ArrayList<DisplayProperty>();

        // 入力履歴．デフォルトで適当なのがいくつか入ってる
        if( !readHistoryList() ) {
            Log.d("onCreate", "History file not found... Initialize DPlist!");
        }
        WindowManager wm = getWindowManager();
        Display dp = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        Point size = new Point();
        dp.getMetrics(dm);
        int wid =0, hei=0;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dp.getRealSize(size);
            wid = size.x;//dp.getWidth();//size.x;
            hei = size.y;//dp.getHeight();//size.y;
        }else {
            try{
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                wid = (Integer)getRawWidth.invoke(dp);
                hei = (Integer)getRawHeight.invoke(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double inchx = wid / dm.xdpi;
        double inchy = hei / dm.ydpi;
        BigDecimal big = new BigDecimal(Math.sqrt(inchx*inchx + inchy*inchy));
        double inch = (big.setScale(1, BigDecimal.ROUND_HALF_UP)).doubleValue();

        DisplayProperty dip = new DisplayProperty(wid, hei, inch, "Your Device"); // Your Device?

            showDisplayParams(dip);


        
        // Calculateボタン押下時の処理
        calcButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // PPI計算して表示
                updateDisplayParams();
                showDisplayParams(m_curDp);

                // 履歴ファイルに保存
                writeHistoryList();
            }

        });
        
        // Historyボタン押下時の処理
        historyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 過去に入力したパラメータの表示
                Intent i = new Intent(getApplicationContext(), HistoryListActivity.class);
                //startActivity(i);
                i.putExtra("DisplayPropertyList", m_dplist);
                startActivityForResult(i, 0); // 戻り値取得
            }
        });
        
        // Inch入力後Enterした時の処理
        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("onEditorAction", "action ID = " + actionId + ", event = " + (event == null ? "null" : event));
                if (event == null && actionId == 6) {
                    updateDisplayParams();
                    showDisplayParams(m_curDp);
                    // 履歴ファイルに保存
                    writeHistoryList();

                    return true;
                }
                return false;
            }
        });

        // Refresh Rate変更時の処理
        refreshSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // set label
                TextView refText = (TextView)findViewById(R.id.refreshText);
                refText.setText(Integer.toString(progress));

                // update param
                m_curDp.set_refresh(progress);
                showDisplayParams(m_curDp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // DSI Line変更時の処理
        dsilaneGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == -1){
                    // no one selected
                }else{
                    //RadioButton selected = (RadioButton)findViewById(checkedId);
                    if(checkedId == R.id.lane1){
                        m_curDp.set_dsi_lane_num(1);
                    }else if(checkedId == R.id.lane2){
                        m_curDp.set_dsi_lane_num(2);
                    }else if(checkedId == R.id.lane3){
                        m_curDp.set_dsi_lane_num(3);
                    }else if(checkedId == R.id.lane4){
                        m_curDp.set_dsi_lane_num(4);
                    }else if(checkedId == R.id.lane8){
                        m_curDp.set_dsi_lane_num(8);
                    }

                    showDisplayParams(m_curDp);
                }
            }
        });

        // Compression変更時の処理
        compGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == -1){
                    // no one checked
                }else{
                    if(checkedId == R.id.none){
                        m_curDp.set_compress_rate(1);
                    }else if(checkedId == R.id.half){
                        m_curDp.set_compress_rate(1.0/2);
                    }else if(checkedId == R.id.oneThird){
                        m_curDp.set_compress_rate(1.0/3);
                    }
                    showDisplayParams(m_curDp);
                }
            }
        });
    }

	/*
	 * 本体を回転させた時にパラメータを再計算させる
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		
		showDisplayParams(m_curDp);
    	
	}

    /*
    入力ビューの値からDisplayPropertyを更新
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private DisplayProperty updateDisplayParams(){
        // 入力用ビュー
        EditText edittext_h=(EditText)findViewById(R.id.editText_Height);
        EditText edittext_w=(EditText)findViewById(R.id.editText_Width);
        EditText edittext_in=(EditText)findViewById(R.id.editText_Inch);
        EditText edittext_na=(EditText)findViewById(R.id.editText_Name);

        // 入力 読み込み用
        Editable input_h = edittext_h.getText();
        Editable input_w = edittext_w.getText();
        Editable input_in = edittext_in.getText();
        Editable input_na = edittext_na.getText();

        // 解像度、サイズの読み込み
        int w=1, h=1;
        Double inch = 1.0;
        String name="-";
        try{
            h = Integer.parseInt(input_h.toString());
        }catch(NumberFormatException e){
            // Error!!
            return m_curDp;
        }

        try{
            w = Integer.parseInt(input_w.toString());
        }catch(NumberFormatException e){
            //error!!
            return m_curDp;
        }

        try{
            inch = Double.parseDouble(input_in.toString());
        }catch(NumberFormatException e){
            //error!!
            return m_curDp;
        }

        name = input_na.toString();
        if(name.isEmpty()){
            name = "-";
        }

        m_curDp.Update(w, h, inch, name);
        return m_curDp;
    }

    // ListにDisplayPropertyを追加
    private void addDisplayPropertyToList(DisplayProperty dp){
        // リストサイズが最大値を超えていたら，一番最後の項目を削除
        if( m_dplist.size() >= m_listsize_max ){
            Log.d("List", "remove zero");
            m_dplist.remove(0);
        }

        if( !m_dplist.contains(dp) ){
            Log.d("List", "add [" + dp.toString() + "]");
            m_dplist.add(dp);
        }else{
            Log.d("List", "set index of dp");
            m_dplist.set(m_dplist.indexOf(dp), dp);
        }
    }

	/*
	 * DisplayPropertyからPPIなどを算出、表示
	 */
    private void showDisplayParams(DisplayProperty dp){
    	// 結果表示用のビュー
    	TextView ppiview = (TextView)findViewById(R.id.ppiValueView);
    	TextView pixelview = (TextView)findViewById(R.id.pixelValueView);
    	TextView fclkview = (TextView)findViewById(R.id.fClockView);
    	TextView arview = (TextView)findViewById(R.id.ARView);
    	TextView sizeview = (TextView)findViewById(R.id.sizeView);
		TextView dsiclkview = (TextView)findViewById(R.id.dsiClkView);
    	
    	// 解像度、インチ数表示用ビュー
    	TextView resolview = (TextView)findViewById(R.id.resolView);

    	// 計算！
    	// まずクラス作成
    	//DisplayProperty dp = new DisplayProperty(w, h, inch, name);
        //m_curDp = new DisplayProperty(w, h, inch, name);

    	// リストに追加
        addDisplayPropertyToList(dp);

    	Log.d("showDisplayParams", "list size = " + ((Integer) m_dplist.size()).toString());
    	
    	// 表示
        Integer tmpi;
        Double tmpd;
        DecimalFormat df = new DecimalFormat("#.##");
        String text;

        // show PPI
        tmpd = dp.calcPpi();
        text = df.format(tmpd) + " ppi";
    	ppiview.setText(text);

        // show Pixel number
        tmpi = dp.calcPixel() / 1000;
        text = tmpi.toString() + " kPixel";
    	pixelview.setText(text);

        // show Pixel clock
        tmpd = dp.calcPixelClock() / 1000 / 1000;
        text = df.format(tmpd) + " MHz";
        fclkview.setText(text);

        // show DSC clock
        tmpd = dp.calcDsiClock() / 1000 / 1000;
        text = df.format(tmpd) + " Mbps / ";
        tmpi = dp.get_dsi_lane_num();
        text += tmpi.toString() + "lane";
        tmpd = dp.get_compress_rate();
        if(tmpd != 1){
            NumberFormat nf = NumberFormat.getPercentInstance();
            text += "\n" + nf.format(tmpd) + " comp";
        }

        dsiclkview.setText(text);

        // show Aspect ratio
        tmpd = dp.getARW();
        text = df.format(tmpd) + " : ";
        tmpd = dp.getARH();
        text += df.format(tmpd);
    	arview.setText(text);

        // show Physical size
        tmpd = dp.calcDisplayInchW();
        text = df.format(tmpd) + " x ";
        tmpd = dp.calcDisplayInchH();
        text += df.format(tmpd) + " inch\n";
        tmpd = dp.calcDisplayInchW()*25.4; // inch to milli
        text += df.format(tmpd) + " x ";
        tmpd = dp.calcDisplayInchH()*25.4; // inch to milli
        text += df.format(tmpd) + " mm";
    	sizeview.setText(text);

        // show Resolution Information
        resolview.setText(dp.toString());

    	// ソフトキーボード隠す
    	InputMethodManager imm
    	= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        	imm.hideSoftInputFromWindow
        	(ppiview.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, m_menu1, Menu.NONE, "Clear History");
    	menu.add(Menu.NONE, m_menu2, Menu.NONE, "Add Example to History");
    	menu.add(Menu.NONE, m_menu3, Menu.NONE, "Preference");
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	// ソフトキーボード隠す
		TextView ppiview = (TextView)findViewById(R.id.ppiValueView);
		InputMethodManager imm = 
				(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ppiview.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
    	switch (item.getItemId()) {
    	case m_menu1:
    		clearHistoryList();
    		Toast.makeText(getApplicationContext(), "Clear History!", Toast.LENGTH_SHORT).show();
    		return true;
    	case m_menu2:
    		addExampleToHistoryList();
    		Toast.makeText(getApplicationContext(), "Add Example to History!", Toast.LENGTH_SHORT).show();
    		return true;
    		
    	case m_menu3:
    		
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
            String ver = "0";
            try{
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
                ver = pi.versionName;
            }catch(NameNotFoundException e){
            	e.printStackTrace();
            }
            // ダイアログの設定
            alertDialog.setIcon(R.drawable.ic_launcher);   //アイコン設定
            alertDialog.setTitle("PPI Calc");      //タイトル設定
            alertDialog.setMessage("Version:\n  " + ver + "\n\n" +
            		"Last Update:\n  2014/04/29");  //内容(メッセージ)設定
     
            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // OKボタン押下時の処理
                    Log.d("AlertDialog", "Positive which :" + which);
                }
            });
     
            // ダイアログの作成と描画
//            alertDialog.create();
            alertDialog.show();
            return true;
    	}
    	return false;
    }
    
    // 履歴データをファイルに保存
    protected boolean writeHistoryList(){
    	String sData = "";
    	Log.d("writeHistoryList", "start");

        FileReadWrite.removeFile(m_historyFilePath);
    	
    	int i=0;
    	while( i<m_dplist.size() ){
    		sData += Integer.toString(m_dplist.get(i).getWidth()) + "\n"
    				+ Integer.toString(m_dplist.get(i).getHeight()) + "\n"
    				+ Double.toString(m_dplist.get(i).getInch())+ "\n"
    				+ m_dplist.get(i).getName()+ "\n";
    		Log.d("writeHistoryList", "write history ... #"+Integer.toString(i));
    		i++;
    	}
    	
    	if(i > 0)
    		FileReadWrite.writeFile(m_historyFilePath, sData, "UTF-8");
    	
    	return i>0;
    }
    
    // 履歴データをファイルから読み出し
    protected  boolean readHistoryList(){
        ArrayList<DisplayProperty> dplist = new ArrayList<DisplayProperty>();
        if( new File(m_historyFilePath).exists() ){
            Log.d("readHistoryList", "start reading...");

            BufferedReader bf = null;
            String filestr = FileReadWrite.readFile(m_historyFilePath, "UTF-8");
            try {

                InputStream bais = new ByteArrayInputStream(filestr.getBytes("UTF-8"));
                bf = new BufferedReader(new InputStreamReader(bais, "UTF-8"));
            }catch(Exception e){
                e.printStackTrace();;
            }

            try{
                String sLine, name;
                int width, height;
                double inch;
                while ((sLine = bf.readLine()) != null) {
                    width = Integer.parseInt(sLine);
                    height = 0;
                    inch = 0;
                    name = "-";

                    if ((sLine = bf.readLine()) != null) {
                        height = Integer.parseInt(sLine);
                    }

                    if ((sLine = bf.readLine()) != null) {
                        inch = Double.parseDouble(sLine);
                    }

                    if ((sLine = bf.readLine()) != null) {
                        name = sLine;
                    }

                    dplist.add(new DisplayProperty(width, height, inch, name));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 読み出せたら（リストがひとつ以上埋まれば）真
            if(!dplist.isEmpty()){
                m_dplist = dplist;
                return true;
            }

        }
        return false;
    }


    protected void clearHistoryList(){
    	m_dplist.clear();
        FileReadWrite.removeFile(m_historyFilePath);
    }
    
    protected void addExampleToHistoryList(){
	    //addDisplayPropertyToList(new DisplayProperty(7680, 4320, 84)); // 4K2K module
		//addDisplayPropertyToList(new DisplayProperty(4096, 2192, 36.4)); // 4K2K module
        addDisplayPropertyToList(new DisplayProperty(3840, 2160, 31.5, "SHARP PN-K321")); // QFHD module
        addDisplayPropertyToList(new DisplayProperty(3840, 2160, 23.8, "DELL UP2414Q")); // QFHD module
        addDisplayPropertyToList(new DisplayProperty(2560, 1600, 8.9, "KindleFire HDX 8.9"));
		addDisplayPropertyToList(new DisplayProperty(2560, 1600, 10.1, "Nexus 10")); // Nexus 10
        addDisplayPropertyToList(new DisplayProperty(1920, 1200, 7, "Nexus 7 2013")); // Nexus 7 2013
        addDisplayPropertyToList(new DisplayProperty(1280, 800, 7, "Nexus 7 2012")); // Nexus 7
        addDisplayPropertyToList(new DisplayProperty(2048, 1536, 7.9, "iPad mini retina")); // iPad mini
        addDisplayPropertyToList(new DisplayProperty(1024, 768, 7.9, "iPad mini")); // iPad mini
        addDisplayPropertyToList(new DisplayProperty(2048, 1536, 9.7, "iPad Air")); // iPad 4
        addDisplayPropertyToList(new DisplayProperty(1920, 1080, 15.6, "NotePC"));
        addDisplayPropertyToList(new DisplayProperty(1366, 768, 13.3, "NotePC"));
        addDisplayPropertyToList(new DisplayProperty(1920, 1080, 5.5, "SmartPhone")); // SmartPhone
        addDisplayPropertyToList(new DisplayProperty(1920, 1080, 5.0, "SmartPhone")); // SmartPhone
        addDisplayPropertyToList(new DisplayProperty(1280, 720, 4.8, "SmartPhone")); // SmartPhone
        addDisplayPropertyToList(new DisplayProperty(1136, 640, 4.0, "iPhone 5")); // SmartPhone
        addDisplayPropertyToList(new DisplayProperty(960, 640, 3.5, "iPhone 4")); // SmartPhone

    }
}
