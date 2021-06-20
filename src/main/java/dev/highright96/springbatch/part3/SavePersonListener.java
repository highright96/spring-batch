package dev.highright96.springbatch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

@Slf4j
public class SavePersonListener {

    // 인터페이스 구현
    public static class SavePersonJobExecutionListener implements JobExecutionListener {

        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("beforeJob");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum();
            log.info("afterJob : {}", sum);
        }
    }

    // 애노테이션 구현
    public static class SavePersonAnnotationJobExecution {

        @BeforeJob
        public void beforeJob(JobExecution jobExecution) {
            log.info("annotationBeforeJob");
        }

        @AfterJob
        public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions().stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum();
            log.info("annotationAfterJob : {}", sum);
        }
    }

    public static class SavePersonAnnotationStepExecution {

        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            log.info("annotationBeforeStep");
        }

        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("annotationAfterStep : {}", stepExecution.getWriteCount());
            return stepExecution.getExitStatus();
        }
    }
}
