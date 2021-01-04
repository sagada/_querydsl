package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void searchTest() {
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 38, teamB);
        Member memberD = new Member("memberD", 13, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setTeamName("A");


        List<MemberTeamDto> search = memberRepository.search(memberSearchCondition);
        search.forEach(System.out::println);
    }

    @Test
    public void simplePagingTest()
    {
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 38, teamB);
        Member memberD = new Member("memberD", 13, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> search = memberRepository.searchPageSimple(memberSearchCondition, pageRequest);

        assertThat(search.getSize()).isEqualTo(3);
        assertThat(search.getContent()).extracting("username").containsExactly("memberA","memberB","memberC");

    }

    @Test
    public void complexPagingTest()
    {
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 38, teamB);
        Member memberD = new Member("memberD", 13, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> search = memberRepository.searchPageComplex(memberSearchCondition, pageRequest);

        assertThat(search.getSize()).isEqualTo(3);
        assertThat(search.getContent()).extracting("username").containsExactly("memberA","memberB","memberC");

    }

    @Test
    public void querydslPredicateExecutorTest()
    {
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 38, teamB);
        Member memberD = new Member("memberD", 13, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        QMember member = QMember.member;
        Iterable<Member> members = memberRepository.findAll(member.age.between(5, 40).and(member.username.eq("memberA")));

        for (Member member1 : members)
        {
            System.out.println("memberA :" + member1);
        }
    }
}
