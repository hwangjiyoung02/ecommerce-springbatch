package jiyoung.ecommerce.batch.domain.product;


import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jiyoung.ecommerce.batch.dto.product.ProductUploadCsvRow;
import jiyoung.ecommerce.batch.util.DateTimeUtils;
import jiyoung.ecommerce.batch.util.RandomUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

  @Id
  private String productId;
  private Long sellerId;

  private String category;
  private String productName;
  private LocalDate salesStartDate;
  private LocalDate salesEndDate;
  private String productStatus;
  private String brand;
  private String manufacturer;

  private int salesPrice;
  private int stockQuantity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static Product of(String productId, Long sellerId, String category, String productName,
      LocalDate salesStartDate,
      LocalDate salesEndDate, String productStatus, String brand,
      String manufacturer, int salesPrice,
      int stockQuantity,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    return new Product(productId, sellerId, category, productName, salesStartDate, salesEndDate,
        productStatus, brand, manufacturer,
        salesPrice, stockQuantity,
        createdAt,
        updatedAt);
  }

  public static Product from(ProductUploadCsvRow row) {
    LocalDateTime now = LocalDateTime.now();
    return new Product(
        RandomUtils.generateRandomId(),
        row.getSellerId(),
        row.getCategory(),
        row.getProductName(),
        DateTimeUtils.toLocalDate(row.getSalesStartDate()),//String -> localDate
        DateTimeUtils.toLocalDate(row.getSalesEndDate()),
        row.getProductStatus(),
        row.getBrand(),
        row.getManufacturer(),
        row.getSalesPrice(),
        row.getStockQuantity(),
        now,
        now
    );
  }
}

