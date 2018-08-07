package com.myscrap.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/27/2017.
 */

public class FlowLayout extends ViewGroup{
    private List<Line> mLines = new ArrayList<>();
    private Line currentLine;
    private int usedWidth = 0;
    private int horizontalSpacing;
    private int verticalSpacing;
    private int width;


    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        horizontalSpacing= DeviceUtils.dp2px(context,13);
        verticalSpacing=DeviceUtils.dp2px(context,13);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mLines.clear();
        currentLine = null;
        usedWidth = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int childWidthMode;
        int childHeightMode;
        childWidthMode = widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode;
        childHeightMode = heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode;
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, childWidthMode);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, childHeightMode);
        currentLine = new Line();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            int measuredWidth = child.getMeasuredWidth();
            if(usedWidth+measuredWidth+horizontalSpacing<width ||currentLine.getChildCount()==0){
                currentLine.addChild(child);
                usedWidth+=measuredWidth;
                usedWidth+=horizontalSpacing;
            }else{
                newLine();
                currentLine.addChild(child);
                usedWidth+=measuredWidth;
                usedWidth+=horizontalSpacing;
            }
        }
        if (!mLines.contains(currentLine)) {
            mLines.add(currentLine);
            Log.d("FlowLayout", "currentLine.getChildCount():" + currentLine.getChildCount());
        }
        int totalHeight = 0;
        for (Line line : mLines) {
            totalHeight += line.getHeight();
        }
        totalHeight += ((mLines.size() - 1) * verticalSpacing)+getPaddingTop()+getPaddingBottom();
        setMeasuredDimension(width+getPaddingLeft()+getPaddingRight(), resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        l+=getPaddingLeft();
        t+=getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            line.layout(l, t);
            t += line.getHeight() + verticalSpacing;
        }
    }


    private class Line {
        int height = 0;
        List<View> children = new ArrayList<>();
        int total = 0;


        public void addChild(View child) {
            children.add(child);
            if (child.getMeasuredHeight() > height) {
                height = child.getMeasuredHeight();
            }
            total += child.getMeasuredWidth();
        }


        public int getChildCount() {
            return children.size();
        }

        public int getHeight() {
            return height;
        }


        public void layout(int l, int t) {
            total += horizontalSpacing * (children.size() - 1);
            int surplusChild = 0;
            int surplus = width - total;
            surplusChild = surplus / children.size();
            for (int i = 0; i < children.size(); i++) {
                TextView view = (TextView) children.get(i);
                view.layout(l, t, l + view.getMeasuredWidth()+surplusChild, t + view.getMeasuredHeight());
                view.setGravity(Gravity.CENTER);
                String text=view.getText().toString();
                view.setText(text);
                l += view.getMeasuredWidth()+surplusChild;
                l += verticalSpacing;
            }
        }
    }


    public void newLine() {
        mLines.add(currentLine);
        currentLine = new Line();
        usedWidth = 0;
    }

}
