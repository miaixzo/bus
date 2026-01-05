# 🖼️ Bus OpenCV: OpenCV Integration for Java

<p align="center">
<strong>Computer Vision and Image Processing with OpenCV</strong>
</p>

-----

## 📖 Project Introduction

**Bus OpenCV** provides a simplified Java interface to OpenCV (Open Source Computer Vision Library), making it easy to perform computer vision tasks, image processing, and video analysis in your Java applications.

-----

## ✨ Core Features

### 🎯 Image Processing

- **Image Filtering**: Blur, sharpen, edge detection
- **Geometric Transformations**: Resize, rotate, warp
- **Color Space Conversion**: RGB, HSV, grayscale conversions
- **Histogram Operations**: Equalization, manipulation
- **Morphological Operations**: Erosion, dilation, opening, closing

### 🌍 Computer Vision

- **Feature Detection**: SIFT, SURF, ORB, FAST
- **Object Detection**: Haar cascades, HOG, deep learning models
- **Face Recognition**: Face detection and recognition
- **Motion Detection**: Optical flow, background subtraction
- **Camera Calibration**: Intrinsic and extrinsic parameters

### 📊 Video Processing

- **Video Capture**: From camera or video files
- **Video Writing**: Create and save video files
- **Frame Processing**: Process individual video frames
- **Real-time Processing**: Live camera stream processing

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-opencv</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Load OpenCV Library

```java
import org.miaixz.bus.opengl.OpenCVKit;

public class Application {
    static {
        // Load OpenCV native library
        OpenCVKit.loadLib();
    }

    public static void main(String[] args) {
        // Your code here
    }
}
```

### Basic Image Operations

```java
import org.miaixz.bus.opengl.ImageProcessor;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageExample {
    public void processImage(String inputPath, String outputPath) {
        // Load image
        Mat image = Imgcodecs.imread(inputPath);

        // Convert to grayscale
        Mat gray = ImageProcessor.toGray(image);

        // Apply Gaussian blur
        Mat blurred = ImageProcessor.gaussianBlur(gray, 15);

        // Detect edges using Canny
        Mat edges = ImageProcessor.canny(blurred, 50, 150);

        // Save result
        Imgcodecs.imwrite(outputPath, edges);

        // Release resources
        image.release();
        gray.release();
        blurred.release();
        edges.release();
    }
}
```

-----

## 💡 Use Cases

### Face Detection

```java
import org.miaixz.bus.opengl.FaceDetector;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class FaceDetectionExample {
    public void detectFaces(String imagePath) {
        // Load image
        Mat image = Imgcodecs.imread(imagePath);

        // Detect faces
        MatOfRect faces = new MatOfRect();
        FaceDetector.detectFaces(image, faces);

        // Draw rectangles around faces
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(
                image,
                new Point(rect.x, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0),
                2
            );
        }

        // Save result
        Imgcodecs.imwrite("faces_detected.jpg", image);
    }
}
```

### Image Filtering

```java
public class ImageFilterExample {
    public void applyFilters(String inputPath) {
        Mat image = Imgcodecs.imread(inputPath);

        // Apply various filters
        Mat blurred = ImageProcessor.gaussianBlur(image, 21);
        Mat sharpened = ImageProcessor.sharpen(image);
        Mat edged = ImageProcessor.canny(image, 100, 200);

        // Display or save results
        // ...
    }
}
```

### Video Processing

```java
import org.miaixz.bus.opengl.VideoCapture;
import org.miaixz.bus.opengl.VideoWriter;

public class VideoExample {
    public void processVideo(String inputPath, String outputPath) {
        // Open video file
        VideoCapture capture = new VideoCapture(inputPath);

        // Get video properties
        double fps = capture.get(Videoio.CAP_PROP_FPS);
        int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);

        // Create video writer
        VideoWriter writer = new VideoWriter(
            outputPath,
            Videoio_fourcc('m', 'p', '4', 'v'),
            fps,
            new Size(width, height)
        );

        Mat frame = new Mat();
        while (capture.read(frame)) {
            // Process frame
            Mat processed = ImageProcessor.toGray(frame);
            Mat edges = ImageProcessor.canny(processed, 50, 150);

            // Write frame
            writer.write(edges);
        }

        // Release resources
        capture.release();
        writer.release();
    }
}
```

### Object Detection

```java
public class ObjectDetectionExample {
    public void detectObjects(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);

        // Detect specific objects using trained models
        List<DetectedObject> objects = ObjectDetector.detect(
            image,
            ObjectDetector.HAAR_CASCADE_FRONTALFACE
        );

        // Process detected objects
        for (DetectedObject obj : objects) {
            System.out.println("Detected: " + obj.getLabel()
                + " at " + obj.getBoundingBox());
        }
    }
}
```

-----

## 🔧 Configuration

### OpenCV Native Library

The framework automatically loads OpenCV native libraries. You can configure the library path:

```yaml
extend:
  opencv:
    library-path: /usr/local/lib
    auto-load: true
```

### Performance Tuning

```yaml
extend:
  opencv:
    use-gpu: false
    thread-count: 4
    buffer-size: 1024
```

-----

## 📊 Performance Tips

### Memory Management

```java
// Always release Mat resources when done
Mat image = Imgcodecs.imread("image.jpg");
try {
    // Process image
} finally {
    image.release();
}

// Or use try-with-resources pattern
try (Mat image = Imgcodecs.imread("image.jpg")) {
    // Process image
}
```

### Parallel Processing

```java
// Process multiple images in parallel
List<String> imagePaths = Arrays.asList("img1.jpg", "img2.jpg", "img3.jpg");
imagePaths.parallelStream().forEach(path -> {
    Mat image = Imgcodecs.imread(path);
    // Process image
    image.release();
});
```

-----

## 🔄 Version Compatibility

| Bus OpenCV Version | OpenCV Version | JDK Version |
|:---|:---|:---|
| 8.x | 4.x | 17+ |
| 7.x | 4.x | 11+ |

-----

## 🎯 Supported Operations

### Image Operations

| Operation | Method | Description |
|:---|:---|:---|
| Load Image | `Imgcodecs.imread()` | Load image from file |
| Save Image | `Imgcodecs.imwrite()` | Save image to file |
| Resize | `Imgproc.resize()` | Resize image |
| Rotate | `Imgproc.rotate()` | Rotate image |
| Crop | `Mat.submat()` | Crop image region |
| Flip | `Imgproc.flip()` | Flip image |

### Filter Operations

| Filter | Method | Description |
|:---|:---|:---|
| Gaussian Blur | `GaussianBlur()` | Blur image |
| Median Blur | `medianBlur()` | Median filtering |
| Bilateral Filter | `bilateralFilter()` | Edge-preserving smoothing |
| Box Filter | `boxFilter()` | Box filtering |

### Feature Detection

| Feature | Method | Description |
|:---|:---|:---|
| Corners | `goodFeaturesToTrack()` | Detect corner features |
| Edges | `Canny()` | Detect edges |
| Contours | `findContours()` | Find contours |
| Lines | `HoughLines()` | Detect lines |

-----

## ❓ FAQ

### Q: Do I need to install OpenCV separately?

A: Yes, you need to install OpenCV on your system. This module provides the Java interface.

### Q: Can I use GPU acceleration?

A: Yes, if you have OpenCV compiled with CUDA support, you can enable GPU operations.

### Q: How do I handle different image formats?

A: OpenCV supports common formats (JPEG, PNG, BMP, TIFF). Use appropriate file extensions.

### Q: What if I get "UnsatisfiedLinkError"?

A: Make sure OpenCV native library is properly installed and accessible in your library path.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[License information]

-----

## 🔗 Related Documentation

- [OpenCV Official Documentation](https://docs.opencv.org/)
- [OpenCV Java Tutorial](https://docs.opencv.org/4.x/d9/d52/tutorial_java_dev_intro.html)
