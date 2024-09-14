package com.yida.lucene.spring;

import com.yida.lucene.exception.EmbeddedLuceneException;
import com.yida.lucene.spring.annotation.ElTransactional;
import com.yida.lucene.transaction.ElTransactionDef;
import com.yida.lucene.transaction.ElTransactionTemplate;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author yida
 * @date 2024/09/02 13:44
 */
@Aspect
@RequiredArgsConstructor
public class ElTransactionAspect {

	private final ElTransactionTemplate txTemplate;

	@Around("@annotation(elTransactional) || @within(elTransactional)")
	public Object doTransaction(ProceedingJoinPoint joinPoint, ElTransactional elTransactional) {
		return txTemplate.execute(() -> {
			try {
				return joinPoint.proceed();
			} catch (Throwable e) {
				e.printStackTrace();
				throw EmbeddedLuceneException.of(e);
			}
		}, getDef(elTransactional));
	}

	private ElTransactionDef getDef(ElTransactional elTransactional) {
		return new ElTransactionDef(elTransactional.timeout(), elTransactional.rollbackFor());
	}
}
