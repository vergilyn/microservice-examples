package com.vergilyn.examples.alibaba.sentinel;

import java.time.LocalTime;
import java.util.List;

import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.google.common.collect.Lists;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 *
 * @author vergilyn
 * @since 2021-11-10
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RuleMockitoTests {
	private static final String RESOURCE = RuleMockitoTests.class.getName();

	private static final int QPS_LIMIT = 4;

	@Spy
	private Handler handler;

	@BeforeAll
	public void beforeAll(){
		MockitoAnnotations.openMocks(this);

		// 这种写法是满足期望的
		// loadRules();
	}

	@RepeatedTest(10)
	public void test(RepetitionInfo repetitionInfo){
		handler.request(repetitionInfo.getCurrentRepetition());
	}

	public static class Handler{

		static {
			loadRules();
		}

		public Handler() {
			// loadRules();
		}

		public void request(Integer index){
			if (SphO.entry(RESOURCE)){
				// 被保护的逻辑
				System.out.printf("[SphO][%s][%d] >>>> entry. \n", LocalTime.now(), index);

				// SphO.exit();
			}else {
				// 处理被流控的逻辑
				System.out.printf("[SphO][%s][%d] >>>> exceeded. \n", LocalTime.now(), index);
			}
		}
	}

	private static void loadRules(){
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

		System.out.println("loadRules >>>> " + FlowRuleManager.getRules().size());
	}
}
