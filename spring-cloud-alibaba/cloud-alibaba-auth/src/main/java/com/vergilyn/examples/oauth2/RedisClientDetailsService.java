package com.vergilyn.examples.oauth2;

import java.util.List;

import javax.sql.DataSource;

import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.util.CollectionUtils;

public class RedisClientDetailsService extends JdbcClientDetailsService {
    /**
     * 缓存client的redis key，这里是hash结构存储
     */
    private static final String CACHE_HASH_OAUTH_CLIENT_DETAILS = "oauth_client_details";
    private Logger logger = LoggerFactory.getLogger(RedisClientDetailsService.class) ;
    private StringRedisTemplate redisTemplate ;

    public RedisClientDetailsService(DataSource dataSource, StringRedisTemplate redisTemplate) {
        super(dataSource);
        this.redisTemplate = redisTemplate;
    }

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
        ClientDetails clientDetails = getRedis(clientId);

        if (clientDetails == null) {
            clientDetails = cacheAndGetClient(clientId);
        }

        return clientDetails;
    }

    /**
     * 缓存client并返回client
     */
    private ClientDetails cacheAndGetClient(String clientId) {
        ClientDetails clientDetails = null ;
        try {
            clientDetails = super.loadClientByClientId(clientId);
            if (clientDetails != null) {
                writeRedis(clientDetails);

                logger.info("缓存clientId:{}, {}", clientId, clientDetails);
            }
        }catch (NoSuchClientException e){
            logger.info("clientId:{}, {}", clientId, clientId );
        }catch (InvalidClientException e) {
            e.printStackTrace();
        }

        return clientDetails;
    }

    @Override
    public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {
        super.updateClientDetails(clientDetails);
        cacheAndGetClient(clientDetails.getClientId());
    }

    @Override
    public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
        super.updateClientSecret(clientId, secret);
        cacheAndGetClient(clientId);
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        super.removeClientDetails(clientId);
        removeRedis(clientId);
    }

    /**
     * 将oauth_client_details全表写入redis
     */
    public void loadAllClientDetailsToRedis() {
        if (redisTemplate.hasKey(CACHE_HASH_OAUTH_CLIENT_DETAILS)) {
            return;
        }
        logger.info("将oauth_client_details全表刷入redis");

        List<ClientDetails> list = super.listClientDetails();
        if (CollectionUtils.isEmpty(list)) {
            logger.warn("oauth_client_details 为空！");
            return;
        }

        list.parallelStream().forEach(this::writeRedis);
    }

    private void removeRedis(String clientId) {
        redisTemplate.boundHashOps(CACHE_HASH_OAUTH_CLIENT_DETAILS).delete(clientId);
    }

    private void writeRedis(ClientDetails clientDetails){
        redisTemplate.boundHashOps(CACHE_HASH_OAUTH_CLIENT_DETAILS)
                .put(clientDetails.getClientId(), JSON.toJSONString(clientDetails));
    }

    private ClientDetails getRedis(String clientId){
        String value = (String) redisTemplate.boundHashOps(CACHE_HASH_OAUTH_CLIENT_DETAILS).get(clientId);
        return value == null || value.isEmpty() ? null : JSON.parseObject(value, BaseClientDetails.class);
    }
}
