package com.misit.faceidchecklogptabp.Helper

import android.graphics.*

class RectOverlay internal constructor(overlay: GraphicOverlay,
                                       private val bond:Rect?):GraphicOverlay.Graphic(overlay){
    private val rectPaint:Paint
    init {
        rectPaint = Paint()
        rectPaint.color= Color.RED
        rectPaint.strokeWidth=4.0f
        rectPaint.style= Paint.Style.STROKE

        postInvalidate()
    }
    override fun draw(canvas: Canvas?) {
        val rect = RectF(bond)
        rect.left = translateX(rect.left)
        rect.right = translateX(rect.right)
        rect.top = translateX(rect.top)
        rect.bottom = translateX(rect.bottom)
        canvas?.drawRect(rect,rectPaint)
    }
}