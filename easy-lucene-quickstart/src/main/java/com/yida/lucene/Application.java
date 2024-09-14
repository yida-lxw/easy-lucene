package com.yida.lucene;

import com.yida.lucene.entity.Article;
import com.yida.lucene.service.ArticleService;
import com.yida.lucene.spring.annotation.EnableEmbeddedLucene;
import com.yida.lucene.spring.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author yida
 * @package com.yida.lucene
 * @date 2024-09-14 20:13
 * @description Type your description over here.
 */
@SpringBootApplication
@EnableEmbeddedLucene
public class Application {
	@Autowired
	private ArticleService articleService;

	public static void main(String[] args) throws IllegalAccessException {
		Field[] fields = Charset.class.getDeclaredFields();
		for (Field field : fields) {
			if (!"defaultCharset".equals(field.getName())) {
				continue;
			}
			field.setAccessible(true);
			field.set(null, Charset.forName("UTF-8"));
		}
		SpringApplication.run(Application.class, args);
	}

	//创建索引/索引查询测试
	@PostConstruct
	public void createIndex() {
		//测试创建索引数据
		//articleService.insert();

		String queryKeyword = "中国经济";
		String queryField = "title";
		List<Article> articleList = articleService.queryByKeyword(queryKeyword, queryField);
		if (null != articleList && articleList.size() > 0) {
			String articleJSON = JacksonUtils.toJSONString(articleList);
			System.out.println(articleJSON);
		}
	}
}
