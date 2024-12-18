//package jiyoung.ecommerce.batch.jobconfig.product.download;
//
//
//import java.util.List;
//import javax.sql.DataSource;
//import jiyoung.ecommerce.batch.domain.product.Product;
//import jiyoung.ecommerce.batch.dto.product.ProductDownloadCsvRow;
//import jiyoung.ecommerce.batch.util.ReflectionUtils;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobExecutionListener;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepExecutionListener;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.database.JdbcPagingItemReader;
//import org.springframework.batch.item.database.PagingQueryProvider;
//import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
//import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
//import org.springframework.batch.item.file.FlatFileItemWriter;
//import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.transaction.PlatformTransactionManager;
//
//
//@Configuration
//public class ProductDownloadJobConfiguration {
//
//  @Bean
//  public Job productDownloadJobConfiguration(JobRepository jobRepository, Step productPagingStep,
//      JobExecutionListener listener) {
//    return new JobBuilder("productDownloadJob", jobRepository)
//        .start(productPagingStep)
//        .listener(listener)
//        .build();
//  }
//
//  //  청크 프로덕트를 읽어서 처리
//  @Bean
//  public Step productPagingStep(JobRepository jobRepository, StepExecutionListener listener,
//      PlatformTransactionManager transactionManager,
//      ItemReader<Product> productPagingReader,
//      ItemProcessor<Product, ProductDownloadCsvRow> productDownloadProcessor,
//      ItemWriter<ProductDownloadCsvRow> productCsvWriter) {
//    return new StepBuilder("productPagingStep", jobRepository)
//        .<Product, Product>chunk(1000, transactionManager)// 한 번에 1000개의 아이템을 읽고 처리한 후, 트랜잭션을 커밋
//        .allowStartIfComplete(true)//  이미 완료된 경우에도 다시 시작할 수 있도록 허용
//        .listener(listener)
//        .build();
//  }
//
////  @Bean
////  public Step productPagingStep(JobRepository jobRepository,
////      PlatformTransactionManager transactionManager,
////      JpaPagingItemReader<Product> productPagingReader,
////      ItemProcessor<Product, ProductDownloadCsvRow> productDownloadProcessor,
////      ItemWriter<ProductDownloadCsvRow> productCsvWriter,
////      StepExecutionListener stepExecutionListener, TaskExecutor taskExecutor) {
////    return new StepBuilder("productPagingStep", jobRepository)
////        .<Product, ProductDownloadCsvRow>chunk(100000, transactionManager)  // 청크 크기 설정
////        .reader(productPagingReader)  // ItemReader
////        .processor(productDownloadProcessor)  // ItemProcessor
////        .writer(productCsvWriter)  // ItemWriter
////        .allowStartIfComplete(true)  // 이전 실행 완료 후 다시 시작 허용
////        .listener(stepExecutionListener)  // StepExecutionListener
////        .taskExecutor(taskExecutor)  // 병렬 처리할 TaskExecutor 설정
////        .build();  // Step 빌드
////  }
//
//  //Reader
//  @Bean
//  public JdbcPagingItemReader<Product> productPagingReader(DataSource dataSource,
//      PagingQueryProvider productPagingQueryProvider) {
//    return new JdbcPagingItemReaderBuilder<Product>()
//        .dataSource(dataSource)
//        .name("productPagingReader")
//        .queryProvider(productPagingQueryProvider)
//        .pageSize(1000)
//        .beanRowMapper(Product.class)
//        .build();
//
//
//  }
//
//  // Query provider
//  @Bean
//  public SqlPagingQueryProviderFactoryBean productPagingQueryProvider(DataSource dataSource) {
//    SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
//    provider.setDataSource(dataSource);
//    provider.setSelectClause(
//        "select product_id,seller_id,category,product_name,sales_start_date,sales_end_date" +
//            "product_status,brand,manufacturer,sales_price,stock_quantity,created_at,updated_at");
//    provider.setFromClause("from products");
//    provider.setSortKey("product_id");// 항상 같은 결과가 나오도록 정렬!
//    return provider;
//  }
//
//
//  //processor (Product를 받아서 -> download.csv 매핑)
//  @Bean
//  public ItemProcessor<Product, ProductDownloadCsvRow> productDownloadProcessor() {
//    return ProductDownloadCsvRow::from;//정적 생성자를 이용해서 프로덕트에서 프로젝트 다운로드 csv로 매핑
//  }
//
//  // Writer
//  @Bean
//  @StepScope
//  public FlatFileItemWriter<ProductDownloadCsvRow> productCsvWriter(
//      @Value("#{jobParameters['outputFilePath']}") String path) {//job파라미터를 가져옴
//    List<String> columns = ReflectionUtils.getFieldNames(ProductDownloadCsvRow.class);
//    return new FlatFileItemWriterBuilder<ProductDownloadCsvRow>()
//        .name("productCsvWriter")
//        .resource(new FileSystemResource(path))
//        .delimited()
//        .names(columns.toArray(String[]::new))
//        .headerCallback(writer -> writer.write(String.join(",", columns)))
//        .build();
//  }
//
//  // 성능 최적화 -> 멀티스레드 환경에서 안전하게 데이터를 기록하도록 동기화
////  @Bean
////  @StepScope
////  public SynchronizedItemStreamWriter<ProductDownloadCsvRow> productCsvWriter(
////      @Value("#{stepExecutionContext['file']}") File file) {
////    List<String> columns = ReflectionUtils.getFieldNames(ProductDownloadCsvRow.class);
////    FlatFileItemWriter<ProductDownloadCsvRow> productCsvWriter = new FlatFileItemWriterBuilder<ProductDownloadCsvRow>()
////        .name("productCsvWriter")
////        .resource(new FileSystemResource(file))
////        .delimited()
////        .names(columns.toArray(String[]::new))
////        .build();
////    return new SynchronizedItemStreamWriterBuilder<ProductDownloadCsvRow>()
////        .delegate(productCsvWriter)
////        .build();
////  }
//
//}
