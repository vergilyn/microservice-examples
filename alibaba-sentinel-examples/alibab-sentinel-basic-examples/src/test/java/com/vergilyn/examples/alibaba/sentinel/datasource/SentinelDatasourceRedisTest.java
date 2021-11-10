package com.vergilyn.examples.alibaba.sentinel.datasource;

import java.util.List;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInstance;

/**
 *
 * @author vergilyn
 * @since 2021-11-10
 *
 * @see <a href="https://sentinelguard.io/zh-cn/docs/dynamic-rule-configuration.html">动态规则扩展</a>
 */
@SuppressWarnings("JavadocReference")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SentinelDatasourceRedisTest {
	private static final String RESOURCE = SentinelDatasourceRedisTest.class.getSimpleName();

	/**
	 * redis > [String] `sentinel:rule:flow:SentinelDatasourceRedisTest
	 * <pre>
	 *   type: String
	 *   key: sentinel:rule:flow:SentinelDatasourceRedisTest
	 *   value:
	 *      [{"clusterMode":false,"controlBehavior":0,"count":4.0,"grade":1,"limitApp":"default","maxQueueingTimeMs":500,
	 *          "resource":"SentinelDatasourceRedisTest","strategy":0,"warmUpPeriodSec":10}]
	 * </pre>
	 *
	 * sentinel-redis 在{@linkplain RedisDataSource#RedisDataSource(RedisConnectionConfig, String, String, Converter)} 中会
	 * {@linkplain RedisDataSource#loadInitialConfig()}去 读取1次 redis，
	 * 将redis中的 FlowRule 通过 {@linkplain FlowRuleManager#loadRules(List)} 加载到 sentinel。
	 * 后续，是通过 redis channel机制更新。
	 */
	@BeforeAll
	public void beforeAll(){
		// 被 redis 中的 值覆盖！
		loadRules();

		Converter<String, List<FlowRule>> qpsRule = (source) -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});

		RedisConnectionConfig config = RedisConnectionConfig.builder()
				.withHost("127.0.0.1")
				.withPort(6379)
				.build();

		String sentinelAppName = SentinelConfig.getAppName();

		String appName = this.getClass().getSimpleName();

		// 构造函数中会 读取 `ruleKey`的值 通过`FlowRuleManager.loadRules()` 加载到内存规则
		ReadableDataSource<String, List<FlowRule>> redisDataSource = new RedisDataSource(config,
		                                                                                 "sentinel:rule:flow:" + appName,
		                                                                                 "sentinel:rule:flow:channel:" + appName,
		                                                                                 qpsRule);
		FlowRuleManager.register2Property(redisDataSource.getProperty());

		// 从 redis 获取 rules
		List<FlowRule> rules = FlowRuleManager.getRules();
		System.out.println("[vergilyn] rules >>>> " + JSON.toJSONString(rules, true));

		// 覆盖 redis加载的FlowRule， 但不会回写redis，所以可能之后被redis-channel 更新回redis的值。
		// loadRules();
	}

	public static void loadRules(){
		// 配置规则.
		List<FlowRule> rules = Lists.newArrayList();
		FlowRule rule = new FlowRule();
		rule.setResource(RESOURCE);
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		// Set limit QPS to X.
		rule.setCount(1);
		rules.add(rule);

		// 核心
		FlowRuleManager.loadRules(rules);
	}


	@RepeatedTest(10)
	public void sph(RepetitionInfo repetitionInfo){
		final List<FlowRule> rules = FlowRuleManager.getRules();

		boolean hasConfig = false;
		for (FlowRule rule : rules) {
			if (RESOURCE.equals(rule.getResource())){
				hasConfig = true;
				System.out.printf("[vergilyn][%s] resource: %s, qps-count: %s \n",
				                  repetitionInfo.getCurrentRepetition(), RESOURCE, rule.getCount());
			}
		}

		if (!hasConfig){
			System.out.printf("[vergilyn][%s] rules not contains resource(%s) \n",
			                  repetitionInfo.getCurrentRepetition(), RESOURCE);
		}
	}
}
