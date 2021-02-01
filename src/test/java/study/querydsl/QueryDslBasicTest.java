package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before() {
        jpaQueryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 15, teamB);
        Member memberD = new Member("memberD", 15, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
    }

    @Test
    public void firstJPQL() {
        // member1 을 찾자
        Member m = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "memberA")
                .getSingleResult();

        assertThat(m.getUsername()).isEqualTo("memberA");
    }

    @Test
    public void queryDsl() {
        QMember m = new QMember("m");

        Member findMember = jpaQueryFactory.select(m)
                .from(m)
                .where(m.username.eq("memberA"))
                .fetchOne();

        if (findMember != null) {
            assertThat(findMember.getUsername()).isEqualTo("memberA");
        }
    }

    @Test
    public void queryDsl2() {
        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("memberA"))
                .fetchOne();

        if (findMember != null) {
            assertThat(findMember.getUsername()).isEqualTo("memberA");
        }
    }

    @Test
    public void search() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("memberA").and(member.age.eq(10)))
                .fetchOne();

        if (findMember != null) {
            assertThat(findMember.getUsername()).isEqualTo("memberA");
        }
    }

    @Test
    public void search2() {
        List<Member> findMembers = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.between(10, 30))
                .fetch();

        findMembers.forEach(System.out::println);
    }

    @Test
    public void resultFetch() {
        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(member)
                .offset(1)
                .limit(2)
                .fetchResults();

        long count = memberQueryResults.getTotal();
        long offset = memberQueryResults.getOffset();
        long limit = memberQueryResults.getLimit();

        System.out.println("offset = " + offset);
        System.out.println("limit = " + limit);
        System.out.println("count = " + count);
        List<Member> contents = memberQueryResults.getResults();
    }

    /*
        1. 나이 내림차순(desc)
        2. 회원 이름 올림차순(asc)
        3. 2에서 회원 이름이 없으면 마지막에 출력
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = jpaQueryFactory.selectFrom(member)
                .where(member.age.eq(100)).orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void tes11t() {

        em.persist(new Member(null, 5));
        em.persist(new Member("member5", 12));
        em.persist(new Member("member6", 7));

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.lt(10))
                .orderBy(member.age.desc())
                .fetch();

        result.forEach(s -> System.out.println("member = " + s));
    }

    @Test
    public void tupleQueryTest() {
        em.persist(new Member("C", 51));
        em.persist(new Member("member5", 12));
        em.persist(new Member("member6", 78));
        em.persist(new Member("A", 56));
        em.persist(new Member("memberT", 12));
        em.persist(new Member("member6", 7));
        em.persist(new Member("CCC", 25));
        em.persist(new Member("memberF", 12));
        em.persist(new Member("member6", 17));

        List<Tuple> result = jpaQueryFactory.select(QMember.member.username, QMember.member.age)
                .from(QMember.member)
                .where(QMember.member.username.contains("member"))
                .orderBy(QMember.member.age.desc())
                .fetch();

        List<Member> convert = result.stream().map(m -> new Member(m.get(member.username), m.get(member.age))).collect(Collectors.toList());
        convert.forEach(s -> System.out.println("member = " + s));

    }

    @Test
    public void paging1() {
        QueryResults<Member> members = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(members.getTotal()).isEqualTo(4);
        assertThat(members.getResults().size()).isEqualTo(2);
        assertThat(members.getOffset()).isEqualTo(1);
        assertThat(members.getLimit()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result =
                jpaQueryFactory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                        .from(member)
                        .fetch();

        Tuple tuple = result.get(0);
        assertThat(result.size()).isEqualTo(1);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(50);
        assertThat(tuple.get(member.age.max())).isEqualTo(15);
    }

    /*
        팀의 이름과 각 팀의 평균 연령을 구하라
     */
    @Test
    public void aggregation2() {
        List<Tuple> result = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();


        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("A");
        assertThat(teamA.get(member.age.avg())).isEqualTo(10);
        assertThat(teamB.get(team.name)).isEqualTo("B");
        assertThat(teamB.get(member.age.avg())).isEqualTo(15);
    }

    @Test
    public void join1() {
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("A")).fetch();

        assertThat(result).extracting("username").containsExactly("memberA", "memberB");
    }

    /*
        세타 조인
        회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        em.persist(new Member("A"));
        em.persist(new Member("B"));
        em.persist(new Member("C"));

        List<Member> members = jpaQueryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(members).extracting("username").containsExactly("A", "B");
    }

    /*
     * 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인, 회원은 모두 조회
     * JPQL : select m, t from Member m left join m.team t on t.name = 'A'
     */
    @Test
    public void join_on_filter() {
        em.persist(new Member("A"));
        em.persist(new Member("B"));
        em.persist(new Member("C"));

        // on 절의 조건이 다름 ...
        List<Tuple> tuples = jpaQueryFactory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("A"))
                .fetch();

        // 관계없는 테이블 막 조인
        List<Tuple> tuples2 = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple1 = " + tuple);
        }

        System.out.println("#######");

        for (Tuple tuple : tuples2) {
            System.out.println("tuple2 = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetch_join_test() {
        em.flush();
        em.clear();

        Member m = jpaQueryFactory.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("memberA"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(m.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");
        List<Member> m = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(m).extracting("age").contains(15);
    }

    @Test
    public void subQuery2() {
        QMember memberSub = new QMember("memberSub");
        List<Member> m = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(m).extracting("age").contains(15);
    }

    @Test
    public void whereSubQueryInTest() {
        QMember memberSub = new QMember("memberSub");
        List<Member> m = jpaQueryFactory.selectFrom(QMember.member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(m.size()).isEqualTo(2);
        assertThat(m).extracting("age").contains(15);
    }

    @Test
    public void selectSubQueryTest() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = jpaQueryFactory
                .select(member.username, select(memberSub.age.avg()).from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void whenTest() {
        List<String> result = jpaQueryFactory.select(
                member.age.when(10).then("10살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String m : result) {
            System.out.println(m);
        }
    }

    @Test
    public void whenTest2() {
        List<String> result = jpaQueryFactory.select(new CaseBuilder().when(member.age.between(0, 10)).then("0~10살").otherwise("기타")).from(member).fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }
}
