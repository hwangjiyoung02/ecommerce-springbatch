package jiyoung.ecommerce.batch.jobconfig.product.upload;

import javax.sql.DataSource;
import jiyoung.ecommerce.batch.domain.product.Product;
import jiyoung.ecommerce.batch.dto.product.ProductUploadCsvRow;
import jiyoung.ecommerce.batch.util.ReflectionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ProductUploadJobConfiguration {

  @Bean//레포 주입받아야함 -> 실행시킬 스텝을 만들어야함, 스텝도 빈을 넣어줘야함,잡이 완료됐는지 확인하기 위해 listener(모니터링을 위한 용도)로그를 찍어서
  //잡안에 스텝 하나가 실행되는거고 로컬 csv파일을 읽은 다음 reader - processor(product로 변환)-> writer를 통해 상품 데이터를 맞춰 넣기
  public Job productUploadJob(JobRepository jobRepository,
      JobExecutionListener jobExecutionListener, Step productUploadStep) {
    return new JobBuilder("productUploadJob", jobRepository)
        .listener(jobExecutionListener)
        .start(productUploadStep).build();

  }

  // 청크 프로세스를 통해 리더, 프로세서, writer를 사용해야함
  //업로드 로우를 받아서 프로덕트형태로 라이터에 집어넣느 청크 방식으로 동작함, 청크 사이즈는 1000
  //트랜잭션 매니저 넣어주고

  @Bean
  public Step productUploadStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      StepExecutionListener stepExecutionListener,
      ItemReader<ProductUploadCsvRow> productReader,
      ItemProcessor<ProductUploadCsvRow, Product> productProcessor,
      ItemWriter<Product> productWriter) {
    return new StepBuilder("productUploadStep", jobRepository)
        .<ProductUploadCsvRow, Product>chunk(1000, transactionManager)
        .allowStartIfComplete(true)//개발단계에서만 treu
        .reader(productReader)
        .processor(productProcessor)
        .writer(productWriter)
        .listener(stepExecutionListener)
        .build();
  }

  //csv를 읽어 오기 때문에 FlatFileItemReader사용 job파라미터의 input을 가져와야함
  @Bean
  @StepScope
  public FlatFileItemReader<ProductUploadCsvRow> productReader(
      @Value("#{jobParameters['inputFilePath']}") String path) {
    return new FlatFileItemReaderBuilder<ProductUploadCsvRow>()
        .name("productReader")
        .resource(new FileSystemResource(path))
        .delimited()
        .names(ReflectionUtils.getFieldNames(ProductUploadCsvRow.class)
            .toArray(String[]::new))//필드를 가져와야함
        .targetType(ProductUploadCsvRow.class)
        .linesToSkip(1)// 첫번째 행은 헤더기 때문에 skip
        .build();
  }

  //
  @Bean
  public ItemProcessor<ProductUploadCsvRow, Product> productProcessor() {
    return Product::from;
  }

  //만들어진 product-> jbdcbatch writer 데이터 소스 주입받아서 넣어주고 빈으로 매핑-> 쿼리
  @Bean
  public JdbcBatchItemWriter<Product> productWriter(DataSource dataSource) {
    String sql =
        "INSERT INTO products (product_id, seller_id, category, product_name, sales_start_date, sales_end_date, \n"
            + "                      product_status, brand, manufacturer, sales_price, stock_quantity, created_at, updated_at) \n"
            + "VALUES (:productId, :sellerId, :category, :productName, :salesStartDate, :salesEndDate, \n"
            + "        :productStatus, :brand, :manufacturer, :salesPrice, :stockQuantity, :createdAt, :updatedAt);\n";

    return new JdbcBatchItemWriterBuilder<Product>()
        .dataSource(dataSource)
        .sql(sql)
        .beanMapped()
        .build();
  }
  // step => reposiotry에서 가져옴, chunk별로 가져옴?, 핸들러를 사용, 끝나면 다시 시작

}
//빌더-> 각 레포 주입
