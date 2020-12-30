package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
public class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testMember1()
    {
        Team teamA = new Team("A");
        Team teamB = new Team("B");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamA);
        Member memberC = new Member("memberC", 13, teamB);
        Member memberD = new Member("memberD", 13, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        em.flush(); // 영속성 컨텍스트에 있는 객체 db 업데이트
        em.clear(); // 영속성 컨텍스트 초기화 (캐시 다 날라감)

        List<Member> members  = em.createQuery("select m from Member m", Member.class).getResultList();
        for (Member member: members)
        {
            System.out.println("member = " + member);
            System.out.println("team = " + member.getTeam());
        }

    }

}
