# 📊 Bus Office: Enterprise Office Document Processing Framework

<p align="center">
<strong>High-Performance, Feature-Rich Office Document Processing Framework</strong>
</p>

-----

## 📖 Project Introduction

**Bus Office** is an enterprise-level office document processing framework based on Apache POI. It provides comprehensive support for **Excel, Word, PDF, CSV, and OFD** formats, designed to simplify document operations and improve development efficiency.

The framework encapsulates the complexity of POI operations while providing powerful features like SAX-based streaming for large files, template-based generation, and style customization.

-----

## ✨ Core Features

### 🎯 Basic Capabilities

* **Multiple Format Support**: Excel (xls/xlsx), Word (doc/docx), CSV, OFD, PDF
* **Simple API**: Fluent design with easy-to-use methods for common operations
* **High Performance**: SAX-based parsing for large Excel files with minimal memory footprint
* **Style Customization**: Rich styling support for cells, fonts, colors, and borders
* **Template Support**: Edit existing templates or create new documents from scratch

### 📄 Excel Processing

| Feature | Description |
| :--- | :--- |
| **Read Operations** | Support for reading as List, Map, Bean, or custom row handlers |
| **Write Operations** | Standard writer for small files, BigWriter for large datasets |
| **SAX Parsing** | Memory-efficient streaming for Excel '03 and '07 formats |
| **Cell Styles** | Predefined style sets with customizable fonts, colors, borders |
| **Data Validation** | Dropdown lists, number validation, date constraints |
* **Formula Support**: Read and write Excel formulas
* **Merged Cells**: Handle merged regions automatically
* **Pictures**: Insert images into Excel files
* **Hyperlinks**: Add hyperlinks to cells
* **Multiple Sheets**: Work with multiple sheets in a workbook

### 📝 Word Processing

* **Document Generation**: Create DOCX documents programmatically
* **Rich Text**: Add text with custom fonts, colors, sizes, and styles
* **Tables**: Create and format tables with borders and alignment
* **Images**: Insert pictures with custom sizing
* **Headers/Footers**: Add page headers and footers
* **Paragraph Styles**: Control alignment, indentation, spacing

### 📊 CSV Processing

* **Flexible Reading**: Read from File, InputStream, or Reader
* **Customizable Configuration**: Delimiter, encoding, quote character
* **Write Support**: Write CSV data with proper escaping
* **Streaming**: Process large CSV files efficiently

### 📋 OFD/PDF Processing

Based on the OFDRW library, providing:
* **OFD to PDF Conversion**: Convert OFD documents to PDF format
* **OFD to Images**: Export OFD pages as JPG, PNG, GIF, BMP, SVG
* **OFD to HTML**: Convert OFD to HTML for web display
* **OFD to Text**: Extract plain text from OFD documents
* **PDF/Text/Image to OFD**: Convert various formats to OFD

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-office</artifactId>
    <version>x.x.x</version>
</dependency>
```

**Note**: This module has optional dependencies on:
- `poi-ooxml` (version 5.4.1) - for Excel and Word processing
- `ofdrw-full` (version 2.3.7) - for OFD/PDF processing

-----

## 📝 Usage Examples

### 1. Excel Reading

#### Read Excel as List

```java
// Read all data from the first sheet as a List of Lists
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<List<Object>> rows = reader.read();

// Read from a specific sheet
ExcelReader reader = ExcelKit.getReader("data.xlsx", "Sheet2");
List<List<Object>> rows = reader.read();
```

#### Read Excel as Map

```java
// First row as headers, subsequent rows as Map<String, Object>
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<Map<String, Object>> maps = reader.readAsMap();
```

#### Read Excel as Bean

```java
// Map Excel rows to Java beans
ExcelReader reader = ExcelKit.getReader("users.xlsx");
List<User> users = reader.readAll(User.class);

// Custom header row index
ExcelReadConfig config = ExcelReadConfig.builder()
    .headerRowIndex(0)  // Header row
    .startRowIndex(1)   // Data start row
    .build();
List<User> users = reader.readAll(User.class, config);
```

#### Read Excel with SAX (Memory Efficient)

```java
// Process large Excel files using SAX (low memory footprint)
ExcelKit.readBySax("large_file.xlsx", 0, new RowHandler() {
    @Override
    public void handle(int sheetIndex, int rowIndex, List<Object> row) {
        // Process each row
        System.out.println("Row " + rowIndex + ": " + row);
    }
});

// Read as beans using SAX
ExcelKit.readBySax("large_file.xlsx", "sheet1", new BeanRowHandler<User>(User.class) {
    @Override
    public void handle(int sheetIndex, int rowIndex, User row) {
        // Process each bean
        userService.save(row);
    }
});
```

### 2. Excel Writing

#### Write Excel from List

```java
// Create Excel from List
List<List<Object>> rows = new ArrayList<>();
rows.add(Arrays.asList("Name", "Age", "Email"));
rows.add(Arrays.asList("Alice", 25, "alice@example.com"));
rows.add(Arrays.asList("Bob", 30, "bob@example.com"));

ExcelWriter writer = ExcelKit.getWriter();
writer.write(rows);
writer.flush(FileKit.file("output.xlsx"));
```

#### Write Excel from Bean List

```java
// Create Excel from beans
List<User> users = getUserList();

ExcelWriter writer = ExcelKit.getWriter();
writer.write(users);
writer.flush(FileKit.file("users.xlsx"));
```

#### Write Excel with Custom Styles

```java
ExcelWriter writer = ExcelKit.getWriter();

// Set header style
StyleSet headerStyle = writer.getStyleSet();
headerStyle.setBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
headerStyle.setBold(FontBoldWeight.BOLD);
headerStyle.setFontHeightInPoints((short) 12);

// Write data
writer.writeRow(Arrays.asList("Name", "Age", "Email"));
writer.setStyleSet(null);  // Reset to default style for data
writer.write(users);

writer.flush(FileKit.file("styled_users.xlsx"));
```

#### Write Large Excel Files

```java
// Use BigExcelWriter for large datasets (memory efficient)
BigExcelWriter writer = ExcelKit.getBigWriter("large_output.xlsx");

// Write row by row
for (int i = 0; i < 100000; i++) {
    writer.writeRow(Arrays.asList("Data" + i, i, "value" + i));
}

writer.close();
```

#### Edit Existing Excel Template

```java
// Load template and edit
ExcelWriter writer = ExcelKit.getWriter("template.xlsx");

// Write data to specific sheet
writer.setCurrentSheet("Sheet1");
writer.write(users);

// Add another sheet
writer.setSheet("Summary");
writer.writeRow(Arrays.asList("Total Users", users.size()));

writer.flush(FileKit.file("report.xlsx"));
```

### 3. Word Processing

#### Create Word Document

```java
// Create a new Word document
Word07Writer writer = DocxKit.getWriter();

// Add a paragraph
writer.addText("Hello, World!", FontStyle.builder()
    .fontSize(16)
    .bold(true)
    .color("000000")
    .build());

// Add a table
DocxTable table = writer.addTable(new int[]{3000, 3000, 3000});
table.addRow(Arrays.asList("Name", "Age", "Email"));
table.addRow(Arrays.asList("Alice", "25", "alice@example.com"));

// Add an image
writer.addPicture(FileKit.file("logo.png"), 100, 100);

// Save to file
writer.flush(FileKit.file("document.docx"));
```

#### Edit Existing Word Document

```java
// Open existing document
XWPFDocument doc = DocxKit.create(FileKit.file("existing.docx"));

// Make modifications
Word07Writer writer = new Word07Writer(doc);
writer.addText("Additional content");

// Save
writer.flush(FileKit.file("modified.docx"));
```

### 4. CSV Processing

#### Read CSV

```java
// Read CSV file
CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), StandardCharsets.UTF_8);
CsvRow row;
while ((row = reader.read()) != null) {
    List<String> fields = row.getRawList();
    // Process row
}
reader.close();

// Read with custom configuration
CsvReadConfig config = CsvReadConfig.builder()
    .delimiter('\t')      // Tab delimiter
    .textDelimiter('"')   // Quote character
    .ignoreEmptyRows(true)
    .build();

CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), StandardCharsets.UTF_8, config);
```

#### Write CSV

```java
// Write CSV file
CsvWriter writer = CsvKit.getWriter("output.csv", StandardCharsets.UTF_8);

writer.writeRow(Arrays.asList("Name", "Age", "Email"));
writer.writeRow(Arrays.asList("Alice", "25", "alice@example.com"));
writer.writeRow(Arrays.asList("Bob", "30", "bob@example.com"));

writer.close();

// Append to existing file
CsvWriter writer = CsvKit.getWriter("output.csv", StandardCharsets.UTF_8, true);
writer.writeRow(Arrays.asList("Charlie", "35", "charlie@example.com"));
writer.close();
```

### 5. OFD/PDF Conversion

```java
import org.miaixz.bus.office.ofd.DocConverter;
import java.nio.file.Paths;

// PDF to OFD
DocConverter.pdfToOfd(
    Paths.get("input.pdf"),
    Paths.get("output.ofd")
);

// OFD to PDF
DocConverter.odfToPdf(
    Paths.get("input.ofd"),
    Paths.get("output.pdf")
);

// OFD to Images (PNG)
DocConverter.odfToImage(
    Paths.get("input.ofd"),
    Paths.get("output_dir"),
    "png",
    10.0  // Pixels per millimeter (quality)
);

// OFD to HTML
DocConverter.odfToHtml(
    Paths.get("input.ofd"),
    Paths.get("output.html")
);

// Images to OFD
DocConverter.imgToOfd(
    Paths.get("output.ofd"),
    Paths.get("page1.png"),
    Paths.get("page2.png"),
    Paths.get("page3.png")
);

// Text to OFD
DocConverter.textToOfd(
    Paths.get("input.txt"),
    Paths.get("output.ofd"),
    12.0  // Font size
);
```

-----

## 📋 API Reference

### Excel API

#### ExcelKit

| Method | Description |
| :--- | :--- |
| `getReader()` | Get Excel reader for standard operations |
| `getReader(path, sheetIndex)` | Get reader for specific sheet |
| `getWriter()` | Get standard Excel writer |
| `getBigWriter()` | Get writer for large files |
| `readBySax()` | Read using SAX for memory efficiency |

#### ExcelReader

| Method | Description |
| :--- | :--- |
| `read()` | Read all data as List of Lists |
| `readAsMap()` | Read as List of Maps (header-based) |
| `readAll(Class)` | Read as List of Beans |
| `read(rowCount, Class)` | Read specified number of rows as beans |

#### ExcelWriter

| Method | Description |
| :--- | :--- |
| `write(rows)` | Write List of Lists |
| `write(bean)` | Write single bean |
| `writeRow(row)` | Write a single row |
| `merge(cells)` | Merge cell regions |
| `setStyleSet()` | Set cell styles |
| `flush(file)` | Write to file |
| `flush(stream)` | Write to stream |

### Word API

#### DocxKit

| Method | Description |
| :--- | :--- |
| `getWriter()` | Create new Word writer |
| `getWriter(file)` | Create writer for target file |
| `create(file)` | Create or open XWPFDocument |
| `getType(fileName)` | Get image type from file name |

#### Word07Writer

| Method | Description |
| :--- | :--- |
| `addText(text)` | Add plain text paragraph |
| `addText(text, style)` | Add styled text |
| `addTable(widths)` | Add table with column widths |
| `addPicture(file, width, height)` | Insert image |
| `flush(file)` | Write to file |

### CSV API

#### CsvKit

| Method | Description |
| :--- | :--- |
| `getReader()` | Get CSV reader |
| `getWriter(path, charset)` | Get CSV writer |
| `getReader(reader, config)` | Get reader with custom config |

### OFD API

#### DocConverter

| Method | Description |
| :--- | :--- |
| `pdfToOfd(src, target)` | Convert PDF to OFD |
| `odfToPdf(src, target)` | Convert OFD to PDF |
| `odfToImage(src, dir, type, ppm)` | Convert OFD to images |
| `odfToHtml(src, target)` | Convert OFD to HTML |
| `odfToText(src, target)` | Convert OFD to text |
| `imgToOfd(target, images)` | Convert images to OFD |

-----

## 💡 Best Practices

### 1. Use SAX for Large Excel Files

```java
// ✅ Recommended: Use SAX for large files (low memory)
ExcelKit.readBySax("large_file.xlsx", 0, new RowHandler() {
    @Override
    public void handle(int sheetIndex, int rowIndex, List<Object> row) {
        // Process row by row
    }
});

// ❌ Not Recommended: Load entire file into memory
List<List<Object>> rows = ExcelKit.getReader("large_file.xlsx").read();
```

### 2. Use BigExcelWriter for Large Datasets

```java
// ✅ Recommended: Use BigExcelWriter for writing large files
BigExcelWriter writer = ExcelKit.getBigWriter("large_output.xlsx");
for (int i = 0; i < 100000; i++) {
    writer.writeRow(getRowData(i));
}
writer.close();

// ❌ Not Recommended: Standard writer may cause memory issues
ExcelWriter writer = ExcelKit.getWriter();
List<List<Object>> allData = getAllData();  // Potential OOM
writer.write(allData);
```

### 3. Close Resources Properly

```java
// ✅ Recommended: Use try-with-resources
try (CsvReader reader = CsvKit.getReader(file, charset)) {
    CsvRow row;
    while ((row = reader.read()) != null) {
        // Process row
    }
}

// ❌ Not Recommended: Manual resource management
CsvReader reader = CsvKit.getReader(file, charset);
// Process...
reader.close();  // May not execute if exception occurs
```

### 4. Reuse Style Sets for Better Performance

```java
// ✅ Recommended: Create and reuse style sets
StyleSet headerStyle = new StyleSet(workbook);
headerStyle.setBold(true);
headerStyle.setFontColor(IndexedColors.RED.getIndex());

for (List<Object> row : rows) {
    writer.setStyleSet(headerStyle);
    writer.writeRow(row);
}

// ❌ Not Recommended: Create new style for each row
for (List<Object> row : rows) {
    StyleSet style = new StyleSet(workbook);  // Inefficient
    writer.setStyleSet(style);
    writer.writeRow(row);
}
```

### 5. Specify Encoding Explicitly for CSV

```java
// ✅ Recommended: Specify encoding explicitly
CsvReader reader = CsvKit.getReader(file, StandardCharsets.UTF_8);
CsvWriter writer = CsvKit.getWriter(file, StandardCharsets.UTF_8);

// ❌ Not Recommended: Rely on platform default encoding
CsvReader reader = CsvKit.getReader(file);
```

-----

## ❓ Frequently Asked Questions

### Q1: How to handle Excel cells with formulas?

```java
ExcelReader reader = ExcelKit.getReader("formulas.xlsx");

// Read formula as string
String formula = CellKit.getCellValueAsString(cell);

// Read evaluated formula result
Object value = CellKit.getCellValue(cell);

// Write formulas
ExcelWriter writer = ExcelKit.getWriter();
writer.writeRow(Arrays.asList("=SUM(A1:A10)"));
```

### Q2: How to handle date cells in Excel?

```java
ExcelReadConfig config = ExcelReadConfig.builder()
    .useDateFormat(true)  // Format dates
    .datePattern("yyyy-MM-dd HH:mm:ss")
    .build();

ExcelReader reader = ExcelKit.getReader("dates.xlsx", 0, config);
List<List<Object>> rows = reader.read();
```

### Q3: How to add data validation to Excel?

```java
ExcelWriter writer = ExcelKit.getWriter();

// Create dropdown list
CellRangeAddressList regions = new CellRangeAddressList(1, 100, 0, 0);
DataValidationHelper dvHelper = writer.getSheet().getDataValidationHelper();
DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(new String[]{"Yes", "No"});
DataValidation validation = dvHelper.createValidation(constraint, regions);

writer.getSheet().addValidationData(validation);
```

### Q4: How to merge cells in Excel?

```java
ExcelWriter writer = ExcelKit.getWriter();

// Merge cells A1:C1
writer.merge(0, 0, 0, 2);  // firstRow, lastRow, firstCol, lastCol

// Write merged content
writer.writeCellValue(0, 0, "Merged Header");
```

### Q5: How to handle empty cells in CSV?

```java
CsvReadConfig config = CsvReadConfig.builder()
    .ignoreEmptyRows(false)      // Keep empty rows
    .treatEmptyFieldsAsNull(true)  // Treat empty fields as null
    .build();

CsvReader reader = CsvKit.getReader(file, charset, config);
```

### Q6: How to customize Excel cell borders?

```java
StyleSet styleSet = new StyleSet(workbook);

// Set border styles
styleSet.setBorderBottom(LineStyle.THIN);
styleSet.setBorderTop(LineStyle.THIN);
styleSet.setBorderLeft(LineStyle.THIN);
styleSet.setBorderRight(LineStyle.THIN);
styleSet.setBorderColor(IndexedColors.BLACK.getIndex());

writer.setStyleSet(styleSet);
writer.writeRow(data);
```

### Q7: How to insert hyperlinks in Excel?

```java
ExcelWriter writer = ExcelKit.getWriter();

// Add hyperlink to cell
Cell cell = writer.getSheet().getRow(0).getCell(0);
CreationHelper createHelper = writer.getWorkbook().getCreationHelper();
Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
link.setAddress("https://www.example.com");
cell.setHyperlink(link);
cell.setCellValue("Click here");
```

-----

## 🔧 Configuration Examples

### Excel Read Configuration

```java
ExcelReadConfig config = ExcelReadConfig.builder()
    .headerRowIndex(0)           // Header row index
    .startRowIndex(1)            // Data start row
    .endRowIndex(100)            // Data end row (optional)
    .ignoreEmptyRow(true)        // Skip empty rows
    .useDateFormat(true)         // Format dates
    .datePattern("yyyy-MM-dd")   // Date format pattern
    .cellEditor(editor)          // Custom cell editor
    .build();

ExcelReader reader = ExcelKit.getReader("data.xlsx", 0, config);
```

### Excel Write Configuration

```java
ExcelWriteConfig config = ExcelWriteConfig.builder()
    .headerRowIndex(0)           // Header row index
    .startRowIndex(1)            // Data start row
    .ignoreEmptyRow(true)        // Skip empty rows
    .styleSet(styleSet)          // Default style set
    .build();

ExcelWriter writer = ExcelKit.getWriter();
writer.setConfig(config);
```

### CSV Read Configuration

```java
CsvReadConfig config = CsvReadConfig.builder()
    .delimiter(',')              // Field delimiter
    .textDelimiter('"')          // Text delimiter (quote)
    .ignoreEmptyRows(true)       // Skip empty rows
    .skipEmptySeparators(true)   // Skip empty fields
    .trim(true)                  // Trim field values
    .build();
```

### CSV Write Configuration

```java
CsvWriteConfig config = CsvWriteConfig.builder()
    .delimiter(',')              // Field delimiter
    .textDelimiter('"')          // Text delimiter (quote)
    .alwaysAddTextDelimiter(true)  // Always quote fields
    .lineSeparator("\n")         // Line separator
    .build();
```

-----

## 🔄 Version Compatibility

| Bus Office Version | POI Version | OFDRW Version | JDK Version |
| :--- | :--- | :--- | :--- |
| 8.x | 5.4.1 | 2.3.7 | 17+ |
| 7.x | 5.2.x | 2.1.x | 11+ |

-----

## 📊 Performance Tips

### Excel Performance

1. **Use SAX for Reading Large Files**: Reduces memory usage by 90%+
2. **Use BigExcelWriter for Large Files**: Writes row-by-row to disk
3. **Batch Size**: Optimal batch size is 1000-5000 rows per flush
4. **Style Reuse**: Create style sets once and reuse them

### CSV Performance

1. **Buffer Size**: Use appropriate buffer size for large files
2. **Encoding**: Use UTF-8 for international character support
3. **Streaming**: Process row-by-row for large CSV files

### Memory Optimization

```java
// Configure BigExcelWriter memory window
BigExcelWriter writer = ExcelKit.getBigWriter(100);  // Keep 100 rows in memory

// Adjust for your use case
// More rows = faster but more memory
// Fewer rows = slower but less memory
```

-----

## 🛠️ Troubleshooting

### Issue: "NoClassDefFoundError: org/apache/poi/..."

**Solution**: Add POI dependencies to your project

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.4.1</version>
</dependency>
```

### Issue: "OutOfMemoryError: Java heap space" when reading large Excel

**Solution**: Use SAX-based reading instead of DOM-based

```java
// Instead of:
List<List<Object>> rows = ExcelKit.getReader("large.xlsx").read();

// Use:
ExcelKit.readBySax("large.xlsx", 0, new RowHandler() { ... });
```

### Issue: Chinese characters display incorrectly in CSV

**Solution**: Specify UTF-8 encoding explicitly

```java
CsvWriter writer = CsvKit.getWriter("file.csv", StandardCharsets.UTF_8);
```

### Issue: OFD conversion fails

**Solution**: Ensure OFDRW dependency is included

```xml
<dependency>
    <groupId>org.ofdrw</groupId>
    <artifactId>ofdrw-full</artifactId>
    <version>2.3.7</version>
</dependency>
```

-----

## 📞 Support and Contributing

- **Issues**: [GitHub Issues](https://github.com/818000/bus/issues)
- **Contributing**: Pull requests are welcome!

-----

**Bus Office** - Making office document processing simple and efficient!
