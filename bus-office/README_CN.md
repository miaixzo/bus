# 📊 Bus Office：企业办公文档处理框架

<p align="center">
<strong>高性能、功能丰富的办公文档处理框架</strong>
</p>

-----

## 📖 项目介绍

**Bus Office**是一个基于Apache POI的企业级办公文档处理框架。它提供
全面支持**Excel、Word、PDF、CSV和OFD**格式，旨在简化文档操作和
提高开发效率。

该框架封装了 POI 操作的复杂性，同时提供基于 SAX 的流等强大功能
适用于大文件、基于模板的生成和样式定制。

-----

## ✨ 核心功能

### 🎯 基本能力

* **多种格式支持**：Excel (xls/xlsx)、Word (doc/docx)、CSV、OFD、PDF
* **简单API**：流畅的设计，简单易用的常用操作方法
* **高性能**：基于SAX的大型Excel文件解析，内存占用最小
* **样式定制**：对单元格、字体、颜色和边框的丰富样式支持
* **模板支持**：编辑现有模板或从头开始创建新文档

### 📄 Excel 处理

| 功能 | 描述 |
|:---------------------|:---------------------------------------------------------------|
| **读取操作** | 支持读取为列表、映射、Bean 或自定义行处理程序 |
| **写入操作** | 小文件的标准编写器，大数据集的BigWriter |
| **SAX 解析** | Excel '03 和 '07 格式的内存高效流 |
| **Cell 样式** | 具有可自定义字体、颜色、边框的预定义样式集 |
| **数据验证** | 下拉列表、数字验证、日期约束 |

* **公式支持**：读写Excel公式
* **Merged Cells**：自动处理合并区域
* **Pictures**：将图像插入Excel文件
* **Hyperlinks**：向单元格添加超链接
* **多张工作表**：处理工作簿中的多张工作表

### 📝 文字处理

* **文档生成**：以编程方式创建DOCX文档
* **Rich Text**：添加具有自定义字体、颜色、大小和样式的文本
* **Tables**：创建带有边框和对齐方式的表格并设置其格式
* **Images**：插入具有自定义尺寸的图片
* **页眉/页脚**：添加页眉和页脚
* **段落样式**：控制对齐、缩进、间距

### 📊 CSV 处理

* **灵活读取**：从文件、InputStream或Reader读取
* **可自定义配置**：分隔符、编码、引号字符
* **写入支持**：通过适当的转义写入CSV数据
* **Streaming**：高效处理大型CSV文件

### 📋 OFD/PDF 处理

基于 OFDRW 库，提供：

* **OFD 到 PDF 转换**：将 OFD 文档转换为 PDF 格式
* **OFD 至 Images**：将 OFD 页面导出为 JPG、PNG、GIF、BMP、SVG
* **OFD 至 HTML**：将 OFD 转换为 HTML 以供网页显示
* **OFD 到 Text**：从 OFD 文档中提取纯文本
* **PDF/文本/图像到OFD**：将各种格式转换为OFD

-----

## 🚀 快速入门

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-office</artifactId>
    <version>x.x.x</version>
</dependency>
```

**Note**：该模块具有可选的依赖项：

- `poi-ooxml`（版本 5.4.1） - 用于 Excel 和 Word 处理
- `ofdrw-full`（版本 2.3.7） - 用于 OFD/PDF 处理

-----

## 📝 使用示例

### 1. Excel 读取

#### 将 Excel 读取为列表

```java
// Read all data from the first sheet as a List of Lists
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<List<Object>> rows = reader.read();

// Read from a specific sheet
ExcelReader reader = ExcelKit.getReader("data.xlsx", "Sheet2");
List<List<Object>> rows = reader.read();
```

#### 将 Excel 读取为 Map

```java
// First row as headers, subsequent rows as Map<String, Object>
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<Map<String, Object>> maps = reader.readAsMap();
```

#### 将 Excel 作为 Bean 读取

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

#### 使用 SAX 读取 Excel（内存高效）

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

### 2. Excel 写入

#### 从列表中写入 Excel

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

#### 从 Bean 列表写入 Excel

```java
// Create Excel from beans
List<User> users = getUserList();

ExcelWriter writer = ExcelKit.getWriter();
writer.write(users);
writer.flush(FileKit.file("users.xlsx"));
```

#### 使用自定义样式编写 Excel

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

#### 写入大型 Excel 文件

```java
// Use BigExcelWriter for large datasets (memory efficient)
BigExcelWriter writer = ExcelKit.getBigWriter("large_output.xlsx");

// Write row by row
for (int i = 0; i < 100000; i++) {
    writer.writeRow(Arrays.asList("Data" + i, i, "value" + i));
}

writer.close();
```

#### 编辑现有 Excel 模板

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

### 3. Word 处理

#### 创建 Word 文档

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

#### 编辑现有 Word 文档

```java
// Open existing document
XWPFDocument doc = DocxKit.create(FileKit.file("existing.docx"));

// Make modifications
Word07Writer writer = new Word07Writer(doc);
writer.addText("Additional content");

// Save
writer.flush(FileKit.file("modified.docx"));
```

### 4. CSV 处理

#### 读取 CSV

```java
// Read CSV file
CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), Charset.UTF_8);
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

CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), Charset.UTF_8, config);
```

#### 写入 CSV

```java
// Write CSV file
CsvWriter writer = CsvKit.getWriter("output.csv", Charset.UTF_8);

writer.writeRow(Arrays.asList("Name", "Age", "Email"));
writer.writeRow(Arrays.asList("Alice", "25", "alice@example.com"));
writer.writeRow(Arrays.asList("Bob", "30", "bob@example.com"));

writer.close();

// Append to existing file
CsvWriter writer = CsvKit.getWriter("output.csv", Charset.UTF_8, true);
writer.writeRow(Arrays.asList("Charlie", "35", "charlie@example.com"));
writer.close();
```

### 5. OFD/PDF 转换

```java


import java.nio.file.Paths;

// PDF to OFD
DocConverter.pdfToOfd(
        Paths.get("input.pdf"),
    Paths.

get("output.ofd")
);

// OFD to PDF
        DocConverter.

odfToPdf(
        Paths.get("input.ofd"),
    Paths.

get("output.pdf")
);

// OFD to Images (PNG)
        DocConverter.

odfToImage(
        Paths.get("input.ofd"),
    Paths.

get("output_dir"),
    "png",
            10.0  // Pixels per millimeter (quality)
            );

// OFD to HTML
            DocConverter.

odfToHtml(
        Paths.get("input.ofd"),
    Paths.

get("output.html")
);

// Images to OFD
        DocConverter.

imgToOfd(
        Paths.get("output.ofd"),
    Paths.

get("page1.png"),
    Paths.

get("page2.png"),
    Paths.

get("page3.png")
);

// Text to OFD
        DocConverter.

textToOfd(
        Paths.get("input.txt"),
    Paths.

get("output.ofd"),
    12.0  // Font size
            );
```

-----

## 📋 API 参考

### Excel API

#### ExcelKit

| 方法 | 描述 |
|:------------------------------|:-----------------------------------------|
| `getReader()` | 获取用于标准操作的 Excel 阅读器 |
| `getReader(path, sheetIndex)` | 获取特定工作表 | 的阅读器
| `getWriter()` | 获取标准 Excel 编写器 |
| `getBigWriter()` | 获取大文件的编写器 |
| `readBySax()` | 使用 SAX 读取以提高内存效率 |

#### ExcelReader

| 方法 | 描述 |
|:------------------------|:---------------------------------------|
| `read()` | 将所有数据读取为列表列表 |
| `readAsMap()` | 读取为地图列表（基于标题） |
| `readAll(Class)` | 读取为 Bean 列表 |
| `read(rowCount, Class)` | 读取指定行数作为bean |

#### ExcelWriter

| 方法 | 说明 |
|:----------------|:--------------------|
| `write(rows)` | 写入列表列表 |
| `write(bean)` | 写单个bean |
| `writeRow(row)` | 写入单行 |
| `merge(cells)` | 合并单元格区域 |
| `setStyleSet()` | 设置单元格样式 |
| `flush(file)` | 写入文件 |
| `flush(stream)` | 写入流 |

### Word API

#### DocxKit

| 方法 | 描述 |
|:--------------------|:------------------------------|
| `getWriter()` | 创建新的文字编写器 |
| `getWriter(file)` | 为目标文件 | 创建 writer
| `create(file)` | 创建或打开 XWPFDocument |
| `getType(fileName)` | 从文件名获取图像类型 |

#### Word07Writer

| 方法 | 描述 |
|:----------------------------------|:-----------------------------|
| `addText(text)` | 添加纯文本段落 |
| `addText(text, style)` | 添加样式文本 |
| `addTable(widths)` | 添加列宽为 | 的表格
| `addPicture(file, width, height)` | 插入图片 |
| `flush(file)` | 写入文件 |

### CSV API

#### CsvKit

| 方法 | 描述 |
|:----------------------------|:------------------------------|
| `getReader()` | 获取 CSV 阅读器 |
| `getWriter(path, charset)` | 获取 CSV 写入器 |
| `getReader(reader, config)` | 获取具有自定义配置的读卡器 |

### OFD API

#### DocConverter

| 方法 | 描述 |
|:----------------------------------|:----------------------|
| `pdfToOfd(src, target)` | 将 PDF 转换为 OFD |
| `odfToPdf(src, target)` | 将 OFD 转换为 PDF |
| `odfToImage(src, dir, type, ppm)` | 将 OFD 转换为图像 |
| `odfToHtml(src, target)` | 将 OFD 转换为 HTML |
| `odfToText(src, target)` | 将 OFD 转换为文本 |
| `imgToOfd(target, images)` | 将图像转换为 OFD |

-----

## 💡 最佳实践

### 1. 对大型 Excel 文件使用 SAX

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

### 2. 使用 BigExcelWriter 处理大型数据集

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

### 3. 正确关闭资源

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

### 4. 重用样式集以获得更好的性能

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

### 5. 显式指定 CSV 的编码

```java
// ✅ Recommended: Specify encoding explicitly
CsvReader reader = CsvKit.getReader(file, Charset.UTF_8);
CsvWriter writer = CsvKit.getWriter(file, Charset.UTF_8);

// ❌ Not Recommended: Rely on platform default encoding
CsvReader reader = CsvKit.getReader(file);
```

-----

## ❓ 常见问题

### Q1：如何处理带有公式的Excel单元格？

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

### Q2：Excel中如何处理日期单元格？

```java
ExcelReadConfig config = ExcelReadConfig.builder()
    .useDateFormat(true)  // Format dates
    .datePattern("yyyy-MM-dd HH:mm:ss")
    .build();

ExcelReader reader = ExcelKit.getReader("dates.xlsx", 0, config);
List<List<Object>> rows = reader.read();
```

### Q3：如何在Excel中添加数据验证？

```java
ExcelWriter writer = ExcelKit.getWriter();

// Create dropdown list
CellRangeAddressList regions = new CellRangeAddressList(1, 100, 0, 0);
DataValidationHelper dvHelper = writer.getSheet().getDataValidationHelper();
DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(new String[]{"Yes", "No"});
DataValidation validation = dvHelper.createValidation(constraint, regions);

writer.getSheet().addValidationData(validation);
```

### Q4：Excel中如何合并单元格？

```java
ExcelWriter writer = ExcelKit.getWriter();

// Merge cells A1:C1
writer.merge(0, 0, 0, 2);  // firstRow, lastRow, firstCol, lastCol

// Write merged content
writer.writeCellValue(0, 0, "Merged Header");
```

### Q5：如何处理CSV中的空单元格？

```java
CsvReadConfig config = CsvReadConfig.builder()
    .ignoreEmptyRows(false)      // Keep empty rows
    .treatEmptyFieldsAsNull(true)  // Treat empty fields as null
    .build();

CsvReader reader = CsvKit.getReader(file, charset, config);
```

### Q6：如何自定义Excel单元格边框？

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

### Q7：如何在Excel中插入超链接？

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

## 🔧 配置示例

### Excel 读取配置

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

### Excel 写入配置

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

### CSV 读取配置

```java
CsvReadConfig config = CsvReadConfig.builder()
    .delimiter(',')              // Field delimiter
    .textDelimiter('"')          // Text delimiter (quote)
    .ignoreEmptyRows(true)       // Skip empty rows
    .skipEmptySeparators(true)   // Skip empty fields
    .trim(true)                  // Trim field values
    .build();
```

### CSV 写入配置

```java
CsvWriteConfig config = CsvWriteConfig.builder()
    .delimiter(',')              // Field delimiter
    .textDelimiter('"')          // Text delimiter (quote)
    .alwaysAddTextDelimiter(true)  // Always quote fields
    .lineSeparator("\n")         // Line separator
    .build();
```

-----

## 🔄 版本兼容性

| 公交办公版本 | POI 版本 | OFDRW 版本 | JDK 版本 |
|:-------------------|:------------|:--------------|:------------|
| 8.x | 5.4.1 | 2.3.7 | 17+ |
| 7.x | 5.2.x | 2.1.x | 11+ |

-----

## 📊 性能提示

### Excel 性能

1. **使用SAX读取大文件**：减少内存使用90%+
2. **使用BigExcelWriter处理大文件**：逐行写入磁盘
3. **Batch Size**：最佳批量大小为每次刷新 1000-5000 行
4. **Style Reuse**：创建样式集一次并重复使用

### CSV 性能

1. **Buffer Size**：对大文件使用适当的缓冲区大小
2. **Encoding**：使用UTF-8进行国际字符支持
3. **Streaming**：对大型CSV文件逐行处理

### 内存优化

```java
// Configure BigExcelWriter memory window
BigExcelWriter writer = ExcelKit.getBigWriter(100);  // Keep 100 rows in memory

// Adjust for your use case
// More rows = faster but more memory
// Fewer rows = slower but less memory
```

-----

## 🛠️ 故障排除

### 问题：“NoClassDefFoundError：org/apache/poi/...”

**解决方案**：将 POI 依赖项添加到您的项目中

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.4.1</version>
</dependency>
```

### 问题：读取大型 Excel 时出现“OutOfMemoryError：Java 堆空间”

**解决方案**：使用基于SAX的读取而不是基于DOM的读取

```java
// Instead of:
List<List<Object>> rows = ExcelKit.getReader("large.xlsx").read();

// Use:
ExcelKit.readBySax("large.xlsx", 0, new RowHandler() { ... });
```

### 问题：CSV 中的中文字符显示不正确

**解决方案**：显式指定UTF-8编码

```java
CsvWriter writer = CsvKit.getWriter("file.csv", Charset.UTF_8);
```

### 问题：OFD 转换失败

**解决方案**：确保包含 OFDRW 依赖项

```xml
<dependency>
    <groupId>org.ofdrw</groupId>
    <artifactId>ofdrw-full</artifactId>
    <version>2.3.7</version>
</dependency>
```

-----

## 📞 支持和贡献

- **Issues**：[GitHub 问题](https://github.com/818000/bus/issues)
- **Contributing**：欢迎拉取请求！

-----

**Bus Office** - 让办公文档处理简单高效！
