/*
 * Copyright (c) 2009, Balazs Lecz <leczbalazs@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 * 
 *     * Neither the name of Balazs Lecz nor the names of
 *       contributors may be used to endorse or promote products derived from this
 *       software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.lecz.android.tiltmazes;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;


public class TiltMazesActivity extends Activity {
	private MazeView mMazeView;
	
	private static final int MENU_RESTART = 1;
	private static final int MENU_MAP_PREV = 2;
	private static final int MENU_MAP_NEXT = 3;
	private static final int MENU_ABOUT = 4;
	
	private Dialog aboutDialog;
	
	private TextView mMazeNameLabel;
	private TextView mRemainingGoalsLabel;

	private GestureDetector mGestureDetector;
	private GameEngine mGameEngine;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(this.toString(), "onCreate() called");

		super.onCreate(savedInstanceState);

		// Build the About Dialog
		aboutDialog = new Dialog(TiltMazesActivity.this);
		aboutDialog.setCancelable(true);
		aboutDialog.setCanceledOnTouchOutside(true);
		aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		aboutDialog.setContentView(R.layout.about_layout);

		Button aboutDialogOkButton = (Button) aboutDialog.findViewById(R.id.about_ok_button);
		aboutDialogOkButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				aboutDialog.cancel();
			}
		});
				
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.game_layout);

		mGameEngine = new GameEngine(getApplicationContext());
		mMazeView = (MazeView) findViewById(R.id.maze_view);
		mGameEngine.setTiltMazesView(mMazeView);
		mMazeView.setGameEngine(mGameEngine);
		mMazeView.calculateUnit();
		
		mMazeNameLabel = (TextView) findViewById(R.id.maze_name);
		mGameEngine.setMazeNameLabel(mMazeNameLabel);
		mMazeNameLabel.setText(mGameEngine.getMap().getName());
		
		mRemainingGoalsLabel = (TextView) findViewById(R.id.remaining_goals);
		mGameEngine.setRemainingGoalsLabel(mRemainingGoalsLabel);
		mRemainingGoalsLabel.setText("" + mGameEngine.getMap().getGoalCount());
		
		// Create gesture detector to detect flings
		mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
						
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				// Roll the ball in the direction of the fling				
				Direction mCommandedRollDirection = Direction.NONE;
				
				if (Math.abs(velocityX) > Math.abs(velocityY)) {
					if (velocityX < 0)	mCommandedRollDirection = Direction.LEFT;
					else				mCommandedRollDirection = Direction.RIGHT;
				}
				else {
					if (velocityY < 0)	mCommandedRollDirection = Direction.UP;
					else 				mCommandedRollDirection = Direction.DOWN;
				}

				if (mCommandedRollDirection != Direction.NONE) {
					mGameEngine.rollBall(mCommandedRollDirection);
				}
				
				return true;
			}
		});
		mGestureDetector.setIsLongpressEnabled(false);
		
		mGameEngine.restoreState(savedInstanceState);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			mGameEngine.rollBall(Direction.LEFT);
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			mGameEngine.rollBall(Direction.RIGHT);
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			mGameEngine.rollBall(Direction.UP);
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			mGameEngine.rollBall(Direction.DOWN);
			return true;

		default:
			return false;
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_MAP_PREV, 0, R.string.menu_map_prev);
        menu.add(0, MENU_RESTART, 0, R.string.menu_restart);
        menu.add(0, MENU_MAP_NEXT, 0, R.string.menu_map_next);
        menu.add(0, MENU_ABOUT, 0, R.string.menu_about);
        
        menu.findItem(MENU_MAP_PREV).setIcon(getResources().getDrawable(android.R.drawable.ic_media_previous));
        menu.findItem(MENU_RESTART).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_rotate));
        menu.findItem(MENU_MAP_NEXT).setIcon(getResources().getDrawable(android.R.drawable.ic_media_next));
        menu.findItem(MENU_ABOUT).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_RESTART:
        	mGameEngine.sendEmptyMessage(Messages.MSG_RESTART);
            return true;
        case MENU_MAP_PREV:
        	mGameEngine.sendEmptyMessage(Messages.MSG_MAP_PREVIOUS);
            return true;
        case MENU_MAP_NEXT:
        	mGameEngine.sendEmptyMessage(Messages.MSG_MAP_NEXT);
            return true;        	
        case MENU_ABOUT:
        	aboutDialog.show();
        	return true;
        }	
        
        return false;
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }

    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		mGameEngine.unregisterListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGameEngine.registerListener();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		mGameEngine.saveState(icicle);
		mGameEngine.unregisterListener();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);		
		mGameEngine.restoreState(savedInstanceState);
	}

	@Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
}