package com.renny.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Zxing生成二维码工厂
 *
 * @author Kelly
 * @version 1.0.0
 * @filename QRCodeFactory.java
 * @time 2018/2/24 9:28
 * @copyright(C) 2018 song
 */
public class QRCodeFactory {
    private static final int BLACK = 0xff000000;

    /**
     * 生成二维码图片（不带图片）
     *
     * @param url
     * @param widthAndHeight
     * @return
     * @throws WriterException
     */
    public static Bitmap createQRCode(String url, int widthAndHeight)
            throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错率越高,二维码的有效像素点就越多
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);


        BitMatrix matrix = new MultiFormatWriter().encode(url,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight,hints);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        //画黑点
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK; //0xff000000
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }



    /**
     * 带图片的二维码
     *
     * @param content
     * @param heightPix
     * @param logoBm
     * @return
     */
    public static Bitmap createQRImage(String content, int heightPix, Bitmap logoBm) {
        try {

            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
             hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, heightPix, heightPix, hints);
            int[] pixels = new int[heightPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < heightPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * heightPix + x] = 0xff000000;
                    } else {
                        pixels[y * heightPix + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(heightPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, heightPix, 0, 0, heightPix, heightPix);

            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save();

            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }


    /**
     * 生成条形码（自适应）
     *
     * @param contents      需要生成的内容
     * @param widthPix  生成条形码的宽度
     * @param heightPix 生成条形码的高度
     * @param displayCode   是否在条形码下方显示内容
     * @return
     */
    public static Bitmap createBarcode(String contents,int widthPix, int heightPix, boolean displayCode) {
        Bitmap ruseltBitmap;
        /**
         * 条形码的编码类型
         */
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;


        if (displayCode) {
            Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
                    widthPix, heightPix);
            ruseltBitmap = mixtureBitmapAndText(contents,barcodeBitmap,widthPix,heightPix, new PointF(0, 8));
        } else {
            ruseltBitmap = encodeAsBitmap(contents, barcodeFormat,
                    widthPix, heightPix);
        }

        return ruseltBitmap;
    }


    /**
     * 生成条形码的Bitmap
     *
     * @param contents      需要生成的内容
     * @param format        编码格式
     * @param widthPix
     * @param heightPix
     * @return
     * @throws WriterException
     */
    private static Bitmap encodeAsBitmap(String contents,
                                           BarcodeFormat format, int widthPix, int heightPix) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, widthPix,
                    heightPix, null);
            result = deleteWhite(result);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 删除白边
     */
    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2];
        int resHeight = rec[3];

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }


    /**
     *
     * 合并条码和条码内容
     * @param contents
     * @param barcodeBitmap
     * @param widthPix
     * @param heightPix
     * @param fromPoint
     * @return
     */
    private static Bitmap mixtureBitmapAndText(String contents, Bitmap barcodeBitmap,int widthPix, int heightPix,
                                           PointF fromPoint) {

        Rect rect = new Rect();
        Paint textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLACK);
        textPaint.setFilterBitmap(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.getTextBounds(contents, 0, contents.length(), rect);
        //文本的宽、高度
        int textWidth = rect.width();
        int textHeight = rect.height();
        int width = Math.max(widthPix, textWidth);
        int height = (int) (heightPix + fromPoint.y + textHeight);
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(barcodeBitmap, (widthPix-barcodeBitmap.getWidth())/2, 0, null);
        canvas.translate(0, heightPix);
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        RectF rectF = new RectF(0, 0, width, textHeight);
        canvas.drawRect(rectF, bgPaint);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = rectF.centerY() + distance + fromPoint.y;
        canvas.drawText(contents, rectF.centerX(), baseline, textPaint);

        canvas.save();
        canvas.restore();

        return newBitmap;
    }


}
