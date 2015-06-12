package com.youngsee.adplayer.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.R;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.manager.ProgramManager;
import com.youngsee.adplayer.pgm.MediaRef;
import com.youngsee.adplayer.util.BitmapUtil;
import com.youngsee.adplayer.util.FileUtils;
import com.youngsee.adplayer.util.TypefaceUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.ViewSwitcher.ViewFactory;

public class AdMultiMediaView extends AdView {

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SHOWPROGBAR = EVENT_BASE + 0;
	private final int EVENT_PLAYVIDEO = EVENT_BASE + 1;
	private final int EVENT_SHOWPICTURE = EVENT_BASE + 2;
	private final int EVENT_HIDEALLVIEWS = EVENT_BASE + 3;

	private final long DEFAULT_MEDIA_DURATION = 1000;

	private final long DEFAULT_THREAD_PERIOD = 1000;
	private final long DEFAULT_THREAD_QUICKPERIOD = 100;
	
	private final long DEFAULT_ANIMATION_DURATION = 1000;
	
	private final int SHOWTYPE_NONE = 0;
	private final int SHOWTYPE_PROGBAR = 1;
	private final int SHOWTYPE_VIDEO = 2;
	private final int SHOWTYPE_PICTURE = 3;
	
	private LinearLayout mProgBarLyt = null;
	private SurfaceView mSurfaceView = null;
	private ImageSwitcher mImageSwitcher = null;
	
	private SurfaceHolder mSurfaceHolder = null;
	
	private MyThread mMyThread = null;
	
	private int mCurrentIndex = -1;
	private boolean mIsPlayingVideo = false;
	private int mCurrentShowType = SHOWTYPE_NONE;
	
	private MediaPlayer mMediaPlayer = null;
	
	private MediaRef mCurrentMedia = null;
	private int mMediaPosition = -1;
	
	private boolean mSkipMedia = false;

	public AdMultiMediaView(Context context) {
		super(context);

		initView(context);
	}
	
	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_ad_multimedia, this);

		mProgBarLyt = (LinearLayout)findViewById(R.id.ad_multimedia_progbarlyt);
		mSurfaceView = (SurfaceView)findViewById(R.id.ad_multimedia_surfaceview);
		mImageSwitcher = (ImageSwitcher)findViewById(R.id.ad_multimedia_imgswitcher);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSurfaceHolder.addCallback(new SurfaceHolderCallBack());
		
		mImageSwitcher.setFactory(mViewFactory);
	}
	
	private final class SurfaceHolderCallBack implements SurfaceHolder.Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			mLogger.i("Surface is changed.");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mLogger.i("Surface is created.");
			if (mCurrentMedia != null) {
				playVideo(mCurrentMedia);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mLogger.i("Surface is destroyed.");
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPosition = mMediaPlayer.getCurrentPosition();
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
	            mMediaPlayer = null;
			}
		}
		
	}
	
	private MediaPlayer.OnPreparedListener mMediaPlayerPreparedListener =
			new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
        	mMediaPlayer.start();
        	if (mMediaPosition != -1) {
        		mMediaPlayer.seekTo(mMediaPosition);
        		mMediaPosition = -1;
        	}
        }
    };
    
    private MediaPlayer.OnCompletionListener mMediaPlayerCompletionListener =
    		new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
        	mLogger.i("Media player completion.");
        	
        	if (mCurrentMedia != null) {
        		mCurrentMedia.playtimes++;
        	}
        	mIsPlayingVideo = false;
        }
    };
    
    private MediaPlayer.OnErrorListener mMediaPlayerErrorListener =
    		new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
        	mLogger.i("Media player error.");
            stopVideo();
            mIsPlayingVideo = false;

            return true;
        }
    };
    
    private ViewFactory mViewFactory = new ViewFactory() {
		@Override
		public View makeView() {
			ImageView imgview = new ImageView(mContext);
			imgview.setScaleType(ScaleType.FIT_XY);
			imgview.setLayoutParams(new ImageSwitcher.LayoutParams(
					ImageSwitcher.LayoutParams.MATCH_PARENT,
					ImageSwitcher.LayoutParams.MATCH_PARENT));

            return imgview;
		}
    };

	@Override
	public void onPause() {
		if ((mMyThread != null) && !mMyThread.isPaused()) {
			mMyThread.onPause();
		}
		cleanupMsg();
	}

	@Override
	public void onResume() {
		if ((mMyThread != null) && mMyThread.isPaused()) {
			mMyThread.onResume();
		}
	}

	@Override
	public void onDestroy() {
		if (mMyThread != null) {
			mMyThread.cancel();
			mMyThread = null;
		}
		cleanupMsg();
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
	}
	
	private void cleanupMsg() {
    	mHandler.removeMessages(EVENT_SHOWPROGBAR);
    	mHandler.removeMessages(EVENT_PLAYVIDEO);
    	mHandler.removeMessages(EVENT_SHOWPICTURE);
    }

	@Override
	public void start() {
		if (mMediaLst == null) {
			mLogger.i("Media list is null.");
			return;
		} else if (mMediaLst.isEmpty()) {
			mLogger.i("No media in the list.");
			return;
		}

        if (mMyThread == null) {
        	mMyThread = new MyThread();
    		mMyThread.start();
        } else if (mMyThread.isPaused()) {
        	mMyThread.onResume();
        }
    }

	@Override
	public void stop() {
		if ((mMyThread != null) && !mMyThread.isPaused()) {
			mMyThread.onPause();
		}
		cleanupMsg();
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
		mCurrentIndex = -1;
		mIsPlayingVideo = false;
		mCurrentShowType = SHOWTYPE_NONE;
		mCurrentMedia = null;
		mMediaPosition = -1;
		mSkipMedia = false;
	}

	private MediaRef findNextMedia() {
		if (mMediaLst == null) {
			mLogger.i("Media list is null.");
			return null;
		}
		
		int size = mMediaLst.size();
		if (size == 0) {
			mLogger.i("No media in the list.");
			return null;
		}

		if (++mCurrentIndex >= size) {
			if (mSkipMedia) { // Any media has been skipped, play again.
				mCurrentIndex = 0;
				mSkipMedia = false;
			} else {
				mCurrentIndex = -1;
				return null;
			}
		}
		
		return mMediaLst.get(mCurrentIndex);
	}
	
	private void showProgressBar() {
		mHandler.sendEmptyMessage(EVENT_SHOWPROGBAR);
	}
	
	private void playVideo(MediaRef media) {
		mLogger.i("Play video '" + media.localpath + "'.");

		Message msg = mHandler.obtainMessage();
		msg.what = EVENT_PLAYVIDEO;
		msg.obj = media;
		msg.sendToTarget();
	}
	
	private Bitmap getImage(String file) {
		Bitmap img = AdApplication.getInstance().getBitmapFromMemoryCache(file);
		if (img == null) {
			img = BitmapUtil.decodeFile(file, getWidth(), getHeight());
			if (img != null) {
				AdApplication.getInstance().addBitmapToMemoryCache(file, img);
			}
		}
		
		return img;
	}
	
	private void showPicture(Bitmap pic, int mode) {
		Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SHOWPICTURE;
		msg.obj = pic;
		msg.arg1 = mode;
		msg.sendToTarget();
	}
	
	private void playImage(MediaRef media) {
		mLogger.i("Play image '" + media.localpath + "'.");

		Bitmap img = getImage(media.localpath);

		showPicture(img, media.mode);
	}
	
	private String getText(String file) {
		return FileUtils.readTextFile(file);
	}
	
	private List<String> splitText(String txt, Paint paint, int viewwidth) {
        List<String> txtlst = new ArrayList<String>();

        char ch = 0;
        int linewidth = 0;
        int istart = 0;
        int length = txt.length();
        float[] widths = new float[1];
        for (int i = 0; i < length; i++) {
            ch = txt.charAt(i);
            if (ch == '\n') {
            	txtlst.add(txt.substring(istart, i));
                istart = i + 1;
                linewidth = 0;
            } else {
            	paint.getTextWidths(String.valueOf(ch), widths);
            	linewidth += (int) Math.ceil(widths[0]);
                if (linewidth > viewwidth) {
                	txtlst.add(txt.substring(istart, i));
                    istart = i;
                    i--;
                    linewidth = 0;
                } else if (i == (length - 1)) {
                    txtlst.add(txt.substring(istart));
                }
            }
        }
        
        return txtlst;
    }
	
	private void playText(MediaRef media) throws InterruptedException {
		mLogger.i("Play text '" + media.localpath + "'.");

		String txt = getText(media.localpath);
		if (TextUtils.isEmpty(txt)) {
			mLogger.i("Text '" + media.localpath + "' is empty.");
			return;
		}

		long durationmillis;
		if (media.duration >= 1) {
			durationmillis = media.duration * 1000;
		} else {
			mLogger.i("Media duration is invalid, media.duration = "
					+ media.duration + ".");
		    mLogger.i("Use default media duration...");
		    durationmillis = DEFAULT_MEDIA_DURATION;
		}

		Paint paint = new Paint();
        paint.setTextSize(media.fontsize);
        paint.setColor(getFontColor(media.fontcolor));
        paint.setAlpha(0xFF);
        paint.setTypeface(TypefaceUtil.getTypeface(media.fontname));
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        int viewwidth = getWidth() - 10;
        int viewheight = getHeight();
        FontMetrics fm = paint.getFontMetrics();
        float lineheight = (float)Math.ceil(fm.descent - fm.ascent);
        int linesperpage = (int)(viewheight / (lineheight + fm.leading));

        List<String> txtlst = splitText(txt, paint, viewwidth);
        int linenum = txtlst.size();
        int pagenum;
        if ((linenum % linesperpage) == 0) {
        	pagenum = linenum / linesperpage;
        } else {
        	pagenum = linenum / linesperpage + 1;
        }

        Bitmap bmp = Bitmap.createBitmap(viewwidth, viewheight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);

        float x;
        float y;
        int currline;
        String linetxt;
        for (int i = 0; i < pagenum; i++) {
            canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            x = 5;
            y = lineheight;
            for (int j = 0; j < linesperpage; j++) {
            	currline = i * linesperpage + j;
                if (currline >= linenum) {
                    break;
                }

                linetxt = txtlst.get(currline);
                if (linetxt == null) {
                	mLogger.i("Line text is null");
                	continue;
                }

                canvas.drawText(linetxt, x, y, paint);
                y += lineheight + fm.leading;
            }

    		showPicture(bmp, media.mode);

            Thread.sleep(durationmillis);
        }
	}

	private void hideAllViews() {
		mHandler.sendEmptyMessage(EVENT_HIDEALLVIEWS);
	}

	private class MyThread extends Thread {
        private Object   mPauseLock = new Object();
        private boolean  mPauseFlag = false;
        
        public MyThread() {

        }

        public void cancel() {
        	mLogger.i("Cancel the multimedia thread.");
            this.interrupt();
        }
        
        public void onPause() {
        	mLogger.i("Pause the multimedia thread.");
            synchronized (mPauseLock) {
                mPauseFlag = true;
            }
        }
        
        public void onResume() {
        	mLogger.i("Resume the multimedia thread.");
            synchronized (mPauseLock) {
            	mPauseFlag = false;
                mPauseLock.notify();
            }
        }
        
        public boolean isPaused() {
            return mPauseFlag;
        }
        
        @Override
        public void run() {
        	mLogger.i("New multimedia thread, id is: " + currentThread().getId());
            
            MediaRef media = null;
            
            while (!isInterrupted()) {
                try {
                	synchronized (mPauseLock) {
                        if (mPauseFlag) {
                            mPauseLock.wait();
                        }
                    }
                	
                    if (mMediaLst == null) {
                    	mLogger.i("Media list is null, thread exit.");
                        return;
                    } else if (mMediaLst.isEmpty()) {
                    	mLogger.i("No media in the list, thread exit.");
                        return;
                    } else if ((mCurrentIndex < -1) || (mCurrentIndex >= mMediaLst.size())) {
                    	mLogger.i("mCurrentIndex (" + mCurrentIndex + ") is invalid, thread exit.");
                        return;
                    }
                    
					if (!mIsPlayingVideo) {
						mCurrentMedia = null;
						media = findNextMedia();
						if (media == null) {
							if (mCurrentIndex != -1) {
								mLogger.i("No media can be found.");
								Thread.sleep(DEFAULT_THREAD_QUICKPERIOD);
							} else {
								mLogger.i("All medias have been played, wait for program manager.");
								ProgramManager.getInstance().informPgmAreaFinished(mPublishId,
										mAreaIndex);
								hideAllViews();
								onPause();
							}

							continue;
						} else if (!FileUtils.isExist(media.localpath) || !isMediaValid(media)) {
							mLogger.i(media.localpath + " doesn't exist or is invalid, skip it.");
							if ((mCurrentShowType != SHOWTYPE_PROGBAR) && noMediaExistsOrValid()) {
								showProgressBar();
							}
							
							downloadMedia(media);
							mSkipMedia = true;
							Thread.sleep(DEFAULT_THREAD_QUICKPERIOD);
							continue;
						}

						switch (media.type) {
						case Constants.MEDIATYPE_VIDEO:
							mIsPlayingVideo = true;
							mCurrentMedia = media;
							FileUtils.updateFileLastTime(media.localpath);

							playVideo(media);

							break;
						case Constants.MEDIATYPE_IMAGE:
							mCurrentMedia = media;
							FileUtils.updateFileLastTime(media.localpath);

							playImage(media);

							if (media.duration >= 1) {
								Thread.sleep(media.duration * 1000);
							} else {
								mLogger.i("Media duration is invalid, media.duration = "
										+ media.duration + ".");
							    mLogger.i("Use default media duration...");
								Thread.sleep(DEFAULT_MEDIA_DURATION);
							}

							media.playtimes++;

							continue;
						case Constants.MEDIATYPE_TEXT:
							mCurrentMedia = media;
							FileUtils.updateFileLastTime(media.localpath);

							playText(media);

							media.playtimes++;

							continue;
						default:
							mLogger.i("Invalid media type, media.type = " + media.type + ".");

							break;
						}
						
						Thread.sleep(DEFAULT_THREAD_QUICKPERIOD);
					} else {
                    
						Thread.sleep(DEFAULT_THREAD_PERIOD);
					}
                } catch (InterruptedException e) {
                	e.printStackTrace();
                }
            }
            
            mLogger.i("Multimedia thread is safely terminated, id is: " + currentThread().getId());
        }
    }
	
	private void doShowProgressBar() {
		if (mCurrentShowType != SHOWTYPE_PROGBAR) {
			mProgBarLyt.setVisibility(View.VISIBLE);
			mSurfaceView.setVisibility(View.GONE);
			mImageSwitcher.setVisibility(View.GONE);

			mCurrentShowType = SHOWTYPE_PROGBAR;
        }
	}
	
	private void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

	private void doPlayVideo(MediaRef media) {
		if (mCurrentShowType != SHOWTYPE_VIDEO) {
			mProgBarLyt.setVisibility(View.GONE);
			mSurfaceView.setVisibility(View.VISIBLE);
			mImageSwitcher.setVisibility(View.GONE);

			mCurrentShowType = SHOWTYPE_VIDEO;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mMediaPlayerPreparedListener);
            mMediaPlayer.setOnCompletionListener(mMediaPlayerCompletionListener);
            mMediaPlayer.setOnErrorListener(mMediaPlayerErrorListener);
        }
        
        try {
        	stopVideo();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(media.localpath);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
			e.printStackTrace();
			mIsPlayingVideo = false;
		}
        
    }

	private void updateAnimation(int mode) {
        Animation inanimation;

        switch (mode) {
        case Constants.MEDIAMODE_NONE:
        	mImageSwitcher.setInAnimation(null);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_LEFTTORIGHT:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

            mImageSwitcher.setInAnimation(inanimation);
            mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_RIGHTTOLEFT:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

        	mImageSwitcher.setInAnimation(inanimation);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_TOPTOBOTTOM:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

        	mImageSwitcher.setInAnimation(inanimation);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_BOTTOMTOTOP:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

        	mImageSwitcher.setInAnimation(inanimation);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_LEFTTOPTORIGHTBOTTOM:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

            mImageSwitcher.setInAnimation(inanimation);
            mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_RIGHTTOPTOLEFTBOTTON:
        	inanimation = new TranslateAnimation(
        			Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0,
        			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

        	mImageSwitcher.setInAnimation(inanimation);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_INSIDETOOUTSIDE:
        	inanimation = new ScaleAnimation(0f, 1.0f, 0f, 1.0f,
        			Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);
            
            mImageSwitcher.setInAnimation(inanimation);
            mImageSwitcher.setOutAnimation(null);
            
            break;
        case Constants.MEDIAMODE_OUTSIDETOINSIDE:
        	inanimation = new ScaleAnimation(3.0f, 1.0f, 3.0f, 1.0f,
        			Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
        	inanimation.setDuration(DEFAULT_ANIMATION_DURATION);

        	mImageSwitcher.setInAnimation(inanimation);
        	mImageSwitcher.setOutAnimation(null);

            break;
        case Constants.MEDIAMODE_RANDOM:
        	updateAnimation(new Random().nextInt(Constants.MEDIAMODE_RANDOM));

            break;
        default:
        	mLogger.i("Invalid mode, mode = " + mode + ".");

        	mImageSwitcher.setInAnimation(null);
        	mImageSwitcher.setOutAnimation(null);

        	break;
        }
    }
	
	private void doShowPicture(Bitmap img, int mode) {
		if (mCurrentShowType != SHOWTYPE_PICTURE) {
			mProgBarLyt.setVisibility(View.GONE);
			mSurfaceView.setVisibility(View.GONE);
			mImageSwitcher.setVisibility(View.VISIBLE);

			mCurrentShowType = SHOWTYPE_PICTURE;
        }
		
		BitmapDrawable imgdwb = new BitmapDrawable(mContext.getResources(), img);
        updateAnimation(mode);
        mImageSwitcher.setImageDrawable(imgdwb);
	}
	
	private void doHideAllViews() {
		mProgBarLyt.setVisibility(View.GONE);
		mSurfaceView.setVisibility(View.GONE);
		mImageSwitcher.setVisibility(View.GONE);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SHOWPROGBAR:
				doShowProgressBar();
				
				break;
			case EVENT_PLAYVIDEO:
				doPlayVideo((MediaRef)msg.obj);
				
				break;
			case EVENT_SHOWPICTURE:
				doShowPicture((Bitmap)msg.obj, msg.arg1);
				
				break;
			case EVENT_HIDEALLVIEWS:
				doHideAllViews();

				break;
            default:
            	mLogger.i("Unknown event, msg.what = " + msg.what + ".");
                break;
            }

            super.handleMessage(msg);
		}
    };

}
