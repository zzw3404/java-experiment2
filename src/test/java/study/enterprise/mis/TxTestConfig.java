package study.enterprise.mis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TxScenario.class)
class TxTestConfig {
}
