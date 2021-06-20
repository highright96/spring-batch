package dev.highright96.springbatch.part3;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@SpringBatchTest
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class,})
class SavePersonConfigurationTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    PersonRepository personRepository;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void test_step() {
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");
        assertThat(jobExecution.getStepExecutions().stream()
            .mapToInt(StepExecution::getWriteCount)
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(3);
    }

    @Test
    void test_allow_duplicate() throws Exception {
        //given
        JobParameters jobParameters = getJobParameters("false");

        //when
        JobExecution jobExecution = jobLauncherTestUtils
            .launchJob(jobParameters); // SavePersonConfiguration 에 설정된 Job 이 실행된다.

        //then
        assertThat(jobExecution.getStepExecutions().stream().mapToInt(
            StepExecution::getWriteCount) // ItemWrite 된 개수를 확인한다.
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(3);
    }

    @Test
    void test_not_allow_duplicate() throws Exception {
        //given
        JobParameters jobParameters = getJobParameters("true");

        //when
        JobExecution jobExecution = jobLauncherTestUtils
            .launchJob(jobParameters); // SavePersonConfiguration 에 설정된 Job 이 실행된다.

        //then
        assertThat(jobExecution.getStepExecutions().stream().mapToInt(
            StepExecution::getWriteCount) // ItemWrite 된 개수를 확인한다.
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(100);
    }

    private JobParameters getJobParameters(String s) {
        return new JobParametersBuilder()
            .addString("allow_duplicate", s)
            .toJobParameters();
    }
}