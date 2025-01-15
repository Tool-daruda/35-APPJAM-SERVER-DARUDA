package com.daruda.darudaserver.domain.user.service;

import com.daruda.darudaserver.domain.community.dto.res.BoardRes;
import com.daruda.darudaserver.domain.community.entity.Board;
import com.daruda.darudaserver.domain.community.entity.BoardImage;
import com.daruda.darudaserver.domain.community.entity.BoardScrap;
import com.daruda.darudaserver.domain.community.repository.BoardImageRepository;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.community.repository.BoardScrapRepository;
import com.daruda.darudaserver.domain.tool.dto.res.ToolDtoGetRes;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolImage;
import com.daruda.darudaserver.domain.tool.entity.ToolScrap;
import com.daruda.darudaserver.domain.tool.repository.ToolImageRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.service.ToolService;
import com.daruda.darudaserver.domain.user.dto.response.*;
import com.daruda.darudaserver.domain.user.entity.UserEntity;
import com.daruda.darudaserver.domain.user.entity.enums.Positions;
import com.daruda.darudaserver.domain.user.entity.enums.SocialType;
import com.daruda.darudaserver.domain.user.repository.UserRepository;
import com.daruda.darudaserver.global.auth.jwt.provider.JwtTokenProvider;

import com.daruda.darudaserver.global.auth.jwt.service.TokenService;
import com.daruda.darudaserver.global.auth.security.UserAuthentication;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.BadRequestException;
import com.daruda.darudaserver.global.error.exception.BusinessException;
import com.daruda.darudaserver.global.error.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final BoardScrapRepository boardScrapRepository;
    private final ToolRepository toolRepository;
    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final ToolScrapRepository toolScrapRepository;
    private final ToolService toolService;

    public LoginResponse oAuthLogin(final UserInfo userInfo) {
        String email = userInfo.email();
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        //등록된 회원이 아닌 경우
        if (userEntity.isEmpty()) {
            return LoginResponse.of(false,email);
        } else { //등록된 회원인 경우
            Long userId = userEntity.get().getId();
            UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

            //토큰 생성 및 refreshToken db에 저장
            String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userAuthentication);
            tokenService.saveRefreshtoken(userId,refreshToken);
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
        tokenService.saveRefreshtoken(userId,refreshToken);

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
        verifyUserIdWithStoredToken(userId,requestToken);

        UserAuthentication userAuthentication = UserAuthentication.createUserAuthentication(userId);

        //새 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userAuthentication);

        JwtTokenResponse jwtTokenResponse = JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(requestToken)
                .build();
        return jwtTokenResponse;


    }
    public FavoriteToolsResponse getFavoriteTools(Long userId, Pageable pageable){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<ToolScrap> toolScrapPage = toolScrapRepository.findAllByUserId(userId,pageable);
        log.info("툴 스크랩을 레포지토리에서 가지고 옵니다");

        List<ToolScrap> toolScraps = toolScrapPage.getContent();
        log.info("리스트 형식으로 변환");
        List<Tool> tools = toolScraps.stream()
                .map(ToolScrap::getTool)
                .toList();
        log.info("tool로 변환");

        List<ToolDtoGetRes> toolDtoGetResList = tools.stream()
                .map(tool->ToolDtoGetRes.from(tool, toolService.convertToKeywordRes(tool)))
                .toList();
        log.info("DTO로 변환");

        PagenationDto pagenationDto = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(), toolScrapPage.getTotalPages());

        FavoriteToolsResponse favoriteToolsResponse = FavoriteToolsResponse.of(toolDtoGetResList, pagenationDto);

        return favoriteToolsResponse;
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

    public FavoriteBoardsRetrieveResponse getFavoriteBoards(Long userId, Pageable pageable){
        validateUser(userId);

        Page<BoardScrap> boardScraps = boardScrapRepository.findAllByUserId(userId, pageable);
        List<FavoriteBoardsResponse> favoriteBoardsResponses = boardScraps.getContent().stream()
                .map(BoardScrap::getBoard)
                .map(board -> FavoriteBoardsResponse.builder()
                        .boardId(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .updatedAt(board.getUpdatedAt())
                        .toolName(getTool(board.getTool().getToolId()).getToolMainName())
                        .toolLogo(getTool(board.getTool().getToolId()).getToolLogo())
                        .build())
                .toList();
        PagenationDto pageInfo = PagenationDto.of(pageable.getPageNumber(), pageable.getPageSize(), boardScraps.getTotalPages());

        FavoriteBoardsRetrieveResponse favoriteBoardsRetrieveResponse = new FavoriteBoardsRetrieveResponse(userId, favoriteBoardsResponses, pageInfo);

        return favoriteBoardsRetrieveResponse;
    }



    private void verifyUserIdWithStoredToken(final Long userId, final String refreshToken){
        Long storedUserId = tokenService.findIdByRefreshToken(refreshToken);

        if(!storedUserId.equals(userId)){
            throw new BadRequestException(ErrorCode.REFRESH_TOKEN_USER_ID_MISMATCH_ERROR);
        }
    }

    private Tool getTool(final Long toolId){
        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(()-> new NotFoundException(ErrorCode.DATA_NOT_FOUND));

        return tool;
    }

    public void validateUser(Long userId){
        userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }



}
