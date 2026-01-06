# Imaging Bus: Professional Medical Imaging & DICOM Processing Framework

<p align="center">
<strong>Enterprise-Grade Medical Imaging, DICOM Protocol, and Healthcare Data Management</strong>
</p>

-----

## Project Introduction

**Bus Image** is an enterprise-level medical imaging and DICOM (Digital Imaging and Communications in Medicine) processing framework designed for healthcare applications. It provides comprehensive support for medical image data manipulation, DICOM protocol operations, image transcoding, and healthcare data management.

Built with performance and reliability in mind, Bus Image handles the complex requirements of medical imaging workflows including DICOM data parsing, image format conversions, protocol operations (C-STORE, C-MOVE, C-FIND, etc.), and compliance with healthcare imaging standards.

**Key Capabilities:**
- Full DICOM standard implementation (DICOM PS3.10 compliance)
- Medical image reading, writing, and transcoding
- Support for multiple modalities (CT, MR, US, XA, etc.)
- DICOM network protocol operations
- Advanced image processing and rendering
- Comprehensive metadata handling
- HL7 and healthcare data integration

-----

## Core Features

### DICOM Data Processing

**Data Structure & Parsing**
- Complete DICOM data model implementation (Attributes, Tags, VRs)
- Support for all standard DICOM value representations
- Efficient binary data encoding/decoding
- Nested sequence and dataset handling
- Private tag and creator support

**Image Format Support**
- **Native Formats**: Explicit VR Little Endian, Implicit VR Little Endian, Explicit VR Big Endian
- **Compressed Formats**: JPEG (baseline, extended, lossless), JPEG 2000, JPEG-LS, RLE
- **Video Formats**: MPEG2, MPEG4, H.265 (HEVC)
- **Special Formats**: Encapsulated PDF, STL, OBJ, MTL models

**Metadata Management**
- 3000+ standard DICOM tags dictionary
- Private tag resolution and handling
- DICOM IOD (Information Object Definition) validation
- Attribute modification and de-identification
- UID generation and management

### Medical Image Processing

**Image Reading & Writing**
```java
// Read DICOM image
ImageReader reader = new ImageReader();
Attributes attrs = reader.readAttributes(file);
BufferedImage image = reader.readImage(file);

// Write DICOM image
ImageWriter writer = new ImageWriter();
writer.write(file, attrs, image);
```

**Image Transcoding**
- Transfer syntax conversion
- Compression/decompression
- Color space transformations (RGB, YCbCr, CIELAB)
- Pixel data encapsulation
- Multi-frame image handling

**Image Enhancement**
- Window level/width adjustment (VOI LUT)
- Color lookup table application
- Pixel padding and overlay handling
- Image rotation and flipping
- Pixel aspect ratio correction

**Advanced Rendering**
- Presentation LUT (Grayscale, Color)
- ICC color profile management
- Overlay rendering (graphics, ROI)
- Shutter shapes (rectangular, circular, polygonal)
- Image quality optimization

### DICOM Network Protocol

**DIMSE Operations**
```java
// C-STORE SCU - Store DICOM images
StoreSCU storeSCU = new StoreSCU();
storeSCU.setRemoteHost("dicom-server");
storeSCU.setRemotePort(104);
storeSCU.store(files);

// C-MOVE SCU - Retrieve images
MoveSCU moveSCU = new MoveSCU();
moveSCU.setDestinationAET("MY_AE");
moveSCU.move(keys);

// C-FIND SCU - Query images
// C-GET SCU - Get images
// StowRS - RESTful DICOM upload
```

**Association Management**
- Application Entity (AE) title negotiation
- Presentation context selection
- Extended negotiation support
- Asynchronous operations
- Timeout and retry handling

**Data Conversion**
- DICOM to JSON
- DICOM to XML
- HL7 integration
- PDF to DICOM
- DICOM anonymization/de-identification

### Modality Support

Supports **18+** medical imaging modalities:

| Modality | Description |
| :--- | :--- |
| **CT** | Computed Tomography |
| **MR** | Magnetic Resonance |
| **US** | Ultrasound |
| **XA** | X-Ray Angiography |
| **RF** | Radiofluoroscopy |
| **DX** | Digital Radiography |
| **MG** | Mammography |
| **PT** | Positron Emission Tomography |
| **NM** | Nuclear Medicine |
| **SC** | Secondary Capture |
| **CR** | Computed Radiography |
| **IO** | Intra-oral Radiography |
| **ECG** | Electrocardiography |
| **EPS** | Cardiac Electrophysiology |
| **HD** | Hemodynamic Waveform |
| **OP** | Ophthalmic Photography |
| **XC** | External-camera Photography |
| **GM** | General Microscopy |

-----

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-image</artifactId>
    <version>8.5.2</version>
</dependency>
```

### Basic DICOM Operations

#### 1. Read DICOM File

```java
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

// Read DICOM file
ImageInputStream dis = new ImageInputStream(new File("image.dcm"));
Attributes attrs = dis.readDataset();
dis.close();

// Access patient information
String patientName = attrs.getString(Tag.PatientName);
String studyDate = attrs.getString(Tag.StudyDate);
String modality = attrs.getString(Tag.Modality);

System.out.println("Patient: " + patientName);
System.out.println("Modality: " + modality);
System.out.println("Study Date: " + studyDate);
```

#### 2. Write DICOM File

```java
// Create new DICOM dataset
Attributes attrs = new Attributes();
attrs.setString(Tag.PatientName, VR.PN, "Doe^John");
attrs.setString(Tag.PatientID, VR.LO, "12345");
attrs.setString(Tag.StudyDate, VR.DA, "20250104");
attrs.setString(Tag.Modality, VR.CS, "CT");
attrs.setString(Tag.SOPClassUID, VR.UI, UID.CTImageStorage.uid);
attrs.setString(Tag.SOPInstanceUID, VR.UI, UID.createUID());

// Write to file
ImageOutputStream dos = new ImageOutputStream(new File("output.dcm"));
dos.writeDataset(attrs.createFileMetaInformation(UID.ExplicitVRLittleEndian.uid), attrs);
dos.close();
```

#### 3. Convert DICOM to Image

```java
import org.miaixz.bus.image.nimble.ImageReader;
import org.miaixz.bus.image.nimble.ImageReadParam;

// Read DICOM image
ImageReader reader = new ImageReader();
ImageReadParam param = reader.newDefaultReadParam();

BufferedImage image = reader.readImage(file, param);

// Save as PNG
ImageIO.write(image, "PNG", new File("output.png"));
```

#### 4. Modify DICOM Attributes

```java
// Read existing file
Attributes attrs = Builder.readAttributes(new File("input.dcm"));

// Modify attributes
attrs.setString(Tag.PatientName, VR.PN, "Smith^Jane");
attrs.setString(Tag.PatientID, VR.LO, "67890");

// Update UIDs
attrs.setString(Tag.StudyInstanceUID, VR.UI,
    attrs.getString(Tag.StudyInstanceUID) + ".1");
attrs.setString(Tag.SeriesInstanceUID, VR.UI,
    attrs.getString(Tag.SeriesInstanceUID) + ".1");
attrs.setString(Tag.SOPInstanceUID, VR.UI,
    attrs.getString(Tag.SOPInstanceUID) + ".1");

// Save modified file
Builder.writeAttributes(new File("output.dcm"), attrs);
```

### DICOM Network Operations

#### C-STORE - Send DICOM Images

```java
import org.miaixz.bus.image.plugin.StoreSCU;

// Configure C-STORE SCU
StoreSCU main = new StoreSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");

// Optional: Set transfer syntax
main.setTransferSyntax(UID.ExplicitVRLittleEndian.uid);

// Add association listener
main.addAssociationListener(new AssociationListener() {
    @Override
    public void negotiate(Association as) {
        // Customize negotiation
    }
});

// Store files
List<File> files = Arrays.asList(
    new File("image1.dcm"),
    new File("image2.dcm")
);

main.store(files);
```

#### C-FIND - Query DICOM Server

```java
import org.miaixz.bus.image.plugin.FindSCU;

// Configure C-FIND SCU
FindSCU main = new FindSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");

// Specify query level (PATIENT, STUDY, SERIES, IMAGE)
main.setLevel("STUDY");

// Set query keys
main.addMatchingKey(Tag.PatientName, "Doe*");
main.addMatchingKey(Tag.StudyDate, "20250101-20250104");
main.addReturnKey(Tag.StudyInstanceUID);
main.addReturnKey(Tag.StudyDate);
main.addReturnKey(Tag.Modality);

// Execute query
main.open();
List<Attributes> results = main.query();
main.close();

// Process results
for (Attributes result : results) {
    System.out.println("Study UID: " + result.getString(Tag.StudyInstanceUID));
    System.out.println("Date: " + result.getString(Tag.StudyDate));
}
```

#### C-MOVE - Retrieve Images

```java
import org.miaixz.bus.image.plugin.MoveSCU;

// Configure C-MOVE SCU
MoveSCU main = new MoveSCU();
main.setRemoteHost("pacs.hospital.com");
main.setRemotePort(104);
main.setCallingAETitle("WORKSTATION");
main.setCalledAETitle("PACS_SERVER");
main.setDestinationAETitle("MY_WORKSTATION");

// Set move keys
main.addMatchingKey(Tag.StudyInstanceUID, "1.2.840.123.456");

// Execute move
main.open();
main.move();
main.close();
```

### Image Processing Operations

#### Window Level Adjustment

```java
import org.miaixz.bus.image.nimble.RGBImageVoiLut;

// Create VOI LUT processor
RGBImageVoiLut voiLut = new RGBImageVoiLut();
voiLut.setWindowCenter(40);
voiLut.setWindowWidth(400);

// Apply to image
BufferedImage image = reader.readImage(file);
BufferedImage adjusted = voiLut.lookup(image);
```

#### Color Space Conversion

```java
import org.miaixz.bus.image.nimble.ColorModelFactory;

// Convert RGB to YCbCr
ColorSpace ycbcr = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr);
ColorConvertOp op = new ColorConvertOp(ycbcr, null);
BufferedImage ycbcrImage = op.filter(rgbImage, null);
```

#### Image Transcoding

```java
import org.miaixz.bus.image.nimble.Transcoder;

// Create transcoder
Transcoder transcoder = new Transcoder();

// Set transfer syntax
transcoder.setTransferSyntax(UID.JPEG2000Lossless.uid);

// Transcode file
transcoder.transcode(new File("input.dcm"), new File("output.dcm"));
```

### Advanced Operations

#### DICOM Anonymization

```java
import org.miaixz.bus.image.plugin.Deidentify;

// Create de-identification
Deidentify deidentify = new Deidentify();

// Add removal tags
deidentify.remove(Tag.PatientName);
deidentify.remove(Tag.PatientID);
deidentify.remove(Tag.PatientBirthDate);
deidentify.remove(Tag.PatientAddress);

// Add replacement tags
deidentify.replace(Tag.PatientName, "ANONYMOUS^PATIENT");
deidentify.replace(Tag.InstitutionName, "TEST HOSPITAL");

// Process file
deidentify.process(new File("input.dcm"), new File("anonymized.dcm"));
```

#### DICOM to JSON

```java
import org.miaixz.bus.image.plugin.Dcm2Json;

// Convert DICOM to JSON
Dcm2Json dcm2Json = new Dcm2Json();
dcm2Json.setPrettyPrint(true);
dcm2Json.convert(new File("input.dcm"), new File("output.json"));
```

#### HL7 Integration

```java
import org.miaixz.bus.image.plugin.HL7Pix;

// HL7 PIX (Patient Identifier Cross-Referencing)
HL7Pix hl7Pix = new HL7Pix();
hl7Pix.setHl7Server("hl7.hospital.com", 2575);
hl7Pix.setSendingApplication("RIS");
hl7Pix.setReceivingApplication("PIX_MANAGER");

// Query patient IDs
List<String> ids = hl7Pix.query("PATIENT_ID_123");
```

-----

## API Reference

### Core Classes

#### Builder
Utility class providing static helper methods for common operations.

```java
// Attribute operations
Builder.addAttributes(attrs, tags, values);
Builder.updateAttributes(data, attrs, uidSuffix);
Builder.getStringFromDicomElement(attrs, tag);

// Date/time operations
Builder.getDicomDate(dateString);
Builder.getDicomTime(timeString);
Builder.dateTime(attrs, dateTag, timeTag);

// File operations
Builder.prepareToWriteFile(file);
```

#### Format
Formatter for creating strings from DICOM attributes with pattern substitution.

```java
// Create formatter
Format format = new Format("Patient: {00100010} - {00100020}");

// Apply to attributes
String result = format.format(attrs);

// Supported placeholders
// {00100010} - Patient Name
// {00100010,date} - Formatted date
// {00100010,uid} - Generate UID
// {00100010,hash} - Hash value
```

#### Tag
DICOM tag constants and utilities (3000+ predefined tags).

```java
// Common tags
Tag.PatientName
Tag.PatientID
Tag.StudyInstanceUID
Tag.SeriesInstanceUID
Tag.SOPInstanceUID

// Tag utilities
int tag = Tag.valueOf("00100010");
String tagPath = Tag.toHexString(tag);
int[] tags = Tag.toTags(pathString);
```

#### UID
DICOM UID (Unique Identifier) constants and generation.

```java
// Standard UIDs
UID.ExplicitVRLittleEndian.uid
UID.ImplicitVRLittleEndian.uid
UID.JPEG2000Lossless.uid
UID.CTImageStorage.uid

// Generate UIDs
String newUID = UID.createUID();
String studyUID = UID.createStudyUID();
String seriesUID = UID.createSeriesUID();
```

### Data Structures

#### Attributes
Primary DICOM dataset container.

```java
// Create dataset
Attributes attrs = new Attributes();

// Set attributes
attrs.setString(Tag.PatientName, VR.PN, "Doe^John");
attrs.setInt(Tag.NumberOfFrames, VR.IS, 10);
attrs.setDate(Tag.StudyDate, new Date());
attrs.setNull(Tag.PixelData, VR.OB);

// Get attributes
String name = attrs.getString(Tag.PatientName);
int frames = attrs.getInt(Tag.NumberOfFrames, 0);
Date date = attrs.getDate(Tag.StudyDate);

// Sequences
Sequence seq = attrs.newSequence(Tag.ReferencedSeriesSequence, 1);
Attributes item = new Attributes();
item.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4");
seq.add(item);
```

#### VR (Value Representation)
DICOM VR types.

```java
// Common VRs
VR.AE  // Application Entity
VR.CS  // Code String
VR.DA  // Date
VR.DS  // Decimal String
VR.DT  // Date Time
VR.FD  // Floating Point Double
VR.FL  // Floating Point Single
VR.IS  // Integer String
VR.LO  // Long String
VR.LT  // Long Text
VR.OB  // Other Byte
VR.OF  // Other Float
VR.OW  // Other Word
VR.PN  // Person Name
VR.SH  // Short String
VR.ST  // Short Text
VR.TM  // Time
VR.UI  // Unique Identifier
VR.UT  // Unlimited Text
```

### Image Processing

#### ImageReader
Reads medical images from DICOM files.

```java
ImageReader reader = new ImageReader();
ImageReadParam param = reader.newDefaultReadParam();

// Read with VOI LUT
param.setWindowCenter(40);
param.setWindowWidth(400);

// Read image
BufferedImage image = reader.readImage(file, param);
```

#### ImageWriter
Writes medical images to DICOM files.

```java
ImageWriter writer = new ImageWriter();
ImageWriteParam param = writer.newDefaultWriteParam();

// Set compression
param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
param.setCompressionType("JPEG2000");

// Write image
writer.write(file, attrs, image, param);
```

#### RGBImageVoiLUT
VOI LUT (Value of Interest) processor for window level adjustment.

```java
RGBImageVoiLut voiLut = new RGBImageVoiLut();
voiLut.setWindowCenter(40);
voiLut.setWindowWidth(400);
voiLut.setPreferVOILUT(true);

BufferedImage result = voiLut.lookup(image);
```

### Network Protocol

#### Device
DICOM network device configuration.

```java
Device device = new Device();
device.setDeviceName("WORKSTATION");
device.setInstalled(true);

// Configure network
Connection conn = new Connection();
conn.setHostname("localhost");
conn.setPort(104);

device.addConnection(conn);

// Application Entity
ApplicationEntity ae = new ApplicationEntity();
ae.setAETitle("WORKSTATION");
ae.setAssociationAcceptor(true);
device.addApplicationEntity(ae);
```

#### Association
DICOM association management.

```java
// Create association
Association as = ae.connect(remoteAE, connectInfo);

// Negotiate presentation contexts
as.addPresentationContext(pc1, pc2, pc3);

// Perform operations
as.cstore(sopClass, sopInstance, ...);
as.cfind(...);

// Release association
as.release();
```

### Utility Classes

#### Editors
Functional interface for modifying DICOM attributes.

```java
Editors editor = (attrs, context) -> {
    attrs.setString(Tag.PatientName, VR.PN, "Modified^Name");
    attrs.remove(Tag.PatientID);
};

editor.apply(attrs, context);
```

#### Dimse
DICOM Message Service Element constants.

```java
Dimse.C_STORE_RQ
Dimse.C_MOVE_RQ
Dimse.C_FIND_RQ
Dimse.C_GET_RQ
Dimse.C_ECHO_RQ
```

#### Status
DICOM status codes.

```java
Status.Success
Status.Pending
Status.Warning
Status.Failure
```

-----

## Best Practices

### 1. Resource Management

```java
// Use try-with-resources
try (ImageInputStream dis = new ImageInputStream(file)) {
    Attributes attrs = dis.readDataset();
    // Process attributes
}
```

### 2. Attribute Access

```java
// Always check for null values
String patientName = attrs.getString(Tag.PatientName, "Unknown");
int frames = attrs.getInt(Tag.NumberOfFrames, 1);
```

### 3. UID Management

```java
// Always use UID.createUID() for new instances
String sopInstanceUID = UID.createUID();

// Never reuse UIDs from other datasets
```

### 4. Error Handling

```java
try {
    Attributes attrs = Builder.readAttributes(file);
} catch (IOException e) {
    Logger.error("Failed to read DICOM file", e);
}
```

### 5. Performance Optimization

```java
// Use buffered I/O for large files
ImageInputStream dis = new ImageInputStream(
    new BufferedInputStream(new FileInputStream(file))
);

// Set appropriate JPEG quality for compression
param.setCompressionQuality(0.85f);
```

### 6. Memory Management

```java
// Process large datasets in chunks
List<Attributes> batch = new ArrayList<>(100);
for (Attributes attrs : allAttrs) {
    batch.add(attrs);
    if (batch.size() >= 100) {
        processBatch(batch);
        batch.clear();
    }
}
```

### 7. Thread Safety

```java
// ImageReader is not thread-safe
// Create separate instance per thread
ImageReader reader = new ImageReader();
```

-----

## FAQ

### Q1: How do I convert between different DICOM transfer syntaxes?

Use the `Transcoder` class:

```java
Transcoder transcoder = new Transcoder();
transcoder.setTransferSyntax(UID.JPEG2000Lossless.uid);
transcoder.transcode(inputFile, outputFile);
```

### Q2: How do I anonymize DICOM data?

Use the `Deidentify` plugin:

```java
Deidentify deidentify = new Deidentify();
deidentify.remove(Tag.PatientName);
deidentify.remove(Tag.PatientID);
deidentify.process(inputFile, outputFile);
```

### Q3: How do I handle multi-frame DICOM images?

The `ImageReader` automatically handles multi-frame images:

```java
int numFrames = attrs.getInt(Tag.NumberOfFrames, 1);
for (int i = 0; i < numFrames; i++) {
    BufferedImage frame = reader.readImage(file, i);
}
```

### Q4: How do I validate DICOM data?

Use IOD (Information Object Definition) validation:

```java
IOD iod = IOD.get("CT Image Storage");
ValidationResult result = attrs.validate(iod);
if (!result.isValid()) {
    System.out.println(result.asText(attrs));
}
```

### Q5: How do I work with private DICOM tags?

```java
String privateCreator = "MY CREATOR";
int privateTag = Tag.createPrivateTag(privateCreator, 0x10, 0x0010);
attrs.setString(privateTag, VR.LO, "Private Data");
```

### Q6: How do I configure DICOM network associations?

```java
ApplicationEntity ae = new ApplicationEntity("WORKSTATION");
ae.setAssociationAcceptor(true);

Connection conn = new Connection();
conn.setHostname("localhost");
conn.setPort(104);

ae.addConnection(conn);
```

### Q7: How do I handle DICOM sequences?

```java
Sequence seq = attrs.getSequence(Tag.ReferencedSeriesSequence);
for (Attributes item : seq) {
    String seriesUID = item.getString(Tag.SeriesInstanceUID);
}
```

### Q8: How do I extract pixel data?

```java
// Option 1: Use ImageReader (recommended)
BufferedImage image = reader.readImage(file);

// Option 2: Access raw pixel data
byte[] pixelData = attrs.getBytes(Tag.PixelData);
```

### Q9: How do I determine the modality of a DICOM file?

```java
String modality = attrs.getString(Tag.Modality);
String sopClass = attrs.getString(Tag.SOPClassUID);

if (UID.CTImageStorage.uid.equals(sopClass)) {
    // CT image
}
```

### Q10: How do I handle character sets in DICOM?

```java
// Set specific character set
attrs.setSpecificCharacterSet("ISO_IR 192"); // UTF-8

// Get character set
String charset = attrs.getSpecificCharacterSet();
```

-----

## Version Compatibility

| Bus Image Version | JDK Version | DICOM Standard |
| :--- | :--- | :--- |
| 8.x | 17+ | DICOM PS3.10 2024a |
| 7.x | 11+ | DICOM PS3.10 2023b |

-----

## Performance Considerations

### Memory Usage
- Large DICOM files (>100MB) require adequate heap space
- Recommended: `-Xmx2g` for typical workloads
- Multi-frame images may require additional memory

### I/O Performance
- Use buffered streams for file operations
- Consider SSD storage for high-throughput scenarios
- Network operations benefit from connection pooling

### Compression Performance
| Format | Compression Ratio | Speed | Quality |
| :--- | :--- | :--- | :--- |
| RLE | 2:1 | Very Fast | Lossless |
| JPEG Lossless | 3:1 | Fast | Lossless |
| JPEG 2000 Lossless | 3:1 | Medium | Lossless |
| JPEG Baseline | 10:1 | Fast | Lossy |
| JPEG 2000 | 20:1 | Medium | Lossy |

-----

## License

**Bus Image** is open-source software licensed under the [MIT License](https://github.com/818000/bus/blob/main/LICENSE).

-----

## Links

- **Project Homepage**: https://www.miaixz.org
- **GitHub Repository**: https://github.com/818000/bus
- **Documentation**: https://docs.miaixz.org
- **DICOM Standard**: https://www.dicomstandard.org
- **Issue Tracking**: https://github.com/818000/bus/issues

-----

## Support

For questions, bug reports, or feature requests:
- GitHub Issues: https://github.com/818000/bus/issues
**Bus Image** - Empowering Healthcare Imaging Applications
