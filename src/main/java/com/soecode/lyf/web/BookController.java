package com.soecode.lyf.web;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.dto.Result;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.entity.Persion;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;
import com.soecode.lyf.service.impl.PersionService;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Controller
@RequestMapping("/book") // url:/模块/资源/{id}/细分 /seckill/list
// @SessionAttributes(value= {"book"})
@Slf4j
public class BookController extends BaseController{

	@Autowired
	private BookService bookService;

	@Autowired
	private PersionService persionService;
	
	/**
	 * @ModelAttribute 的使用从一个应用场景说起，就是在页面中修改某条数据库中的记录的时候，不想改变全部字段的值，
	 * 而是只想改变部分值，该怎么优雅的进行处理。
	 * 
	 * 一种办法是先从数据库中查询出来，然后在页面上用隐藏字段。
	 * 另一种办法是，在进行更新操作的时候，逐一的进行设置值。
	 * 以上两种办法，不是页面端太多重复的工作，就是后台代码段太多的set操作，都不够优雅。
	 * 
	 * @ModelAttribute 就可以很优雅的解决这类场景，但是首先要弄清楚其使用的规律。
	 * 
	 * 每一个请求都会先进入到这里
	 * @param bookId
	 * @param map
	 */
	/*@ModelAttribute
	public void book(@RequestParam(value="bookId",required=false) Long bookId,
			Map<String,Object> map) {
		if(bookId != null) {
			Book book = bookService.getById(bookId);
			System.out.println("从数据库中获取到book:"+book);
			map.put("abc", book);
		}
	}*/
	
	
	/**
	 * 这种直接返回一个对象的模式，model中的健是返回对象类型的首字母小写之后的字符串，也就是book
	 * @param bookId
	 * @return
	 */
	@ModelAttribute
	public Book book2(@RequestParam(value="bookId",required=false) Long bookId) {
		if(bookId != null) {
			Book book = bookService.getById(bookId);
			System.out.println("从数据库中获取到book:"+book);
			if(book != null) return book;
			return new Book();
		}
		return new Book();
	}

	@ModelAttribute
	public void persion(@RequestParam(value="persionId",required=false) Long persionId, Map<String,Object> map) {
		if(persionId != null) {
			Persion persion = new Persion();
			persion.setId(persionId);
			List<Persion> list = persionService.select(persion);
			System.out.println("从数据库中获取到book:"+list);
			if(list != null && list.size()>0 ) map.put("persion",list.get(0));
			else map.put("persion",new Persion());
		}else{
			map.put("persion",new Persion());
		}
	}




	
	@RequestMapping("editBook")

	public String editBook(@ModelAttribute("book") Book book,Model model) {
//		model.addAttribute("command", book);
		return "detail";
	}
	
	/**
	 * 遇到的问题
	 * 1：中文乱码。
	 * 2：
	 * @param book
	 * @return
	 */
	@RequestMapping(value="updateBook",method=RequestMethod.POST)
	public String updateBook(@ModelAttribute("book") Book book,Model model) {
		bookService.update(book);
//		model.addAttribute("command", book);
		return "detail";
	}
	
	@RequestMapping(value="newBook",method=RequestMethod.POST)
	public String newBook(Book book,Model model){
		String name = book.getName();
		System.out.println(name);
		
		bookService.save(book);
		try {
			name = URLDecoder.decode(name, "UTF-8");
//			name = URLDecoder.decode(name, "ISO8859-1");
			System.out.println(name);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("新增成功====="+book);
		model.addAttribute("book", book);
		return "redirect:/book/list";
	}
	
	@RequestMapping(value="/delBook/{bookId}",method=RequestMethod.GET)
	public String delBook(@PathVariable("bookId") long bookId) {
		bookService.delete(bookId);
		log.debug("删除成功=====");
		return "redirect:/book/list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	private String list(Model model) {
		List<Book> list = bookService.getList();
		model.addAttribute("list", list);
		// list.jsp + model = ModelAndView
		List<Persion> persions = persionService.select(new Persion());
		model.addAttribute("persions",persions);
		return "list";// WEB-INF/jsp/"list".jsp
	}

	@RequestMapping(value="newPersion",method=RequestMethod.POST)
	public String newPersion(Persion persion,Model model){
		persionService.insert(persion);
		log.debug("新增成功=====");
		model.addAttribute("persion", persion);
		return "redirect:/book/list";
	}

	/**
	 * 调用这个方法的流程
	 * 1，先调用@ModelAttribute注解修饰的方法，在map中放入健为book的键值对
	 * 2，springmvc从map中取出("book",book)，并把页面的请求参数赋给book对象的对应属性
	 * 3，把book对象，作为参数，传入到目标方法中
	 * 这里的关键点，就是，在调用目标方法的时候，springmvc框架，是经过怎样的算法，来从map中找到键值对的。
	 * 如果找不到，是不是就创建一个全新的。
	 * 
	 * 
	 * 做一个小实验：
	 * 1，map.put("book", book);改为map.put("abc", book)
	 * 2,testModelAttribute(Book book)改为：testModelAttribute(Book abc)
	 * 
	 * 最后的结果是：
	 * 从数据库中获取到book:Book [bookId=1000, name=《激荡三十年》, number=133, price=100.0]
	 * 修改后的bookBook [bookId=1000, name=激荡十年, number=45, price=0.0]
	 * 
	 * 再做一个小实验：
	 * 1，map.put("book", book);改为map.put("abc", book)
	 * 2，testModelAttribute(Book book)保持不变
	 * 结果：
	 * 从数据库中获取到book:Book [bookId=1000, name=《激荡三十年》, number=133, price=100.0]
	 * 修改后的bookBook [bookId=1000, name=激荡十年, number=45, price=0.0]
	 * 再做一个小实验：
	 * 1，map.put("book", book);保持不变
	 * 2，testModelAttribute(Book book)改为：testModelAttribute(Book abc)
	 * 结果：
	 * 从数据库中获取到book:Book [bookId=1000, name=《激荡三十年》, number=133, price=100.0]
	 * 修改后的bookBook [bookId=1000, name=激荡十年, number=45, price=100.0]
	 * 
	 * 通过以上实验，得到一个结论：
	 * 1，map.put("book", book)中的健和testModelAttribute(Book book)中的参数的类型满足一定的规律，
	 * 就可以实现不重新创建一个新的对象接收页面参数的效果，这个规律就是这个健必须是参数类型的第一个首字母小写。
	 * 2，@ModelAttribute还可以用在方法的参数上，指定使用model中的哪个键值作为接收参数的pojo
	 * 3，@ModelAttribute标注的方法，如果直接返回一个对象值，那么其隐藏的健，就是这个返回值类型的首字母小写的字符串
	 * 
	 * 
	 * @param book
	 * @return
	 */
	@RequestMapping("testModelAttribute")
	public String testModelAttribute(/*@ModelAttribute("abc") */Book book) {
		System.out.println("修改后的book"+book);
		return "success";
	}

	@RequestMapping(value = "/{bookId}/detail", method = RequestMethod.GET)
	private String detail(@PathVariable(value = "bookId") Long bookId, Model model) {
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

	@RequestMapping(value = "/{bookId}/appoint", method = RequestMethod.GET, produces = {
			"application/json; charset=utf-8" })
	@ResponseBody
	private Result<AppointExecution> appoint(@PathVariable("bookId") Long bookId,
			@RequestParam(value = "studentId", required = true, defaultValue = "1") Long studentId) {

		if (studentId == null || studentId.equals("")) {
			return new Result<>(false, "学号不能为空");
		}
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

	/**
	 * 文件上传
	 * @param file
	 * @param request
	 * @param response
	 * @return
	 */
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
}
