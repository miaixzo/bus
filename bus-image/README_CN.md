# 医学影像总线: 专业医学影像与 DICOM 处理框架

<p align="center">
<strong>企业级医学影像、DICOM 协议和医疗数据管理</strong>
</p>

-----

## 项目介绍

**Bus Image** 是一个企业级医学影像和 DICOM（医学数字成像与通信）处理框架，专为医疗应用设计。它全面支持医学影像数据处理、DICOM 协议操作、影像转码和医疗数据管理。

该框架以性能和可靠性为核心，处理医学影像工作流的复杂需求，包括 DICOM 数据解析、影像格式转换、协议操作（C-STORE、C-MOVE、C-FIND 等）以及符合医疗影像标准。

**核心能力:**
- 完整的 DICOM 标准实现（符合 DICOM PS3.10）
- 医学影像读取、写入和转码
- 支持多种模态（CT、MR、US、XA 等）
- DICOM 网络协议操作
- 高级影像处理和渲染
- 全面的元数据处理
- HL7 和医疗数据集成

-----

## 核心特性

### DICOM 数据处理

**数据结构与解析**
- 完整的 DICOM 数据模型实现（属性、标签、VR）
- 支持所有标准 DICOM 值表示
- 高效的二进制数据编码/解码
- 嵌套序列和数据集处理
- 私有标签和创建者支持

**影像格式支持**
- **原生格式**: 显式 VR 小端序、隐式 VR 小端序、显式 VR 大端序
- **压缩格式**: JPEG（基线、扩展、无损）、JPEG 2000、JPEG-LS、RLE
- **视频格式**: MPEG2、MPEG4、H.265 (HEVC)
- **特殊格式**: 封装 PDF、STL、OBJ、MTL 模型

**元数据管理**
- 3000+ 标准 DICOM 标签字典
- 私有标签解析和处理
- DICOM IOD（信息对象定义）验证
- 属性修改和去标识化
- UID 生成和管理

### 医学影像处理

**影像读写**
```java
// 读取 DICOM 影像
ImageReader reader = new ImageReader();
Attributes attrs = reader.readAttributes(file);
BufferedImage image = reader.readImage(file);

// 写入 DICOM 影像
ImageWriter writer = new ImageWriter();
writer.write(file, attrs, image);
```

**影像转码**
- 传输语法转换
- 压缩/解压缩
- 色彩空间转换（RGB、YCbCr、CIELAB）
- 像素数据封装
- 多帧影像处理

**影像增强**
- 窗宽/窗位调整（VOI LUT）
- 色彩查找表应用
- 像素填充和叠加处理
- 影像旋转和翻转
- 像素纵横比校正

**高级渲染**
- 表现 LUT（灰度、彩色）
- ICC 色彩配置文件管理
- 叠加渲染（图形、ROI）
- 遮光罩形状（矩形、圆形、多边形）
- 影像质量优化

### DICOM 网络协议

**DIMSE 操作**
```java
// C-STORE SCU - 存储 DICOM 影像
StoreSCU storeSCU = new StoreSCU();
storeSCU.setRemoteHost("dicom-server");
storeSCU.setRemotePort(104);
storeSCU.store(files);

// C-MOVE SCU - 检索影像
MoveSCU moveSCU = new MoveSCU();
moveSCU.setDestinationAET("MY_AE");
moveSCU.move(keys);

// C-FIND SCU - 查询影像
// C-GET SCU - 获取影像
// StowRS - RESTful DICOM 上传
```

**关联管理**
- 应用实体（AE）标题协商
- 表现上下文选择
- 扩展协商支持
- 异步操作
- 超时和重试处理

**数据转换**
- DICOM 到 JSON
- DICOM 到 XML
- HL7 集成
- PDF 到 DICOM
- DICOM 匿名化/去标识化

### 模态支持

支持 **18+** 种医学影像模态：

| 模态 | 描述 |
| :--- | :--- |
| **CT** | 计算机断层扫描 |
| **MR** | 磁共振成像 |
| **US** | 超声成像 |
| **XA** | X 射线血管造影 |
| **RF** | X 射线透视 |
| **DX** | 数字 X 射线摄影 |
| **MG** | 乳腺钼靶摄影 |
| **PT** | 正电子发射断层扫描 |
| **NM** | 核医学 |
| **SC** | 次级捕获 |
| **CR** | 计算机X射线摄影 |
| **IO** | 口内X射线摄影 |
| **ECG** | 心电图 |
| **EPS** | 心脏电生理 |
| **HD** | 血液动力学波形 |
| **OP** | 眼科摄影 |
| **XC** | 外部相机摄影 |
| **GM** | 普通显微镜 |

-----

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-image</artifactId>
    <version>8.5.2</version>
</dependency>
```

### 基础 DICOM 操作

#### 1. 读取 DICOM 文件

```java
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

// 读取 DICOM 文件
ImageInputStream dis = new ImageInputStream(new File("image.dcm"));
Attributes attrs = dis.readDataset();
dis.close();

// 访问患者信息
String patientName = attrs.getString(Tag.PatientName);
String studyDate = attrs.getString(Tag.StudyDate);
String modality = attrs.getString(Tag.Modality);

System.out.println("患者: " + patientName);
System.out.println("模态: " + modality);
System.out.println("检查日期: " + studyDate);
```

#### 2. 写入 DICOM 文件

```java
// 创建新的 DICOM 数据集
Attributes attrs = new Attributes();
attrs.setString(Tag.PatientName, VR.PN, "Doe^John");
attrs.setString(Tag.PatientID, VR.LO, "12345");
attrs.setString(Tag.StudyDate, VR.DA, "20250104");
attrs.setString(Tag.Modality, VR.CS, "CT");
attrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage.uid);
attrs.setString(Tag.SOPInstanceUID, VR.UI, UID.createUID());

// 写入文件
ImageOutputStream dos = new ImageOutputStream(new File("output.dcm"));
dos.writeDataset(attrs.createFileMetaInformation(UID.ExplicitVRLittleEndian.uid), attrs);
dos.close();
```

#### 3. 转换 DICOM 为影像

```java
import org.miaixz.bus.image.nimble.ImageReader;
import org.miaixz.bus.image.nimble.ImageReadParam;

// 读取 DICOM 影像
ImageReader reader = new ImageReader();
ImageReadParam param = reader.newDefaultReadParam();

BufferedImage image = reader.readImage(file, param);

// 保存为 PNG
ImageIO.write(image, "PNG", new File("output.png"));
```

#### 4. 修改 DICOM 属性

```java
// 读取现有文件
Attributes attrs = Builder.readAttributes(new File("input.dcm"));

// 修改属性
attrs.setString(Tag.PatientName, VR.PN, "Smith^Jane");
attrs.setString(Tag.PatientID, VR.LO, "67890");

// 更新 UID
attrs.setString(Tag.StudyInstanceUID, VR.UI,
    attrs.getString(Tag.StudyInstanceUID) + ".1");
attrs.setString(Tag.SeriesInstanceUID, VR.UI,
    attrs.getString(Tag.SeriesInstanceUID) + ".1");
attrs.setString(Tag.SOPInstanceUID, VR.UI,
    attrs.getString(Tag.SOPInstanceUID) + ".1");

// 保存修改后的文件
Builder.writeAttributes(new File("output.dcm"), attrs);
```

### DICOM 网络操作

#### C-STORE - 发送 DICOM 影像

```java
import org.miaixz.bus.image.plugin.StoreSCU;

// 配置 C-STORE SCU
StoreSCU main = new StoreSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");

// 可选: 设置传输语法
main.setTransferSyntax(UID.ExplicitVRLittleEndian.uid);

// 添加关联监听器
main.addAssociationListener(new AssociationListener() {
    @Override
    public void negotiate(Association as) {
        // 自定义协商
    }
});

// 存储文件
List<File> files = Arrays.asList(
    new File("image1.dcm"),
    new File("image2.dcm")
);

main.store(files);
```

#### C-FIND - 查询 DICOM 服务器

```java
import org.miaixz.bus.image.plugin.FindSCU;

// 配置 C-FIND SCU
FindSCU main = new FindSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");

// 指定查询级别（PATIENT、STUDY、SERIES、IMAGE）
main.setLevel("STUDY");

// 设置查询键
main.addMatchingKey(Tag.PatientName, "Doe*");
main.addMatchingKey(Tag.StudyDate, "20250101-20250104");
main.addReturnKey(Tag.StudyInstanceUID);
main.addReturnKey(Tag.StudyDate);
main.addReturnKey(Tag.Modality);

// 执行查询
main.open();
List<Attributes> results = main.query();
main.close();

// 处理结果
for (Attributes result : results) {
    System.out.println("检查 UID: " + result.getString(Tag.StudyInstanceUID));
    System.out.println("日期: " + result.getString(Tag.StudyDate));
}
```

#### C-MOVE - 检索影像

```java
import org.miaixz.bus.image.plugin.MoveSCU;

// 配置 C-MOVE SCU
MoveSCU main = new MoveSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");
main.setDestinationAETitle("MY_WORKSTATION");

// 设置移动键
main.addMatchingKey(Tag.StudyInstanceUID, "1.2.840.123.456");

// 执行移动
main.open();
main.move();
main.close();
```

### 影像处理操作

#### 窗位调整

```java
import org.miaixz.bus.image.nimble.RGBImageVoiLut;

// 创建 VOI LUT 处理器
RGBImageVoiLut voiLut = new RGBImageVoiLut();
voiLut.setWindowCenter(40);
voiLut.setWindowWidth(400);

// 应用于影像
BufferedImage image = reader.readImage(file);
BufferedImage adjusted = voiLut.lookup(image);
```

#### 色彩空间转换

```java
import org.miaixz.bus.image.nimble.ColorModelFactory;

// 转换 RGB 到 YCbCr
ColorSpace ycbcr = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr);
ColorConvertOp op = new ColorConvertOp(ycbcr, null);
BufferedImage ycbcrImage = op.filter(rgbImage, null);
```

#### 影像转码

```java
import org.miaixz.bus.image.nimble.Transcoder;

// 创建转码器
Transcoder transcoder = new Transcoder();

// 设置传输语法
transcoder.setTransferSyntax(UID.JPEG2000Lossless.uid);

// 转码文件
transcoder.transcode(new File("input.dcm"), new File("output.dcm"));
```

### 高级操作

#### DICOM 匿名化

```java
import org.miaixz.bus.image.plugin.Deidentify;

// 创建去标识化
Deidentify deidentify = new Deidentify();

// 添加删除标签
deidentify.remove(Tag.PatientName);
deidentify.remove(Tag.PatientID);
deidentify.remove(Tag.PatientBirthDate);
deidentify.remove(Tag.PatientAddress);

// 添加替换标签
deidentify.replace(Tag.PatientName, "ANONYMOUS^PATIENT");
deidentify.replace(Tag.InstitutionName, "TEST HOSPITAL");

// 处理文件
deidentify.process(new File("input.dcm"), new File("anonymized.dcm"));
```

#### DICOM 到 JSON

```java
import org.miaixz.bus.image.plugin.Dcm2Json;

// 转换 DICOM 为 JSON
Dcm2Json dcm2Json = new Dcm2Json();
dcm2Json.setPrettyPrint(true);
dcm2Json.convert(new File("input.dcm"), new File("output.json"));
```

#### HL7 集成

```java
import org.miaixz.bus.image.plugin.HL7Pix;

// HL7 PIX（患者标识符交叉引用）
HL7Pix hl7Pix = new HL7Pix();
hl7Pix.setHl7Server("hl7.hospital.com", 2575);
hl7Pix.setSendingApplication("RIS");
hl7Pix.setReceivingApplication("PIX_MANAGER");

// 查询患者 ID
List<String> ids = hl7Pix.query("PATIENT_ID_123");
```

-----

## API 参考

### 核心类

#### Builder
提供静态辅助方法的实用类，用于常见操作。

```java
// 属性操作
Builder.addAttributes(attrs, tags, values);
Builder.updateAttributes(data, attrs, uidSuffix);
Builder.getStringFromDicomElement(attrs, tag);

// 日期/时间操作
Builder.getDicomDate(dateString);
Builder.getDicomTime(timeString);
Builder.dateTime(attrs, dateTag, timeTag);

// 文件操作
Builder.prepareToWriteFile(file);
```

#### Format
从 DICOM 属性创建字符串的格式化器，支持模式替换。

```java
// 创建格式化器
Format format = new Format("患者: {00100010} - {00100020}");

// 应用于属性
String result = format.format(attrs);

// 支持的占位符
// {00100010} - 患者姓名
// {00100010,date} - 格式化日期
// {00100010,uid} - 生成 UID
// {00100010,hash} - 哈希值
```

#### Tag
DICOM 标签常量和工具（3000+ 预定义标签）。

```java
// 常用标签
Tag.PatientName
Tag.PatientID
Tag.StudyInstanceUID
Tag.SeriesInstanceUID
Tag.SOPInstanceUID

// 标签工具
int tag = Tag.valueOf("00100010");
String tagPath = Tag.toHexString(tag);
int[] tags = Tag.toTags(pathString);
```

#### UID
DICOM UID（唯一标识符）常量和生成。

```java
// 标准 UID
UID.ExplicitVRLittleEndian.uid
UID.ImplicitVRLittleEndian.uid
UID.JPEG2000Lossless.uid
UID.CTImageStorage.uid

// 生成 UID
String newUID = UID.createUID();
String studyUID = UID.createStudyUID();
String seriesUID = UID.createSeriesUID();
```

### 数据结构

#### Attributes
主要的 DICOM 数据集容器。

```java
// 创建数据集
Attributes attrs = new Attributes();

// 设置属性
attrs.setString(Tag.PatientName, VR.PN, "Doe^John");
attrs.setInt(Tag.NumberOfFrames, VR.IS, 10);
attrs.setDate(Tag.StudyDate, new Date());
attrs.setNull(Tag.PixelData, VR.OB);

// 获取属性
String name = attrs.getString(Tag.PatientName);
int frames = attrs.getInt(Tag.NumberOfFrames, 0);
Date date = attrs.getDate(Tag.StudyDate);

// 序列
Sequence seq = attrs.newSequence(Tag.ReferencedSeriesSequence, 1);
Attributes item = new Attributes();
item.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4");
seq.add(item);
```

#### VR (值表示)
DICOM VR 类型。

```java
// 常用 VR
VR.AE  // 应用实体
VR.CS  // 代码字符串
VR.DA  // 日期
VR.DS  // 十进制字符串
VR.DT  // 日期时间
VR.FD  // 双精度浮点
VR.FL  // 单精度浮点
VR.IS  // 整数字符串
VR.LO  // 长字符串
VR.LT  // 长文本
VR.OB  // 其他字节
VR.OF  // 其他浮点
VR.OW  // 其他字
VR.PN  // 个人姓名
VR.SH  // 短字符串
VR.ST  // 短文本
VR.TM  // 时间
VR.UI  // 唯一标识符
VR.UT  // 无限文本
```

### 影像处理

#### ImageReader
从 DICOM 文件读取医学影像。

```java
ImageReader reader = new ImageReader();
ImageReadParam param = reader.newDefaultReadParam();

// 使用 VOI LUT 读取
param.setWindowCenter(40);
param.setWindowWidth(400);

// 读取影像
BufferedImage image = reader.readImage(file, param);
```

#### ImageWriter
将医学影像写入 DICOM 文件。

```java
ImageWriter writer = new ImageWriter();
ImageWriteParam param = writer.newDefaultWriteParam();

// 设置压缩
param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
param.setCompressionType("JPEG2000");

// 写入影像
writer.write(file, attrs, image, param);
```

#### RGBImageVoiLUT
用于窗位调整的 VOI LUT（感兴趣值）处理器。

```java
RGBImageVoiLut voiLut = new RGBImageVoiLut();
voiLut.setWindowCenter(40);
voiLut.setWindowWidth(400);
voiLut.setPreferVOILUT(true);

BufferedImage result = voiLut.lookup(image);
```

### 网络协议

#### Device
DICOM 网络设备配置。

```java
Device device = new Device();
device.setDeviceName("WORKSTATION");
device.setInstalled(true);

// 配置网络
Connection conn = new Connection();
conn.setHostname("localhost");
conn.setPort(104);

device.addConnection(conn);

// 应用实体
ApplicationEntity ae = new ApplicationEntity();
ae.setAETitle("WORKSTATION");
ae.setAssociationAcceptor(true);
device.addApplicationEntity(ae);
```

#### Association
DICOM 关联管理。

```java
// 创建关联
Association as = ae.connect(remoteAE, connectInfo);

// 协商表现上下文
as.addPresentationContext(pc1, pc2, pc3);

// 执行操作
as.cstore(sopClass, sopInstance, ...);
as.cfind(...);

// 释放关联
as.release();
```

### 实用类

#### Editors
用于修改 DICOM 属性的函数式接口。

```java
Editors editor = (attrs, context) -> {
    attrs.setString(Tag.PatientName, VR.PN, "Modified^Name");
    attrs.remove(Tag.PatientID);
};

editor.apply(attrs, context);
```

#### Dimse
DICOM 消息服务元素常量。

```java
Dimse.C_STORE_RQ
Dimse.C_MOVE_RQ
Dimse.C_FIND_RQ
Dimse.C_GET_RQ
Dimse.C_ECHO_RQ
```

#### Status
DICOM 状态码。

```java
Status.Success
Status.Pending
Status.Warning
Status.Failure
```

-----

## 最佳实践

### 1. 资源管理

```java
// 使用 try-with-resources
try (ImageInputStream dis = new ImageInputStream(file)) {
    Attributes attrs = dis.readDataset();
    // 处理属性
}
```

### 2. 属性访问

```java
// 始终检查空值
String patientName = attrs.getString(Tag.PatientName, "Unknown");
int frames = attrs.getInt(Tag.NumberOfFrames, 1);
```

### 3. UID 管理

```java
// 始终使用 UID.createUID() 创建新实例
String sopInstanceUID = UID.createUID();

// 永远不要重用其他数据集的 UID
```

### 4. 错误处理

```java
try {
    Attributes attrs = Builder.readAttributes(file);
} catch (IOException e) {
    Logger.error("读取 DICOM 文件失败", e);
}
```

### 5. 性能优化

```java
// 对大文件使用缓冲 I/O
ImageInputStream dis = new ImageInputStream(
    new BufferedInputStream(new FileInputStream(file))
);

// 为压缩设置适当的 JPEG 质量
param.setCompressionQuality(0.85f);
```

### 6. 内存管理

```java
// 分批处理大数据集
List<Attributes> batch = new ArrayList<>(100);
for (Attributes attrs : allAttrs) {
    batch.add(attrs);
    if (batch.size() >= 100) {
        processBatch(batch);
        batch.clear();
    }
}
```

### 7. 线程安全

```java
// ImageReader 不是线程安全的
// 为每个线程创建单独的实例
ImageReader reader = new ImageReader();
```

-----

## 常见问题

### Q1: 如何在不同 DICOM 传输语法之间转换？

使用 `Transcoder` 类：

```java
Transcoder transcoder = new Transcoder();
transcoder.setTransferSyntax(UID.JPEG2000Lossless.uid);
transcoder.transcode(inputFile, outputFile);
```

### Q2: 如何匿名化 DICOM 数据？

使用 `Deidentify` 插件：

```java
Deidentify deidentify = new Deidentify();
deidentify.remove(Tag.PatientName);
deidentify.remove(Tag.PatientID);
deidentify.process(inputFile, outputFile);
```

### Q3: 如何处理多帧 DICOM 影像？

`ImageReader` 自动处理多帧影像：

```java
int numFrames = attrs.getInt(Tag.NumberOfFrames, 1);
for (int i = 0; i < numFrames; i++) {
    BufferedImage frame = reader.readImage(file, i);
}
```

### Q4: 如何验证 DICOM 数据？

使用 IOD（信息对象定义）验证：

```java
IOD iod = IOD.get("CT Image Storage");
ValidationResult result = attrs.validate(iod);
if (!result.isValid()) {
    System.out.println(result.asText(attrs));
}
```

### Q5: 如何使用私有 DICOM 标签？

```java
String privateCreator = "MY CREATOR";
int privateTag = Tag.createPrivateTag(privateCreator, 0x10, 0x0010);
attrs.setString(privateTag, VR.LO, "Private Data");
```

### Q6: 如何配置 DICOM 网络关联？

```java
ApplicationEntity ae = new ApplicationEntity("WORKSTATION");
ae.setAssociationAcceptor(true);

Connection conn = new Connection();
conn.setHostname("localhost");
conn.setPort(104);

ae.addConnection(conn);
```

### Q7: 如何处理 DICOM 序列？

```java
Sequence seq = attrs.getSequence(Tag.ReferencedSeriesSequence);
for (Attributes item : seq) {
    String seriesUID = item.getString(Tag.SeriesInstanceUID);
}
```

### Q8: 如何提取像素数据？

```java
// 选项 1: 使用 ImageReader（推荐）
BufferedImage image = reader.readImage(file);

// 选项 2: 访问原始像素数据
byte[] pixelData = attrs.getBytes(Tag.PixelData);
```

### Q9: 如何确定 DICOM 文件的模态？

```java
String modality = attrs.getString(Tag.Modality);
String sopClass = attrs.getString(Tag.SOPClassUID);

if (UID.CTImageStorage.uid.equals(sopClass)) {
    // CT 影像
}
```

### Q10: 如何处理 DICOM 中的字符集？

```java
// 设置特定字符集
attrs.setSpecificCharacterSet("ISO_IR 192"); // UTF-8

// 获取字符集
String charset = attrs.getSpecificCharacterSet();
```

-----

## 版本兼容性

| Bus Image 版本 | JDK 版本 | DICOM 标准 |
| :--- | :--- | :--- |
| 8.x | 17+ | DICOM PS3.10 2024a |
| 7.x | 11+ | DICOM PS3.10 2023b |

-----

## 性能考虑

### 内存使用
- 大型 DICOM 文件（>100MB）需要足够的堆空间
- 推荐: 对于典型工作负载使用 `-Xmx2g`
- 多帧影像可能需要额外内存

### I/O 性能
- 对文件操作使用缓冲流
- 对于高吞吐量场景考虑 SSD 存储
- 网络操作受益于连接池

### 压缩性能
| 格式 | 压缩比 | 速度 | 质量 |
| :--- | :--- | :--- | :--- |
| RLE | 2:1 | 非常快 | 无损 |
| JPEG 无损 | 3:1 | 快 | 无损 |
| JPEG 2000 无损 | 3:1 | 中等 | 无损 |
| JPEG 基线 | 10:1 | 快 | 有损 |
| JPEG 2000 | 20:1 | 中等 | 有损 |

-----

## 许可证

**Bus Image** 是基于 [MIT 许可证](https://github.com/818000/bus/blob/main/LICENSE)的开源软件。

-----

## 链接

- **项目主页**: https://www.miaixz.org
- **GitHub 仓库**: https://github.com/818000/bus
- **文档**: https://docs.miaixz.org
- **DICOM 标准**: https://www.dicomstandard.org
- **问题追踪**: https://github.com/818000/bus/issues

-----

## 支持

如有问题、错误报告或功能请求：
- GitHub Issues: https://github.com/818000/bus/issues
**Bus Image** - 赋能医疗影像应用
