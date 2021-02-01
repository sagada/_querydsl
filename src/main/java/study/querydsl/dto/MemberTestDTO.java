package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MemberTestDTO {
    private Long id;
    private String username;
    private int age;
    private String teamname;

    public MemberTestDTO(Long id, String username, int age, String teamname) {
        this.id = id;
        this.username = username;
        this.age = age;
        this.teamname = teamname;
    }
}
