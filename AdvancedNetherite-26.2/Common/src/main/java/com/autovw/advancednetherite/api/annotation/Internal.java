package com.autovw.advancednetherite.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 클래스·메서드·필드 등을 <b>내부용</b>으로 표시하는 어노테이션.
 * 실행 시에는 <b>남지 않으며</b>, 코드가 내부용임을 표시하는 데만 쓴다.
 * 이 어노테이션이 붙은 코드는 사용하거나 {@link Override}하지 않는 것이 좋고, 배포 버전마다 바뀔 수 있다.
 * 내부용 코드의 대체 수단은 javadoc으로 안내할 수 있다.
 * @since 1.11.0
 * @author Autovw
 */
@Retention(RetentionPolicy.CLASS)
public @interface Internal
{
}
