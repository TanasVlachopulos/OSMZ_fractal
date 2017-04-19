#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>

#define  LOG_TAG    "LibBitmap"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_kru13_bitmaptest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kru13_bitmaptest_MainActivity_bitmapChange(JNIEnv *env, jobject obj, jobject img, jdouble xPos, jdouble yPos, jdouble zoom) {
    AndroidBitmapInfo info;
    void *pixels;
    int ret;
    static int init;

    if ((ret = AndroidBitmap_getInfo(env, img, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, img, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }


    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }


    uint32_t **raw_data = (uint32_t **) pixels;

    int height = info.height;
    int width = info.width;


    LOGI("start");
//    LOGI("info height: %d", info.height);
//    LOGI("info width : %d", info.width);

    uint32_t *imgp = (uint32_t *) pixels;

//    double MinRe = -2.0;
    double MinRe = xPos;
//    double MaxRe = 1.0;
    double  MaxRe = xPos + zoom;
//    double MaxRe = re;
//    double MinIm = -1.2;
    double MinIm = yPos;
    double ImageHeight = info.height;
    double ImageWidth = info.width;
    double scaleFactor = (ImageHeight / ImageWidth);
    double MaxIm = MinIm + (MaxRe - MinRe) * scaleFactor;
    double Re_factor = (MaxRe - MinRe) / (ImageWidth - 1);
    double Im_factor = (MaxIm - MinIm) / (ImageHeight - 1);
    unsigned MaxIterations = 200;

    for (unsigned y = 0; y < ImageHeight; ++y) {
        double c_im = MaxIm - y * Im_factor;
        unsigned n;
        for (unsigned x = 0; x < ImageWidth; ++x) {
            double c_re = MinRe + x * Re_factor;
            double Z_re = c_re, Z_im = c_im;

            for (n = 0; n < MaxIterations; ++n) {
                double Z_re2 = Z_re * Z_re, Z_im2 = Z_im * Z_im;

                if (Z_re2 + Z_im2 > 4) {
                    imgp[y * info.width + x] = 0xff000000;

                    break;
                } else{
                    imgp[y * info.width + x] = 0xff0000ff;
                }

                Z_im = 2 * Z_re * Z_im + c_im;
                Z_re = Z_re2 - Z_im2 + c_re;
            }
        }
    }
    AndroidBitmap_unlockPixels(env, img);
}
