package jiyoung.ecommerce.batch.service.product;


import jiyoung.ecommerce.batch.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final JdbcTemplate jdbcTemplate;

  public Long countProducts() {
    return jdbcTemplate.queryForObject("select count(*) from products", Long.class);
  }

  public void save(Product of) {
  }

  //vmfproduct 데이터 넣기 - jpa로 ...
//  public void save(Product product) {
//    String sql = "insert into products(product_id,seller_id)";
//
//    jdbcTemplate.update(sql);
//  }

}
