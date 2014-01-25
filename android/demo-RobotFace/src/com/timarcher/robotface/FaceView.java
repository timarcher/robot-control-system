package com.timarcher.robotface;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * The view which draws the face
 * 
 * @author tarcher
 *
 */
public class FaceView extends View {

	Rect leftEyeRect;
	
	ShapeDrawable rightEye;
	Rect rightEyeRect;

	ShapeDrawable leftEyeBlink;
	Rect leftEyeBlinkRect;
	
	ShapeDrawable rightEyeBlink;
	Rect rightEyeBlinkRect;

	ShapeDrawable mouthTopLip;
	Rect mouthTopLipRect;

	ShapeDrawable mouthBottomLip;
	Rect mouthBottomLipRect;
	
	public FaceView(Context context) {
		super(context);
		leftEyeRect = new Rect(100, 50, 500, 200);
		leftEyeBlinkRect = new Rect(99, 49, 501, 49);

		
		rightEyeRect = new Rect(750, 50, 1150, 200);
		rightEyeBlinkRect = new Rect(749, 49, 1151, 49);
		
		mouthTopLipRect = new Rect(400, 350, 850, 500);		
		mouthBottomLipRect = new Rect(400, 310, 850, 550);
	}
	
	 @Override
	 protected void onDraw(Canvas canvas) {
	     super.onDraw(canvas);
	     
         //Toast.makeText(this.getContext(), 
         //        "onDraw called", 
         //        Toast.LENGTH_SHORT).show();      
         
	     /*
	     Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	     paint.setColor(0xFFFF0000);	     
	     //canvas.drawCircle(xPos, yPos, radius, paint);	     
	     canvas.drawCircle(20, 20, 30, paint);
	     */

	     //
	     //Left Eye
	     //
	     ShapeDrawable leftEye = new ShapeDrawable(new RoundRectShape(new float[] {80, 80, 80, 80, 80, 80, 80, 80}, null, null));
	     //leftEye.setIntrinsicHeight(25);
	     //leftEye.setIntrinsicWidth(150);
	     leftEye.getPaint().setColor(Color.WHITE);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     leftEye.setBounds(leftEyeRect);
	     leftEye.draw(canvas);

	     //
	     //Left eye iris
	     //
	     ShapeDrawable leftEyeIris = new ShapeDrawable(new OvalShape());
	     leftEyeIris.getPaint().setColor(Color.GREEN);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     leftEyeIris.setBounds(225, 50, 375, 200);
	     leftEyeIris.draw(canvas);

	     //
	     //Left eye pupil
	     //
	     ShapeDrawable leftEyePupil = new ShapeDrawable(new OvalShape());
	     leftEyePupil.getPaint().setColor(Color.BLACK);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     leftEyePupil.setBounds(250, 75, 350, 175);
	     leftEyePupil.draw(canvas);
	     
	     //
	     //For blinking right eye
	     //
	     leftEyeBlink = new ShapeDrawable(new RoundRectShape(new float[] {80, 80, 80, 80, 80, 80, 80, 80}, null, null));
	     leftEyeBlink.getPaint().setColor(Color.BLACK);
	     leftEyeBlink.setBounds(leftEyeBlinkRect);
	     leftEyeBlink.draw(canvas);
	     	     
	     
	     //
	     //Right Eye
	     //
	     rightEye = new ShapeDrawable(new RoundRectShape(new float[] {80, 80, 80, 80, 80, 80, 80, 80}, null, null));
	     //rightEye.setIntrinsicHeight(25);
	     //rightEye.setIntrinsicWidth(150);
	     rightEye.getPaint().setColor(Color.WHITE);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     //rightEye.setBounds(750, 50, 1150, 200);
	     rightEye.setBounds(rightEyeRect);
	     rightEye.draw(canvas);

	     //
	     //Right eye iris
	     //
	     ShapeDrawable rightEyeIris = new ShapeDrawable(new OvalShape());
	     rightEyeIris.getPaint().setColor(Color.GREEN);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     rightEyeIris.setBounds(875, 50, 1025, 200);
	     rightEyeIris.draw(canvas);

	     //
	     //Right eye pupil
	     //
	     ShapeDrawable rightEyePupil = new ShapeDrawable(new OvalShape());
	     rightEyePupil.getPaint().setColor(Color.BLACK);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     //rightEyePupil.setBounds(900, 75, 1000, 175);
	     rightEyePupil.setBounds(925, 100, 975, 150);
	     rightEyePupil.draw(canvas);	     

	     //
	     //For blinking right eye
	     //
	     rightEyeBlink = new ShapeDrawable(new RoundRectShape(new float[] {80, 80, 80, 80, 80, 80, 80, 80}, null, null));
	     rightEyeBlink.getPaint().setColor(Color.BLACK);
	     rightEyeBlink.setBounds(rightEyeBlinkRect);
	     rightEyeBlink.draw(canvas);
	     	     
	     //
	     //Nose
	     //
	     Path p = new Path();
	     p.moveTo(0,  0);
	     p.lineTo(30, 300);
	     p.lineTo(150, 300);
	     p.lineTo(180, 0);
	     ShapeDrawable nose = new ShapeDrawable(new PathShape(p, 150, 300));
	     nose.getPaint().setColor(Color.WHITE);
	     nose.getPaint().setStyle(Paint.Style.STROKE);
	     nose.getPaint().setStrokeWidth(4);	     
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     nose.setBounds(535, 150, 685, 375);
	     nose.draw(canvas);
	     
	     
	     //
	     //Mouth
	     //
	     /*
	     ShapeDrawable mouthTopLip = new ShapeDrawable(new ArcShape(0, 180));
	     mouthTopLip.getPaint().setColor(Color.YELLOW);
	     mouthTopLip.getPaint().setStyle(Paint.Style.STROKE);
	     mouthTopLip.getPaint().setStrokeWidth(6);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     //mouthTopLip.setBounds(400, 400, 850, 500);
	     //mouthTopLip.setBounds(500, 400, 750, 600);
	     //Big smile
	     mouthTopLip.setBounds(350, 400, 900, 500);	    
	     mouthTopLip.draw(canvas);
	     */
	     
	     //Draw just the arc portion
	     float mouthTopLipWidth = 450;
	     float mouthTopLipHeight = 150;
	     RectF oval = new RectF();
	     oval.set(0, 0, mouthTopLipWidth, mouthTopLipHeight);
	     Path myPath = new Path();
	     myPath.arcTo(oval, 0, 180, true);
	     ShapeDrawable mouthTopLip = new ShapeDrawable(new PathShape(myPath, mouthTopLipWidth, mouthTopLipHeight));
	     mouthTopLip.getPaint().setColor(Color.YELLOW);
	     mouthTopLip.getPaint().setStyle(Paint.Style.STROKE);
	     mouthTopLip.getPaint().setStrokeWidth(4);	     
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     mouthTopLip.setBounds(mouthTopLipRect);
	     mouthTopLip.draw(canvas);

	     float mouthBottomLipWidth = 450;
	     float mouthBottomLipHeight = 250;
	     oval = new RectF();
	     oval.set(0, 0, mouthBottomLipWidth, mouthBottomLipHeight);
	     myPath = new Path();
	     myPath.arcTo(oval, 0, 180, true);
	     mouthBottomLip = new ShapeDrawable(new PathShape(myPath, mouthBottomLipWidth, mouthBottomLipHeight));
	     mouthBottomLip.getPaint().setColor(Color.RED);
	     mouthBottomLip.getPaint().setStyle(Paint.Style.STROKE);
	     mouthBottomLip.getPaint().setStrokeWidth(4);	     
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     //mouthBottomLip.setBounds(400, 210, 850, 600);
	     mouthBottomLip.setBounds(mouthBottomLipRect);
	     mouthBottomLip.draw(canvas);

	     
	 }	
	 
	 
	 public void blink() {
		 final View v = this;
		 
		leftEyeBlinkRect = new Rect(99, 49, 501, 49);
		rightEyeBlinkRect = new Rect(749, 49, 1151, 49);
		Thread blinkThread = new Thread(new Runnable() {
		    public void run() {
		    	try {
		    		for (int i=0; i < 150; i++) {
		    			leftEyeBlinkRect.bottom = leftEyeBlinkRect.bottom + 1;
		    			rightEyeBlinkRect.bottom = rightEyeBlinkRect.bottom + 1;
		    		     v.postInvalidate();	
		    		     Thread.sleep(1);
		    		}
		    		
		    		 Thread.sleep(10);
		    		 
		    		for (int i=150; i > 0; i--) {
		    	         leftEyeBlinkRect.bottom = leftEyeBlinkRect.bottom - 1;
		    	         rightEyeBlinkRect.bottom = rightEyeBlinkRect.bottom - 1;
		    		     v.postInvalidate();	
		    		     Thread.sleep(1);
		    		}		    		
		    	} catch (Exception e) {
		    		//Need to figure out how to handle
		    		Log.e("app", "Error", e);		        		
		    	}
		    	
		    }
		});
		blinkThread.start();
		 
	 }

	 public void talk() {
		 final View v = this;
		 final Random r = new Random();

		 mouthBottomLipRect = new Rect(400, 310, 850, 550);

		Thread talkThread = new Thread(new Runnable() {
		    public void run() {
		    	try {
		    		for (int i=0; i < 250; i++) {
			    		int newHeight = r.nextInt(100);
			    		int newWidth = r.nextInt(25);
			    		
			    		for (int newPos = 1; newPos <= newHeight; newPos += 2) {
				    		mouthBottomLipRect.top = 310 - newPos;
				    		mouthBottomLipRect.bottom = 550 + newPos;
				    		
				    		mouthTopLipRect.left = 400 + newWidth;
				    		mouthTopLipRect.right = 850 - newWidth;
				    		mouthBottomLipRect.left = 400 + newWidth;
				    		mouthBottomLipRect.right = 850 - newWidth;
				    		
				    		v.postInvalidate();
			    		    Thread.sleep(8);
			    		}
			    		/*
			    		if (Math.random() < 0.5) {
			    			i1 = i1*-1;
			    		}
			    		*/
			    		
		    		    	
		    		    Thread.sleep(5);
		    		}
		    	} catch (Exception e) {
		    		//Need to figure out how to handle
		    		Log.e("app", "Error", e);		        		
		    	}
		    	
		    }
		});
		talkThread.start();
		 
	 }
	 
}
