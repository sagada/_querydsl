package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }


    public void save(Member member) {
        entityManager.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = entityManager.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return entityManager.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByUsername(String username) {
        return entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCondition) {

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(memberSearchCondition.getUsername())) {
            builder.and(member.username.eq(memberSearchCondition.getUsername()));
        }

        if (StringUtils.hasText(memberSearchCondition.getTeamName())) {
            builder.and(team.name.eq(memberSearchCondition.getTeamName()));
        }

        if (memberSearchCondition.getAgeGoe() != null) {
            builder.and(member.age.goe(memberSearchCondition.getAgeGoe()));
        }

        if (memberSearchCondition.getAgeLoe() != null) {
            builder.and(member.age.loe(memberSearchCondition.getAgeLoe()));
        }

        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCondition searchCondition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(searchCondition.getUsername()),
                        teamNameEq(searchCondition.getTeamName()),
                        ageGoe(searchCondition.getAgeGoe()),
                        ageLoe(searchCondition.getAgeLoe()))
                .fetch();
    }

    public List<Member> searchEntity(MemberSearchCondition searchCondition) {
        return jpaQueryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        userNameEq(searchCondition.getUsername()),
                        teamNameEq(searchCondition.getTeamName()),
                        ageGoe(searchCondition.getAgeGoe()),
                        ageLoe(searchCondition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
        return ageGoe(ageLoe).and(ageLoe(ageLoe));
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression userNameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }
}
