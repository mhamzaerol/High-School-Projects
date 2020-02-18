#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

//#include <windows.graphics.h>
#include <iostream>
#include <queue>
#include <stdio.h>
#include <math.h>

#include "constants.h"
#include "findEyeCenter.h"
#include "findEyeCorner.h"
#include "stdafx.h"
#include <thread>         // std::this_thread::sleep_for
#include <chrono> 
#include <windows.h>

using namespace cv;
using namespace std;

#define SPEED 15

/** Constants **/


/** Function Headers */
void detectAndDisplay(cv::Mat frame, std:: string winName,bool ok);


int a[645][645][3];
/** Global variables */
//-- Note, either copy these two files from opencv/data/haarscascades to your current folder, or change these locations
int STEPX = 187;
int STEPY = 183;
cv::String face_cascade_name = "haarcascade_frontalface_alt.xml";
cv::CascadeClassifier face_cascade;
std::string main_window_name = "Capture - Face detection";
std::string face_window_name = "Capture - Face";
std::string BIG = "BIG - WINDOW";
cv::RNG rng(12345);
cv::Mat debugImage;
cv::Mat skinCrCbHist = cv::Mat::zeros(cv::Size(256, 256), CV_8UC1);
cv::Point Cnt;
cv::Point EyeCnt;
int CalPointsx[10];
int CalPointsy[10];
cv::Point EYE[10][10];
bool NO=false;
clock_t t;
std::vector<Mat> openedEye, closedEye;
MatND openedEyeHistR, openedEyeHistG, openedEyeHistB;
MatND closedEyeHistR, closedEyeHistG, closedEyeHistB;
int METHOD = 3;
int histSize = 256;
float range[] = { 0, 256 };
const float* histRange = { range };
int totblink;
Point EyeCntBase;


/**
* @function main
*/

MatND get_histogram(cv::Mat frame) {

	MatND res;

	calcHist(&frame, 1, 0, Mat(), res, 1, &histSize, &histRange, true, false);

	return res;

}

bool is_blink(Mat frame) {

	std::vector<Mat> now;

	split(frame, now);

	MatND histR = get_histogram(now[2]);
	MatND histG = get_histogram(now[1]);
	MatND histB = get_histogram(now[0]);

	double distanceToOpened = compareHist(histR, openedEyeHistR, METHOD) + compareHist(histG, openedEyeHistG, METHOD) + compareHist(histB, openedEyeHistB, METHOD);
	double distanceToClosed = compareHist(histR, closedEyeHistR, METHOD) + compareHist(histG, closedEyeHistG, METHOD) + compareHist(histB, closedEyeHistB, METHOD);

	if (distanceToOpened / 10.0 * 8 > distanceToClosed) return true;
	
	return false;
	
}

void RightClick() {

	INPUT    Input = { 0 };
	// Press the right key
	Input.type = INPUT_MOUSE;
	Input.mi.dwFlags = MOUSEEVENTF_RIGHTDOWN;
	::SendInput(1, &Input, sizeof(INPUT));
	// Right up
	::ZeroMemory(&Input, sizeof(INPUT));
	Input.type = INPUT_MOUSE;
	Input.mi.dwFlags = MOUSEEVENTF_RIGHTUP;
	::SendInput(1, &Input, sizeof(INPUT));

}

void LeftClick() {
	
	INPUT    Input = { 0 };
	// Press the button
	Input.type = INPUT_MOUSE;
	Input.mi.dwFlags = MOUSEEVENTF_LEFTDOWN;
	::SendInput(1, &Input, sizeof(INPUT));
	// The left button up
	::ZeroMemory(&Input, sizeof(INPUT));
	Input.type = INPUT_MOUSE;
	Input.mi.dwFlags = MOUSEEVENTF_LEFTUP;
	::SendInput(1, &Input, sizeof(INPUT));

}

void MouseMove(int x, int y)
{
	double fScreenWidth = ::GetSystemMetrics(SM_CXSCREEN) - 1;
	double fScreenHeight = ::GetSystemMetrics(SM_CYSCREEN) - 1;
	double fx = x * (65535.0f / fScreenWidth);
	double fy = y * (65535.0f / fScreenHeight);
	INPUT  Input = { 0 };
	Input.type = INPUT_MOUSE;
	Input.mi.dwFlags = MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE;
	Input.mi.dx = fx;
	Input.mi.dy = fy;
	::SendInput(1, &Input, sizeof(INPUT));
}

void MoveMouse() {

	for (int i = 0; i < 4; i++) {

		for (int j = 0; j < 3; j++) {

			if (EYE[i][j].x <= EyeCnt.x && EYE[i][j].y <= EyeCnt.y && EYE[i + 1][j + 1].x >= EyeCnt.x && EYE[i + 1][j + 1].y >= EyeCnt.y) {

				int X = CalPointsx[i] + STEPX * (1.0*(EyeCnt.x - EYE[i][j].x) / (EYE[i + 1][j + 1].x - EYE[i][j].x));
				int Y = CalPointsy[i] + STEPY * (1.0*(EyeCnt.y - EYE[i][j].y) / (EYE[i + 1][j + 1].y - EYE[i][j].y));

				MouseMove(X,Y);

				break;
				
			}

		}

	}

}

void moveit(POINT now, Point to) {

	Point vec(to.x - now.x, to.y - now.y);

	double K = sqrt(1.0*(SPEED*SPEED) / (vec.x*vec.x + vec.y*vec.y));

	Point res(now.x + vec.x*K, now.y + vec.y*K);

	MouseMove(res.x,res.y);

}

int dist(Point a, Point b) {

	return sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y));

}

void CalibrateCam(VideoCapture cpt) {

	cv::namedWindow("EyeNow",CV_WINDOW_AUTOSIZE);

	while (1) {

	//	if (GetAsyncKeyState(VK_LBUTTON) & 0x8000) break;

		Mat mat;

		cpt.read(mat);

		int dist = 50;

		int width = mat.size().width;
		int height = mat.size().height;

		for (int i = 0; i<height; i += dist)
			cv::line(mat, Point(0, i), Point(width, i), cv::Scalar(255, 255, 255));

		for (int i = 0; i<width; i += dist)
			cv::line(mat, Point(i, 0), Point(i, height), cv::Scalar(255, 255, 255));

		for (int i = 0; i < height; i += dist)
			for (int j = 0; j < width; j += dist)
				mat.at<cv::Vec3b>(i, j).val[0] = 10,
				mat.at<cv::Vec3b>(i, j).val[1] = 10,
				mat.at<cv::Vec3b>(i, j).val[2] = 10;

		imshow("EyeNow", mat);

		int c = cv::waitKey(10);
		if ((char)c == 'c') { break; }

	}

	destroyWindow("EyeNow");

}

Vec3b middle(Vec3b x, Vec3b y) {

	Vec3b res;

	for (int i = 0; i < 3; i++) res[i] = (x[i] + y[i]) / 2;
	
	return res;

}

void arrange(Mat &now) {

	memset(a, 0, sizeof(a));

	for (int i = 0; i < now.rows; i++) {
	
		for (int j = 0; j < now.cols; j++) {

			Vec3b cur = now.at<Vec3b>(i, j);

			for (int k = 0; k < 3; k++) {

				a[i][j][k] = (i>0?a[i - 1][j][k]:0) + (j>0?a[i][j-1][k]:0)-((i>0 && j>0)?a[i-1][j-1][k]:0)+(cur[k]<180)*cur[k];
			
			}

		}
	
	}
	
	for (int i = 0; i < now.rows; i++) {

		for (int j = 0; j < now.cols; j++) {

			Vec3b cur = now.at<Vec3b>(i, j);

			if (cur[0] < 180 && cur[1] < 180 && cur[2] < 180) {		
			}
			else {

				for (int k = 0; k < 3; k++) {

					now.at<Vec3b>(i, j)[k] = (a[min(i + 6, now.rows - 1)][min(j + 6, now.cols - 1)][k] - a[min(now.rows-1,i+6)][max(0, j - 6)][k] - a[max(0, i - 6)][min(j+6,now.cols-1)][k] + a[max(0, i - 6)][max(0, j - 6)][k]);
				
				}

			}

		}

	}
	
	/*for (int i = 0; i < cpy.rows; i++) {

		for (int j = 0; j < cpy.cols; j++) {

			Vec3b cur = cpy.at<Vec3b>(i, j);

			if(cur[0]==255) {
			
				now.at<Vec3b>(i, j) = (0, 0, 0);//middle(now.at<Vec3b>(i, j - 1), now.at<Vec3b>(i - 1, j));

			}

		}

	}*/

}

int main(int argc, const char** argv) {
	
	// 799 599

	cv::namedWindow("Name", CV_WINDOW_NORMAL);
	cv::setWindowProperty("Name", CV_WND_PROP_FULLSCREEN, CV_WINDOW_FULLSCREEN);


	/*cv::Mat image = cv::Mat(599, 799, CV_8UC3);

	putText(image, "GOZLERINIZI ACIN", cvPoint(100, 100),
		FONT_HERSHEY_COMPLEX_SMALL, 1.1, cvScalar(0, 0, 0), 1, CV_AA);

	imshow("Name", image);

	while (1) {

		char c=waitKey(10);

		if (c == 'c') break;

	}

	return 0;*/

	cv::Mat frame;

	// Load the cascades
	//if (!face_cascade.load(face_cascade_name)) { printf("--(!)Error loading face cascade, please change face_cascade_name in source code.\n"); return -1; };

	createCornerKernels();
	ellipse(skinCrCbHist, cv::Point(113, 155.6), cv::Size(23.4, 15.2),
		43.0, 0.0, 360.0, cv::Scalar(255, 255, 255), -1);

	cv::VideoCapture capt(0);

	/*namedWindow("Name1", CV_WINDOW_NORMAL);
	namedWindow("Name2", CV_WINDOW_NORMAL);

	while (true) {

		cv::Mat i1;
		captL.read(i1);
		cv::Mat i2;
		captR.read(i2);

		imshow("Name1", i1);
		imshow("Name2", i2);
		waitKey(10);

	}*/

	if (!capt.isOpened()) {
		
		printf("Not able to use camera!!!");

		return 0;

	}

	CalibrateCam(capt);

	//return 0;

	int stx = 25;
	int sty = 25;

	for (int i = 0; i < 5; i++) {

		CalPointsx[i] = stx;
		CalPointsy[i] = sty;
		stx += STEPX;
		sty += STEPY;

	}

	for (int i = 0; i < 5; i++) {

		for (int j = 0; j < 4; j++) {
			 
			cv::Mat temp;
			cv::Mat	img = cv::Mat(599, 799, CV_8UC3, Scalar(255, 255, 255));
			
			circle(img, Point(CalPointsx[i], CalPointsy[j]), 25, Scalar(255, 255, 255), -1, 8, 0);
			circle(img, Point(CalPointsx[i], CalPointsy[j]), 20, Scalar(0, 0, 0), -1, 8, 0);
			circle(img, Point(CalPointsx[i], CalPointsy[j]), 15, Scalar(235, 213, 104), -1, 8, 0);
			circle(img, Point(CalPointsx[i], CalPointsy[j]), 10, Scalar(62, 62, 238), -1, 8, 0);
			circle(img, Point(CalPointsx[i], CalPointsy[j]), 5,  Scalar(51, 255, 255), -1, 8, 0);

			imshow("Name", img);

			cv::waitKey(10);

			while (1) {

				if (GetAsyncKeyState(VK_LBUTTON) & 0x8000) break;
				capt.read(frame);

			}

			/*while (1) {

				if ((GetKeyState(VK_LBUTTON) & 0x100) != 0) break ;

				captL.read(frame);
				captR.read(frame);

			}*/

			//std::this_thread::sleep_for(std::chrono::seconds(1));

			capt.read(frame);

			frame.copyTo(temp);

			// mirror it
			cv::flip(temp, temp, 1);
			//temp.copyTo(debugImage);

			arrange(frame);

			// Apply the classifier to the frame
			if(!temp.empty()) {
			
				detectAndDisplay(temp,"Name",false);

				EYE[i][j] = Cnt;

			}

			t = clock();

			while (clock() - t < 600);

		}


	}

	// getting closed and opened eye pictures
	cv::Mat image = cv::Mat(599, 799, CV_8UC3);

	putText(image, "GOZLERINIZI ACIN", cvPoint(100, 100),
		FONT_HERSHEY_COMPLEX_SMALL, 1.1, cvScalar(0, 0, 0), 1, CV_AA);

	imshow("Name", image);

	cvWaitKey(10);

	while (1) {

		if (GetAsyncKeyState(VK_LBUTTON) & 0x8000) break;

		capt.read(frame);

	}

	capt.read(frame);

	Mat open = frame.clone();

	// mirror it
	cv::flip(open, open, 1);

	arrange(open);

	detectAndDisplay(open, "Eye", false);

	split(open, openedEye);

	EyeCntBase = Cnt;

	openedEyeHistR = get_histogram(openedEye[2]/*(cv::Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);
	openedEyeHistG = get_histogram(openedEye[1]/*(cv::Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);
	openedEyeHistB = get_histogram(openedEye[0]/*(cv::Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);

	image = cv::Mat(599, 799, CV_8UC3);

	putText(image, "GOZLERINIZI KAPATIN", cvPoint(100, 100),
		FONT_HERSHEY_COMPLEX_SMALL, 2.2, cvScalar(0, 0, 0), 1, CV_AA);

	imshow("Name", image);

	waitKey(10);

	while (1) {

		if (GetAsyncKeyState(VK_LBUTTON) & 0x8000) break;
		capt.read(frame);

	}

	capt.read(frame);

	Mat close = frame.clone();

	// mirror it
	cv::flip(close,close, 1);

	arrange(close);

	detectAndDisplay(close, "EyeL", false);

	split(close, closedEye);

	closedEyeHistR = get_histogram(closedEye[2]/*Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);
	closedEyeHistG = get_histogram(closedEye[1]/*(Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);
	closedEyeHistB = get_histogram(closedEye[0]/*(Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);

	cv::destroyWindow("Name");

	cv::namedWindow("EyeNow", CV_WINDOW_NORMAL);

	cv::moveWindow("EyeNow", 100, 100);
	
	/*for (int i = 0; i < 5; i++) {

		for (int j = 0; j < 4; j++) {

			printf("%d %d %d %d %d %d\n", i, j, REYE[i][j].x, REYE[i][j].y, LEYE[i][j].x, LEYE[i][j].y);

		}

	}*/


	//std::this_thread::sleep_for(std::chrono::seconds(100));
	/*cv::namedWindow("Leye", CV_WINDOW_NORMAL);
	cv::moveWindow("Leye", 200, 200);
	cv::namedWindow("Reye", CV_WINDOW_NORMAL);
	cv::moveWindow("Reye", 400, 400);*/
	/*cv::namedWindow(main_window_name, CV_WINDOW_NORMAL);
	cv::moveWindow(main_window_name, 400, 100);
	cv::namedWindow(face_window_name, CV_WINDOW_NORMAL);
	cv::moveWindow(face_window_name, 10, 100);*/

	//NO = true;

	while (true) {
		
		bool blink = 0;
		
		cv::Mat temp;

		capt.read(frame);

		frame.copyTo(temp);

		// mirror it
		cv::flip(temp,temp ,1);
		//frame.copyTo(debugImage);

		// Apply the classifier to the frame
		
		arrange(frame);

		if (!temp.empty()) {
		
			detectAndDisplay(temp,"EyeNow",true);
			
			EyeCnt = Cnt;
			
			/*if (Cnt.x - 15 < 0 || Cnt.y - 100 < 0 || Cnt.x + 15 > 640 || Cnt.y + 100 > 480) {

				Cnt = LCntBase;

			}*/

			blink = is_blink(temp/*(Rect(Cnt.x - 15, Cnt.y - 100, 30, 200))*/);
		
		}
		else {
			printf(" --(!) No captured frame -- Break!");
			break;
		}

		//imshow(main_window_name, debugImage);

		int mn = 1000000000;
		int tuti = -1,tutj=-1;

		for (int i = 0; i < 5; i++) {

			for (int j = 0; j < 4; j++) {

				if (dist(EYE[i][j], EyeCnt) < mn) {

					tuti = i;
					tutj = j;
					mn = dist(EYE[i][j], EyeCnt);

				}

			}

		}

		POINT P;

		GetCursorPos(&P);

		moveit(P, EYE[tuti][tutj]);

		//MoveMouse();		// Tekrardan ac

		if (blink) {

			totblink++;

		}

		if (!blink) totblink = 0;

		if (totblink == 10) LeftClick();

/*
		if (totblinkL == 10) {

			LeftClick();
			totblinkL = 0;

		}

		if (totblinkR == 10) {

			RightClick();
			totblinkR = 0;

		}
*/ // tekrardan ac
		printf("BLINKS-->%d\n", totblink);

		int c = cv::waitKey(10);
		if ((char)c == 'c') { break; }
		/*if ((char)c == 'f') {
			imwrite("frame.png", frame);
		}*/

	}
	

	releaseCornerKernels();

	return 0;
}

void findEye(cv::Mat frame_gray/*, cv::Rect face*/) {
	/*cv::Mat faceROI = frame_gray(face);
	cv::Mat debugFace = faceROI;*/

	if (kSmoothFaceImage) {
		double sigma = kSmoothFaceFactor * /*face.width*/frame_gray.cols;
		GaussianBlur(/*faceROI, faceROI,*/frame_gray,frame_gray, cv::Size(0, 0), sigma);
	}
	//-- Find eye regions and draw them
	/*int eye_region_width = face.width * (kEyePercentWidth / 100.0);
	int eye_region_height = face.width * (kEyePercentHeight / 100.0);
	int eye_region_top = face.height * (kEyePercentTop / 100.0);
	cv::Rect leftEyeRegion(face.width*(kEyePercentSide / 100.0),
		eye_region_top, eye_region_width, eye_region_height);
	cv::Rect rightEyeRegion(face.width - eye_region_width - face.width*(kEyePercentSide / 100.0),
		eye_region_top, eye_region_width, eye_region_height);
	*/
	//-- Find Eye Centers
	//cv::Mat leftROI = frame_gray(cv::Rect(0, 0, frame_gray.cols / 2, frame_gray.rows));
	//cv::Mat rightROI = frame_gray(cv::Rect(frame_gray.cols / 2, 0, frame_gray.cols/2, frame_gray.rows));


	cv::Point Pupil = findEyeCenter(/*faceROI, leftEyeRegion,*/ /*"Left Eye"*/frame_gray);
	//cv::Point rightPupil = findEyeCenter(/*faceROI, rightEyeRegion,*/ rightROI/*,"Right Eye"*/, main_window_name);
	// get corner regions
	/*cv::Rect leftRightCornerRegion(leftEyeRegion);
	leftRightCornerRegion.width -= leftPupil.x;
	leftRightCornerRegion.x += leftPupil.x;
	leftRightCornerRegion.height /= 2;
	leftRightCornerRegion.y += leftRightCornerRegion.height / 2;
	cv::Rect leftLeftCornerRegion(leftEyeRegion);
	leftLeftCornerRegion.width = leftPupil.x;
	leftLeftCornerRegion.height /= 2;
	leftLeftCornerRegion.y += leftLeftCornerRegion.height / 2;
	cv::Rect rightLeftCornerRegion(rightEyeRegion);
	rightLeftCornerRegion.width = rightPupil.x;
	rightLeftCornerRegion.height /= 2;
	rightLeftCornerRegion.y += rightLeftCornerRegion.height / 2;
	cv::Rect rightRightCornerRegion(rightEyeRegion);
	rightRightCornerRegion.width -= rightPupil.x;
	rightRightCornerRegion.x += rightPupil.x;
	rightRightCornerRegion.height /= 2;
	rightRightCornerRegion.y += rightRightCornerRegion.height / 2;
	rectangle(debugFace, leftRightCornerRegion, 200);
	rectangle(debugFace, leftLeftCornerRegion, 200);
	rectangle(debugFace, rightLeftCornerRegion, 200);
	rectangle(debugFace, rightRightCornerRegion, 200);*/
	// change eye centers to face coordinates
	/*rightPupil.x += rightEyeRegion.x;
	rightPupil.y += rightEyeRegion.y;
	leftPupil.x += leftEyeRegion.x;
	leftPupil.y += leftEyeRegion.y;*/
	// draw eye centers
	/*circle(debugFace, rightPupil, 3, 1234);
	circle(debugFace, leftPupil, 3, 1234);*/

	Cnt = Pupil;

	//-- Find Eye Corners
	/*if (kEnableEyeCorner) {
		cv::Point2f leftRightCorner = findEyeCorner(faceROI(leftRightCornerRegion), true, false);
		leftRightCorner.x += leftRightCornerRegion.x;
		leftRightCorner.y += leftRightCornerRegion.y;
		cv::Point2f leftLeftCorner = findEyeCorner(faceROI(leftLeftCornerRegion), true, true);
		leftLeftCorner.x += leftLeftCornerRegion.x;
		leftLeftCorner.y += leftLeftCornerRegion.y;
		cv::Point2f rightLeftCorner = findEyeCorner(faceROI(rightLeftCornerRegion), false, true);
		rightLeftCorner.x += rightLeftCornerRegion.x;
		rightLeftCorner.y += rightLeftCornerRegion.y;
		cv::Point2f rightRightCorner = findEyeCorner(faceROI(rightRightCornerRegion), false, false);
		rightRightCorner.x += rightRightCornerRegion.x;c
		rightRightCorner.y += rightRightCornerRegion.y;
		circle(faceROI, leftRightCorner, 3, 200);
		circle(faceROI, leftLeftCorner, 3, 200);
		circle(faceROI, rightLeftCorner, 3, 200);
		circle(faceROI, rightRightCorner, 3, 200);
	}*/

	//circle(frame_gray, cv::Point(rightPupil.x+10,rightPupil.y+100), 10, 1234);

	//if(NO) imshow(face_window_name, /*faceROI*/frame_gray);
	//  cv::Rect roi( cv::Point( 0, 0 ), faceROI.size());
	//  cv::Mat destinationROI = debugImage( roi );
	//  faceROI.copyTo( destinationROI );
}


cv::Mat findSkin(cv::Mat &frame) {
	cv::Mat input;
	cv::Mat output = cv::Mat(frame.rows, frame.cols, CV_8U);

	cvtColor(frame, input, CV_BGR2YCrCb);

	for (int y = 0; y < input.rows; ++y) {
		const cv::Vec3b *Mr = input.ptr<cv::Vec3b>(y);
		//    uchar *Or = output.ptr<uchar>(y);
		cv::Vec3b *Or = frame.ptr<cv::Vec3b>(y);
		for (int x = 0; x < input.cols; ++x) {
			cv::Vec3b ycrcb = Mr[x];
			//      Or[x] = (skinCrCbHist.at<uchar>(ycrcb[1], ycrcb[2]) > 0) ? 255 : 0;
			if (skinCrCbHist.at<uchar>(ycrcb[1], ycrcb[2]) == 0) {
				Or[x] = cv::Vec3b(0, 0, 0);
			}
		}
	}
	return output;
}

/**
* @function detectAndDisplay
*/
void detectAndDisplay(cv::Mat frame,std::string winName,bool ok) {
	/*std::vector<cv::Rect> faces;
	//cv::Mat frame_gray;

	std::vector<cv::Mat> rgbChannels(3);
	cv::split(frame, rgbChannels);
	cv::Mat frame_gray = rgbChannels[2];

	//cvtColor( frame, frame_gray, CV_BGR2GRAY );
	//equalizeHist( frame_gray, frame_gray );
	//cv::pow(frame_gray, CV_64F, frame_gray);
	//-- Detect faces
	face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE | CV_HAAR_FIND_BIGGEST_OBJECT, cv::Size(150, 150));
	//  findSkin(debugImage);

	for (int i = 0; i < faces.size(); i++)
	{
		rectangle(debugImage, faces[i], 1234);
	}
	//-- Show what you got
	if (faces.size() > 0) {
		findEyes(frame_gray, faces[0]);
	}*/

	cv::Mat grayframe;

	cv::cvtColor(frame, grayframe, CV_BGR2GRAY);

	//imshow(grayframe);

	findEye(grayframe);

	if (ok) {
	
		circle(grayframe, Cnt, 5, cv::Scalar(255, 255, 255));

		/*if (winName == "EyeL") {

			rectangle(grayframe, Rect(Cnt.x - 15, Cnt.y - 100, 30, 200), Scalar(255, 255, 255));
		
		}
		else {

			rectangle(grayframe, Rect(Cnt.x - 15, Cnt.y - 100, 30, 200), Scalar(255, 255, 255));

		}*/

		imshow(winName, grayframe);

		waitKey(1);

	}

}
