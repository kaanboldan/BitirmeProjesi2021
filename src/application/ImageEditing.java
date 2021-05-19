package application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageEditing {

   public String xmlChanger() {
         //insan yüzü aranır...
         return "mnt/cascade/xml/lbpcascade_frontalface.xml";
   }

   public Mat Facerecognition(String imgFile, String xmlFile) {

      Mat src = Imgcodecs.imread(imgFile);
      CascadeClassifier cc = new CascadeClassifier(xmlFile);

      MatOfRect faceDetection = new MatOfRect();
      cc.detectMultiScale(src, faceDetection);
      System.out.println(String.format("Detected faces: %d", faceDetection.toArray().length));

      for (Rect rect: faceDetection.toArray()) {
         Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
      }
      return src;
   }
   public Mat slider1(Mat src, double value) {
      Mat dest = new Mat(src.rows(), src.cols(), src.type());
      src.convertTo(dest, -1, 1, value);
      Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
      return dest;
   }
   public Mat slider2(Mat src, double value) {
      Mat dest = new Mat(src.rows(), src.cols(), src.type());
      Imgproc.GaussianBlur(src, dest, new Size(0, 0), 10);
      Core.addWeighted(src, value, dest, -0.5, 0, dest);
      Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
      return dest;
   }
   public Mat slider3(Mat src, double value) {
      if (value % 2 == 1) {
         System.out.println("calisti");
         Mat dst = new Mat(src.rows(), src.cols(), src.type());
         Imgproc.GaussianBlur(src, dst, new Size(value, value), 0);
         Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dst);
         return dst;
      }
      return null;
   }
   public Mat slider4(Mat src, double value) {
      Mat dest = new Mat(src.rows(), src.cols(), src.type());
      src.convertTo(dest, -1, value, 0);
      Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
      return dest;
   }
   public Mat showHistogramFunc(Mat frame) {
      List < Mat > images = new ArrayList < Mat > ();
      Core.split(frame, images);
      MatOfInt histSize = new MatOfInt(256);
      MatOfInt channels = new MatOfInt(0);
      MatOfFloat histRange = new MatOfFloat(0, 256);
      Mat hist_b = new Mat();
      Mat hist_g = new Mat();
      Mat hist_r = new Mat();

      Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);

      Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
      Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);

      int hist_w = 125; 
      int hist_h = 125; 
      int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);

      Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));
      Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
      Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
      Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

      for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
         Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
            new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);

         Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
            new Point(bin_w * (i), hist_h - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8,
            0);
         Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
            new Point(bin_w * (i), hist_h - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8,
            0);

      }

      return histImage;

   }
public static Mat rgbImage(Mat src,String ColorChoice) {
	
	
	List<Mat> channels = new ArrayList<Mat>(3);
	Core.split(src, channels);
	if (ColorChoice=="Blue") { 
		return channels.get(0);
	}
	if(ColorChoice=="Red")
		return channels.get(1);
	if(ColorChoice=="Green")
		return channels.get(2);
	return null;

	

}
   
   
   public static Image mat2Image(Mat frame) {
      try {
         return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
      } catch (Exception e) {
         System.err.println("Cannot convert the Mat object:");
         e.printStackTrace();

         return null;
      }
   }
   private static BufferedImage matToBufferedImage(Mat original) {
      // init
      BufferedImage image = null;
      int width = original.width(), height = original.height(), channels = original.channels();
      byte[] sourcePixels = new byte[width * height * channels];
      original.get(0, 0, sourcePixels);

      if (original.channels() > 1) {
         image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
      } else {
         image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
      }
      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

      return image;
   }
   public void updateImageView(ImageView view, Image image) {
       onFXThread(view.imageProperty(), image);
   }
   public static < T > void onFXThread(final ObjectProperty < T > property, final T value) 
   {
       Platform.runLater(() -> {
           property.set(value);
       });
   }
   public static void FileDeleter(String filename,int val,String extension) {
	  for (int i = 0; i <val ; i++) {
		  File myObj = new File(filename+i+extension);
		  myObj.delete();
	}		  
	  System.out.println("all deleted...");

   }
}