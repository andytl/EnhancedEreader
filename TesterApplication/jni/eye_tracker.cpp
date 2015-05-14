/*
 * eye_tracker.cpp
 *
 *  Created on: May 5, 2015
 *      Author: Shahar_Levari
 */

#include <opencv2/core/core.hpp>
#include <opencv2/core/types_c.h>

#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/objdetect/objdetect.hpp>
cv::CascadeClassifier face_cascade;
cv::CascadeClassifier eye_cascade;

std::vector<cv::Mat> eye_tpls;
std::vector<cv::Rect> face_bbs, eye_bbs;
double eye_thresh = 60;

bool detectEyes(cv::Mat *im, std::vector<cv::Rect>& faces, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls);
void trackEye(cv::Mat* im, cv::Mat& tpl, cv::Rect& rect);



std::pair<double, double> *cppOnNewFrame(cv::Mat *mat)
{
	try {
		if (eye_bbs.size() == 0)
		{
			// Detection stage
			// Try to detect the face and the eye of the user
			bool r = detectEyes(mat, face_bbs, eye_bbs, eye_tpls);
	//		if (r) {
	//			return new std::pair<double, double>(10, 10);
	//		} else {
	//			return new std::pair<double, double>(0, 0);
	//		}
			if (eye_bbs.size() == 0) {
				return new std::pair<double, double>(0, 0);
			} else {
				cv::Rect& eye = eye_bbs[0];
	//			return new std::pair<double, double>(eye.x + eye.width/2, eye.y + eye.height/2 );
				return new std::pair<double, double>(500, 500);
			}
		}
		else if (eye_bbs.size() > 0)
		{
			for (int i = 0; i < eye_bbs.size(); i++)
			{
				cv::Rect& eye_bb = eye_bbs[i];
				cv::Mat& eye_tpl = eye_tpls[i];

				// Tracking stage with template matching
				trackEye(mat, eye_tpl, eye_bb);

				// Update template with new image
				eye_tpl = (*mat)(eye_bb);
			}

			// Draw bounding rectangle for the eye
			if (true)
			{
				// draw rect around face
	//			for (auto& face_bb : face_bbs)
	//			{
	//				cv::rectangle(*mat, face_bb, CV_RGB(0,0,255));
	//				auto tmp = face_bb;
	//				tmp.height /= 2;
	//				cv::rectangle(*mat, tmp, CV_RGB(0,0,255));
	//			}

				for (auto& eye_bb : eye_bbs)
				{
	//				draw rect around eye
	//				cv::rectangle(*mat, eye_bb, CV_RGB(0,255,0));
					std::pair<double, double> *sp = new std::pair<double, double>;
					sp->first = eye_bb.x + eye_bb.width/2;
					sp->second = eye_bb.y + eye_bb.height/2;
					sp->first = -1000;
					sp->second = -1000;
					return sp;

				}


	//			cv::Rect large_rect = eye_bbs[0];
	//			large_rect.x -= eye_bbs[0].width;
	//			large_rect.y -= eye_bbs[0].height;
	//			large_rect.width *= 3;
	//			large_rect.height *= 3;
	//			cv::rectangle(*mat, large_rect, CV_RGB(255, 0, 0));

	//			cv::putText(*mat, std::to_string(centered_Y - eye_bbs[0].y), cv::Point(50, 50), 1, 2, CV_RGB(255, 0, 255));
			}
		}

	} catch (cv::Exception &e) {
		std::pair<double, double> *sp =  new std::pair<double, double>;
		sp->first = -1;
		sp->second = -1;
		eye_bbs.clear();
		face_bbs.clear();
		return sp;
	}

//	return new std::pair<double, double>(-.5, -.5);
}

int cppTrainOnFrame(cv::Mat *mat, double x, double y)
{



	return 1;
}

int setupNativeCode(std::string face, std::string eye)
{
	face_cascade.load(face);
	eye_cascade.load(eye);
	// Check if everything is ok
	if (face_cascade.empty() || eye_cascade.empty()) {
		return 0;
	}
	return 1;
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

	cv::groupRectangles(eyes, 0);

	for (auto& eye : eyes)
	{
		tpls.push_back(im(eyes[0]));
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
bool detectEyes(cv::Mat *im, std::vector<cv::Rect>& faces, std::vector<cv::Rect>& eyes, std::vector<cv::Mat>& tpls)
{
	const static int scale = 1;

	faces.clear();
	eyes.clear();
	tpls.clear();

	face_cascade.detectMultiScale(*im, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(30*scale,30*scale));
	if (faces.size() == 0) {
		return false;
	}
	for (int i = 0; i < faces.size(); i++)
	{
		auto half_face = faces[i];
		half_face.height /= 2;
		cv::Mat face = (*im)(half_face);
		std::vector<cv::Rect> these_eyes;
		std::vector<cv::Mat> these_tpls;

		detectEyesInFace(face, these_eyes, these_tpls);

		for (auto& eye : these_eyes)
		{
			eye += cv::Point(half_face.x, half_face.y);
		}

		eyes.insert(eyes.end(), these_eyes.begin(), these_eyes.end());
	}

	for (auto& eye : eyes)
	{
		tpls.push_back((*im)(eyes[0]));
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
void trackEye(cv::Mat* im, cv::Mat& tpl, cv::Rect& rect)
{
	cv::Size size(rect.width * 2, rect.height * 2);
	cv::Rect window(rect + size - cv::Point(size.width/2, size.height/2));

	window &= cv::Rect(0, 0, im->cols, im->rows);

	cv::Mat dst(window.width - tpl.rows + 1, window.height - tpl.cols + 1, CV_32FC1);
	cv::matchTemplate((*im)(window), tpl, dst, CV_TM_SQDIFF_NORMED);

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

