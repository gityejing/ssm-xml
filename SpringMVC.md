# SpringMVC 框架学习知识点

### 1，常用注解

@Controller:放在类上，用來標注這個類是一個controller
@RequestMapping：放在类上和方法上
@PathVariable：@RequestMapping的映射中使用占位符来接收实际url中的参数值，然后通过@PathVariable注解，将值指定注入到方法参数中。比如例子中将url映射中的bookId映射到参数bookId中去。

```java
	@RequestMapping(value = "/{bookId}/detail", method = RequestMethod.GET)
	private String detail(@PathVariable("bookId") Long bookId, Model model){
	
	}
```

@RequestParam:将url中的请求参数映射到方法参数中。注意required=true时，url中必须带有指定的参数，否则访问报错。
@RequestParam(value="studentId",required=true,defaultValue="1")

``` java
	@RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.POST, produces = {
			"application/json; charset=utf-8" })
	@ResponseBody
	private Result<AppointExecution> appoint(@PathVariable("bookId") Long bookId,
			@RequestParam("studentId") Long studentId) {
			
	}
```

@RequestHeader:将请求头中的一些参数值映射到方法的参数中
@RequestBody：
@CookieValue：将cookie中的键值映射到方法的参数中
@RequestPart：
@RequiredTypes：
@RequestWrapper：	
@ResponseBody：直接返回数据给响应体。而不是返回一个页面。通常用来返回json数据。



### 2，@RequestMapping 注解支持Ant风格（不重要，了解）

### 3，@RequestMapping 注解的常用的参数配置

method
produces

### 4,HiddenHttpMethodFilter的作用
通过传递hidder的属性值：_method=PUT/DELETE/POST/GET，来实现Restful的方式

### 5,使用pojo接收参数

自动的进行匹配，而且还支持级联属性。


### 6,servlet 原生的api使用

HttpServletRequest
HttpServletResponse
HttpSession
InputStream
OutputStream
Reader
Writer
Locale

### 7,如何将数据传递给前端页面 ###

```txt
如果需要向页面上传递数据可以如下操作：
1，方法参数中定义一个Map，将数据放到map中
2，方法参数中定义一个Model,将数据放到Model中
3，方法参数中定义一个ModelMap,将数据放到ModelMap中
4，返回一个ModelAndView,通常我们返回的string也会被包装成一个ModelAndView
5,@SessionAttribute 放在类上，可以根据属性名和属性值得类型将一些数据放到session域中，分别使用期value和types两个参数进行设置。value负责根据属性名来放置数据，types负责根据属性的值得类型来放置数据
6，@ModelAttribute
```

### 8,@ModelAttribute ###

@ModelAttribute：放在方法上和方法的参数上

```java
	
	// 这个方法，在controller中的其他方法被调用前，都会被调用一次，
	// map中的键值，会作用到其他方法的参数中
	@ModelAttribute
	public void book(@RequestParam(value="bookId",required=false) Long bookId,
			Map<String,Object> map) {
		if(bookId != null) {
			Book book = bookService.getById(bookId);
			System.out.println("从数据库中获取到book:"+book);
			map.put("book", book);
		}
	}

	@ModelAttribute
	public Book book2(@RequestParam(value="bookId",required=false) Long bookId) {
		if(bookId != null) {
			Book book = bookService.getById(bookId);
			System.out.println("从数据库中获取到book:"+book);
			return book;
		}
		return new Book();
	}
	
	// 这里不会重新new一个book，而是会使用上面的book，然后再接收页面传递的参数
	@RequestMapping("testModelAttribute")
	public String testModelAttribute(Book book) {
		System.out.println("修改后的book"+book);
		return "success";
	}

```

@ModelAttribute 使用总结出的结论：

```txt
1，@ModelAttribute标注的无返回值得方法，需要在参数中定义一个map，然后将键值放到map中，这样就将数据放到了模型域中
2，@ModelAttribute模型域中存放的键值对，例如：map.put("book", book)中的健的字符串，
如果和testModelAttribute(Book book)中的参数的类型满足一定的规律，就可以实现不重新创建一个
新的对象接收页面参数的效果，这个规律就是这个健必须是参数类型的第一个首字母小写。
3，@ModelAttribute标注的方法，如果直接返回一个对象值，那么其隐藏的健，就是这个返回值类型的首字母小写的字符串
4，@ModelAttribute还可以用在方法的参数上，指定使用模型域中的哪个键值作为方法接收参数的pojo，而不用重新new一个
```

### 9,@SessionAttributes ###

只能放在类上。

默认情况下Spring MVC将模型中的数据存储到request域中。当一个请求结束后，数据就失效了。如果要跨页面使用。那么需要使用到session。而@SessionAttributes注解就可以使得模型中的数据存储一份到session域中。

原理理解：它的做法大概可以理解为将Model中的被注解的attrName属性保存在一个SessionAttributesHandler中，在每个RequestMapping的方法执行后，这个SessionAttributesHandler都会将它自己管理的“属性”从Model中写入到真正的HttpSession；同样，在每个RequestMapping的方法执行前，SessionAttributesHandler会将HttpSession中的被@SessionAttributes注解的属性写入到新的Model中。如果想删除session中共享的参数，可以通过SessionStatus.setComplete()，这句只会删除通过@SessionAttribute保存到session中的参数

注意：
使用这个注解，当我们的目标方法中的参数碰巧与@SessionAttributes的value中配置的一样时，如果在session中没有相应的值，会报错。这时的解决办法有两个，第一就是在目标方法中尽量避免使用和@SessionAttributes的value一样的参数。第二个方法就是使用@ModelAttribute标注的方法，将session需要的值放到map中。



### 10,视图解析器的工作流程 ###

```xml

 <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
 	<property name="viewClass" value="org.springframework.web.servlet.view.JstlView" />
 	<property name="prefix" value="/WEB-INF/jsp/" />
 	<property name="suffix" value=".jsp" />
 </bean>
	 

```
InternalResourceViewResolver

### 11,转发和重定向操作 ###

return "redirect:/xxx"; // 重定向
return "xxx"; // 转发


### 12，RESTFUL风格的CRUD ###

### 13,springmvc 的页面标签 ###

<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

使用Spring的form标签主要有两个作用，
第一是它会自动的绑定来自Model中的一个属性值到当前form对应的实体对象，默认是command属性，这样我们就可以在form表单体里面方便的使用该对象的属性了；
第二是它支持我们在提交表单的时候使用除GET和POST之外的其他方法进行提交，包括DELETE和PUT等。




### 14，数据转化，数据绑定，数据校验 ###
WebDataBinder 的作用就是将页面的字符串值，绑定到后台的javabean属性值。比如页面提交的时间是一个字符串类型，
到后台是Date类型，这是就需要用到WebDataBinder。使用的方式是，在controller中使用@InitBinder 注解在一个void方法上，
并且其参数是WebDataBinder。

```java
    // 第一种自定义数据绑定
    @InitBinder // 只针对当前的controller或继承的子controller
    public void initBinder(WebDataBinder webDataBinder){
        // 将页面传递的日期字符串数据，绑定成日期时间类数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }
```

### 15,springmvc如何返回json ###
1，在方法上加上@ResponseBody 注解，直接把数据返回给页面，而不是返回一个jsp页面。
2，同时还依赖第三方的jar包，jackson

HttpMessageConverter 接口

### 16，文件上传 ###
首先配置xml文件：
```xml
	<!-- 文件上传解析器 -->
	<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
		<property name="defaultEncoding" value="utf-8"></property>
		<property name="maxUploadSize" value="10485760000"></property><!-- 最大上传文件大小 -->
		<property name="maxInMemorySize" value="10960"></property>
	</bean>
```
依赖commons-fileupload的jar包
页面上采用：method="post" enctype="multipart/form-data"
```jsp
	<!-- method必须为post 及enctype属性-->
	<form action="${ctx}/book/bookUpload" method="post" enctype="multipart/form-data">
		<input type="file" name="file">
		<input type="submit" value="上传">
	</form>

```
后台java代码：
``` java
@RequestMapping("/bookUpload")
	public String bookUpload(@RequestParam("file") CommonsMultipartFile file,
							 HttpServletRequest request,HttpServletResponse response){
		long startTime = System.currentTimeMillis();   // 获取开始时间
		if(!file.isEmpty()){
			try {
				// 定义输出流 将文件保存在D盘
				// file.getOriginalFilename()为获得文件的名字
				FileOutputStream os = new FileOutputStream("D:/"+file.getOriginalFilename());
				InputStream in = file.getInputStream();
				byte[] buf = new byte[1024];
				int b = 0;
				while((b=in.read(buf))!=-1){ // 读取文件
					os.write(b);
				}
				os.flush(); // 关闭流
				in.close();
				os.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis(); // 获取结束时间
		System.out.println("上传文件共使用时间："+(endTime-startTime));
		return "success";
	}
```

### 17，拦截器 ###

HandlerInterceptor 接口

```java

@Slf4j
public class MyHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
        throws Exception {

        log.debug(" 在请求目标方法前调用，返回false，请求结束。");
        if(handler instanceof HandlerMethod){
            HandlerMethod hm = (HandlerMethod) handler;
            log.debug("==="+hm.getMethod()); // 返回调用的那个类的方法的签名
            log.debug("==="+hm.getBean());// controller 的一个具体的对象
            log.debug("==="+hm.getBeanType());// controller 的全类名
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) 
        throws Exception {
        log.debug(" 在目标方法调用完后执行，前提是preHandle返回true");
        log.debug("==="+modelAndView.getViewName());// 返回视图的逻辑名
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) 
        throws Exception {

    }
}

```
xml 中的配置：

```xml
    <mvc:interceptors>
        <bean class="com.soecode.lyf.web.MyHandlerInterceptor"/>
        <!--<mvc:interceptor>
            <mvc:mapping path="/test/number.do"/>
            <bean class="com.soecode.lyf.web.MyHandlerInterceptor"/>
        </mvc:interceptor>-->
    </mvc:interceptors>
```

### 18，异常处理 ###

1,使用 @ ExceptionHandler 注解
> 定以一个异常类，然后在类的方法上使用这个注解，注解中指定是哪个异常

2,实现 HandlerExceptionResolver 接口
> 定以一个类，实现这个接口，然后用@Compont注解，将类放到容器中管理，这个类就成了全局的异常处理

3,使用 @controlleradvice 注解

这种用法是统一一个地方处理所有的所有的异常。
>1）首先定以一个用@controlleradvice注解的类，
2）然后在这个类中使用定义遇到各种异常的具体处理方法，
3）再然后使用@ExceptionHandler和具体的异常类来注解这些方法，
4）最后在controller的具体方法上标注@ ExceptionHandler，但是不用指定是哪个异常。

4.ResponseStatusExceptionResolver和@ResponseStatus注解










