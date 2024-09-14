package com.yida.lucene.service;

import com.yida.lucene.entity.Article;
import com.yida.lucene.repository.ElRepository;
import com.yida.lucene.spring.annotation.ElTransactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yida
 */
@Service
@RequiredArgsConstructor
@ElTransactional
public class ArticleService {
	@Getter
	private final ElRepository<Article> repository;

	@ElTransactional
	public void insert() {
		repository.insert(Article.DATA);
		// 添加@ElTransactional注解后，模拟发生异常后事务会自动回滚
		//throw EmbeddedLuceneException.of("insert article data into lucene occur exception!");
	}

	public List<Article> queryByKeyword(String queryKeyword, String queryField) {
		return repository.selectList(queryKeyword, queryField, null, 10, null, null);
	}
}
