package com.foodietrip.android.library;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ImageViewHelper {
	Boolean isZoomed = false;
	ImageView imageView;
	Bitmap bitmap;
	Context context;
	private float originalX, originalY;
	private Matrix matrix = new Matrix();
	private Matrix saved_matrix = new Matrix();
	private static final int NONE = 0;    //��l��
	private static final int DRAG = 1;    //�즲
	private static final int ZOOM = 2;    //�Y��
	private DisplayMetrics dm;
	private int mode = NONE;
	private PointF prev = new PointF();
	private PointF mid = new PointF();
	private float dist = 1f;
	private static final float MIN_ZOOM = 0.5f;
	private static final float MAX_ZOOM = 2.0f;
    GestureDetector gestureDetector;
    public ImageViewHelper(Context context,DisplayMetrics dm,ImageView imageView,Bitmap bitmap) {
    	this.dm = dm;
    	this.imageView = imageView;
    	this.bitmap = bitmap;
    	this.context = context;
    	setImageSize();
    	//minZoom();
    	center();
    	imageView.setImageMatrix(matrix);
    	originalX = bitmap.getWidth();
    	originalY = bitmap.getHeight();
    }
    
    //��������
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (isZoomed) {
				Matrix originalMartrix = new Matrix();
				originalMartrix.postScale(originalX, originalY);
				imageView.setImageMatrix(originalMartrix);
				imageView.setScaleType(ScaleType.FIT_CENTER);
				isZoomed = false;
				//Log.e("Touch event Log", "Action Double Tap");
			}
			return true;
		}
    }
    
    public Matrix getMatrix() {
    	return matrix;
    }
    
    public void center() {
    	center(true,true);
    }
    
    //���X�ù��d��^�����I
    public void center(boolean horizontal,boolean vertical) {
    	Matrix m = new Matrix();
    	m.set(matrix);
    	RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	m.mapRect(rect);
    	float height = rect.height();
    	float width = rect.width();
    	float delta_x = 0, delta_y = 0;
    	//�Ϥ��p��ù��j�p�A�h�m��
    	//�j��ù��A�W��d�ťիh���W���A�U��d�ťիh���U��
    	if (vertical) {
    		int screen_height = dm.heightPixels;
    		if (height < screen_height) {
    			delta_y = (screen_height - height) / 2 - rect.top;
    		}
    		else if (rect.top > 0) {
				delta_y = -rect.top;
			} 
    		else if (rect.bottom < screen_height) {
				delta_y = imageView.getHeight() - rect.bottom;
			}
    	}
    	if (horizontal) {
    		int screen_width = dm.widthPixels;
    		if (width < screen_width) {
    			delta_x = (screen_width - width) / 2 - rect.left;
    		}
    		else if (rect.left > 0) {
    			delta_x = -rect.left;
    		}
    		else if (rect.right < screen_width) {
    			delta_x = screen_width - rect.right;
    		}
    	}
    	matrix.postTranslate(delta_x, delta_y);
    }
    
    //��ťĲ���ƥ�,���: ���, ����: �Y��
    public void setImageSize() {
    	gestureDetector = new GestureDetector(context, new GestureListener());
    	imageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);    //�ҥ�����
                LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					saved_matrix.set(matrix);
					prev.set(event.getX(), event.getY());
					mode = DRAG;
					//Log.e("Touch event Log", "Action Down");
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					dist = spacing(event);
					//�p�G���I���Z���W�L10, �N�P�_���h�IĲ�� -- �Y��Ҧ�
					if (spacing(event) > 10f) {
						saved_matrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
						//Log.e("Touch event Log", "Action Pointer Down");
					}
					break;
				case MotionEvent.ACTION_UP:
					//Log.e("Touch event Log", "Action Up");
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					//Log.e("Touch event Log", "Action Pointer Up");
					mode = NONE;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG && isZoomed) {
						matrix.set(saved_matrix);
						matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
					}
					else if (mode == ZOOM) {
						float newDist = spacing(event);    //������������ʪ��Z��
						if (newDist > 10f) {
							matrix.set(saved_matrix);
							float tScale = newDist / dist;
							tScale = Math.max(MIN_ZOOM, Math.min(tScale, MAX_ZOOM));
							matrix.postScale(tScale, tScale, mid.x, mid.y);
							imageView.setLayoutParams(vp);
							isZoomed = true;
						}
					}
					//Log.e("Touch event Log", "Action Move");
					break;
				}
                if (isZoomed) imageView.setScaleType(ScaleType.MATRIX);
                limitZoom(matrix);
                limitDrag(matrix);
				imageView.setImageMatrix(matrix);
				center();
				return true;
			}
		});
    }
    
    //���I���Z��
    public float spacing(MotionEvent event) {
    	float x = event.getX(0) - event.getX(1);
    	float y = event.getY(0) - event.getY(1);
    	float result = (float)Math.sqrt(x * x + y * y);
    	return result;
    }
    
    //���I�����I
    public void midPoint(PointF point, MotionEvent event) {
    	float x = event.getX(0) + event.getX(1);
    	float y = event.getY(0) + event.getY(1);
    	point.set(x / 2, y / 2);
    }
    
    private void limitZoom(Matrix matrix) {
    	float[] values = new float[9];
    	matrix.getValues(values);
    	float scaleX = values[Matrix.MSCALE_X];
    	float scaleY = values[Matrix.MSCALE_Y];
    	if (scaleX > MAX_ZOOM)
    		scaleX = MAX_ZOOM;
    	else if (scaleX < MIN_ZOOM)
    		scaleX = MIN_ZOOM;
    	if (scaleY > MAX_ZOOM)
    		scaleY = MAX_ZOOM;
    	else if (scaleY < MIN_ZOOM)
    		scaleY = MIN_ZOOM;
    	values[Matrix.MSCALE_X] = scaleX;
    	values[Matrix.MSCALE_Y] = scaleY;
    	matrix.setValues(values);
    }
    
    private void limitDrag(Matrix matrix) {
    	float[] values = new float[9];
    	matrix.getValues(values);
    	float transX = values[Matrix.MTRANS_X];
    	float transY = values[Matrix.MTRANS_Y];
    	float scaleX = values[Matrix.MSCALE_X];
    	float scaleY = values[Matrix.MSCALE_Y];
    	Rect bounds = imageView.getDrawable().getBounds();
    	int viewWidth = context.getResources().getDisplayMetrics().widthPixels;
    	int viewHeight = context.getResources().getDisplayMetrics().heightPixels;
    	int width = bounds.right - bounds.left;
    	int height = bounds.bottom - bounds.top;
    	float minX = (-width + 20) * scaleX;
    	float minY = (-height + 20) * scaleY;
    	if (transX > (viewWidth - 20))
    		transX = viewWidth -20;
    	else if (transX < minX)
    		transX = minX;
    	if (transY > (viewHeight - 20))
    		transY = viewHeight - 20;    //org80
    	else if (transY < minY)
    		transY = minY;
    	values[Matrix.MTRANS_X] = transX;
    	values[Matrix.MTRANS_Y] = transY;
    	matrix.setValues(values);
    }

}
