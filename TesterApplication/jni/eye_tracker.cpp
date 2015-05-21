/**
 * eye-tracking.cpp:
 * Eye detection and tracking with OpenCV
 *
 * This program tries to detect and tracking the user's eye with webcam.
 * At startup, the program performs face detection followed by eye detection
 * using OpenCV's built-in Haar cascade classifier. If the user's eye detected
 * successfully, an eye template is extracted. This template will be used in
 * the subsequent template matching for tracking the eye.
 */
#include <iostream>
#include <iomanip>

#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/objdetect/objdetect.hpp>

#include "doublefann.h"
#include "fann_cpp.h"
#include <android/log.h>
#define APPNAME "EyeTracker"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_WARN, APPNAME, __VA_ARGS__))

static cv::CascadeClassifier face_cascade;
static cv::CascadeClassifier eye_cascade;
static bool loaded = false;

static bool printmethods = false;

static FANN::neural_net net;

static cv::Point click_pt;
static cv::Point2d click_data;
static bool bdown = false;

static const double pixel_scale = 8.0;
static const cv::Size eye_size = cv::Size(36, 26);

static std::vector<double*> in_data;
static std::vector<double*> out_data;

static int trainCount = 0;


// Callback function that simply prints the information to cout
int print_callback(FANN::neural_net &net, FANN::training_data &train,
    unsigned int max_epochs, unsigned int epochs_between_reports,
    float desired_error, unsigned int epochs, void *user_data)
{
    std::cout << "Epochs     " << std::setw(8) << epochs << ". "
         << "Current Error: " << std::left << net.get_MSE() << std::right << std::endl;
    return 0;
}

void update_net(cv::Mat * eye_data, double x, double y) {
	double *in = new double[eye_size.area() / 4];
	for (unsigned i = 0; i < eye_size.area() / 4; i++) {
		in[i] = (eye_data->data[i] / 128.) - 1.;
	}
	double *out = new double[2];
	out[0] = x;
	out[1] = y;
	in_data.push_back(in);
	out_data.push_back(out);
}

void net_train() {
	if (printmethods) LOGD("net_train -- enter");
	LOGD("net train");
	const float desired_error = 0.001f;
	const unsigned int max_iterations = 250;// 300000;
//		const unsigned int max_iterations = 500;
	const unsigned int iterations_between_reports = 50; // 1000
//	const unsigned int iterations_between_reports = 100;
	FANN::training_data data;
//	net.set_callback(print_callback, NULL);
	data.set_train_data(in_data.size(), eye_size.area() / 4, in_data.data(), 2, out_data.data());
	net.init_weights(data);
	net.train_on_data(data, max_iterations, iterations_between_reports, desired_error);
	data.save_train("/sdcard/TrainData/data.fann");
	if (printmethods) LOGD("net train -- exit");
}

void create_nn(FANN::neural_net& net) {
	if (printmethods) LOGD("create nn -- enter");
	const float learning_rate = 0.7f;
    const unsigned int num_layers = 4;
    const unsigned int num_input = eye_size.area() / 4; // because we'll be downsizing
    const unsigned int num_hidden = 8;
    const unsigned int num_output = 2;

    LOGD("create nn");
	net.create_standard(num_layers, num_input, num_hidden, num_hidden, num_output);

    net.set_learning_rate(learning_rate);

    net.set_activation_steepness_hidden(1.0);
    net.set_activation_steepness_output(1.0);

    net.set_activation_function_hidden(FANN::SIGMOID_SYMMETRIC_STEPWISE);
    net.set_activation_function_output(FANN::SIGMOID_SYMMETRIC_STEPWISE);

//	net.set_training_algorithm(FANN::TRAIN_INCREMENTAL);

    if (printmethods) LOGD("create nn -- exit");
}

void MouseCallback(int event, int x, int y, int flags, void* userdata)
{
	if (printmethods) LOGD("MouseCallback -- enter");
	if (event == cv::EVENT_MOUSEMOVE || event == cv::EVENT_LBUTTONDOWN || event == cv::EVENT_LBUTTONDBLCLK || bdown)
	{
		click_pt.x = x;
		click_pt.y = y;

		click_data = click_pt;
		click_data.x /= pixel_scale;
		click_data.x /= (eye_size.width / 2.0);
		click_data.x -= 1.0;
		click_data.y /= pixel_scale;
		click_data.y /= (eye_size.height / 2.0);
		click_data.y -= 1.0;

		if (event == cv::EVENT_LBUTTONDOWN) {
			bdown = true;
		}
	}
	if (event == cv::EVENT_LBUTTONUP) {
		bdown = false;
	}
	if (printmethods) LOGD("MouseCallback -- exit");
}

/**
 * Function to detect eyes from an image.
 *
 * @param  im    The source image
 * @param  tpl   Will be filled with the eye template, if detection success.
 * @return eyes
 */
bool detectEyesInFace(cv::Mat& im, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls)
{
	if (printmethods) LOGD("detectEyesInFace -- enter");
	const static int scale = 1;

	eyes.clear();
	tpls.clear();

	eye_cascade.detectMultiScale(im, eyes, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(20*scale,20*scale));
	//eye_cascade.detectMultiScale(im, eyes, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(26,22), cv::Size(26,22));

	cv::groupRectangles(eyes, 0);

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eye));
	}
	if (printmethods) LOGD("detectEyesInFace -- exit");
	return true;
}

/**
 * Function to detect human face and the eyes from an image.
 *
 * @param  im    The source image
 * @param  tpl   Will be filled with the eye template, if detection success.
 * @return eyes
 */
bool detectEyes(cv::Mat& im, std::vector<cv::Rect>& faces, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls)
{
	const static int scale = 1;
	if (printmethods) LOGD("Detect Eyes -- enter");
	faces.clear();
	eyes.clear();
	tpls.clear();

	face_cascade.detectMultiScale(im, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(30*scale,30*scale));

	for (int i = 0; i < faces.size(); i++)
	{
		auto quarter_face = faces[i];
		quarter_face.height /= 2;
		quarter_face.width /= 2;
		cv::Mat face = im(quarter_face);
		std::vector<cv::Rect> these_eyes;
		std::vector<cv::Mat> these_tpls;

		detectEyesInFace(face, these_eyes, these_tpls);

		for (auto& eye : these_eyes)
		{
			eye += cv::Point(quarter_face.x, quarter_face.y);
		}

		eyes.insert(eyes.end(), these_eyes.begin(), these_eyes.end());
	}

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eye));
	}
	if (printmethods) LOGD("Detect Eyes -- exit");

	return true;
}

/**
 * Perform template matching to search the user's eye in the given image.
 *
 * @param   im    The source image
 * @param   tpl   The eye template
 * @param   rect  The eye bounding box, will be updated with the new location of the eye
 */
void trackEye(cv::Mat& im, cv::Mat& tpl, cv::Rect& rect)
{
	if (printmethods) LOGD("Track Eyes -- enter");

	cv::Rect window = rect;
	window.x -= rect.width/2;
	window.y -= rect.height/2;
	window.width *= 2;
	window.height *= 2;

	window &= cv::Rect(0, 0, im.cols, im.rows);
	if (window.height - tpl.cols + 1 < 0 || window.width - tpl.rows + 1< 0) {
		LOGD("Window is 0");
		rect.x = rect.y = rect.width = rect.height = 0;
		return;
	}
	cv::Mat dst(window.width - tpl.rows + 1, window.height - tpl.cols + 1, CV_32FC1);
	cv::matchTemplate(im(window), tpl, dst, CV_TM_SQDIFF_NORMED);

	double minval, maxval;
	cv::Point minloc, maxloc;
	cv::minMaxLoc(dst, &minval, &maxval, &minloc, &maxloc);

	if (minval <= 0.2)
	{
		rect.x = window.x + minloc.x;
		rect.y = window.y + minloc.y;
	}
	else
		rect.x = rect.y = rect.width = rect.height = 0;
	if (printmethods) LOGD("Track Eyes -- exit");
}

cv::Rect resized(const cv::Rect& rect, const cv::Size& size) {
	if (printmethods) LOGD("resized -- enter");
	cv::Rect nr = rect;
	nr += cv::Point(nr.width / 2, nr.height / 2);
	nr.width = size.width;
	nr.height = size.height;
	nr -= cv::Point(nr.width / 2, nr.height / 2);
	if (printmethods) LOGD("resized -- exit");
	return nr;
}

int main(int argc, char** argv)
{
	return 0;
}

cv::Mat * processFrame(const cv::Mat *frame) {

	static std::vector<cv::Mat> eye_tpls;
	static std::vector<cv::Rect> face_bbs, eye_bbs;

	static const double eye_thresh = 50;

	static const bool do_zoom = true;
	static const bool do_processing = true;
	static const bool do_equalize = true;
	static const bool do_threshold = false;
	static const bool do_downsample = true;
	static const bool do_gauss = false;
	static const bool do_borders = false;

	if (printmethods) LOGD("Process Frame -- enter");
	if (frame->empty()) {
		return NULL;
	}
	try {
		// Flip the frame horizontally, Windows users might need this
		//cv::flip(*frame, *frame, 1);

		// Convert to grayscale and
		// adjust the image contrast using histogram equalization
		cv::Mat gray;
		//TODO: Ask sunjay why????????
//		cv::cvtColor(*frame, gray, CV_BGR2GRAY);
		gray = *frame;

		if (eye_bbs.size() == 0 || eye_bbs[0].area() == 0) {
			// Detection stage
			// Try to detect the face and the eye of the user
			detectEyes(gray, face_bbs, eye_bbs, eye_tpls);
		} else if (eye_bbs.size() > 0) {
			for (int i = 0; i < eye_bbs.size(); i++) {
				cv::Rect& eye_bb = eye_bbs[i];
				cv::Mat& eye_tpl = eye_tpls[i];

				// Tracking stage with template matching
				trackEye(gray, eye_tpl, eye_bb);

				// Update template with new image
				//eye_tpl = gray(eye_bb);
			}
		}

		// Display video
		if (eye_bbs.size() > 0 && eye_bbs[0].area() && do_zoom)
		{
			double scale_factor = pixel_scale;
			cv::Rect tempRect = resized(eye_bbs[0], eye_size);
			cv::Mat *zoomed = new cv::Mat((*frame)(tempRect));


			if (do_processing)
			{
				std::string processing_msg = "gray";
				// TODO: talk to sunjay about if we are in the correct color mode
//				cv::cvtColor(*zoomed, *zoomed, CV_BGR2GRAY);


				if (do_equalize) {
					cv::equalizeHist(*zoomed, *zoomed);
					processing_msg += "+equalizeHist";
				}

				if (do_threshold) {
					cv::threshold(*zoomed, *zoomed, eye_thresh, 255, cv::THRESH_BINARY);
					processing_msg += "+threshold";
				}


				if (do_gauss) {
					cv::GaussianBlur(*zoomed, *zoomed, cv::Size(9, 9), 2, 2);
					processing_msg += "+gauss";
				}
				if (do_downsample) {
					cv::resize(*zoomed, *zoomed, cv::Size(), 0.5, 0.5, cv::INTER_CUBIC);
					scale_factor *= 2;
					processing_msg += "+downsample";
				}
				// cv::cvtColor(zoomed, zoomed, CV_GRAY2BGR);

				/*for (int i = 0; i < contours.size(); i++) {
					cv::drawContours(zoomed, contours, i, CV_RGB(0, 0, 255), 1);
				}

				cv::resize(zoomed, zoomed, cv::Size(), scale_factor, scale_factor, cv::INTER_NEAREST);

				cv::putText(zoomed, processing_msg, cv::Point(20, 20), 1, 0.75, CV_RGB(255,0,0));
				cv::putText(zoomed, std::to_string(eye_thresh), cv::Point(20, 40), 1, 1, CV_RGB(255, 0, 255));
				cv::Point res = cv::Point(int(std::round(zoomed.cols / scale_factor)), int(std::round(zoomed.rows / scale_factor)));
				cv::putText(zoomed, std::to_string(res.x) + " x " + std::to_string(res.y), cv::Point(20, 60), 1, 1, CV_RGB(0,0,255));*/
				if (printmethods) LOGD("Process Frame -- exit");
				return zoomed;
			}
		}
	} catch (cv::Exception&) {
		std::string sss = NULL;
		sss.c_str();
		if (printmethods) LOGD("Process Frame -- exit");
		return NULL;
	}
	if (printmethods) LOGD("Process Frame -- exit");
	return NULL;
}

int setupNativeCode(std::string face, std::string eye)
{
	face_cascade.load(face);
	eye_cascade.load(eye);
	// Check if everything is ok
	if (face_cascade.empty() || eye_cascade.empty()) {
		loaded = 0;
		return 0;
	}
	create_nn(net);

	loaded = 1;
	return 1;
}

int cppTrainOnFrame(const cv::Mat *frame, double x, double y) {
	trainCount++;
	cv::Mat *zoomed = processFrame(frame);
	if (!zoomed)
		return -1;

	update_net(zoomed, x, y);

	delete zoomed;

	return 0;
}

cv::Point2d cppOnNewFrame(cv::Mat* frame) {
	//cv::putText(zoomed, std::to_string(click_data.x) + ", " + std::to_string(click_data.y), cv::Point(20, 80), 1, 1, CV_RGB(0,255,0));
	LOGD("OnNewFrame -- enter");
	LOGD("%p", &net);
	cv::Mat *eye_data = processFrame(frame);
	if (eye_data == NULL) {
		LOGD("eye data is null");
		return cv::Point2d(-10, -10);
	}
	double *in = new double[eye_size.area() / 4];
	LOGD("num pixels: %d", eye_size.area());
	for (unsigned i = 0; i < eye_size.area() / 4; i++) {
		in[i] = (eye_data->data[i] / 128.) - 1.;
		if (in[i] < -1 || in[i] > 1) {
			LOGD("PIXEL OUT OF RANGE");
		}
	}
	double *out = net.run(in);
	//cv::putText(zoomed, std::to_string(out[0]) + ", " + std::to_string(out[1]), cv::Point(20, 100), 1, 1, CV_RGB(0,255,255));
	LOGD("%lf, %lf", out[0], out[1]);
	LOGD("%p", out);
	cv::Point2d outpt(out[0], out[1]);
	//cv::circle(zoomed, guesspt, 1, CV_RGB(0,255,255), 1);
	delete in;
	delete eye_data;
	LOGD("OnNewFrame -- exit");
	return outpt;
}





void forcedFail() {
	int * n = (int*)0x00;
	int x = *n;
}



















//
///*
// * eye_tracker.cpp
// *
// *  Created on: May 5, 2015
// *      Author: Shahar_Levari
// */
//
//#include <opencv2/core/core.hpp>
//#include <opencv2/core/types_c.h>
//
//#include <opencv2/imgproc/imgproc.hpp>
//#include <opencv2/highgui/highgui.hpp>
//#include <opencv2/objdetect/objdetect.hpp>
//cv::CascadeClassifier face_cascade;
//cv::CascadeClassifier eye_cascade;
//
//std::vector<cv::Mat> eye_tpls;
//std::vector<cv::Rect> face_bbs, eye_bbs;
//double eye_thresh = 60;
//
//bool detectEyes(cv::Mat *im, std::vector<cv::Rect>& faces, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls);
//void trackEye(cv::Mat* im, cv::Mat& tpl, cv::Rect& rect);
//
//
//
//std::pair<double, double> *cppOnNewFrame(cv::Mat *mat)
//{
//	try {
//		if (eye_bbs.size() == 0)
//		{
//			// Detection stage
//			// Try to detect the face and the eye of the user
//			bool r = detectEyes(mat, face_bbs, eye_bbs, eye_tpls);
//	//		if (r) {
//	//			return new std::pair<double, double>(10, 10);
//	//		} else {
//	//			return new std::pair<double, double>(0, 0);
//	//		}
//			if (eye_bbs.size() == 0) {
//				return new std::pair<double, double>(0, 0);
//			} else {
//				cv::Rect& eye = eye_bbs[0];
//	//			return new std::pair<double, double>(eye.x + eye.width/2, eye.y + eye.height/2 );
//				return new std::pair<double, double>(500, 500);
//			}
//		}
//		else if (eye_bbs.size() > 0)
//		{
//			for (int i = 0; i < eye_bbs.size(); i++)
//			{
//				cv::Rect& eye_bb = eye_bbs[i];
//				cv::Mat& eye_tpl = eye_tpls[i];
//
//				// Tracking stage with template matching
//				trackEye(mat, eye_tpl, eye_bb);
//
//				// Update template with new image
//				eye_tpl = (*mat)(eye_bb);
//			}
//
//			// Draw bounding rectangle for the eye
//			if (true)
//			{
//				// draw rect around face
//	//			for (auto& face_bb : face_bbs)
//	//			{
//	//				cv::rectangle(*mat, face_bb, CV_RGB(0,0,255));
//	//				auto tmp = face_bb;
//	//				tmp.height /= 2;
//	//				cv::rectangle(*mat, tmp, CV_RGB(0,0,255));
//	//			}
//
//				for (auto& eye_bb : eye_bbs)
//				{
//	//				draw rect around eye
//	//				cv::rectangle(*mat, eye_bb, CV_RGB(0,255,0));
//					std::pair<double, double> *sp = new std::pair<double, double>;
//					sp->first = eye_bb.x + eye_bb.width/2;
//					sp->second = eye_bb.y + eye_bb.height/2;
//					sp->first = -1000;
//					sp->second = -1000;
//					return sp;
//
//				}
//
//
//	//			cv::Rect large_rect = eye_bbs[0];
//	//			large_rect.x -= eye_bbs[0].width;
//	//			large_rect.y -= eye_bbs[0].height;
//	//			large_rect.width *= 3;
//	//			large_rect.height *= 3;
//	//			cv::rectangle(*mat, large_rect, CV_RGB(255, 0, 0));
//
//	//			cv::putText(*mat, std::to_string(centered_Y - eye_bbs[0].y), cv::Point(50, 50), 1, 2, CV_RGB(255, 0, 255));
//			}
//		}
//
//	} catch (cv::Exception &e) {
//		std::pair<double, double> *sp =  new std::pair<double, double>;
//		sp->first = -1;
//		sp->second = -1;
//		eye_bbs.clear();
//		face_bbs.clear();
//		return sp;
//	}
//
////	return new std::pair<double, double>(-.5, -.5);
//}
//
//int cppTrainOnFrame(cv::Mat *mat, double x, double y)
//{
//	if (x < -1 || y < -1 || x > 1 || y > 1) {
//		return -1;
//	}
//
//
//	return 1;
//}
//
//int setupNativeCode(std::string face, std::string eye)
//{
//	face_cascade.load(face);
//	eye_cascade.load(eye);
//	// Check if everything is ok
//	if (face_cascade.empty() || eye_cascade.empty()) {
//		return 0;
//	}
//	return 1;
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
///**
// * Function to detect eyes from an image.
// *
// * @param  im    The source image
// * @param  tpl   Will be filled with the eye template, if detection success.
// * @return eyes
// */
//bool detectEyesInFace(cv::Mat& im, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls)
//{
//	const static int scale = 1;
//
//	eyes.clear();
//	tpls.clear();
//
//	eye_cascade.detectMultiScale(im, eyes, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(20*scale,20*scale));
//
//	cv::groupRectangles(eyes, 0);
//
//	for (auto& eye : eyes)
//	{
//		tpls.push_back(im(eyes[0]));
//	}
//
//	return true;
//}
//
//
///**
// * Function to detect human face and the eyes from an image.
// *
// * @param  im    The source image
// * @param  tpl   Will be filled with the eye template, if detection success.
// * @return eyes
// */
//bool detectEyes(cv::Mat *im, std::vector<cv::Rect>& faces, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls)
//{
//	const static int scale = 1;
//
//	faces.clear();
//	eyes.clear();
//	tpls.clear();
//
//	face_cascade.detectMultiScale(*im, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(30*scale,30*scale));
//	if (faces.size() == 0) {
//		return false;
//	}
//	for (int i = 0; i < faces.size(); i++)
//	{
//		auto half_face = faces[i];
//		half_face.height /= 2;
//		cv::Mat face = (*im)(half_face);
//		std::vector<cv::Rect> these_eyes;
//		std::vector<cv::Mat> these_tpls;
//
//		detectEyesInFace(face, these_eyes, these_tpls);
//
//		for (auto& eye : these_eyes)
//		{
//			eye += cv::Point(half_face.x, half_face.y);
//		}
//
//		eyes.insert(eyes.end(), these_eyes.begin(), these_eyes.end());
//	}
//
//	for (auto& eye : eyes)
//	{
//		tpls.push_back((*im)(eyes[0]));
//	}
//
//	return true;
//}
//
//
//
///**
// * Perform template matching to search the user's eye in the given image.
// *
// * @param   im    The source image
// * @param   tpl   The eye template
// * @param   rect  The eye bounding box, will be updated with the new location of the eye
// */
//void trackEye(cv::Mat* im, cv::Mat& tpl, cv::Rect& rect)
//{
//	cv::Size size(rect.width * 2, rect.height * 2);
//	cv::Rect window(rect + size - cv::Point(size.width/2, size.height/2));
//
//	window &= cv::Rect(0, 0, im->cols, im->rows);
//
//	cv::Mat dst(window.width - tpl.rows + 1, window.height - tpl.cols + 1, CV_32FC1);
//	cv::matchTemplate((*im)(window), tpl, dst, CV_TM_SQDIFF_NORMED);
//
//	double minval, maxval;
//	cv::Point minloc, maxloc;
//	cv::minMaxLoc(dst, &minval, &maxval, &minloc, &maxloc);
//
//	if (minval <= 0.2)
//	{
//		rect.x = window.x + minloc.x;
//		rect.y = window.y + minloc.y;
//	}
//	else
//		rect.x = rect.y = rect.width = rect.height = 0;
//}
//
