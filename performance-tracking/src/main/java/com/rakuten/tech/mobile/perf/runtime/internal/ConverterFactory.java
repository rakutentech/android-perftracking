package com.rakuten.tech.mobile.perf.runtime.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;

public class ConverterFactory extends Factory {

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    Class<?> targetClass = getRawType(type);
    if (targetClass.equals(ConfigurationResponse.class)) {
      return res -> new ConfigurationResponse(res.string());
    } else if (targetClass.equals(GeoLocationResponse.class)) {
      return res -> new GeoLocationResponse(res.string());
    } else {
      return null;
    }
  }
}
