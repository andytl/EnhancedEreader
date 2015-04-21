#include <jni.h>

// Prevent c++ name mangling
extern "C" {

	// TODO: Verify that all examples of JNI seen so far insert methods directly into classes
	// That is, The ReaderActivity below must match the class it is called from or it doesnt work
	JNIEXPORT jint JNICALL Java_com_example_testerapplication_ReaderActivity_foo
		(JNIEnv *env, jobject obj) {
		return 13371337;
	}

}
