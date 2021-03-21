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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private Button imageAdd;

    private Mat image;
    private Stage stage;
    private List < Mat > planes;
    private Mat complexImage;

    Date dNow = new Date();
    SimpleDateFormat ft =
        new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    String date = ft.format(dNow).toString();

    void init() {
        //init çalýþtý
        this.image = new Mat();
        this.planes = new ArrayList < > ();

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
            this.updateImageView(image_unlock, mat2Image(this.image));
            this.image_unlock.setFitWidth(250);
            this.image_unlock.setPreserveRatio(true);
            Imgcodecs.imwrite("C:/Users/skaanb/Desktop/image.png", image);
            System.out.println("Resim kaydedildi");
            if (!this.planes.isEmpty()) {
                this.planes.clear();
            }

        }
    }
    @FXML
    void imageSave_btn(ActionEvent event) throws IOException {

    }
    @FXML
    void lock_button_click(ActionEvent event) {

        image = Imgcodecs.imread("C:/Users/skaanb/Desktop/image.png", Imgcodecs.IMREAD_GRAYSCALE);

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // optimize the dimension of the loaded image
        Mat padded = this.optimizeImageDim(image);
        padded.convertTo(padded, CvType.CV_32F);
        // prepare the image planes to obtain the complex image
        this.planes.add(padded);
        this.planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        // prepare a complex image for performing the dft
        Core.merge(this.planes, this.complexImage);

        // dft
        Core.dft(this.complexImage, this.complexImage);

        // optimize the image resulting from the dft operation
        Mat magnitude = this.createOptimizedMagnitude(this.complexImage);

        // show the result of the transformation as an image
        this.updateImageView(image_lock, mat2Image(magnitude));
        // set a fixed width
        this.image_lock.setFitWidth(250);
        // preserve image ratio
        this.image_lock.setPreserveRatio(true);

        // enable the button for performing the antitransformation
        // disable the button for applying the dft
        //this.transformButton.setDisable(true);

    }
    @FXML
    void unlock_button_click(ActionEvent event) {

    }

    //Functions
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
    private void updateImageView(ImageView view, Image image) {
        onFXThread(view.imageProperty(), image);
    }
    public static < T > void onFXThread(final ObjectProperty < T > property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
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
        // get the optimal rows size for dft
        int addPixelRows = Core.getOptimalDFTSize(image.rows());
        // get the optimal cols size for dft
        int addPixelCols = Core.getOptimalDFTSize(image.cols());
        // apply the optimal cols and rows size to the image
        Core.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
            Core.BORDER_CONSTANT, Scalar.all(0));

        return padded;
    }
    private Mat createOptimizedMagnitude(Mat complexImage) {
        // init
        List < Mat > newPlanes = new ArrayList < > ();
        Mat mag = new Mat();
        // split the comples image in two planes
        Core.split(complexImage, newPlanes);
        // compute the magnitude
        Core.magnitude(newPlanes.get(0), newPlanes.get(1), mag);

        // move to a logarithmic scale
        Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);
        Core.log(mag, mag);
        // optionally reorder the 4 quadrants of the magnitude image
        this.shiftDFT(mag);
        // normalize the magnitude image for the visualization since both JavaFX
        // and OpenCV need images with value between 0 and 255
        // convert back to CV_8UC1
        mag.convertTo(mag, CvType.CV_8UC1);
        Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        // you can also write on disk the resulting image...
        // Imgcodecs.imwrite("../magnitude.png", mag);

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
    private Slider imageEditing_slider3;

    @FXML
    private Slider imageEditing_slider4;

    @FXML
    private Slider imageEditing_slider5;

    @FXML
    private Slider imageEditing_slider6;

    @FXML
    private Button imageEditing_LoadButton;

    @FXML
    private Button imageEditing_SaveImageButton;

    @FXML
    void imageEditingLoad_Button_click(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            image = Imgcodecs.imread(file.getAbsolutePath());
            this.updateImageView(imageEditing_currentImage, mat2Image(this.image));
            //this.Recognition_Image.setFitWidth(250);
            this.Recognition_Image.setPreserveRatio(true);
            Imgcodecs.imwrite("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\resim1.png", image);
            imageEditing_slider1.setDisable(false);
            imageEditing_slider1.setShowTickLabels(true);
            imageEditing_slider1.setMax(100);
            imageEditing_slider1.setMin(-100);

            imageEditing_slider2.setDisable(false);

            imageEditing_slider3.setDisable(false);

            imageEditing_slider4.setDisable(false);
            imageEditing_slider4.setShowTickLabels(true);
            imageEditing_slider4.setMax(3);
            imageEditing_slider4.setMin(0);
            imageEditing_SaveImageButton.setDisable(false);

            imageEditing_slider5.setDisable(false);

            imageEditing_slider6.setDisable(false);
            System.out.println("Resim kaydedildi");
        }

    }

    @FXML
    void imageEditing_slider1_ondrag(MouseEvent event) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //Reading the Image from the file
        Mat src = this.image;
        //Creating an empty matrix
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        //Increasing the brightness of an image
        src.convertTo(dest, -1, 1, imageEditing_slider1.getValue());
        // Writing the image
        updateImageView(imageEditing_currentImage, mat2Image(dest));
        Imgcodecs.imwrite("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\sonuc.png", dest);
    }

    @FXML
    void imageEditing_slider2_ondrag(MouseEvent event) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = this.image;

        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.GaussianBlur(src, dest, new Size(0, 0), 10);
        Core.addWeighted(src, imageEditing_slider2.getValue(), dest, -0.5, 0, dest);
        updateImageView(imageEditing_currentImage, mat2Image(dest));
        Imgcodecs.imwrite("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\sonuc.png", dest);

    }
    @FXML
    void imageEditing_slider3_ondrag(MouseEvent event) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (imageEditing_slider3.getValue() % 2 == 1) {
            System.out.println("çalýþtý");
            Mat src = image;
            //Creating destination matrix
            Mat dst = new Mat(src.rows(), src.cols(), src.type());
            //Applying GaussianBlur on the Image

            Imgproc.GaussianBlur(src, dst, new Size(imageEditing_slider3.getValue(), imageEditing_slider3.getValue()), 0);
            //Converting matrix to JavaFX writable image
            Imgcodecs.imwrite("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\sonuc.png", dst);
            updateImageView(imageEditing_currentImage, mat2Image(dst));
        }

    }

    @FXML
    void imageEditing_slider4_ondrag(MouseEvent event) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = image;
        //Creating an empty matrix
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        //Increasing the contrast of the image
        src.convertTo(dest, -1, imageEditing_slider4.getValue(), 0);
        // Writing the image
        updateImageView(imageEditing_currentImage, mat2Image(dest));
        Imgcodecs.imwrite("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\sonuc.png", dest);
    }

    @FXML
    void imageEditing_slider5_ondrag(MouseEvent event) {

    }

    @FXML
    void imageEditing_slider6_ondrag(MouseEvent event) {

    }

    @FXML
    void imageEditing_SaveImageButton_click(ActionEvent event) {
        System.out.println("save button");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        System.out.println(selectedDirectory.getAbsolutePath());
        try {
            image = Imgcodecs.imread("C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\\\cache\\imagedit\\sonuc.png");
            Imgcodecs.imwrite(selectedDirectory.getAbsolutePath() + "sonuc.jpg", image);
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
            this.updateImageView(Recognition_Image, mat2Image(this.image));
            //this.Recognition_Image.setFitWidth(250);
            this.Recognition_Image.setPreserveRatio(true);
            Imgcodecs.imwrite("C:/Users/skaanb/eclipse-workspace/BitirmeProjesi2021/mnt/cache/facerecognition/img/source/1.png", image);
            System.out.println("Resim kaydedildi");
            ObservableList < String > list = Recognition_turSelection_cb.getItems();
            list.add("Ýnsan");
            list.add("Kuþ");
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

        String imgFile = "C:/Users/skaanb/eclipse-workspace/BitirmeProjesi2021/mnt/cache/facerecognition/img/source/1.png";
        Mat src = Imgcodecs.imread(imgFile);
        String xmlFile;

        xmlFile = "C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\cascade\\xml\\lbpcascade_frontalface.xml";
        Recognition_calculateButton.setDisable(false);

        if (Recognition_turSelection_cb.getValue() == "Kuþ") {
            xmlFile = "C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\cascade\\xml\\bird.xml";
            Recognition_calculateButton.setDisable(false);
        }

        if (Recognition_turSelection_cb.getValue() == "At") {
            xmlFile = "C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\cascade\\xml\\horse.xml";
            Recognition_calculateButton.setDisable(false);
        }

        if (Recognition_turSelection_cb.getValue() == "Koyun") {
            xmlFile = "C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\cascade\\xml\\sheep.xml";
            Recognition_calculateButton.setDisable(false);
        }

        if (Recognition_turSelection_cb.getValue() == "Köpek") {
            xmlFile = "C:\\Users\\skaanb\\eclipse-workspace\\BitirmeProjesi2021\\mnt\\cascade\\xml\\dog.xml";
            Recognition_calculateButton.setDisable(false);
        }

        System.out.println(Recognition_turSelection_cb.getValue());
        CascadeClassifier cc = new CascadeClassifier(xmlFile);

        MatOfRect faceDetection = new MatOfRect();
        cc.detectMultiScale(src, faceDetection);
        System.out.println(String.format("Detected faces: %d", faceDetection.toArray().length));

        for (Rect rect: faceDetection.toArray()) {
            Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
        }
        this.updateImageView(Recognition_Image, mat2Image(src));
        Imgcodecs.imwrite("C:/Users/skaanb/eclipse-workspace/BitirmeProjesi2021/mnt/cache/facerecognition/img/result/1.png", src);
        System.out.println("Image Detection Finished");
        Recognition_saveImageButton.setDisable(false);
    }

    @FXML
    void Recognition_saveImageButton_click(ActionEvent event) {

        System.out.println("save button");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        System.out.println(selectedDirectory.getAbsolutePath());
        image = Imgcodecs.imread("C:/Users/skaanb/eclipse-workspace/BitirmeProjesi2021/mnt/cache/facerecognition/img/result/1.png");
        Imgcodecs.imwrite(selectedDirectory.getAbsolutePath() + "\\image1.jpg", image);
    }

    //tab3 end
}