package jiyoung.ecommerce.batch.jobconfig.product.upload;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import jiyoung.ecommerce.batch.jobconfig.BaseBatchIntegrationTest;
import jiyoung.ecommerce.batch.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

// 잡이 여러개인 경우 어떤 잡인지 이름을 넣어줘야함 기본 잡 실행을 비활성
@TestPropertySource(properties = {
    "spring.batch.job.name=productUploadJob"})
class ProductUploadJobConfigurationTest extends BaseBatchIntegrationTest {

  //joblaucherutil로 테스트할 수 있게 해줌
  @Autowired
  private JdbcTemplate jdbcTemplate;

//  @Autowired
//  public void setDataSource(DataSource dataSource) {
//    jdbcTemplate = new JdbcTemplate(dataSource);
//  }

  @Value("classpath:/data/products_for_upload.csv")
  private Resource input;

  @Autowired
  private ProductService productService;


  @Test
  void testJob(@Autowired Job productUploadJob) throws Exception {
    JobParameters jobParameters = jobParameters();
    System.out.println("Job Parameters: " + jobParameters);

    jobLauncherTestUtils.setJob(productUploadJob); // 실행할 잡 설정
    System.out.println("Product count: " + productService.countProducts());

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(
        jobParameters); // jobParameters를 넣어 실행

    assertAll(
        () -> assertThat(productService.countProducts()).isEqualTo(6),
        () -> assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode()));
  }

  private JobParameters jobParameters() throws IOException {
    return new JobParametersBuilder()
        .addString("inputFilePath", input.getFile().getPath()) // 경로를 String으로 전달
        .toJobParameters();
  }

}
//리눅스 명령어
//    head -n7 data/random_product.csv > products_for_upload.csv
//
//클래스패스로 파일접근 잡 주입받기잡 잡파라미터 넣어야함 파싱해서 프로젝트를 넣어줘야함->실행시킬 잡을 지정해줌- 파라미터를 넣어서 실행시킬 수 있고 결과는 jobexcution에 넣어줌
//    exitstatus를 하면 ㄴ완료됐는지 확인
//    jdbc 템플릿으로 결과가 6이 맞는지 확인하는 코드를 만듦
//프로젝트 쿼리를 수행하는 서비스를 만들겠음
