package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.TransactionScoped;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Transactional
@SpringBootTest
public class StudyTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before()
    {
        jpaQueryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 15, teamB);
        Member memberD = new Member("memberD", 15, teamB);
        Member memberE = new Member("memberE", 17, null);
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
        em.persist(memberE);
    }

    @Test
    public void joinTest()
    {
        List<String> result = jpaQueryFactory
                .select(member.username)
                .from(member)
                .join(member.team, team)
                .fetch();

        assertThat(result.size()).isEqualTo(4);

        List<Tuple> result2 = jpaQueryFactory
                .select(member.username, member.team.name)
                .from(member)
                .leftJoin(member.team, team)
                .orderBy(member.username.asc())
                .fetch();

        assertThat(result2.size()).isEqualTo(5);

        result2.forEach(s-> System.out.println("member = " + s.get(member.username) + "/ " +  s.get(member.team.name)));

        Tuple one = result2.get(0);

        assertThat(one.get(member.username)).isEqualTo("memberA");
    }

    @Test
    public void 세타조인테스트()
    {
        Team team2 = new Team("memberA");
        em.persist(team2);

        List<Member> list = jpaQueryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    public void 

}
