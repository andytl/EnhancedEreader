#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/core/types_c.h>

#include "eye_tracker.cpp"

#include <cstdio>

using namespace std;
//using namespace cv;

// Prevent c++ name mangling
extern "C" {

	JNIEXPORT jint JNICALL Java_com_example_testerapplication_NativeInterface_nativeInitializeTracker
		(JNIEnv *env, jobject obj, jstring face, jstring eye)
	{
		const char* faceptr = env->GetStringUTFChars(face, NULL);
		const char* eyeptr = env->GetStringUTFChars(eye, NULL);
		return setupNativeCode(faceptr, eyeptr);

	}


	JNIEXPORT jint JNICALL Java_com_example_testerapplication_NativeInterface_nativeOnNewFrame
		(JNIEnv *env, jobject obj, jlong mat, jobject dp)
	{
		//TODO: translation from java to c, then call sunjay's code
		cv::Mat *cMat = (cv::Mat *)mat;
		std::pair<double, double> *resultPt = new std::pair<double, double>(1337,1337);
		try {
			cv::Point2d p2d = cppOnNewFrame(cMat);
			resultPt->first = p2d.x;
			resultPt->second = p2d.y;
			//resultPt = new std::pair<double, double>(1337,1337);
		} catch (cv::Exception &e) {
			return -1;
		}
		jclass cls = env->GetObjectClass(dp);
		jfieldID x = env->GetFieldID(cls, "x", "D");
		jfieldID y = env->GetFieldID(cls, "y", "D");
		env->SetDoubleField(dp, x, resultPt->first);
		env->SetDoubleField(dp, y , resultPt->second);
		delete resultPt;
		return 1;
	}

	void GetJStringContent(JNIEnv *AEnv, jstring AStr, std::string &ARes)
	{
		  if (!AStr) {
		    ARes.clear();
		    return;
		  }

		  const char *s = AEnv->GetStringUTFChars(AStr,NULL);
		  ARes=s;
		  AEnv->ReleaseStringUTFChars(AStr,s);
	}

	JNIEXPORT jint JNICALL Java_com_example_testerapplication_NativeInterface_nativeTrainOnFrame
		(JNIEnv *env, jobject obj, jlong mat, jdouble x, jdouble y)
	{
		cv::Mat *cMat = (cv::Mat *)mat;
//		int resultCode = 0;
		int resultCode = cppTrainOnFrame(cMat, x, y);
		return resultCode;
	}

	JNIEXPORT jint JNICALL Java_com_example_testerapplication_NativeInterface_nativeTrainNeuralNet
		(JNIEnv *env, jobject obj, jstring saveLocation)
	{
		std::string str;
		GetJStringContent(env, saveLocation, str);
		net_train(str);
		return 1;
	}

	JNIEXPORT void JNICALL Java_com_example_testerapplication_NativeInterface_nativeSaveTrainData
		(JNIEnv *env, jobject obj, jstring saveLocation)
	{
		std::string str;
		GetJStringContent(env, saveLocation, str);
		save_train(str);
	}

	JNIEXPORT void JNICALL Java_com_example_testerapplication_NativeInterface_nativeLoadNeuralNet
		(JNIEnv *env, jobject obj, jstring filename)
	{
		std::string str;
		GetJStringContent(env, filename, str);
		load_net(str);
	}



}
