package com.gmail.dailyefforts.android.reviwer;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.dailyefforts.android.reviwer.Launcher.Word;

public class TestPage extends Activity implements OnTouchListener {

	private static final String TAG = TestPage.class.getSimpleName();

	private TextView tv;

//	private OptBtn btnOpt0;
//	private OptBtn btnOpt1;
//	private OptBtn btnOpt2;

	private TextView tvBingoRate;

	private String word;
	private String meaning;

	private SparseArray<Word> map;

	private SparseArray<Word> pageMap;

	private Drawable bgColorNormal;
	private Drawable bgColorPressedBingo;
	private Drawable bgColorPressedWarning;

	private int totalNum;
	private int bingoNum;

	private boolean isFirstTouch;

	private LinearLayout optCat;

	private ArrayList<OptBtn> mOptList;

	private static final int OPTION_NUMBER = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_page);

		tv = (TextView) findViewById(R.id.tv_word);

		optCat = (LinearLayout) findViewById(R.id.opt_category);
		optCat.setWeightSum(OPTION_NUMBER);

		/*
		 * btnOpt0 = (Button) findViewById(R.id.btn_option_0); btnOpt1 =
		 * (Button) findViewById(R.id.btn_option_1); btnOpt2 = (Button)
		 * findViewById(R.id.btn_option_2);
		 */

		mOptList = new ArrayList<OptBtn>();
		
		for (int i = 0; i< OPTION_NUMBER; i++) {
			OptBtn btn = new OptBtn(this, i);
			mOptList.add(btn);
		}

		for (OptBtn tmp : mOptList) {
			optCat.addView(tmp);
			tmp.setOnTouchListener(this);
		}

		bgColorNormal = getResources()
				.getDrawable(R.drawable.opt_btn_bg_normal);
		bgColorPressedBingo = getResources().getDrawable(
				R.drawable.opt_btn_bg_pressed_bingo);
		bgColorPressedWarning = getResources().getDrawable(
				R.drawable.opt_btn_bg_pressed_warning);

		tvBingoRate = (TextView) findViewById(R.id.tv_bingo_rate);

		map = Launcher.getMap();

		buildTestCase();

	}

	private void buildTestCase() {
		Random random = new Random();

		int idx = random.nextInt(map.size());

		word = map.get(idx).getWord();
		meaning = map.get(idx).getMeaning();
		tv.setText(word);

		pageMap = new SparseArray<Word>();

		// two another different words' meaning
		ArrayList<Integer> arrList = new ArrayList<Integer>();
		while (arrList.size() < OPTION_NUMBER - 1) {
			int tmp = random.nextInt(map.size());
			if (tmp != idx && !arrList.contains(tmp)) {
				arrList.add(tmp);
			}
		}

		int answerIdx = random.nextInt(OPTION_NUMBER);

		for (int i = 0; i < mOptList.size(); i++) {
			OptBtn btn = mOptList.get(i);
			if (i == answerIdx) {
				btn.setText(meaning);
				pageMap.put(btn.getId(), map.get(idx));
			} else {
				int tmp = random.nextInt(map.size());
				btn.setText(getMeaningByIdx(tmp));
				pageMap.put(btn.getId(), map.get(tmp));
			}
		}

		isFirstTouch = true;

		if (tvBingoRate != null) {
			if (totalNum <= 0) {
				tvBingoRate.setText("");
			} else {
				tvBingoRate.setText(String.format("%d / %d  %.0f%%", bingoNum,
						totalNum, bingoNum * 100.0f / totalNum));
			}
		}

		totalNum++;
	}

	private String getMeaningByIdx(int idx) {
		if (idx >= 0 && map != null && idx < map.size()) {
			return map.get(idx).getMeaning();
		}
		return null;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean returnValue = false;
		boolean bingGo = false;
		if (v != null && event != null) {
			if (Debuger.DEBUG) {
				Log.d(TAG, "onTouch() id: " + v.getId());
				for (int i = 0; i < pageMap.size(); i++) {
					Log.d(TAG, String.format("onTouch() %d: %s", i, pageMap.get(i).toString()));
				}
			}
			Word w = pageMap.get(v.getId());
			if (w != null) {
				if (word != null && word.equals(w.getWord())) {
					bingGo = true;
				}
			}
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				((Button) v).setText(w.getWord());
				if (bingGo) {
					if (isFirstTouch) {
						bingoNum++;
					}
					v.setBackgroundDrawable(bgColorPressedBingo);
				} else {
					v.setBackgroundDrawable(bgColorPressedWarning);
				}
				returnValue = true;
				break;
			case MotionEvent.ACTION_UP:
				if (bingGo) {
					buildTestCase();
				} else {
					isFirstTouch = false;
					((Button) v).setText(w.getMeaning());
				}
				v.setBackgroundDrawable(bgColorNormal);
				returnValue = true;
				break;
			default:
				break;
			}
		}
		return returnValue;
	}

	private long lastPressedTime;
	private static final int PERIOD = 2000;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				if (event.getDownTime() - lastPressedTime < PERIOD) {
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Press again to exit.", Toast.LENGTH_SHORT).show();
					lastPressedTime = event.getEventTime();
				}
				break;
			}
			return true;
		}
		return false;
	}
}
