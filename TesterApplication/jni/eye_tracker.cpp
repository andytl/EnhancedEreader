/*
 * eye_tracker.cpp
 *
 *  Created on: May 5, 2015
 *      Author: Shahar_Levari
 */

#include <opencv2/core/core.hpp>
#include <opencv2/core/types_c.h>


std::pair<double, double> * cppOnNewFrame(cv::Mat * mat) {
	return new std::pair<double, double>(-.5, -.5);
}

int cppTrainOnFrame(cv::Mat *mat, double x, double y) {
	return 1;
}
