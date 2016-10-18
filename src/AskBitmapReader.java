package ru.askor.blagosfera;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by dave on 03.08.2016.
 */
public class AskBitmapReader {
    public static final int DPI_500 = 19778;

    private static void memcpy(byte[] dstbuf, int dstoffset, byte[] srcbuf, int srcoffset, int size) {
        for (int i = 0; i < size; i++) {
            dstbuf[dstoffset + i] = srcbuf[srcoffset + i];
        }
    }

    private static byte[] flipH(byte[] src, int size, int offset, int width, int height) {
        byte dst[] = new byte[size];
        memcpy(dst, 0, src, 0, offset);

        for (int i = 0; i < height; i++) {
            memcpy(dst, (offset + (i * width)), src, (offset + ((height - i - 1) * width)), width);
        }

        return dst;
    }

    private static byte[] flipV(byte[] src, int width, int height) {
        final int srcSize = src.length;
        byte dst[] = new byte[srcSize];

        int i = 0, j = 0, shift = 0;
        for (; j < height; j++) {
            shift = j * width;
            for (i = 0; i < width; i++) {
                dst[shift + i] = src[shift + width - i - 1];
            }
        }

        return dst;
    }

    public static class BmpImage {
        public byte[] dst = null;
        public int width = 0;
        public int height = 0;
        public int size = 0;
    }

    private static BmpImage stretch(byte[] src, int srcWidth, int srcHeight, float kx, float ky) {
        byte[] dst = new byte[1024 * 1024];

        double k1, k2;
        int x = 0, y = 0,
            i = 0, j = 0;

        int dstHeight = (int)(srcHeight * ky);
        int dstWidth  = (int)(srcWidth * kx);

        //destination/source matrix row lenght
        int dstRow = 0, srcRow = 0,
            srcShift = 0, dstShift = 0;

        dstWidth = dstWidth / 4 * 4;
        dstRow = dstWidth;

        //source matrix row lenght (we suppose that)
        srcRow = ((srcWidth + 3) >> 2) << 2;

        for (j = 0; j < dstHeight; j++) {
            y = (int) (j / ky);
            if (y > srcHeight - 2) {
                y = srcHeight - 2;
            }
            k1 = j / ky - y;

            srcShift = srcRow * y;
            for (i = 0; i < dstWidth - 1; i++) {
                x = (int) (i / kx);
                if (x > srcWidth - 2) {
                    x = srcWidth - 2;
                }
                k2 = i / kx - x;

                double subd1 = (double)src[srcShift + x];
                if (subd1 < 0) { subd1 = 256 + subd1; }

                double subd2 = (double)src[srcShift + x + 1];
                if (subd2 < 0) { subd2 = 256 + subd2; }

                double subd3 = (double)src[srcShift + srcRow + x];
                if (subd3 < 0) { subd3 = 256 + subd3; }

                double subd4 = (double)src[srcShift + srcRow + x + 1];
                if (subd4 < 0) { subd4 = 256 + subd4; }

                double d1 = (subd1 * (1 - k1) * (1 - k2));
                double d2 = (subd2 * k2 * (1 - k1));
                double d3 = (subd3 * k1 * (1 - k2));
                double d4 = (subd4 * k1 * k2);
                double dsum = (d1 + d2 + d3 + d4);

                dst[dstShift + i] = (byte)dsum;
            }

            dst[dstShift + dstWidth - 1] = src[srcShift + srcWidth - 1];
            //arrange the row
            for (i = dstWidth; i < dstRow; i++) {
                dst[dstShift + i] = 0;
            }

            dstShift += dstRow;
        }

        BmpImage bmpImage = new BmpImage();
        bmpImage.width = dstRow;
        bmpImage.height = dstHeight;
        bmpImage.size = bmpImage.width * bmpImage.height;//только размер изображения

        //копируем полностью все данные файла с заголовками
        bmpImage.dst = new byte[bmpImage.size];
        System.arraycopy(dst, 0, bmpImage.dst, 0, bmpImage.size);

        return bmpImage;
    }

    private static byte[] readFile(File file, int offset, int len) {
        byte[] bytes = new byte[len];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, offset, len);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static BmpImage readBmp(File bmpFile) {
        //Наш Большой временный буфер

        final int sizeOfBitmapFileHeader = 14; //BITMAPFILEHEADER
        final int sizeOfBitmapInfoHeader = 40; //BITMAPINFOHEADER

        //Смещение до начала данных
        final int offset = sizeOfBitmapFileHeader + sizeOfBitmapInfoHeader + (256 * 4);
        //Общий размер файла
        final int bmpSize = (int) bmpFile.length();
        //Размер ТОЛЬКО данных без заголовков
        final int dibSize = bmpSize - (sizeOfBitmapFileHeader + sizeOfBitmapInfoHeader);

        //Читаем полностью Файл в dst
        byte dst[] = readFile(bmpFile, 0, bmpSize);
        int dstSize = dst.length;

        //Инвертируем данные начиная со смещения в dst
        byte[] flippedImage = flipH(dst,  dstSize, offset,  152, 200);
        int flippedSize = flippedImage.length;

        byte arrayToStretch[] = Arrays.copyOfRange(flippedImage, offset, flippedSize);
        //byte arrayToStretch[] = Arrays.copyOfRange(dst, offset, dstSize);
        BmpImage bmpImage = stretch(arrayToStretch, 152, 200, 1.37f, 1.37f);

        byte dst2[] = new byte[bmpImage.size + offset];

        System.arraycopy(flippedImage, 0, dst2, 0, offset);
        //System.arraycopy(dst, 0, dst2, 0, offset);

        System.arraycopy(bmpImage.dst, 0, dst2, offset, bmpImage.size);
        bmpImage.size  = bmpImage.size + offset;
        bmpImage.dst = new byte[bmpImage.size];
        System.arraycopy(dst2, 0, bmpImage.dst, 0, bmpImage.size);

        //bmpImage.dst содержит полность формированный bmp c заголовками и данными
        return bmpImage;
    }

    private static byte[] writeInt(int value) throws IOException {
        byte[] b = new byte[4];

        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);

        return b;
    }

    private static byte[] writeShort(short value) throws IOException {
        byte[] b = new byte[2];

        b[0] = (byte) (value & 0x00FF);
        b[1] = (byte) ((value & 0xFF00) >> 8);

        return b;
    }

    private static byte[] changeByte(int data) {
        byte b4 = (byte) ((data) >> 24);
        byte b3 = (byte) (((data) << 8) >> 24);
        byte b2 = (byte) (((data) << 16) >> 24);
        byte b1 = (byte) (((data) << 24) >> 24);
        byte[] bytes = {b1, b2, b3, b4};
        return bytes;
    }


    public static byte[] save2Bmp(BmpImage bmpImage) {
        byte[] buffer = null;

        //Это размер только данных
        int sizeImage = bmpImage.size;
        if (sizeImage == 0) {
            sizeImage = bmpImage.height * bmpImage.width;
        }

        //Размеры заголовков
        final int sizeOfBitmapFileHeader = 14; //BITMAPFILEHEADER
        final int sizeOfBitmapInfoHeader = 40; //BITMAPINFOHEADER

        //Смещение до начала данных
        final int offset = sizeOfBitmapFileHeader + sizeOfBitmapInfoHeader + (256 * 4);

        //Реальный размер файла: заголовки + данные
        int sizeBmp = offset + sizeImage;

        //Записываем данные
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            int bfType = 0x4d42;
            int bfSize = 54 + 1024 + bmpImage.width * bmpImage.height;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            int bfOffBits = 54 + 1024;

            dos.write(changeByte(bfType), 0, 2);
            dos.write(changeByte(bfSize), 0, 4);
            dos.write(changeByte(bfReserved1), 0, 2);
            dos.write(changeByte(bfReserved2), 0, 2);
            dos.write(changeByte(bfOffBits), 0, 4);

            int biSize = 40;
            int biWidth = bmpImage.width;
            int biHeight = -bmpImage.height;
            int biPlanes = 1;
            int biBitcount = 8;
            int biCompression = 0;
            int biSizeImage = bmpImage.width * bmpImage.height;
            int biXPelsPerMeter = 19687;
            int biYPelsPerMeter = 19687;
            int biClrUsed = 256;
            int biClrImportant = 0;

            dos.write(changeByte(biSize), 0, 4);
            dos.write(changeByte(biWidth), 0, 4);
            dos.write(changeByte(biHeight), 0, 4);
            dos.write(changeByte(biPlanes), 0, 2);
            dos.write(changeByte(biBitcount), 0, 2);
            dos.write(changeByte(biCompression), 0, 4);
            dos.write(changeByte(biSizeImage), 0, 4);
            dos.write(changeByte(biXPelsPerMeter), 0, 4);
            dos.write(changeByte(biYPelsPerMeter), 0, 4);
            dos.write(changeByte(biClrUsed), 0, 4);
            dos.write(changeByte(biClrImportant), 0, 4);

            //Записывваем данные за вычитом первого заголовка мы его записали выше
            byte array2 [] = Arrays.copyOfRange(bmpImage.dst, (14 + 40), bmpImage.size);
            dos.write(array2);

            dos.flush();
            buffer = baos.toByteArray();

            dos.close();
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return buffer;
    }
}
