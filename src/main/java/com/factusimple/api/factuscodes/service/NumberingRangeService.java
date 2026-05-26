package com.factusimple.api.factuscodes.service;

import com.factusimple.api.factuscodes.dto.NumberingRangeDto;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.factus.client.FactusNumberingRangesClient;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NumberingRangeService {

    private final FactusNumberingRangesClient factusNumberingRangesClient;
    private final UserRepository userRepository;

    @Transactional
    public NumberingRangeDto getActive(UUID userId) {
        log.debug("Obteniendo rango de numeración activo para usuario: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return factusNumberingRangesClient.getFirstActiveRange(user);
    }
}
