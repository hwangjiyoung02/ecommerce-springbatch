package jiyoung.ecommerce.batch.jobconfig.product.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import jiyoung.ecommerce.batch.domain.product.Product;
import jiyoung.ecommerce.batch.jobconfig.BaseBatchIntegrationTest;
import jiyoung.ecommerce.batch.service.product.ProductService;
import jiyoung.ecommerce.batch.util.DateTimeUtils;
import jiyoung.ecommerce.batch.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

/// *
// * 잡 이름을 프로퍼티로 그리고 통합 테스트니까
//  다운로드 데이터가 db에 들어가 있어야되기 떄문에 프로덕트를 저장시키는 코드가 팔요
//  * 저장한후 저장된 걸 읽어서 배치 처리가 되면 csv file에 써야함 -> 임시 파일
//  *
//  * job 파라미터 지정-> 아웃풋 파일 path지정 ,->프로덕트 다운로드잡을 launcher에 등록->job parameter를 매개변수로 받아 잡을 실행시킴 ->assert구문으로 테크->outputFile 기대값(파일로)
// * */
@TestPropertySource(properties = {"spring.batch.job.name=productDownloadJob"})
class ProductDownloadJobConfigurationTest extends BaseBatchIntegrationTest {

  @Value("classpath:/data/products_downloaded_expected.csv")
  private Resource expectedResource;
  @Autowired
  private ProductService productService;
  File outputFile;

  @Test
  public void testJob(@Autowired Job productDownloadJob) throws Exception {
    saveProducts();
    outputFile = FileUtils.createTempFile("products_downloaded", ".csv");
    JobParameters jobParameters = jobParameters();
    jobLauncherTestUtils.setJob(productDownloadJob);

    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    assertAll(() -> assertThat(Files.readString(Path.of(outputFile.getPath()))).isEqualTo(
            Files.readString(Path.of(expectedResource.getFile().getPath()))),
        () -> assertJobCompleted(jobExecution));
  }

  private void saveProducts() {
    productService.save(
        Product.of("1", 1L, "식품", "햇반", LocalDate.of(2023, 7, 4),
            LocalDate.of(2026, 5, 28), "AVAILABLE", "아모레퍼시픽1",
            "나이키코리아1", 25154, 439,
            DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404"),
            DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404")));
    productService.save(
        Product.of("2", 2L, "식품", "햇반", LocalDate.of(2023, 7, 4),
            LocalDate.of(2026, 5, 28), " AVAILABLE", "아모레퍼시픽2",
            "나이키코리아2", 25154, 439,
            DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404"),
            DateTimeUtils.toLocalDateTime("2024-09-19 14:24:41.404")));
  }


  private JobParameters jobParameters() {
    return new JobParametersBuilder()
        .addJobParameter("outputFilePath",
            new JobParameter<>(outputFile.getPath(), String.class, false))
        .addJobParameter("gridSize",
            new JobParameter<>(2, Integer.class, false))
        .toJobParameters();
  }


}