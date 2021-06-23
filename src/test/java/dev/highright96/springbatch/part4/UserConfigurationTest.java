package dev.highright96.springbatch.part4;

import static org.assertj.core.api.Assertions.assertThat;

import dev.highright96.springbatch.part3.TestConfiguration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@SpringBatchTest
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class})
class UserConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    void test() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        int size = userRepository.findAllByUpdateDate(LocalDate.now()).size();

        int sum = jobExecution.getStepExecutions().stream()
            .filter(x -> x.getStepName().equals("userLevelUpStep")).mapToInt(
                StepExecution::getWriteCount).sum();

        assertThat(sum).isEqualTo(size).isEqualTo(300);
        assertThat(userRepository.count()).isEqualTo(400);
    }
}