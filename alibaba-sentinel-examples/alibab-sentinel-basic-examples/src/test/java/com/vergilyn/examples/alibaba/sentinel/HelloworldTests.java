package com.vergilyn.examples.alibaba.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloworldTests {
	private static final String RESOURCE = "helloworld";

	private final static int QPS_LIMIT = 4;
	private static final AtomicInteger INDEX = new AtomicInteger(0);

	static {
		// 配置规则.
		List<FlowRule> rules = Lists.newArrayList();
		FlowRule rule = new FlowRule();
		rule.setResource(RESOURCE);
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		// Set limit QPS to X.
		rule.setCount(QPS_LIMIT);
		rules.add(rule);

		// 核心
		FlowRuleManager.loadRules(rules);
	}

	@BeforeEach
	public void beforeEach(){

	}

	/**
	 * <a href="https://sentinelguard.io/zh-cn/docs/quick-start.html">本地Demo</a>
	 */
	@RepeatedTest(10)
	@SneakyThrows
	public void code() {
		INDEX.incrementAndGet();

		// 1.5.0 版本开始可以直接利用 try-with-resources 特性
		try (Entry entry = SphU.entry(RESOURCE)) {
			// 被保护的逻辑
			System.out.printf("[SphO][%s][%d] >>>> helloworld. \n", LocalTime.now(), INDEX.get());

			// entry.exit();
		} catch (BlockException ex) {
			// 处理被流控的逻辑
			System.out.printf("[SphO][%s][%d] >>>> blocked. \n", LocalTime.now(), INDEX.get());
		}
	}

	@RepeatedTest(10)
	@SneakyThrows
	public void sphO() {
		INDEX.incrementAndGet();

		if (SphO.entry(RESOURCE)){
			// 被保护的逻辑
			System.out.printf("[SphO][%s][%d] >>>> helloworld. \n", LocalTime.now(), INDEX.get());

			// SphO.exit();
		}else {
			// 处理被流控的逻辑
			System.out.printf("[SphO][%s][%d] >>>> blocked. \n", LocalTime.now(), INDEX.get());
		}

		TimeUnit.MILLISECONDS.sleep(RandomUtils.nextLong(100, 200));
	}
}
