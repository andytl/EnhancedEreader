#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/core/types_c.h>

#include <cstdio>

using namespace std;
//using namespace cv;

// Prevent c++ name mangling
extern "C" {

	// TODO: Verify that all examples of JNI seen so far insert methods directly into classes
	// That is, The ReaderActivity below must match the class it is called from or it doesnt work
	JNIEXPORT jint JNICALL Java_com_example_testerapplication_NativeInterface_foo
		(JNIEnv *env, jobject obj) {
		cv::Mat M(7,7,CV_32FC2,cv::Scalar(1,3));
		printf("hello world %d", M.cols);

		return M.cols;
	}

}
