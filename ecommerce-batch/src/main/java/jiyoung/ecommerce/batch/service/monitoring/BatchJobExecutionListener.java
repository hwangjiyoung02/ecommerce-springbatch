package jiyoung.ecommerce.batch.service.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/*
 * 리스너로 동작하려면 JobExecutionListener를 해줘야함
 *
 * */

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchJobExecutionListener implements JobExecutionListener {

  @Override
  public void beforeJob(JobExecution jobExecution) {
    log.info("listener:before Job");
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    log.info("listener:after Job");
  }
}
