package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    public void findDtoByQueryProjection() {
        List<UserDto> result = jpaQueryFactory
                .select(new QUserDto(member.username, member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println(userDto);
        }
    }

    @Test
    public void dynamicQuery_Boolean_Builder() {
        String usernameParam = "T";
        Integer ageParam = 10;

        List<Member> results = searchMember1(usernameParam, ageParam);
        List<Member> members = searchMember2(usernameParam, ageParam);

        assertThat(members.size()).isEqualTo(1);
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void dynamicQueryBooleanTest() {

    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();

        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return jpaQueryFactory.selectFrom(member)
                .where(builder)
                .fetch();
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(allEq(usernameParam, ageParam))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageParam) {
        if (ageParam == null) {
            return null;
        }
        return member.age.eq(ageParam);
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if (usernameParam == null) {
            return null;
        }
        return member.username.eq(usernameParam);
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    public void bulkUpdate() {
        long count = jpaQueryFactory
                .update(member)
                .set(member.username, "회원")
                .where(member.age.gt(15))
                .execute();

        // 초기화 해 버리기...
        entityManager.flush();
        entityManager.clear();

        List<Member> members = jpaQueryFactory.selectFrom(member)
                .where(member.age.gt(15))
                .fetch();

        for (Member member : members) {
            System.out.println("member = " + member);
        }
        assertThat(count).isEqualTo(7);
    }

    @Test
    public void bulkUpdateTest() {
        long execute = jpaQueryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    public void bulkDelete() {
        long ct = jpaQueryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

}
