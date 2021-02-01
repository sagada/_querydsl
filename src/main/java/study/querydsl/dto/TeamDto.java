package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(of = {"teamId", "teamName"})

public class TeamDto {
    private Long teamId;
    private String teamName;

    @QueryProjection
    public TeamDto(Long teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
