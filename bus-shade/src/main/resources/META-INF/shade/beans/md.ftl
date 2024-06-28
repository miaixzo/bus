# ${title!'数据库设计文档'}

<#if (database)??>
    **数据库名：** ${database!''}

</#if>
<#if (version)??>
    **文档版本：** ${version!''}

</#if>
<#if (description)??>
    **文档描述：** ${description!''}
</#if>

| 表名                  | 说明       |
| :---: | :---: |
<#list tables>
    <#items as t>
        | [${t.tableName!''}](#${t.tableName!''}) | ${t.remarks!''} |
    </#items>
</#list>
<#list tables><#items as t>

    **表名：** <a id="${t.tableName!''}">${t.tableName!''}</a>

    **说明：** ${t.remarks!''}

    **数据列：**

    <#list t.columns>
        | 序号 | 名称 | 数据类型 | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
        | :---: | :---: | :---:| :---: | :---: | :---: | :---: | :---: |
        <#items as c>
            |  ${c?index+1}   | ${c.columnName!''} |   ${c.columnType!''}  |   ${c.decimalDigits!'0'}    |    ${c.nullable!''}     |  ${c.primaryKey!''}   |   ${c.columnDef!''}    | ${c.remarks!''}  |
        </#items>
    </#list></#items>
</#list>