package com.timarcher.robotface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;

/**
 * The view which draws the face
 * 
 * @author tarcher
 *
 */
public class FaceView extends View {

	public FaceView(Context context) {
		super(context);
	}
	
	 @Override
	 protected void onDraw(Canvas canvas) {
	     super.onDraw(canvas);
	     
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
	     leftEye.setBounds(100, 50, 500, 200);
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
	     //Right Eye
	     //
	     ShapeDrawable rightEye = new ShapeDrawable(new RoundRectShape(new float[] {80, 80, 80, 80, 80, 80, 80, 80}, null, null));
	     //rightEye.setIntrinsicHeight(25);
	     //rightEye.setIntrinsicWidth(150);
	     rightEye.getPaint().setColor(Color.WHITE);
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     rightEye.setBounds(750, 50, 1150, 200);
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
	     float mouthTopLipWidth = 150;
	     float mouthTopLipHeight = 300;
	     RectF oval = new RectF();
	     oval.set(0, 0, mouthTopLipWidth, mouthTopLipHeight);
	     Path myPath = new Path();
	     myPath.arcTo(oval, 0, 180, true);
	     ShapeDrawable mouthTopLip = new ShapeDrawable(new PathShape(myPath, mouthTopLipWidth, mouthTopLipHeight));
	     mouthTopLip.getPaint().setColor(Color.YELLOW);
	     mouthTopLip.getPaint().setStyle(Paint.Style.STROKE);
	     mouthTopLip.getPaint().setStrokeWidth(6);	     
	     //Specify a bounding rectangle to draw into
	     //left, top, right, bottom
	     mouthTopLip.setBounds(400, 400, 850, 600);
	     mouthTopLip.draw(canvas);
	     
	     
	     
	 }	

}
