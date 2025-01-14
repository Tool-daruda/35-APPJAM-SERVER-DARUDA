package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.user.dto.response.*;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtValidationType;
import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import com.daruda.darudaserver.global.error.exception.UnauhtorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    public LoginResponse oAuthLogin(final UserInfo userInfo) {
        String email = userInfo.email();
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        //등록된 회원이 아닌 경우
        if (userEntity.isEmpty()) {
            return LoginResponse.of(false,email);
        } else { //등록된 회원인 경우
            UserEntity user = userEntity.get();
            Long userId = userEntity.get().getId();
            UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

            //토큰 생성 및 refreshToken db에 저장
            String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);
            tokenService.saveRefreshtoken(user,refreshToken);
            JwtTokenResponse jwtTokenResponse = JwtTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return LoginResponse.of(true, jwtTokenResponse);
        }
    }

    @Transactional
    public SignUpSuccessResponse createUser(final String email, final String nickname, final String positions){
        if(userRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .positions(Positions.fromString(positions))
                .build();
        Long userId = userRepository.save(userEntity).getId();
        UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);
        String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);
        tokenService.saveRefreshtoken(userEntity,refreshToken);

        JwtTokenResponse jwtTokenResponse = JwtTokenResponse.of(accessToken,refreshToken);

        return SignUpSuccessResponse.of(nickname,positions,email,jwtTokenResponse);
    }

    public Long deleteUser(final Long userId){
        tokenService.deleteRefreshToken(userId);

        return userId;
    }

    public boolean isDuplicated(final String nickname){
        return userRepository.existsByNickname(nickname);
    }

    public JwtTokenResponse reissueToken(Long userId){
        String requestToken = tokenService.getRefreshTokenByUserId(userId);
        UserEntity userEntity = userRepository.findById(userId)
                        .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
        verifyUserIdWithStoredToken(userEntity,requestToken);

        UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

        //새 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);

        JwtTokenResponse jwtTokenResponse = JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(requestToken)
                .build();
        return jwtTokenResponse;


    }

    public UpdateMyResponse updateMy(Long userId, String nickname, String positions){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));
        if(isDuplicated(nickname)){
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }
        if(nickname == null){
            userEntity.updatePositions(Positions.fromString(positions));
            return UpdateMyResponse.of(userEntity.getNickname(),positions);
        }
        if(positions == null){
            userEntity.updateNickname(nickname);
            return UpdateMyResponse.of(nickname, userEntity.getPositions().getName());
        }

        userEntity.updateNickname(nickname);
        userEntity.updatePositions(Positions.fromString(positions));
        return UpdateMyResponse.of(nickname,positions);
    }

    public void getFavoriteTools(Long userId, int pageNo, String criteria){
        Pageable pageable = PageRequest.of(pageNo,10, Sort.by(Sort.Direction.DESC, criteria));
    }


    private void verifyUserIdWithStoredToken(final UserEntity userEntity, final String refreshToken){
        Long storedUserId = tokenService.findIdByRefreshToken(refreshToken);

        if(!storedUserId.equals(userEntity.getId())){
            throw new BadRequestException(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR);
        }
    }




}
