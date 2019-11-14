<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperUrl}.${entityName}Mapper">

    <resultMap id="BaseResultMap" type="${entityUrl}.${entityName}">
        <#list cis as ci>
            <id column="${ci.column}" property="${ci.property}"/>
        </#list>
    </resultMap>
    <sql id="Base_Column_List">
        ${agile}
    </sql>

</mapper>