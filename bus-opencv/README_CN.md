# 🖼️ Bus OpenCV: Java OpenCV 集成

<p align="center">
<strong>使用 OpenCV 进行计算机视觉和图像处理</strong>
</p>

-----

## 📖 项目介绍

**Bus OpenCV** 为 OpenCV (开源计算机视觉库)提供了简化的 Java 接口，使在 Java 应用程序中执行计算机视觉任务、图像处理和视频分析变得简单。

-----

## ✨ 核心特性

### 🎯 图像处理

- **图像滤波**: 模糊、锐化、边缘检测
- **几何变换**: 调整大小、旋转、变形
- **色彩空间转换**: RGB、HSV、灰度转换
- **直方图操作**: 均衡化、操作
- **形态学操作**: 腐蚀、膨胀、开运算、闭运算

### 🌍 计算机视觉

- **特征检测**: SIFT、SURF、ORB、FAST
- **目标检测**: Haar 级联、HOG、深度学习模型
- **人脸识别**: 人脸检测和识别
- **运动检测**: 光流、背景减除
- **相机标定**: 内参和外参

### 📊 视频处理

- **视频采集**: 从相机或视频文件
- **视频写入**: 创建和保存视频文件
- **帧处理**: 处理单个视频帧
- **实时处理**: 实时相机流处理

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-opencv</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 加载 OpenCV 库

```java
import org.miaixz.bus.opengl.OpenCVKit;

public class Application {
    static {
        // 加载 OpenCV 本机库
        OpenCVKit.loadLib();
    }

    public static void main(String[] args) {
        // 您的代码在这里
    }
}
```

### 基本图像操作

```java
import org.miaixz.bus.opengl.ImageProcessor;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageExample {
    public void processImage(String inputPath, String outputPath) {
        // 加载图像
        Mat image = Imgcodecs.imread(inputPath);

        // 转换为灰度
        Mat gray = ImageProcessor.toGray(image);

        // 应用高斯模糊
        Mat blurred = ImageProcessor.gaussianBlur(gray, 15);

        // 使用 Canny 检测边缘
        Mat edges = ImageProcessor.canny(blurred, 50, 150);

        // 保存结果
        Imgcodecs.imwrite(outputPath, edges);

        // 释放资源
        image.release();
        gray.release();
        blurred.release();
        edges.release();
    }
}
```

-----

## 💡 使用场景

### 人脸检测

```java
import org.miaixz.bus.opengl.FaceDetector;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class FaceDetectionExample {
    public void detectFaces(String imagePath) {
        // 加载图像
        Mat image = Imgcodecs.imread(imagePath);

        // 检测人脸
        MatOfRect faces = new MatOfRect();
        FaceDetector.detectFaces(image, faces);

        // 在人脸周围绘制矩形
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(
                image,
                new Point(rect.x, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0),
                2
            );
        }

        // 保存结果
        Imgcodecs.imwrite("faces_detected.jpg", image);
    }
}
```

### 图像滤波

```java
public class ImageFilterExample {
    public void applyFilters(String inputPath) {
        Mat image = Imgcodecs.imread(inputPath);

        // 应用各种滤波器
        Mat blurred = ImageProcessor.gaussianBlur(image, 21);
        Mat sharpened = ImageProcessor.sharpen(image);
        Mat edged = ImageProcessor.canny(image, 100, 200);

        // 显示或保存结果
        // ...
    }
}
```

### 视频处理

```java
import org.miaixz.bus.opengl.VideoCapture;
import org.miaixz.bus.opengl.VideoWriter;

public class VideoExample {
    public void processVideo(String inputPath, String outputPath) {
        // 打开视频文件
        VideoCapture capture = new VideoCapture(inputPath);

        // 获取视频属性
        double fps = capture.get(Videoio.CAP_PROP_FPS);
        int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);

        // 创建视频写入器
        VideoWriter writer = new VideoWriter(
            outputPath,
            Videoio_fourcc('m', 'p', '4', 'v'),
            fps,
            new Size(width, height)
        );

        Mat frame = new Mat();
        while (capture.read(frame)) {
            // 处理帧
            Mat processed = ImageProcessor.toGray(frame);
            Mat edges = ImageProcessor.canny(processed, 50, 150);

            // 写入帧
            writer.write(edges);
        }

        // 释放资源
        capture.release();
        writer.release();
    }
}
```

### 目标检测

```java
public class ObjectDetectionExample {
    public void detectObjects(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);

        // 使用训练好的模型检测特定对象
        List<DetectedObject> objects = ObjectDetector.detect(
            image,
            ObjectDetector.HAAR_CASCADE_FRONTALFACE
        );

        // 处理检测到的对象
        for (DetectedObject obj : objects) {
            System.out.println("检测到: " + obj.getLabel()
                + " 位置 " + obj.getBoundingBox());
        }
    }
}
```

-----

## 🔧 配置

### OpenCV 本机库

框架自动加载 OpenCV 本机库。您可以配置库路径:

```yaml
extend:
  opencv:
    library-path: /usr/local/lib
    auto-load: true
```

### 性能调优

```yaml
extend:
  opencv:
    use-gpu: false
    thread-count: 4
    buffer-size: 1024
```

-----

## 📊 性能提示

### 内存管理

```java
// 完成后始终释放 Mat 资源
Mat image = Imgcodecs.imread("image.jpg");
try {
    // 处理图像
} finally {
    image.release();
}

// 或使用 try-with-resources 模式
try (Mat image = Imgcodecs.imread("image.jpg")) {
    // 处理图像
}
```

### 并行处理

```java
// 并行处理多个图像
List<String> imagePaths = Arrays.asList("img1.jpg", "img2.jpg", "img3.jpg");
imagePaths.parallelStream().forEach(path -> {
    Mat image = Imgcodecs.imread(path);
    // 处理图像
    image.release();
});
```

-----

## 🔄 版本兼容性

| Bus OpenCV 版本 | OpenCV 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 4.x | 17+ |
| 7.x | 4.x | 11+ |

-----

## 🎯 支持的操作

### 图像操作

| 操作 | 方法 | 描述 |
|:---|:---|:---|
| 加载图像 | `Imgcodecs.imread()` | 从文件加载图像 |
| 保存图像 | `Imgcodecs.imwrite()` | 将图像保存到文件 |
| 调整大小 | `Imgproc.resize()` | 调整图像大小 |
| 旋转 | `Imgproc.rotate()` | 旋转图像 |
| 裁剪 | `Mat.submat()` | 裁剪图像区域 |
| 翻转 | `Imgproc.flip()` | 翻转图像 |

### 滤波操作

| 滤波器 | 方法 | 描述 |
|:---|:---|:---|
| 高斯模糊 | `GaussianBlur()` | 模糊图像 |
| 中值模糊 | `medianBlur()` | 中值滤波 |
| 双边滤波 | `bilateralFilter()` | 边缘保持平滑 |
| 盒式滤波 | `boxFilter()` | 盒式滤波 |

### 特征检测

| 特征 | 方法 | 描述 |
|:---|:---|:---|
| 角点 | `goodFeaturesToTrack()` | 检测角点特征 |
| 边缘 | `Canny()` | 检测边缘 |
| 轮廓 | `findContours()` | 查找轮廓 |
| 直线 | `HoughLines()` | 检测直线 |

-----

## ❓ 常见问题

### 问: 需要单独安装 OpenCV 吗？

答: 是的，您需要在系统上安装 OpenCV。此模块提供 Java 接口。

### 问: 可以使用 GPU 加速吗？

答: 如果您拥有使用 CUDA 支持编译的 OpenCV，则可以启用 GPU 操作。

### 问: 如何处理不同的图像格式？

答: OpenCV 支持常见格式(JPEG、PNG、BMP、TIFF)。使用适当的文件扩展名。

### 问: 如果出现 "UnsatisfiedLinkError" 怎么办？

答: 确保 OpenCV 本机库已正确安装并在库路径中可访问。

-----

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

-----

## 📄 许可证

[许可证信息]

-----

## 🔗 相关文档

- [OpenCV 官方文档](https://docs.opencv.org/)
- [OpenCV Java 教程](https://docs.opencv.org/4.x/d9/d52/tutorial_java_dev_intro.html)
