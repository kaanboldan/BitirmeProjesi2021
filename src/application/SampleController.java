package application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SampleController {

  //Tab1
  @FXML
  private Button image_save;

  @FXML
  private Button lock_button;

  @FXML
  private Button unlock_button;

  @FXML
  private ImageView image_unlock;

  @FXML
  private ImageView image_lock;

  @FXML
  private ImageView image_unlocked;

  @FXML
  private Button imageAdd;

  private Mat image;
  private Stage stage;
  private List < Mat > planes;
  private Mat complexImage;
  public Mat magnitude;
  public Mat restoredImage;
  public int int_random;


  ImageEditing imageEditing = new ImageEditing();

  Date dNow = new Date();
  SimpleDateFormat ft =
    new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
  String date = ft.format(dNow).toString();


  @FXML
  void initialize() {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    this.complexImage = new Mat();
    this.image = new Mat();
    this.planes = new ArrayList < > ();
    imageEditing_slider1.setShowTickLabels(true);
    imageEditing_slider1.setMax(100);
    imageEditing_slider1.setMin(-100);
    imageEditing_slider4.setShowTickLabels(true);
    imageEditing_slider4.setMax(3);
    imageEditing_slider4.setMin(0);
    System.out.println("init calisti");

  }

  //Buttons
  @FXML
  void imageAdd_btn(ActionEvent event) throws IOException {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Resource File");
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      this.image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
      this.imageEditing.updateImageView(image_unlock, mat2Image(this.image));
      this.image_unlock.setFitWidth(250);
      this.image_unlock.setPreserveRatio(true);
      Imgcodecs.imwrite("mnt/cache/image0.png", image);
      System.out.println("Resim kaydedildi");
      if (!this.planes.isEmpty()) {
        this.planes.clear();
      }

    }
    lock_button.setDisable(false);
  }

  @FXML
  void lock_button_click(ActionEvent event) {
	  ImageEditing.FileDeleter("mnt/locking/Lockimage", this.int_random,".png");
	  ImageEditing.FileDeleter("mnt/locking/Lockimage_final", this.int_random,".png");
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Random rand = new Random(); 
    int upperbound = 25;
    this.int_random = rand.nextInt(upperbound); 
    int ls = 1;
    for (int l = 0; l <= this.int_random; l++) {
      System.out.print(l + ".kitlenme adım |");
      System.out.print(ls + ".kitlenme durum |");

      this.planes = new ArrayList < > ();
      if(l==0)     
    	  image = Imgcodecs.imread("mnt/cache/image0.png", Imgcodecs.IMREAD_GRAYSCALE);
      else
    	  image = Imgcodecs.imread("mnt/locking/Lockimage" + l + ".png", Imgcodecs.IMREAD_GRAYSCALE);
      Mat padded = this.optimizeImageDim(image);
      padded.convertTo(padded, CvType.CV_32F);

      this.planes.add(padded);
      this.planes.add(Mat.zeros(padded.size(), CvType.CV_32F));

      Core.merge(this.planes, image);
      Core.dft(image, image);
      this.magnitude = this.createOptimizedMagnitude(image);
      Imgcodecs.imwrite("mnt/locking/Lockimage" + ls + ".png", this.magnitude);
      System.out.println(l + ".kitlenme adım sonu");
      ++ls;

    }

    imageEditing.updateImageView(image_lock, mat2Image(this.magnitude));
    this.image_lock.setFitWidth(250);
    this.image_lock.setPreserveRatio(true);
    Imgcodecs.imwrite("mnt/locking/Lockimage_final.png", this.magnitude);
    unlock_button.setDisable(false);
  }

  @FXML
  void unlock_button_click(ActionEvent event) {
	  ImageEditing.FileDeleter("mnt/locking/Unlockimage", this.int_random,".png");
	  ImageEditing.FileDeleter("mnt/locking/imageUnlockFinal", 1,".png");
	  
	  int us = this.int_random;
	  System.out.println("us: "+us);
	  
	  for (int u = this.int_random ; u >= 0; u--,us--) {
		 
      System.out.print(u + ".açma adım |");
      System.out.print(us + ".açma durum |");

      this.planes = new ArrayList < > ();
      
      Mat imageLock = Imgcodecs.imread("mnt/locking/Lockimage" + us + ".png");
      Core.idft(imageLock, imageLock);

      Mat restoredImage = new Mat();
      Core.split(imageLock, this.planes);
      Core.normalize(this.planes.get(0), restoredImage, 0, 255, Core.NORM_MINMAX);

      this.restoredImage.convertTo(this.restoredImage, CvType.CV_8U);
      Imgcodecs.imwrite("mnt/unlocking/Unlockimage" + us + ".png", this.restoredImage);
      System.out.println(u + ".açma adım sonu");
      
    }
    Imgcodecs.imwrite("mnt/unlocking/imageUnlockFinal.png", this.restoredImage);
    imageEditing.updateImageView(image_unlocked, mat2Image(restoredImage));

    this.image_unlocked.setFitWidth(250);

    this.image_unlocked.setPreserveRatio(true);

  }
  @FXML
  void imageSave_btn(ActionEvent event) throws IOException {

  }

  void FileCopy(String source, String destination) throws IOException {
    Path a, b;
    a = Paths.get(source);
    b = Paths.get(destination);

    Files.copy(a, b, StandardCopyOption.REPLACE_EXISTING);
  }
  public static Image mat2Image(Mat frame) {
    try {
      return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
    } catch (Exception e) {
      // show the exception details
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
  private Mat optimizeImageDim(Mat image) {
    // init
    Mat padded = new Mat();

    int addPixelRows = Core.getOptimalDFTSize(image.rows());

    int addPixelCols = Core.getOptimalDFTSize(image.cols());

    Core.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
      Core.BORDER_CONSTANT, Scalar.all(0));

    return padded;
  }
  private Mat createOptimizedMagnitude(Mat complexImage) {
    // init
    List < Mat > newPlanes = new ArrayList < > ();
    Mat mag = new Mat();

    Core.split(complexImage, newPlanes);

    Core.magnitude(newPlanes.get(0), newPlanes.get(1), mag);

    Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);
    Core.log(mag, mag);
    this.shiftDFT(mag);
    mag.convertTo(mag, CvType.CV_8UC1);
    Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);


    return mag;
  }
  private void shiftDFT(Mat image) {
    image = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
    int cx = image.cols() / 2;
    int cy = image.rows() / 2;

    Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
    Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
    Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
    Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

    Mat tmp = new Mat();
    q0.copyTo(tmp);
    q3.copyTo(q0);
    tmp.copyTo(q3);

    q1.copyTo(tmp);
    q2.copyTo(q1);
    tmp.copyTo(q2);
  }
  public void setStage(Stage stage) {
    this.stage = stage;
  }

  //tab1 end
  //tab2 start

  @FXML
  private ImageView imageEditing_currentImage;
  @FXML
  private Slider imageEditing_slider1;

  @FXML
  private Slider imageEditing_slider2;

  @FXML
  private CheckBox imageEditing_blur;

  @FXML
  private CheckBox imageEditing_B;

  @FXML
  private CheckBox imageEditing_R;

  @FXML
  private CheckBox imageEditing_G;

  @FXML
  private Slider imageEditing_slider4;

  @FXML
  private Button imageEditing_LoadButton;

  @FXML
  private Button imageEditing_SaveImageButton;

  @FXML
  private ImageView imageEditing_histogram;

  @FXML
  void imageEditingLoad_Button_click(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Resource File");
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      image = Imgcodecs.imread(file.getAbsolutePath());
      this.imageEditing.updateImageView(imageEditing_currentImage, mat2Image(this.image));

      this.Recognition_Image.setPreserveRatio(true);
      Imgcodecs.imwrite("/mnt/cache/resim2.png", image);
      imageEditing_slider1.setDisable(false);
      imageEditing_slider2.setDisable(false);
      imageEditing_slider4.setDisable(false);
      imageEditing_R.setDisable(false);
      imageEditing_G.setDisable(false);
      imageEditing_B.setDisable(false);

      this.showHistogram(image);

      System.out.println("Resim kaydedildi");
    }

  }

  @FXML
  void imageEditing_slider1_ondrag(MouseEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat dest = imageEditing.slider1(this.image, imageEditing_slider1.getValue());
    imageEditing.updateImageView(imageEditing_currentImage, mat2Image(dest));
    showHistogram(dest);
  }

  @FXML
  void imageEditing_slider2_ondrag(MouseEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat dest = imageEditing.slider2(image, imageEditing_slider2.getValue());
    imageEditing.updateImageView(imageEditing_currentImage, mat2Image(dest));
    showHistogram(dest);
  }

  @FXML
  void imageEditing_R_checked(ActionEvent event) {
    if (imageEditing_R.isSelected() == true)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(imageEditing.rgbImage(image, "Red")));
    if (imageEditing_R.isSelected() == false)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(image));
  }

  @FXML
  void imageEditing_G_checked(ActionEvent event) {
    if (imageEditing_G.isSelected() == true)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(imageEditing.rgbImage(image, "Green")));
    if (imageEditing_G.isSelected() == false)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(image));
  }

  @FXML
  void imageEditing_B_checked(ActionEvent event) {
    if (imageEditing_B.isSelected() == true)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(imageEditing.rgbImage(image, "Blue")));
    if (imageEditing_B.isSelected() == false)
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(image));
  }

  @FXML
  void imageEditing_blur_checked(ActionEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    System.out.println("calisti");
    Mat src = image;
    Mat dest = new Mat(src.rows(), src.cols(), src.type());
    if (imageEditing_blur.isSelected() == true) {
      Imgproc.GaussianBlur(src, dest, new Size(25, 25), 0);
      Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(dest));

    } else {
      Imgproc.GaussianBlur(src, dest, new Size(25, 25), 0);
      Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
      imageEditing.updateImageView(imageEditing_currentImage, mat2Image(dest));
    }
    showHistogram(dest);
  }

  @FXML
  void imageEditing_slider4_ondrag(MouseEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    Mat dest = imageEditing.slider4(image, imageEditing_slider4.getValue());
    imageEditing.updateImageView(imageEditing_currentImage, mat2Image(dest));
    Imgcodecs.imwrite("mnt/cache/imageEditSonuc.png", dest);
  }
  private void showHistogram(Mat frame) {
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

    int hist_w = 150;
    int hist_h = 150;
    int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);

    Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));

    Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
    Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
    Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

    for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
      Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
        new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);
      Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
        new Point(bin_w * (i), hist_h - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8, 0);
      Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
        new Point(bin_w * (i), hist_h - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8, 0);
    }

    Image histImg = mat2Image(histImage);
    imageEditing.updateImageView(imageEditing_histogram, histImg);

  }

  @FXML
  void imageEditing_SaveImageButton_click(ActionEvent event) {
    System.out.println("save button");
    DirectoryChooser directoryChooser = new DirectoryChooser();
    File selectedDirectory = directoryChooser.showDialog(stage);

    System.out.println(selectedDirectory.getAbsolutePath());
    try {
      image = Imgcodecs.imread("mnt/cache/imagEditSonuc.png");
      Imgcodecs.imwrite(selectedDirectory.getAbsolutePath() + "sonuc.jpg", image);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //tab2 end

  //tab3 start
  @FXML
  private Button Recognition_calculateButton;

  @FXML
  private Button Recognition_loadImageButton;

  @FXML
  private ImageView Recognition_Image;

  @FXML
  private ChoiceBox < String > Recognition_turSelection_cb;

  @FXML
  private Button Recognition_saveImageButton;

  @FXML
  void Recognition_loadImage_button_click(ActionEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Resource File");
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      this.image = Imgcodecs.imread(file.getAbsolutePath());
      this.imageEditing.updateImageView(Recognition_Image, mat2Image(this.image));
      //this.Recognition_Image.setFitWidth(250);
      this.Recognition_Image.setPreserveRatio(true);
      Imgcodecs.imwrite("mnt/cache/3.png", image);
      System.out.println("Resim kaydedildi");
      ObservableList < String > list = Recognition_turSelection_cb.getItems();
      list.add("Insan");
      list.add("Kus");
      list.add("At");
      list.add("Koyun");
      list.add("Köpek");
      Recognition_calculateButton.setDisable(false);
      Recognition_saveImageButton.setDisable(true);
    }
  }

  @FXML
  void Recognition_calculate_button_click(ActionEvent event) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    String imgFile = "mnt/cache/3.png";
    Mat src = imageEditing.Facerecognition(imgFile, imageEditing.xmlChanger(Recognition_turSelection_cb.getValue()));
    this.imageEditing.updateImageView(Recognition_Image, mat2Image(src));
    Imgcodecs.imwrite("mnt/cache/3.png", src);
    System.out.println("Image Detection Finished");
    Recognition_saveImageButton.setDisable(false);
  }

  @FXML
  void Recognition_saveImageButton_click(ActionEvent event) {

    System.out.println("save button");
    DirectoryChooser directoryChooser = new DirectoryChooser();
    File selectedDirectory = directoryChooser.showDialog(stage);
    System.out.println(selectedDirectory.getAbsolutePath() + "/image1");
    image = Imgcodecs.imread("mnt/cache/3.png");
    Imgcodecs.imwrite(selectedDirectory.getAbsolutePath() + "/image1.jpg", image);
  }

  //tab3 end
}