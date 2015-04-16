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
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/objdetect/objdetect.hpp>

cv::CascadeClassifier face_cascade;
cv::CascadeClassifier eye_cascade;

/**
 * Function to detect eyes from an image.
 *
 * @param  im    The source image
 * @param  tpl   Will be filled with the eye template, if detection success.
 * @return eyes
 */
std::vector<cv::Rect> detectEyesInFace(cv::Mat& im, std::vector<cv::Mat>& tpls)
{
	const static int scale = 1;
	std::vector<cv::Rect> eyes;

	tpls.clear();
	eye_cascade.detectMultiScale(im, eyes, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(20*scale,20*scale));

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eyes[0]));
	}

	return eyes;
}

/**
 * Function to detect human face and the eyes from an image.
 *
 * @param  im    The source image
 * @param  tpl   Will be filled with the eye template, if detection success.
 * @return eyes
 */
std::vector<cv::Rect> detectEyes(cv::Mat& im, std::vector<cv::Mat>& tpls)
{
	const static int scale = 1;
	std::vector<cv::Rect> faces, eyes;
	face_cascade.detectMultiScale(im, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(30*scale,30*scale));

	tpls.clear();

	for (int i = 0; i < faces.size(); i++)
	{
		cv::Mat face = im(faces[i]);
		std::vector<cv::Rect> these_eyes;
		std::vector<cv::Mat> these_tpls;

		these_eyes = detectEyesInFace(face, these_tpls);
		
		for (auto& eye : these_eyes)
		{
			eye += cv::Point(faces[i].x, faces[i].y);
		}

		eyes.insert(eyes.cend(), these_eyes.cbegin(), these_eyes.cend());
	}

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eyes[0]));
	}

	return eyes;
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
	cv::Size size(rect.width * 2, rect.height * 2);
	cv::Rect window(rect + size - cv::Point(size.width/2, size.height/2));
	
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

int main(int argc, char** argv)
{
	// Load the cascade classifiers
	// Make sure you point the XML files to the right path, or 
	// just copy the files from [OPENCV_DIR]/data/haarcascades directory
	face_cascade.load("haarcascade_frontalface_alt2.xml");
	eye_cascade.load("haarcascade_eye.xml");

	// Open webcam
	cv::VideoCapture cap(0);

	// Check if everything is ok
	if (face_cascade.empty() || eye_cascade.empty() || !cap.isOpened())
		return 1;

	/* Set video to 320x240
	cap.set(CV_CAP_PROP_FRAME_WIDTH, 320);
	cap.set(CV_CAP_PROP_FRAME_HEIGHT, 240);*/

	cv::Mat frame;
	std::vector<cv::Mat> eye_tpls;
	std::vector<cv::Rect> eye_bbs;

	bool do_zoom = false;

	int lastKey = 0;
	do
	{
		lastKey = cv::waitKey(15);
		cap >> frame;
		if (frame.empty())
			break;

		// Flip the frame horizontally, Windows users might need this
		cv::flip(frame, frame, 1);

		// Convert to grayscale and 
		// adjust the image contrast using histogram equalization
		cv::Mat gray;
		cv::cvtColor(frame, gray, CV_BGR2GRAY);

		if (eye_bbs.size() == 0 || lastKey == 'r')
		{
			// Detection stage
			// Try to detect the face and the eye of the user
			eye_bbs = detectEyes(gray, eye_tpls);
		}
		else if (lastKey == 'l')
		{
			// Do re-detection on area surrounding current bounding rectangle
			cv::Rect large_rect = eye_bbs[0];
			large_rect.x -= eye_bbs[0].width;
			large_rect.y -= eye_bbs[0].height;
			large_rect.width *= 3;
			large_rect.height *= 3;

			std::vector<cv::Mat> tpls;
			auto eyes = detectEyesInFace(gray(large_rect), tpls);
			eyes[0] += cv::Point(large_rect.x, large_rect.y);
			eye_bbs[0] = eyes[0];
			eye_tpls[0] = tpls[0];
		}
		else if (eye_bbs.size() > 0)
		{
			for (int i = 0; i < eye_bbs.size(); i++)
			{
				cv::Rect& eye_bb = eye_bbs[i];
				cv::Mat& eye_tpl = eye_tpls[i];

				// Tracking stage with template matching
				trackEye(gray, eye_tpl, eye_bb);

				// Update template with new image
				eye_tpl = gray(eye_bb);
			}

			// Draw bounding rectangle for the eye
			if (!do_zoom)
			{
				for (auto& eye_bb : eye_bbs)
				{
					cv::rectangle(frame, eye_bb, CV_RGB(0,255,0));
				}

				cv::Rect large_rect = eye_bbs[0];
				large_rect.x -= eye_bbs[0].width;
				large_rect.y -= eye_bbs[0].height;
				large_rect.width *= 3;
				large_rect.height *= 3;
				cv::rectangle(frame, large_rect, CV_RGB(255, 0, 0));
			}
		}

		if (lastKey == ' ')
			do_zoom = !do_zoom;

		// Display video
		if (eye_bbs.size() > 0 && eye_bbs[0].area() && do_zoom)
		{
			static const double scale_factor = 4.0;
			cv::Mat zoomed;
			cv::resize(frame(eye_bbs[0]), zoomed, cv::Size(), scale_factor, scale_factor);
			cv::imshow("video", zoomed);
		}
		else
			cv::imshow("video", frame);
	} while (lastKey != 'q');

	return 0;
}

