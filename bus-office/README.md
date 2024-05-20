#### 项目说明

1. 支持office，pdf等办公文档
1. 支持txt,java,php,py,md,js,css等所有纯文本
1. 支持zip,rar,jar,tar,gzip等压缩包
1. 支持jpg，jpeg，png，gif等图片预览(翻转，缩放，镜像)
1. 使用spring boot开发，预览服务搭建部署非常简便
1. rest接口提供服务，跨平台特性(java,php,python,go,php，....)都支持，应用接入简单方便
1. 抽象预览服务接口，方便二次开发，非常方便添加其他类型文件预览支持
1. 最最重要MIT协议开源，代码pull下来想干嘛就干嘛

### 快速开始

> 依赖外部环境

- OpenOffice或者LibreOffice

> 下载地址如下:
> 地址①：http://mirrors.ustc.edu.cn/tdf/libreoffice/stable
> 地址②：https://pan.baidu.com/s/1ZSGCIVXTweK8tbOPudkaQQ  提取码：vn5v

```text
wget http://mirrors.ustc.edu.cn/tdf/libreoffice/stable/6.3.4/rpm/x86_64/LibreOffice_6.3.4_Linux_x86-64_rpm.tar.gz
wget http://mirrors.ustc.edu.cn/tdf/libreoffice/stable/6.3.4/rpm/x86_64/LibreOffice_6.3.4_Linux_x86-64_rpm_langpack_zh-CN.tar.gz
```

解压

```text
tar xvf LibreOffice_6.3.4_Linux_x86-64_rpm.tar.gz
tar xvf LibreOffice_6.3.4_Linux_x86-64_rpm_langpack_zh-CN.tar.gz
```

安装

```text
yum install LibreOffice_6.3.4.2_Linux_x86-64_rpm/RPMS/*.rpm
yum install LibreOffice_6.3.4.2_Linux_x86-64_rpm_langpack_zh-CN/RPMS/*.rpm
```

> 结合bus-starter项目配套使用

```java
@EnableOffice
```

> 具体使用如下：

```java

@Resource
OfficeProviderService officeProviderService;

@ApiOperation(value = "将传入的文档转换为指定的格式", notes = "文档转换")
@PostMapping("/index")
public Object convertToUsingParam(
        @ApiParam(value = "The input document to convert.", required = true)
        @RequestParam("data") final MultipartFile inputFile,
        @ApiParam(value = "The document format to convert the input document to.", required = true)
        @RequestParam(name = "format") final String type,
        @ApiParam(value = "The options to apply to the type.")
        @RequestParam(required = false) final Map<String, String> parameters) {
    Logger.debug("convertUsingRequestParam > Converting file to {}", type);
    if (inputFile.isEmpty()) {
        return write(ErrorCode.EM_100506);
    }

    if (StringKit.isBlank(convertToFormat)) {
        return write(ErrorCode.EM_100506);
    }

    try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final DocumentFormat targetFormat = DefaultFormatRegistry.getFormatByExtension(type);

        final Map<String, Object> loadProperties = new HashMap<>(LocalOfficeProvider.DEFAULT_LOAD_PROPERTIES);
        final Map<String, Object> storeProperties = new HashMap<>();
        decodeParameters(parameters, loadProperties, storeProperties);

        Provider effectProvider = officeProviderService.get(Registry.LOCAL);
        effectProvider.convert(inputStream)
                .as(DefaultFormatRegistry.getFormatByExtension(FileKit.getExtension(filename)))
                .to(outputStream)
                .as(targetFormat)
                .execute();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
        headers.add(
                "Content-Disposition",
                "attachment; filename="
                        + ObjectID.id()
                        + "."
                        + targetFormat.getExtension());

        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    } catch (InternalException | IOException ex) {
        return write(ErrorCode.EM_100506);
    }
}

```