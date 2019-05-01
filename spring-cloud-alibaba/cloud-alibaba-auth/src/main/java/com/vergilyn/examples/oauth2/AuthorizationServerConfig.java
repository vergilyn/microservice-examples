package com.vergilyn.examples.oauth2;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.vergilyn.examples.properties.PermitUrlProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

/**
 * 声明一个授权服务器只需要继承 AuthorizationServerConfigurerAdapter，添加
 * {@linkplain EnableAuthorizationServer @EnableAuthorizationServer} 注解。
 * 这个注解告诉 Spring 这个应用是 OAuth2 的认证中心。
 * <p/>
 * 并且重写如下三个方法：
 * - ClientDetailsServiceConfigurer: 这个configurer定义了客户端细节服务。客户详细信息可以被初始化。<br/>
 * - AuthorizationServerSecurityConfigurer: 在令牌端点上定义了安全约束。<br/>
 * - AuthorizationServerEndpointsConfigurer: 定义了授权和令牌端点和令牌服务。<br/>
 *
 */
@Configuration
public class AuthorizationServerConfig{

    /**
     * 用户验证信息的保存策略: db/redis等
     */
    @Bean
    public TokenStore tokenStore(RedisConnectionFactory redisConnectionFactory){
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * ClientDetails实现
     */
    @Bean
    public ClientDetailsService clientDetailsService(DataSource dataSource, StringRedisTemplate stringRedisTemplate) {
        return new RedisClientDetailsService(dataSource, stringRedisTemplate);
    }

    @Configuration
    @EnableAuthorizationServer
    @AutoConfigureAfter(AuthorizationServerEndpointsConfigurer.class)
    static class CustomAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
        @Autowired
        private ClientDetailsService clientDetailsService;
        @Autowired
        private UserDetailsService userDetailsService;
        @Autowired
        private TokenStore tokenStore;
        @Autowired
        private WebResponseExceptionTranslator webResponseExceptionTranslator;
        @Autowired(required = false)
        private RandomValueAuthorizationCodeServices authorizationCodeServices;
        /** 注入authenticationManager 来支持 password grant-type */
        @Autowired
        private AuthenticationManager authenticationManager;

        /**
         * 配置身份认证器，配置认证方式，TokenStore，TokenGranter，OAuth2RequestFactory
         */
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            DefaultTokenServices tokenServices = new DefaultTokenServices();
            tokenServices.setTokenStore(tokenStore);
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setClientDetailsService(clientDetailsService);
            tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
            tokenServices.setAccessTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(30));

            endpoints.tokenServices(tokenServices)
                    // .accessTokenConverter()
                    .authenticationManager(authenticationManager)
                    .userDetailsService(userDetailsService)
                    .authorizationCodeServices(authorizationCodeServices)
                    .exceptionTranslator(webResponseExceptionTranslator);
        }

        /**
         *
         * 这个方法主要是用于校验注册的第三方客户端的信息，可以存储在数据库中，默认方式是存储在内存中，如下所示，注释掉的代码即为内存中存储的方式
         */
        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception{
            clients.withClientDetails(clientDetailsService);
            ((RedisClientDetailsService)clientDetailsService).loadAllClientDetailsToRedis();
        }

        /**
         * 允许表单验证，浏览器直接发送post请求即可获取token
         */
        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            // url: /oauth/token_key,exposes
            security.tokenKeyAccess("permitAll()")
                    // public key for token verification if using JWT tokens. url:/oauth/check_token
                    .checkTokenAccess("isAuthenticated()")
                    // allow check token
                    .allowFormAuthenticationForClients();
        }
    }


    @Configuration
    @EnableResourceServer
    @EnableConfigurationProperties(PermitUrlProperties.class)
    static class ResourceServerConfig extends ResourceServerConfigurerAdapter {

        @Autowired
        private PermitUrlProperties permitUrlProperties;

        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/health");
            web.ignoring().antMatchers("/oauth/user/token");
            web.ignoring().antMatchers("/oauth/client/token");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatcher(
                    /** 判断来源请求是否包含oauth2授权信息 */
                    new RequestMatcher() {
                        private AntPathMatcher antPathMatcher = new AntPathMatcher();

                        @Override
                        public boolean matches(HttpServletRequest request) {
                            // 请求参数中包含access_token参数
                            if (request.getParameter(OAuth2AccessToken.ACCESS_TOKEN) != null) {
                                return true;
                            }

                            // 头部的Authorization值以Bearer开头
                            String auth = request.getHeader("Authorization");
                            if (auth != null && auth.startsWith(OAuth2AccessToken.BEARER_TYPE)) {
                                return true;
                            }
                            if (antPathMatcher.match(request.getRequestURI(), "/oauth/userinfo")) {
                                return true;
                            }
                            if (antPathMatcher.match(request.getRequestURI(), "/oauth/remove/token")) {
                                return true;
                            }
                            if (antPathMatcher.match(request.getRequestURI(), "/oauth/get/token")) {
                                return true;
                            }
                            if (antPathMatcher.match(request.getRequestURI(), "/oauth/refresh/token")) {
                                return true;
                            }

                            if (antPathMatcher.match(request.getRequestURI(), "/oauth/token/list")) {
                                return true;
                            }

                            if (antPathMatcher.match("/clients/**", request.getRequestURI())) {
                                return true;
                            }

                            if (antPathMatcher.match("/services/**", request.getRequestURI())) {
                                return true;
                            }
                            if (antPathMatcher.match("/redis/**", request.getRequestURI())) {
                                return true;
                            }
                            return false;
                        }
                    }

            ).authorizeRequests().antMatchers(permitUrlProperties.getIgnored()).permitAll().anyRequest()
                    .authenticated();
        }

    }

}
