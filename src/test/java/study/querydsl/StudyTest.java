package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberTestDTO;
import study.querydsl.dto.QTeamDto;
import study.querydsl.dto.TeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Transactional
@SpringBootTest
public class StudyTest {
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
        Member memberE = new Member("memberE", 10, null);
        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
        em.persist(memberE);
    }

    @Test
    public void joinTest() {
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

        result2.forEach(s -> System.out.println("member = " + s.get(member.username) + "/ " + s.get(member.team.name)));

        Tuple one = result2.get(0);

        assertThat(one.get(member.username)).isEqualTo("memberA");
    }

    @Test
    public void 세타조인테스트() {
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
    public void onJoinTest() {
        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .on(team.name.eq("A"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("result = " + tuple.get(member));
        }
    }

    @Test
    public void fetchTest() {
        em.flush();
        em.clear();

        List<Member> members = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("memberA"))
                .fetch();

        for (Member m : members) {
            System.out.println("result = " + m);
        }
    }

    @Test
    public void subQueryTest() {
        em.flush();
        em.clear();

        QMember subMember = new QMember("subMember");


        List<Member> members1 = jpaQueryFactory
                .selectFrom(member)
                .leftJoin(member.team, team).fetchJoin()
                .where(member.age.goe(select(subMember.age.min()).from(subMember))).fetch();

        assertThat(members1.size()).isEqualTo(5);
        assertThat(members1).extracting("username").contains("memberA");
    }

    @Test
    public void selectSubQuery() {
        QMember subMember = new QMember("subMember");
        List<Tuple> list = jpaQueryFactory
                .select(member.username, select(subMember.age.avg()).from(subMember))
                .from(member)
                .fetch();

        for (Tuple tuple : list) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(select(subMember.age.avg()).from(subMember)));
        }
    }

    @Test
    public void propertyAccessSetterDto() {
        List<MemberTestDTO> result =
                jpaQueryFactory.select(
                        Projections.bean(MemberTestDTO.class,
                                member.id, member.username, member.age, member.team.name.as("teamname")))
                        .from(member)
                        .join(member.team, team)
                        .fetch();

//        for (MemberTestDTO memberTestDTO : result)
//        {
//            System.out.println("result = " + memberTestDTO);
//        }
//
        List<MemberTestDTO> result2 =
                jpaQueryFactory
                        .select(Projections.fields(MemberTestDTO.class, member.id, member.username, member.age, member.team.name.as("teamname")))
                        .from(member)
                        .join(member.team, team)
                        .fetch();

        for (MemberTestDTO memberTestDTO : result2) {
            System.out.println("result = " + memberTestDTO);
        }
    }

    @Test
    public void propertyAccessSetterDto2() {
//        QTeamDto dto = QTeamDto.
        Map<Team, List<MemberDto>> members = jpaQueryFactory
                .select(Projections.constructor(TeamDto.class, team.id.as("teamId"), team.name.as("teamName")))
                .from(team)
                .innerJoin(team.memberList, member)
                .transform(groupBy(team).as(list(Projections.constructor(MemberDto.class, member.username, member.age))));

        members.forEach((key, value) -> System.out.println(key
                + " / " + value));
    }
}
