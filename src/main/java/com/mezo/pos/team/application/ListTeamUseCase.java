package com.mezo.pos.team.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.team.domain.entity.TeamMember;
import com.mezo.pos.team.domain.port.TeamRepository;
import com.mezo.pos.team.infrastructure.web.dto.TeamMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListTeamUseCase {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> execute(UUID businessId) {
        List<TeamMember> members = teamRepository.findByBusinessId(businessId);

        return members.stream()
                .map(member -> {
                    String email = userRepository.findById(member.getUserId())
                            .map(user -> user.getEmail().getValue())
                            .orElse(null);
                    return TeamMemberResponse.fromEntity(member, email);
                })
                .toList();
    }
}
