package jiyoung.ecommerce.batch.util;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

  //필드 추출해서 csv 헤더가 될 리스트에 추가하기
  public static List<String> getFieldNames(Class<?> clazz) {
    List<String> fieldNames = new ArrayList<>();

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (!isStatic(field.getModifiers())) {
        fieldNames.add(field.getName());
      }
    }
    return fieldNames;
  }


}
