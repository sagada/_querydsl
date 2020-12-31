package study.querydsl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QUserDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QueryDslSampleTest {

    @Autowired
    EntityManager entityManager;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void init() {
        jpaQueryFactory = new JPAQueryFactory(entityManager);

        Team teamA = new Team("A");
        Team teamB = new Team("B");

        entityManager.persist(teamA);
        entityManager.persist(teamB);

        entityManager.persist(new Member("T", 10));
        entityManager.persist(new Member("B", 20));
        entityManager.persist(new Member("E", 30));
        entityManager.persist(new Member("Z", 15));
        entityManager.persist(new Member("T", 17));
        entityManager.persist(new Member("S", 12));
        entityManager.persist(new Member("P", 19));
        entityManager.persist(new Member("P", 19));
        entityManager.persist(new Member("M", 29));
        entityManager.persist(new Member("V", 25));


        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void oneTest() {
        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.gt(15))
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void quereyDtoTestSetter() {
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void quereyDtoTestField() {
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void quereyDtoTestConstructor() {
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void quereyUserDtoTestConstructor() {
        List<UserDto> result = jpaQueryFactory
                .select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void quereyUserDtoTestConstructor3() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = jpaQueryFactory
                .select(
                        Projections.fields(UserDto.class
                                , member.username.as("name"),
                                ExpressionUtils
                                        .as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age")
                        )
                )
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    public void queryTestFileds() {
        QMember memberSub = new QMember("memberSub");


        List<UserDto> result = jpaQueryFactory
                .select(
                        Projections.fields(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age")
                        )
                )
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println(userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection()
    {
        List<UserDto> result = jpaQueryFactory
                .select(new QUserDto(member.username, member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println(userDto);
        }
    }




}
