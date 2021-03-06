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

#include <doublefann.h>
#include <fann_cpp.h>

static cv::CascadeClassifier face_cascade;
static cv::CascadeClassifier eye_cascade;
static bool loaded = false;

static FANN::neural_net net;

static cv::Point click_pt;
static cv::Point2d click_data;
static bool bdown = false;

static const double pixel_scale = 8.0;
static const cv::Size eye_size = cv::Size(36, 26);

static std::vector<double*> in_data;
static std::vector<double*> out_data;

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
	const float desired_error = 0.001f;
	const unsigned int max_iterations = 250;// 300000;
	const unsigned int iterations_between_reports = 50; // 1000

	FANN::training_data data;
	net.set_callback(print_callback, NULL);
	data.set_train_data(in_data.size(), eye_size.area() / 4, in_data.data(), 2, out_data.data());
	net.init_weights(data);
	net.train_on_data(data, max_iterations, iterations_between_reports, desired_error);
	data.save_train("data.fann");
}

void create_nn(FANN::neural_net& net) {
	const float learning_rate = 0.7f;
    const unsigned int num_layers = 4;
    const unsigned int num_input = eye_size.area() / 4; // because we'll be downsizing
    const unsigned int num_hidden = 8;
    const unsigned int num_output = 2;

	net.create_standard(num_layers, num_input, num_hidden, num_hidden, num_output);

    net.set_learning_rate(learning_rate);

    net.set_activation_steepness_hidden(1.0);
    net.set_activation_steepness_output(1.0);
    
    net.set_activation_function_hidden(FANN::SIGMOID_SYMMETRIC_STEPWISE);
    net.set_activation_function_output(FANN::SIGMOID_SYMMETRIC_STEPWISE);

	//net.set_training_algorithm(FANN::TRAIN_INCREMENTAL);
}

void MouseCallback(int event, int x, int y, int flags, void* userdata)
{
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

		eyes.insert(eyes.cend(), these_eyes.cbegin(), these_eyes.cend());
	}

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eye));
	}

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
	cv::Rect window = rect;
	window.x -= rect.width/2;
	window.y -= rect.height/2;
	window.width *= 2;
	window.height *= 2;
	
	window &= cv::Rect(0, 0, im.cols, im.rows);

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
}

cv::Rect resized(const cv::Rect& rect, const cv::Size& size) {
	cv::Rect nr = rect;
	nr += cv::Point(nr.width / 2, nr.height / 2);
	nr.width = size.width;
	nr.height = size.height;
	nr -= cv::Point(nr.width / 2, nr.height / 2);

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
	static const bool do_gauss = true;
	static const bool do_borders = false;

	if (frame->empty())
		return NULL;
	try {
		// Flip the frame horizontally, Windows users might need this
		//cv::flip(*frame, *frame, 1);

		// Convert to grayscale and 
		// adjust the image contrast using histogram equalization
		cv::Mat gray;
		cv::cvtColor(*frame, gray, CV_BGR2GRAY);

		if (eye_bbs.size() == 0) {
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
			cv::Mat *zoomed = new cv::Mat((*frame)(resized(eye_bbs[0], eye_size)));
			if (do_processing)
			{
				std::string processing_msg = "gray";
				cv::cvtColor(*zoomed, *zoomed, CV_BGR2GRAY);

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
				return zoomed;
			}
		}
	} catch (cv::Exception&) {
		return NULL;
	}
	return NULL;
}

int cppTrainOnFrame(const cv::Mat *frame, double x, double y) {
	if (!loaded) {
		// Load the cascade classifiers
		// Make sure you point the XML files to the right path, or 
		// just copy the files from [OPENCV_DIR]/data/haarcascades directory
		face_cascade.load("haarcascades/haarcascade_frontalface_alt2.xml");
		//eye_cascade.load("haarcascades/haarcascade_eye_tree_eyeglasses.xml");
		eye_cascade.load("haarcascades/haarcascade_mcs_lefteye.xml");

		// Check if everything is ok
		if (face_cascade.empty() || eye_cascade.empty())
			return 1;

		create_nn(net);

		loaded = true;
	}

	cv::Mat *zoomed = processFrame(frame);
	if (!zoomed)
		return -1;

	update_net(zoomed, x, y);

	delete zoomed;

	return 0;
}

cv::Point2d cppOnNewFrame(cv::Mat* frame) {
	//cv::putText(zoomed, std::to_string(click_data.x) + ", " + std::to_string(click_data.y), cv::Point(20, 80), 1, 1, CV_RGB(0,255,0));

	cv::Mat *eye_data = processFrame(frame);

	double *in = new double[eye_size.area() / 4];
	for (unsigned i = 0; i < eye_size.area() / 4; i++) {
		in[i] = (eye_data->data[i] / 128.) - 1.;
	}
	double *out = net.run(in);
	//cv::putText(zoomed, std::to_string(out[0]) + ", " + std::to_string(out[1]), cv::Point(20, 100), 1, 1, CV_RGB(0,255,255));
	cv::Point2d outpt(out[0], out[1]);
	//cv::circle(zoomed, guesspt, 1, CV_RGB(0,255,255), 1);
	delete in;
	delete eye_data;

	return outpt;
}

