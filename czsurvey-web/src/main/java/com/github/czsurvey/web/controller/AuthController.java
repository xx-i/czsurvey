package com.github.czsurvey.web.controller;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.github.czsurvey.extra.security.component.JwtAuthenticationFilter;
import com.github.czsurvey.extra.security.component.JwtTokenStore;
import com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationToken;
import com.github.czsurvey.project.entity.enums.WxQrcodeLoginStatus;
import com.github.czsurvey.project.request.LoginRequest;
import com.github.czsurvey.project.request.WxQrcodeScene;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.concurrent.TimeUnit;

import static com.github.czsurvey.common.costant.CommonConstant.WEB_CLIENT_BASE_URL;
import static com.github.czsurvey.extra.security.grant.WxQrcodeAuthenticationToken.AuthenticationType.AUTHENTICATION_CODE;
import static com.github.czsurvey.project.constant.AuthConstant.WX_LOGIN_CODE_KEY;
import static com.github.czsurvey.project.constant.AuthConstant.WX_LOGIN_TOKEN_KEY;
import static com.github.czsurvey.project.entity.enums.WxQrcodeSceneType.LOGIN;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenStore jwtTokenStore;

    private final WxMpService wxMpService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(@Validated @RequestBody LoginRequest loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        return authenticateToken(authenticationToken);
    }


    /**
     * 获取微信登录的二维码
     * @return 二维码
     */
    @GetMapping("/wx/login/qrcode")
    public QrcodeResponse wxLoginQrcode() {
        String uuid = IdUtil.simpleUUID();
        QrConfig qrConfig = new QrConfig(300, 300);

        String url = WEB_CLIENT_BASE_URL + "/wx/auth?login_token=" + uuid;
        qrConfig.setErrorCorrection(ErrorCorrectionLevel.H);
        String image = QrCodeUtil.generateAsBase64(url, qrConfig, ImgUtil.IMAGE_TYPE_PNG);
        String codeKey = WX_LOGIN_CODE_KEY + uuid;
        redisTemplate.opsForValue().set(codeKey, WxQrcodeLoginStatus.WAITING, 3 * 60, TimeUnit.SECONDS);
        log.debug("wx qrcode content: {}", url);
        return new QrcodeResponse(image, uuid);
    }

    /**
     * 微信登录扫码
     * @return 二维码状态
     */
    @PostMapping("/wx/login/scan/qrcode")
    public WxQrcodeLoginStatus wxLoginScanQrcode(@Validated @RequestBody WxLoginCodeRequest code) {
        String codeKey = WX_LOGIN_CODE_KEY + code.codeId();
        String status = (String) redisTemplate.opsForValue().get(codeKey);
        if (StrUtil.isEmpty(status)) {
            return WxQrcodeLoginStatus.EXPIRED;
        }
        if (status.equals(WxQrcodeLoginStatus.WAITING.name())) {
            redisTemplate.opsForValue().set(codeKey, WxQrcodeLoginStatus.SCANNED, 3 * 60, TimeUnit.SECONDS);
        }
        return WxQrcodeLoginStatus.SCANNED;
    }

    /**
     * 微信扫码登录
     * @param wxQrcodeLoginRequest 登录参数
     */
    @PostMapping("/wx/qrcode/login")
    public void wxQrcodeLogin(@Validated @RequestBody WxQrcodeLoginRequest wxQrcodeLoginRequest) {
        Authentication authentication = new WxQrcodeAuthenticationToken(AUTHENTICATION_CODE, wxQrcodeLoginRequest.authorizationCode());
        Authentication authenticate = authenticationManager.authenticate(authentication);
        String jwtToken = jwtTokenStore.createToken(authenticate);
        redisTemplate.opsForValue().set(WX_LOGIN_TOKEN_KEY + wxQrcodeLoginRequest.wxLoginCode(), jwtToken, 5 * 60, TimeUnit.SECONDS);
    }

    @PostMapping("/wx/web/login")
    public ResponseEntity<JwtTokenResponse> wxLogin(@Validated @RequestBody WxWebLoginRequest wxWebLoginRequest) {
        Authentication authenticationToken = new WxQrcodeAuthenticationToken(AUTHENTICATION_CODE, wxWebLoginRequest.authorizationCode());
        return authenticateToken(authenticationToken);
    }

    @GetMapping("/wx/login/token/status")
    public WxTokenResponse wxTokenStatus(@RequestParam String codeId) {
        String jwtToken = (String) redisTemplate.opsForValue().get(WX_LOGIN_TOKEN_KEY + codeId);
        if (StrUtil.isBlank(jwtToken)) {
            String status = (String) redisTemplate.opsForValue().get(WX_LOGIN_CODE_KEY + codeId);
            return status != null ? new WxTokenResponse(Enum.valueOf(WxQrcodeLoginStatus.class, status), null)
                : new WxTokenResponse(WxQrcodeLoginStatus.EXPIRED, null);
        }
        return new WxTokenResponse(WxQrcodeLoginStatus.SUCCESS, jwtToken);
    }


    /**
     * 微信登录（需要关注公众号）：二维码获取
     * @return 二维码信息
     */
    @GetMapping("/wx/publicAccount/login/qrcode")
    public QrcodeResponse wxPublicAccountLoginQrcode() throws WxErrorException {
        String uuid = IdUtil.simpleUUID();
        String sceneStr = new WxQrcodeScene(LOGIN, uuid).toSceneStr();
        redisTemplate.opsForValue().set(sceneStr, true, 3 * 60, TimeUnit.SECONDS);
        WxMpQrcodeService qrcodeService = wxMpService.getQrcodeService();
        WxMpQrCodeTicket ticket = qrcodeService.qrCodeCreateTmpTicket(sceneStr, 3 * 60);
        String url = qrcodeService.qrCodePictureUrl(ticket.getTicket());
        return new QrcodeResponse(url, uuid);
    }

    /**
     * 微信登录（需要关注公众号）：检测二维码状态
     * @param codeId 二维码Id
     * @return token以及token状态
     */
    @GetMapping("/wx/publicAccount/login/token/status")
    public WxTokenResponse wxPublicAccountTokenStatus(@RequestParam String codeId) {
        String jwtToken = (String) redisTemplate.opsForValue().get(WX_LOGIN_TOKEN_KEY + codeId);
        if (StrUtil.isBlank(jwtToken)) {
            Object flag = redisTemplate.opsForValue().get(new WxQrcodeScene(LOGIN, codeId).toSceneStr());
            return flag == null ? new WxTokenResponse(WxQrcodeLoginStatus.EXPIRED, null) : new WxTokenResponse(WxQrcodeLoginStatus.WAITING, null);
        }
        return new WxTokenResponse(WxQrcodeLoginStatus.SUCCESS, jwtToken);
    }

    private ResponseEntity<JwtTokenResponse> authenticateToken(Authentication authenticationToken) {
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        String jwt = jwtTokenStore.createToken(authenticate);
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtAuthenticationFilter.AUTHORIZATION_TOKEN_HEADER, JwtAuthenticationFilter.BEARER_TOKEN_PREFIX + jwt);
        return new ResponseEntity<>(new JwtTokenResponse(jwt), headers, HttpStatus.OK);
    }

    record WxQrcodeLoginRequest(@NotBlank String authorizationCode, @NotBlank String wxLoginCode) {}

    record WxWebLoginRequest(@NotBlank String authorizationCode) {}

    record WxLoginCodeRequest(@NotBlank String codeId) {}

    record JwtTokenResponse(String token) {}

    record QrcodeResponse(String image, String codeId) {}

    record WxTokenResponse(WxQrcodeLoginStatus status, String token) {}
}
