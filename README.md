# SSM 整合案例代码 #

1,spring 和 mybatis 的整合
2,spring 和 springmvc 的整合



# 具体步骤 #

第一步：我们先在`spring`文件夹里新建`spring-dao.xml`文件，因为spring的配置太多，我们这里分三层，分别是dao,service,web。

 1. 读入数据库连接相关参数（可选）
 2. 配置数据连接池
  1). 配置连接属性，可以不读配置项文件直接在这里写死
  2). 配置c3p0，只配了几个常用的
 3. 配置SqlSessionFactory对象（mybatis）
 4. 扫描dao层接口，动态实现dao接口，也就是说不需要daoImpl，sql和参数都写在xml文件上

**spring-dao.xml**

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd">
	<!-- 配置整合mybatis过程 -->
	<!-- 1.配置数据库相关参数properties的属性：${url} -->
	<context:property-placeholder location="classpath:jdbc.properties" />

	<!-- 2.数据库连接池 -->
	<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<!-- 配置连接池属性 -->
		<property name="driverClass" value="${jdbc.driver}" />
		<property name="jdbcUrl" value="${jdbc.url}" />
		<property name="user" value="${jdbc.username}" />
		<property name="password" value="${jdbc.password}" />

		<!-- c3p0连接池的私有属性 -->
		<property name="maxPoolSize" value="30" />
		<property name="minPoolSize" value="10" />
		<!-- 关闭连接后不自动commit -->
		<property name="autoCommitOnClose" value="false" />
		<!-- 获取连接超时时间 -->
		<property name="checkoutTimeout" value="10000" />
		<!-- 当获取连接失败重试次数 -->
		<property name="acquireRetryAttempts" value="2" />
	</bean>

	<!-- 3.配置SqlSessionFactory对象 -->
	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
		<!-- 注入数据库连接池 -->
		<property name="dataSource" ref="dataSource" />
		<!-- 配置MyBaties全局配置文件:mybatis-config.xml -->
		<property name="configLocation" value="classpath:mybatis-config.xml" />
		<!-- 扫描entity包 使用别名 -->
		<property name="typeAliasesPackage" value="com.soecode.lyf.entity" />
		<!-- 扫描sql配置文件:mapper需要的xml文件 -->
		<property name="mapperLocations" value="classpath:mapper/*.xml" />
	</bean>

	<!-- 4.配置扫描Dao接口包，动态实现Dao接口，注入到spring容器中 -->
	<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
		<!-- 注入sqlSessionFactory -->
		<property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
		<!-- 给出需要扫描Dao接口包 -->
		<property name="basePackage" value="com.soecode.lyf.dao" />
	</bean>
</beans>
```

因为数据库配置相关参数是读取配置文件，所以在`resources`文件夹里新建一个`jdbc.properties`文件，存放我们4个最常见的数据库连接属性，这是我本地的，大家记得修改呀~还有喜欢传到github上“大头虾们”记得删掉密码，不然别人就很容易得到你服务器的数据库配置信息，然后干一些羞羞的事情，你懂的！！

**jdbc.properties**

``` properties
jdbc.driver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3307/ssm?useUnicode=true&characterEncoding=utf8
jdbc.username=root
jdbc.password=
```

**友情提示**：配置文件中的jdbc.username，如果写成username，可能会与系统环境中的username变量冲突，所以到时候真正连接数据库的时候，用户名就被替换成系统中的用户名（有得可能是administrator），那肯定是连接不成功的，这里有个小坑，我被坑了一晚上！！

因为这里用到了mybatis，所以需要配置mybatis核心文件，在`recources`文件夹里新建`mybatis-config.xml`文件。

 1. 使用自增主键
 2. 使用列别名
 3. 开启驼峰命名转换 create_time -> createTime

**mybatis-config.xml**

``` xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- 配置全局属性 -->
	<settings>
		<!-- 使用jdbc的getGeneratedKeys获取数据库自增主键值 -->
		<setting name="useGeneratedKeys" value="true" />

		<!-- 使用列别名替换列名 默认:true -->
		<setting name="useColumnLabel" value="true" />

		<!-- 开启驼峰命名转换:Table{create_time} -> Entity{createTime} -->
		<setting name="mapUnderscoreToCamelCase" value="true" />
	</settings>
</configuration>
```

第二步：刚弄好dao层，接下来到service层了。在`spring`文件夹里新建`spring-service.xml`文件。

 1. 扫描service包所有注解 @Service
 2. 配置事务管理器，把事务管理交由spring来完成
 3. 配置基于注解的声明式事务，可以直接在方法上@Transaction

**spring-service.xml**

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/tx
	http://www.springframework.org/schema/tx/spring-tx.xsd">
	
	<!-- 配置事务管理器 -->
	<bean id="transactionManager"
		class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<!-- 注入数据库连接池 -->
		<property name="dataSource" ref="dataSource" />
	</bean>

	<!-- 配置基于注解的声明式事务 -->
	<tx:annotation-driven transaction-manager="transactionManager" />
</beans>
```
spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/tx
	http://www.springframework.org/schema/tx/spring-tx.xsd">
	<!-- 扫描service包下所有使用注解的类型 -->
	<context:component-scan base-package="com.soecode.lyf.service" />
   
    <import resource="classpath:spring-dao.xml"/>
	<import resource="classpath:spring-service.xml"/>
</beans>

```

-------------

第三步：配置web层，在`spring`文件夹里新建`spring-servlet.xml`文件。

 1. 开启SpringMVC注解模式，可以使用@RequestMapping，@PathVariable，@ResponseBody等
 2. 对静态资源处理，如js，css，jpg等
 3. 配置jsp 显示ViewResolver，例如在controller中某个方法返回一个string类型的"login"，实际上会返回"/WEB-INF/login.jsp"
 4. 扫描web层 @Controller

**spring-servlet.xml**
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:mvc="http://www.springframework.org/schema/mvc" 
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/mvc
	http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd">
	<!-- 配置SpringMVC -->
	<!-- 1.开启SpringMVC注解模式 -->
	<!-- 简化配置： 
		(1)自动注册DefaultAnootationHandlerMapping,AnotationMethodHandlerAdapter 
		(2)提供一些列：数据绑定，数字和日期的format @NumberFormat, @DateTimeFormat, xml,json默认读写支持 
	-->
	<mvc:annotation-driven />
	
	<!-- 2.静态资源默认servlet配置
		(1)加入对静态资源的处理：js,gif,png
		(2)允许使用"/"做整体映射
	 -->
	 <mvc:default-servlet-handler/>
	 
	 <!-- 3.配置jsp 显示ViewResolver -->
	 <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
	 	<property name="viewClass" value="org.springframework.web.servlet.view.JstlView" />
	 	<property name="prefix" value="/WEB-INF/jsp/" />
	 	<property name="suffix" value=".jsp" />
	 </bean>
	 
	 <!-- 4.扫描web相关的bean -->
	 <context:component-scan base-package="com.soecode.lyf.web" />
</beans>
```

------------

第四步：最后就是修改`web.xml`文件了，它在`webapp`的`WEB-INF`下。

**web.xml**

``` xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                      http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1" metadata-complete="true">
	
	
	<!-- 配置spring ioc容器 -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring/spring.xml</param-value>
    </context-param>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
    	
	<!-- 配置DispatcherServlet -->
	<servlet>
		<servlet-name>seckill-dispatcher</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>classpath:spring/spring-servlet.xml</param-value>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>seckill-dispatcher</servlet-name>
		<url-pattern>/</url-pattern> 	<!-- 默认匹配所有的请求 -->
	</servlet-mapping>
	
	<!-- 中文乱码：注意，此段编码设置必须在所有filter的前面，否则过滤器有可能不起作用。 -->
    	<filter>
    		<filter-name>characterEncodingFilter</filter-name>
    		<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    		<init-param>
    			<param-name>encoding</param-name>
    			<param-value>UTF-8</param-value>
    		</init-param>
    		<init-param>
    			<param-name>forceEncoding</param-name>
    			<param-value>true</param-value>
    		</init-param>
    	</filter>
    	<filter-mapping>
    		<filter-name>characterEncodingFilter</filter-name>
    		<url-pattern>/*</url-pattern>
    	</filter-mapping>
    	
</web-app>

```

-----------

我们在项目中经常会使用到日志，所以这里还有配置日志xml，在`resources`文件夹里新建`logback.xml`文件，所给出的日志输出格式也是最基本的控制台s呼出，大家有兴趣查看[logback官方文档](http://logback.qos.ch/manual/)。

**logback.xml**
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true">
	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
		<!-- encoders are by default assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
		</encoder>
	</appender>

	<root level="debug">
		<appender-ref ref="STDOUT" />
	</root>
</configuration>
```

## SSM框架应用实例（图书管理系统）

首先新建数据库名为`ssm`，再创建两张表：图书表`book`和预约图书表`appointment`，并且为`book`表初始化一些数据，sql如下。

**schema.sql**
``` sql
-- 创建图书表
CREATE TABLE `book` (
  `book_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `name` varchar(100) NOT NULL COMMENT '图书名称',
  `number` int(11) NOT NULL COMMENT '馆藏数量',
  PRIMARY KEY (`book_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='图书表'

-- 初始化图书数据
INSERT INTO `book` (`book_id`, `name`, `number`)
VALUES
	(1000, 'Java程序设计', 10),
	(1001, '数据结构', 10),
	(1002, '设计模式', 10),
	(1003, '编译原理', 10)

-- 创建预约图书表
CREATE TABLE `appointment` (
  `book_id` bigint(20) NOT NULL COMMENT '图书ID',
  `student_id` bigint(20) NOT NULL COMMENT '学号',
  `appoint_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '预约时间' ,
  PRIMARY KEY (`book_id`, `student_id`),
  INDEX `idx_appoint_time` (`appoint_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='预约图书表'
```

------------

在`entity`包中添加两个对应的实体，图书实体`Book.java`和预约图书实体`Appointment.java`。

**Book.java**
```java
package com.soecode.lyf.entity;

public class Book {

	private long bookId;// 图书ID

	private String name;// 图书名称

	private int number;// 馆藏数量

	// 省略构造方法，getter和setter方法，toString方法

}
```

**Appointment.java**
``` java
package com.soecode.lyf.entity;

import java.util.Date;

/**
 * 预约图书实体
 */
public class Appointment {

	private long bookId;// 图书ID

	private long studentId;// 学号

	private Date appointTime;// 预约时间

	// 多对一的复合属性
	private Book book;// 图书实体
	
	// 省略构造方法，getter和setter方法，toString方法

}
```

------------

在`dao`包新建接口`BookDao.java`和`Appointment.java`

**BookDao.java**
``` java
package com.soecode.lyf.dao;

import java.util.List;

import com.soecode.lyf.entity.Book;

public interface BookDao {

	/**
	 * 通过ID查询单本图书
	 * 
	 * @param id
	 * @return
	 */
	Book queryById(long id);

	/**
	 * 查询所有图书
	 * 
	 * @param offset 查询起始位置
	 * @param limit 查询条数
	 * @return
	 */
	List<Book> queryAll(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 减少馆藏数量
	 * 
	 * @param bookId
	 * @return 如果影响行数等于>1，表示更新的记录行数
	 */
	int reduceNumber(long bookId);
}

```

**AppointmentDao.java**
```java
package com.soecode.lyf.dao;

import org.apache.ibatis.annotations.Param;

import com.soecode.lyf.entity.Appointment;

public interface AppointmentDao {

	/**
	 * 插入预约图书记录
	 * 
	 * @param bookId
	 * @param studentId
	 * @return 插入的行数
	 */
	int insertAppointment(@Param("bookId") long bookId, @Param("studentId") long studentId);

	/**
	 * 通过主键查询预约图书记录，并且携带图书实体
	 * 
	 * @param bookId
	 * @param studentId
	 * @return
	 */
	Appointment queryByKeyWithBook(@Param("bookId") long bookId, @Param("studentId") long studentId);

}
```
**提示**：这里为什么要给方法的参数添加`@Param`注解呢？是因为该方法有两个或以上的参数，一定要加，不然mybatis识别不了。上面的`BookDao`接口的`queryById`方法和`reduceNumber`方法只有一个参数`book_id`，所以可以不用加 `@Param`注解，当然加了也无所谓~

---------------

注意，这里不需要实现dao接口不用编写daoImpl， mybatis会给我们动态实现，但是我们需要编写相应的mapper。
在`mapper`目录里新建两个文件`BookDao.xml`和`AppointmentDao.xml`，分别对应上面两个dao接口，代码如下。

**BookDao.xml**
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.soecode.lyf.dao.BookDao">
	<!-- 目的：为dao接口方法提供sql语句配置 -->
	<select id="queryById" resultType="Book" parameterType="long">
		<!-- 具体的sql -->
		SELECT
			book_id,
			name,
			number
		FROM
			book
		WHERE
			book_id = #{id}
	</select>
	
	<select id="queryAll" resultType="Book">
		SELECT
			book_id,
			name,
			number
		FROM
			book
		ORDER BY
			book_id
		LIMIT #{offset}, #{limit}
	</select>
	
	<update id="reduceNumber">
		UPDATE book
		SET number = number - 1
		WHERE
			book_id = #{bookId}
		AND number > 0
	</update>
</mapper>
```

**AppointmentDao.xml**
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.soecode.lyf.dao.AppointmentDao">
	<insert id="insertAppointment">
		<!-- ignore 主键冲突，报错 -->
		INSERT ignore INTO appointment (book_id, student_id)
		VALUES (#{bookId}, #{studentId})
	</insert>
	
	<select id="queryByKeyWithBook" resultType="Appointment">
		<!-- 如何告诉MyBatis把结果映射到Appointment同时映射book属性 -->
		<!-- 可以自由控制SQL -->
		SELECT
			a.book_id,
			a.student_id,
			a.appoint_time,
			b.book_id "book.book_id",
			b.`name` "book.name",
			b.number "book.number"
		FROM
			appointment a
		INNER JOIN book b ON a.book_id = b.book_id
		WHERE
			a.book_id = #{bookId}
		AND a.student_id = #{studentId}
	</select>
</mapper>
```

**mapper总结**：`namespace`是该xml对应的接口全名，`select`和`update`中的`id`对应方法名，`resultType`是返回值类型，`parameterType`是参数类型（这个其实可选），最后`#{...}`中填写的是方法的参数，看懂了是不是很简单！！我也这么觉得~ 还有一个小技巧要交给大家，就是在返回`Appointment`对象包含了一个属性名为`book`的Book对象，那么可以使用`"book.属性名"`的方式来取值，看上面`queryByKeyWithBook`方法的sql。

------------

`dao`层写完了，接下来`test`对应的`package`写我们测试方法吧。
因为我们之后会写很多测试方法，在测试前需要让程序读入spring-dao和mybatis等配置文件，所以我这里就抽离出来一个`BaseTest`类，只要是测试方法就继承它，这样那些繁琐的重复的代码就不用写那么多了~

**BaseTest.java**
``` java
package com.soecode.lyf;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器 spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring配置文件
@ContextConfiguration({ "classpath:spring/spring.xml"})
public class BaseTest {

}

```
因为`spring-service`在`service`层的测试中会时候到，这里也一起引入算了！

新建`BookDaoTest.java`和`AppointmentDaoTest.java`两个dao测试文件。

**BookDaoTest.java**
``` java
package com.soecode.lyf.dao;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.soecode.lyf.BaseTest;
import com.soecode.lyf.entity.Book;

public class BookDaoTest extends BaseTest {

	@Autowired
	private BookDao bookDao;

	@Test
	public void testQueryById() throws Exception {
		long bookId = 1000;
		Book book = bookDao.queryById(bookId);
		System.out.println(book);
	}

	@Test
	public void testQueryAll() throws Exception {
		List<Book> books = bookDao.queryAll(0, 4);
		for (Book book : books) {
			System.out.println(book);
		}
	}

	@Test
	public void testReduceNumber() throws Exception {
		long bookId = 1000;
		int update = bookDao.reduceNumber(bookId);
		System.out.println("update=" + update);
	}

}
```

**AppointmentDaoTest.java**
``` java
package com.soecode.lyf.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.soecode.lyf.BaseTest;
import com.soecode.lyf.entity.Appointment;

public class AppointmentDaoTest extends BaseTest {

	@Autowired
	private AppointmentDao appointmentDao;

	@Test
	public void testInsertAppointment() throws Exception {
		long bookId = 1000;
		long studentId = 12345678910L;
		int insert = appointmentDao.insertAppointment(bookId, studentId);
		System.out.println("insert=" + insert);
	}

	@Test
	public void testQueryByKeyWithBook() throws Exception {
		long bookId = 1000;
		long studentId = 12345678910L;
		Appointment appointment = appointmentDao.queryByKeyWithBook(bookId, studentId);
		System.out.println(appointment);
		System.out.println(appointment.getBook());
	}

}
```

-------------

嗯，到这里一切到很顺利~那么我们继续service层的编码吧~可能下面开始信息里比较大，大家要做好心理准备~

首先，在写我们的业务之前，我们先定义几个预约图书操作返回码的数据字典，也就是我们要返回给客户端的信息。我们这类使用枚举类，没听过的小伙伴要好好恶补一下了（我也是最近才学到的= =）

**预约业务操作返回码说明**

| 返回码 | 说明 |
| --- | --- |
| 1 | 预约成功 |
| 0 | 库存不足 |
| -1 | 重复预约 |
| -2 | 系统异常 |

新建一个包叫`enums`，在里面新建一个枚举类`AppointStateEnum.java`，用来定义预约业务的数据字典，没听懂没关系，我们直接看代码吧~是不是感觉有模有样了！

**AppointStateEnum.java**
```java
package com.soecode.lyf.enums;

/**
 * 使用枚举表述常量数据字典
 */
public enum AppointStateEnum {

	SUCCESS(1, "预约成功"), NO_NUMBER(0, "库存不足"), REPEAT_APPOINT(-1, "重复预约"), INNER_ERROR(-2, "系统异常");

	private int state;

	private String stateInfo;

	private AppointStateEnum(int state, String stateInfo) {
		this.state = state;
		this.stateInfo = stateInfo;
	}

	public int getState() {
		return state;
	}

	public String getStateInfo() {
		return stateInfo;
	}

	public static AppointStateEnum stateOf(int index) {
		for (AppointStateEnum state : values()) {
			if (state.getState() == index) {
				return state;
			}
		}
		return null;
	}

}
```
-------------

接下来，在`dto`包下新建`AppointExecution.java`用来存储我们执行预约操作的返回结果。

**AppointExecution.java**
```java
package com.soecode.lyf.dto;

import com.soecode.lyf.entity.Appointment;
import com.soecode.lyf.enums.AppointStateEnum;

/**
 * 封装预约执行后结果
 */
public class AppointExecution {

	// 图书ID
	private long bookId;

	// 秒杀预约结果状态
	private int state;

	// 状态标识
	private String stateInfo;

	// 预约成功对象
	private Appointment appointment;

	public AppointExecution() {
	}

	// 预约失败的构造器
	public AppointExecution(long bookId, AppointStateEnum stateEnum) {
		this.bookId = bookId;
		this.state = stateEnum.getState();
		this.stateInfo = stateEnum.getStateInfo();
	}

	// 预约成功的构造器
	public AppointExecution(long bookId, AppointStateEnum stateEnum, Appointment appointment) {
		this.bookId = bookId;
		this.state = stateEnum.getState();
		this.stateInfo = stateEnum.getStateInfo();
		this.appointment = appointment;
	}
	
	// 省略getter和setter方法，toString方法

}

```

-------------

接着，在`exception`包下新建三个文件
`NoNumberException.java`
`RepeatAppointException.java`
`AppointException.java`
预约业务异常类（都需要继承RuntimeException），分别是无库存异常、重复预约异常、预约未知错误异常，用于业务层非成功情况下的返回（即成功返回结果，失败抛出异常）。

**NoNumberException.java**
``` java
package com.soecode.lyf.exception;

/**
 * 库存不足异常
 */
public class NoNumberException extends RuntimeException {

	public NoNumberException(String message) {
		super(message);
	}

	public NoNumberException(String message, Throwable cause) {
		super(message, cause);
	}

}

```

**RepeatAppointException.java**
``` java
package com.soecode.lyf.exception;

/**
 * 重复预约异常
 */
public class RepeatAppointException extends RuntimeException {

	public RepeatAppointException(String message) {
		super(message);
	}

	public RepeatAppointException(String message, Throwable cause) {
		super(message, cause);
	}

}

```

**AppointException.java**
``` java
package com.soecode.lyf.exception;

/**
 * 预约业务异常
 */
public class AppointException extends RuntimeException {

	public AppointException(String message) {
		super(message);
	}

	public AppointException(String message, Throwable cause) {
		super(message, cause);
	}

}

```

-------------

咱们终于可以编写业务代码了，在`service`包下新建`BookService.java`图书业务接口。

**BookService.java**
``` java
package com.soecode.lyf.service;

import java.util.List;

import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.entity.Book;

/**
 * 业务接口：站在"使用者"角度设计接口 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 */
public interface BookService {

	/**
	 * 查询一本图书
	 * 
	 * @param bookId
	 * @return
	 */
	Book getById(long bookId);

	/**
	 * 查询所有图书
	 * 
	 * @return
	 */
	List<Book> getList();

	/**
	 * 预约图书
	 * 
	 * @param bookId
	 * @param studentId
	 * @return
	 */
	AppointExecution appoint(long bookId, long studentId);

}
```

在`service.impl`包下新建`BookServiceImpl.java`使用`BookService`接口，并实现里面的方法。

**BookServiceImpl**
``` java
package com.soecode.lyf.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soecode.lyf.dao.AppointmentDao;
import com.soecode.lyf.dao.BookDao;
import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.entity.Appointment;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.AppointException;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// 注入Service依赖
	@Autowired
	private BookDao bookDao;

	@Autowired
	private AppointmentDao appointmentDao;


	@Override
	public Book getById(long bookId) {
		return bookDao.queryById(bookId);
	}

	@Override
	public List<Book> getList() {
		return bookDao.queryAll(0, 1000);
	}

	@Override
	@Transactional
	/**
	 * 使用注解控制事务方法的优点： 1.开发团队达成一致约定，明确标注事务方法的编程风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部
	 * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
	 */
	public AppointExecution appoint(long bookId, long studentId) {
		try {
			// 减库存
			int update = bookDao.reduceNumber(bookId);
			if (update <= 0) {// 库存不足
				//return new AppointExecution(bookId, AppointStateEnum.NO_NUMBER);//错误写法				
				throw new NoNumberException("no number");
			} else {
				// 执行预约操作
				int insert = appointmentDao.insertAppointment(bookId, studentId);
				if (insert <= 0) {// 重复预约
					//return new AppointExecution(bookId, AppointStateEnum.REPEAT_APPOINT);//错误写法
					throw new RepeatAppointException("repeat appoint");
				} else {// 预约成功
					Appointment appointment = appointmentDao.queryByKeyWithBook(bookId, studentId);
					return new AppointExecution(bookId, AppointStateEnum.SUCCESS, appointment);
				}
			}
		// 要先于catch Exception异常前先catch住再抛出，不然自定义的异常也会被转换为AppointException，导致控制层无法具体识别是哪个异常
		} catch (NoNumberException e1) {
			throw e1;
		} catch (RepeatAppointException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// 所有编译期异常转换为运行期异常
			//return new AppointExecution(bookId, AppointStateEnum.INNER_ERROR);//错误写法
			throw new AppointException("appoint inner error:" + e.getMessage());
		}
	}

}

```

--------------

下面我们来测试一下我们的业务代码吧~因为查询图书的业务不复杂，所以这里只演示我们最重要的预约图书业务！！

**BookServiceImplTest.java**
``` java
package com.soecode.lyf.service.impl;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.soecode.lyf.BaseTest;
import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.service.BookService;

public class BookServiceImplTest extends BaseTest {

	@Autowired
	private BookService bookService;

	@Test
	public void testAppoint() throws Exception {
		long bookId = 1001;
		long studentId = 12345678910L;
		AppointExecution execution = bookService.appoint(bookId, studentId);
		System.out.println(execution);
	}

}

```


咱们还需要在`dto`包里新建一个封装json返回结果的类`Result.java`，设计成泛型。

**Result.java**
``` java
package com.soecode.lyf.dto;

/**
 * 封装json对象，所有返回结果都使用它
 */
public class Result<T> {

	private boolean success;// 是否成功标志

	private T data;// 成功时返回的数据

	private String error;// 错误信息

	public Result() {
	}

	// 成功时的构造器
	public Result(boolean success, T data) {
		this.success = success;
		this.data = data;
	}

	// 错误时的构造器
	public Result(boolean success, String error) {
		this.success = success;
		this.error = error;
	}

	// 省略getter和setter方法
}

```

------------

最后，我们写web层，也就是controller，我们在`web`包下新建`BookController.java`文件。

**BookController.java**
``` java
package com.soecode.lyf.web;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.dto.Result;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;

@Controller
@RequestMapping("/book") // url:/模块/资源/{id}/细分 /seckill/list
public class BookController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BookService bookService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	private String list(Model model) {
		List<Book> list = bookService.getList();
		model.addAttribute("list", list);
		// list.jsp + model = ModelAndView
		return "list";// WEB-INF/jsp/"list".jsp
	}

	@RequestMapping(value = "/{bookId}/detail", method = RequestMethod.GET)
	private String detail(@PathVariable("bookId") Long bookId, Model model) {
		if (bookId == null) {
			return "redirect:/book/list";
		}
		Book book = bookService.getById(bookId);
		if (book == null) {
			return "forward:/book/list";
		}
		model.addAttribute("book", book);
		return "detail";
	}

	//ajax json
	@RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces = {
			"application/json; charset=utf-8" })
	@ResponseBody
	private Result<AppointExecution> appoint(@PathVariable("bookId") Long bookId, @RequestParam("studentId") Long studentId) {
		if (studentId == null || studentId.equals("")) {
			return new Result<>(false, "学号不能为空");
		}
		//AppointExecution execution = bookService.appoint(bookId, studentId);//错误写法，不能统一返回，要处理异常（失败）情况
		AppointExecution execution = null;
		try {
			execution = bookService.appoint(bookId, studentId);
		} catch (NoNumberException e1) {
			execution = new AppointExecution(bookId, AppointStateEnum.NO_NUMBER);
		} catch (RepeatAppointException e2) {
			execution = new AppointExecution(bookId, AppointStateEnum.REPEAT_APPOINT);
		} catch (Exception e) {
			execution = new AppointExecution(bookId, AppointStateEnum.INNER_ERROR);
		}
		return new Result<AppointExecution>(true, execution);
	}

}

```

