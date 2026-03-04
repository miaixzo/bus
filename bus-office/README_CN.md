# 📊 Bus Office: 企业办公文档处理框架

<p align="center">
<strong>高性能、功能丰富的办公文档处理框架</strong>
</p>

-----

## 📖 项目介绍

**Bus Office** 是基于 Apache POI 的企业级办公文档处理框架。它提供对 **Excel、Word、PDF、CSV 和 OFD** 格式的全面支持，旨在简化文档操作并提高开发效率。

该框架封装了 POI 操作的复杂性，同时提供了强大的功能，如大文件的 SAX 流式处理、基于模板的生成和样式自定义。

-----

## ✨ 核心特性

### 🎯 基础能力

* **多格式支持**: Excel (xls/xlsx)、Word (doc/docx)、CSV、OFD、PDF
* **简单 API**: 流畅设计，易于使用的方法处理常见操作
* **高性能**: 基于 SAX 的大 Excel 文件解析，最小内存占用
* **样式自定义**: 丰富的单元格、字体、颜色和边框样式支持
* **模板支持**: 编辑现有模板或从头创建新文档

### 📄 Excel 处理

| 功能 | 描述 |
| :--- | :--- |
| **读取操作** | 支持读取为 List、Map、Bean 或自定义行处理器 |
| **写入操作** | 小文件标准写入器，大数据集 BigWriter |
| **SAX 解析** | Excel '03 和 '07 格式的内存高效流式处理 |
| **单元格样式** | 预定义样式集，可自定义字体、颜色、边框 |
| **数据验证** | 下拉列表、数字验证、日期约束 |
* **公式支持**: 读写 Excel 公式
* **合并单元格**: 自动处理合并区域
* **图片**: 在 Excel 文件中插入图片
* **超链接**: 向单元格添加超链接
* **多个工作表**: 在工作簿中使用多个工作表

### 📝 Word 处理

* **文档生成**: 以编程方式创建 DOCX 文档
* **富文本**: 添加自定义字体、颜色、大小和样式的文本
* **表格**: 创建带边框和对齐方式的表格
* **图片**: 插入自定义尺寸的图片
* **页眉/页脚**: 添加页眉和页脚
* **段落样式**: 控制对齐、缩进、间距

### 📊 CSV 处理

* **灵活读取**: 从 File、InputStream 或 Reader 读取
* **可配置配置**: 分隔符、编码、引号字符
* **写入支持**: 使用适当转义写入 CSV 数据
* **流式处理**: 高效处理大型 CSV 文件

### 📋 OFD/PDF 处理

基于 OFDRW 库，提供：
* **OFD 转 PDF**: 将 OFD 文档转换为 PDF 格式
* **OFD 转图片**: 导出 OFD 页面为 JPG、PNG、GIF、BMP、SVG
* **OFD 转 HTML**: 将 OFD 转换为 HTML 以便网页显示
* **OFD 转文本**: 从 OFD 文档提取纯文本
* **PDF/文本/图片转 OFD**: 将各种格式转换为 OFD

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-office</artifactId>
    <version>x.x.x</version>
</dependency>
```

**注意**: 此模块对以下内容有可选依赖：
- `poi-ooxml` (版本 5.4.1) - 用于 Excel 和 Word 处理
- `ofdrw-full` (版本 2.3.7) - 用于 OFD/PDF 处理

-----

## 📝 使用示例

### 1. Excel 读取

#### 读取 Excel 为 List

```java
// 从第一个工作表读取所有数据为 List of Lists
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<List<Object>> rows = reader.read();

// 从特定工作表读取
ExcelReader reader = ExcelKit.getReader("data.xlsx", "Sheet2");
List<List<Object>> rows = reader.read();
```

#### 读取 Excel 为 Map

```java
// 第一行作为标题，后续行作为 Map<String, Object>
ExcelReader reader = ExcelKit.getReader("data.xlsx");
List<Map<String, Object>> maps = reader.readAsMap();
```

#### 读取 Excel 为 Bean

```java
// 将 Excel 行映射为 Java beans
ExcelReader reader = ExcelKit.getReader("users.xlsx");
List<User> users = reader.readAll(User.class);

// 自定义标题行索引
ExcelReadConfig config = ExcelReadConfig.builder()
    .headerRowIndex(0)  // 标题行
    .startRowIndex(1)   // 数据起始行
    .build();
List<User> users = reader.readAll(User.class, config);
```

#### 使用 SAX 读取 Excel（内存高效）

```java
// 使用 SAX 处理大型 Excel 文件（低内存占用）
ExcelKit.readBySax("large_file.xlsx", 0, new RowHandler() {
    @Override
    public void handle(int sheetIndex, int rowIndex, List<Object> row) {
        // 处理每一行
        System.out.println("行 " + rowIndex + ": " + row);
    }
});

// 使用 SAX 读取为 beans
ExcelKit.readBySax("large_file.xlsx", "sheet1", new BeanRowHandler<User>(User.class) {
    @Override
    public void handle(int sheetIndex, int rowIndex, User row) {
        // 处理每个 bean
        userService.save(row);
    }
});
```

### 2. Excel 写入

#### 从 List 写入 Excel

```java
// 从 List 创建 Excel
List<List<Object>> rows = new ArrayList<>();
rows.add(Arrays.asList("姓名", "年龄", "邮箱"));
rows.add(Arrays.asList("张三", 25, "zhangsan@example.com"));
rows.add(Arrays.asList("李四", 30, "lisi@example.com"));

ExcelWriter writer = ExcelKit.getWriter();
writer.write(rows);
writer.flush(FileKit.file("output.xlsx"));
```

#### 从 Bean List 写入 Excel

```java
// 从 beans 创建 Excel
List<User> users = getUserList();

ExcelWriter writer = ExcelKit.getWriter();
writer.write(users);
writer.flush(FileKit.file("users.xlsx"));
```

#### 使用自定义样式写入 Excel

```java
ExcelWriter writer = ExcelKit.getWriter();

// 设置标题样式
StyleSet headerStyle = writer.getStyleSet();
headerStyle.setBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
headerStyle.setBold(FontBoldWeight.BOLD);
headerStyle.setFontHeightInPoints((short) 12);

// 写入数据
writer.writeRow(Arrays.asList("姓名", "年龄", "邮箱"));
writer.setStyleSet(null);  // 为数据重置为默认样式
writer.write(users);

writer.flush(FileKit.file("styled_users.xlsx"));
```

#### 写入大型 Excel 文件

```java
// 使用 BigExcelWriter 处理大型数据集（内存高效）
BigExcelWriter writer = ExcelKit.getBigWriter("large_output.xlsx");

// 逐行写入
for (int i = 0; i < 100000; i++) {
    writer.writeRow(Arrays.asList("数据" + i, i, "值" + i));
}

writer.close();
```

#### 编辑现有 Excel 模板

```java
// 加载模板并编辑
ExcelWriter writer = ExcelKit.getWriter("template.xlsx");

// 写入数据到特定工作表
writer.setCurrentSheet("Sheet1");
writer.write(users);

// 添加另一个工作表
writer.setSheet("摘要");
writer.writeRow(Arrays.asList("总用户数", users.size()));

writer.flush(FileKit.file("report.xlsx"));
```

### 3. Word 处理

#### 创建 Word 文档

```java
// 创建新的 Word 文档
Word07Writer writer = DocxKit.getWriter();

// 添加段落
writer.addText("你好，世界！", FontStyle.builder()
    .fontSize(16)
    .bold(true)
    .color("000000")
    .build());

// 添加表格
DocxTable table = writer.addTable(new int[]{3000, 3000, 3000});
table.addRow(Arrays.asList("姓名", "年龄", "邮箱"));
table.addRow(Arrays.asList("张三", "25", "zhangsan@example.com"));

// 添加图片
writer.addPicture(FileKit.file("logo.png"), 100, 100);

// 保存到文件
writer.flush(FileKit.file("document.docx"));
```

### 4. CSV 处理

#### 读取 CSV

```java
// 读取 CSV 文件
CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), StandardCharsets.UTF_8);
CsvRow row;
while ((row = reader.read()) != null) {
    List<String> fields = row.getRawList();
    // 处理行
}
reader.close();

// 使用自定义配置读取
CsvReadConfig config = CsvReadConfig.builder()
    .delimiter('\t')      // 制表符分隔符
    .textDelimiter('"')   // 引号字符
    .ignoreEmptyRows(true)
    .build();

CsvReader reader = CsvKit.getReader(FileKit.file("data.csv"), StandardCharsets.UTF_8, config);
```

#### 写入 CSV

```java
// 写入 CSV 文件
CsvWriter writer = CsvKit.getWriter("output.csv", StandardCharsets.UTF_8);

writer.writeRow(Arrays.asList("姓名", "年龄", "邮箱"));
writer.writeRow(Arrays.asList("张三", "25", "zhangsan@example.com"));
writer.writeRow(Arrays.asList("李四", "30", "lisi@example.com"));

writer.close();

// 追加到现有文件
CsvWriter writer = CsvKit.getWriter("output.csv", StandardCharsets.UTF_8, true);
writer.writeRow(Arrays.asList("王五", "35", "wangwu@example.com"));
writer.close();
```

### 5. OFD/PDF 转换

```java


import java.nio.file.Paths;

// PDF 转 OFD
DocConverter.pdfToOfd(
        Paths.get("input.pdf"),
    Paths.

get("output.ofd")
);

// OFD 转 PDF
        DocConverter.

odfToPdf(
        Paths.get("input.ofd"),
    Paths.

get("output.pdf")
);

// OFD 转图片（PNG）
        DocConverter.

odfToImage(
        Paths.get("input.ofd"),
    Paths.

get("output_dir"),
    "png",
            10.0  // 每毫米像素数（质量）
            );

// OFD 转 HTML
            DocConverter.

odfToHtml(
        Paths.get("input.ofd"),
    Paths.

get("output.html")
);

// 图片转 OFD
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

// 文本转 OFD
        DocConverter.

textToOfd(
        Paths.get("input.txt"),
    Paths.

get("output.ofd"),
    12.0  // 字体大小
            );
```

-----

## 💡 最佳实践

### 1. 对大型 Excel 文件使用 SAX

```java
// ✅ 推荐: 对大文件使用 SAX（低内存）
ExcelKit.readBySax("large_file.xlsx", 0, new RowHandler() {
    @Override
    public void handle(int sheetIndex, int rowIndex, List<Object> row) {
        // 逐行处理
    }
});

// ❌ 不推荐: 将整个文件加载到内存
List<List<Object>> rows = ExcelKit.getReader("large_file.xlsx").read();
```

### 2. 对大型数据集使用 BigExcelWriter

```java
// ✅ 推荐: 使用 BigExcelWriter 写入大文件
BigExcelWriter writer = ExcelKit.getBigWriter("large_output.xlsx");
for (int i = 0; i < 100000; i++) {
    writer.writeRow(getRowData(i));
}
writer.close();

// ❌ 不推荐: 标准写入器可能导致内存问题
ExcelWriter writer = ExcelKit.getWriter();
List<List<Object>> allData = getAllData();  // 可能 OOM
writer.write(allData);
```

### 3. 正确关闭资源

```java
// ✅ 推荐: 使用 try-with-resources
try (CsvReader reader = CsvKit.getReader(file, charset)) {
    CsvRow row;
    while ((row = reader.read()) != null) {
        // 处理行
    }
}

// ❌ 不推荐: 手动资源管理
CsvReader reader = CsvKit.getReader(file, charset);
// 处理...
reader.close();  // 如果发生异常可能不会执行
```

### 4. 重用样式集以提高性能

```java
// ✅ 推荐: 创建并重用样式集
StyleSet headerStyle = new StyleSet(workbook);
headerStyle.setBold(true);
headerStyle.setFontColor(IndexedColors.RED.getIndex());

for (List<Object> row : rows) {
    writer.setStyleSet(headerStyle);
    writer.writeRow(row);
}

// ❌ 不推荐: 为每行创建新样式
for (List<Object> row : rows) {
    StyleSet style = new StyleSet(workbook);  // 低效
    writer.setStyleSet(style);
    writer.writeRow(row);
}
```

### 5. 为 CSV 明确指定编码

```java
// ✅ 推荐: 明确指定编码
CsvReader reader = CsvKit.getReader(file, StandardCharsets.UTF_8);
CsvWriter writer = CsvKit.getWriter(file, StandardCharsets.UTF_8);

// ❌ 不推荐: 依赖平台默认编码
CsvReader reader = CsvKit.getReader(file);
```

-----

## ❓ 常见问题

### Q1: 如何处理带公式的 Excel 单元格？

```java
ExcelReader reader = ExcelKit.getReader("formulas.xlsx");

// 将公式读取为字符串
String formula = CellKit.getCellValueAsString(cell);

// 读取公式计算结果
Object value = CellKit.getCellValue(cell);

// 写入公式
ExcelWriter writer = ExcelKit.getWriter();
writer.writeRow(Arrays.asList("=SUM(A1:A10)"));
```

### Q2: 如何处理 Excel 中的日期单元格？

```java
ExcelReadConfig config = ExcelReadConfig.builder()
    .useDateFormat(true)  // 格式化日期
    .datePattern("yyyy-MM-dd HH:mm:ss")
    .build();

ExcelReader reader = ExcelKit.getReader("dates.xlsx", 0, config);
List<List<Object>> rows = reader.read();
```

### Q3: 如何为 Excel 添加数据验证？

```java
ExcelWriter writer = ExcelKit.getWriter();

// 创建下拉列表
CellRangeAddressList regions = new CellRangeAddressList(1, 100, 0, 0);
DataValidationHelper dvHelper = writer.getSheet().getDataValidationHelper();
DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(new String[]{"是", "否"});
DataValidation validation = dvHelper.createValidation(constraint, regions);

writer.getSheet().addValidationData(validation);
```

### Q4: 如何在 Excel 中合并单元格？

```java
ExcelWriter writer = ExcelKit.getWriter();

// 合并单元格 A1:C1
writer.merge(0, 0, 0, 2);  // firstRow, lastRow, firstCol, lastCol

// 写入合并内容
writer.writeCellValue(0, 0, "合并标题");
```

### Q5: 如何处理 CSV 中的空单元格？

```java
CsvReadConfig config = CsvReadConfig.builder()
    .ignoreEmptyRows(false)      // 保留空行
    .treatEmptyFieldsAsNull(true)  // 将空字段视为 null
    .build();

CsvReader reader = CsvKit.getReader(file, charset, config);
```

### Q6: 如何自定义 Excel 单元格边框？

```java
StyleSet styleSet = new StyleSet(workbook);

// 设置边框样式
styleSet.setBorderBottom(LineStyle.THIN);
styleSet.setBorderTop(LineStyle.THIN);
styleSet.setBorderLeft(LineStyle.THIN);
styleSet.setBorderRight(LineStyle.THIN);
styleSet.setBorderColor(IndexedColors.BLACK.getIndex());

writer.setStyleSet(styleSet);
writer.writeRow(data);
```

### Q7: 如何在 Excel 中插入超链接？

```java
ExcelWriter writer = ExcelKit.getWriter();

// 向单元格添加超链接
Cell cell = writer.getSheet().getRow(0).getCell(0);
CreationHelper createHelper = writer.getWorkbook().getCreationHelper();
Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
link.setAddress("https://www.example.com");
cell.setHyperlink(link);
cell.setCellValue("点击这里");
```

-----

## 🔄 版本兼容性

| Bus Office 版本 | POI 版本 | OFDRW 版本 | JDK 版本 |
| :--- | :--- | :--- | :--- |
| 8.x | 5.4.1 | 2.3.7 | 17+ |
| 7.x | 5.2.x | 2.1.x | 11+ |

-----

## 📊 性能建议

### Excel 性能

1. **使用 SAX 读取大文件**: 减少 90%+ 内存使用
2. **使用 BigExcelWriter 写入大文件**: 逐行写入磁盘
3. **批量大小**: 每次刷新的最佳批量大小为 1000-5000 行
4. **样式重用**: 一次创建样式集并重用

### CSV 性能

1. **缓冲区大小**: 为大文件使用适当的缓冲区大小
2. **编码**: 使用 UTF-8 支持国际字符
3. **流式处理**: 对大型 CSV 文件逐行处理

### 内存优化

```java
// 配置 BigExcelWriter 内存窗口
BigExcelWriter writer = ExcelKit.getBigWriter(100);  // 在内存中保留 100 行

// 根据用例调整
// 更多行 = 更快但更多内存
// 更少行 = 更慢但更少内存
```

-----

## 🛠️ 故障排除

### 问题: "NoClassDefFoundError: org/apache/poi/..."

**解决方案**: 向项目添加 POI 依赖

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.4.1</version>
</dependency>
```

### 问题: 读取大 Excel 时出现 "OutOfMemoryError: Java heap space"

**解决方案**: 使用基于 SAX 的读取而不是基于 DOM

```java
// 不要使用:
List<List<Object>> rows = ExcelKit.getReader("large.xlsx").read();

// 而是使用:
ExcelKit.readBySax("large.xlsx", 0, new RowHandler() { ... });
```

### 问题: CSV 中的中文字符显示不正确

**解决方案**: 明确指定 UTF-8 编码

```java
CsvWriter writer = CsvKit.getWriter("file.csv", StandardCharsets.UTF_8);
```

### 问题: OFD 转换失败

**解决方案**: 确保包含 OFDRW 依赖

```xml
<dependency>
    <groupId>org.ofdrw</groupId>
    <artifactId>ofdrw-full</artifactId>
    <version>2.3.7</version>
</dependency>
```

-----

## 📞 支持和贡献

- **问题**: [GitHub Issues](https://github.com/818000/bus/issues)
- **贡献**: 欢迎拉取请求！

-----

**Bus Office** - 让办公文档处理简单高效！
