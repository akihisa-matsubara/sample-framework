package dev.sample.framework.rest.provider.filter;

import dev.sample.common.constant.Encoding;
import dev.sample.framework.core.code.LoggerVo;
import dev.sample.framework.core.constant.PrioritiesExt;
import dev.sample.framework.rest.pres.dto.ResponseBaseDto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * ログ出力フィルター.
 */
@Provider
@Priority(PrioritiesExt.FRAMEWORK)
@Slf4j
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {
  /** MDC Key - start time. */
  private static final String START_TIME = "start-time";

  /** Logging Header Template. */
  private static final String DUMP_TEMPLATE = "%-20s%s\n";

  /** Performance Logger. */
  private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger(LoggerVo.PERFORMANCE_LOGGER.getCode());
  /** Access Logger. */
  private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger(LoggerVo.ACCESS_LOGGER.getCode());

  /** Resource Info. */
  @Context
  private ResourceInfo resourceInfo;

  /**
   * リクエスト受付時にアクセス履歴とリクエストに関する情報をログ出力します.
   * TODO BADリクエスト(4XX)次第でSkipされてしまう
   *
   * @param context ContainerRequestContext
   */
  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    MDC.put(START_TIME, String.valueOf(System.currentTimeMillis()));
    logAccess(context);
    logRequest(context);
    logRequestBody(context);
  }

  /**
   * リクエスト応答時に性能とレスポンスに関する情報をログ出力します.
   *
   * @param requestContext ContainerRequestContext
   * @param responseContext ContainerResponseContext
   */
  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    // エラー時はContainerRequest.filterを通らないので、レスポンス時に出力
    String startTime = MDC.get(START_TIME);
    if (startTime == null || startTime.isEmpty()) {
      logAccess(requestContext);
      logRequest(requestContext);

    } else {
      logPerformance(requestContext);

    }

    logResponse(responseContext);
    // clear the context on exit
    MDC.clear();
  }

  /**
   * メッセージボディーをログ出力します.
   *
   * @param context WriterInterceptorContext
   */
  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
    logResponseBody(context);

  }

  /**
   * リクエスト情報をログ出力します.
   *
   * @param context ContainerRequestContext
   */
  private void logRequest(ContainerRequestContext context) {
    StringBuilder builder = new StringBuilder();
    builder.append("\n----- HTTP REQUEST ---------------------\n");
    builder.append(String.format(DUMP_TEMPLATE, "Request URL", context.getUriInfo().getAbsolutePath()));
    builder.append(String.format(DUMP_TEMPLATE, "Request Method", context.getMethod()));
    builder.append(String.format(DUMP_TEMPLATE, "Resource Method",
        resourceInfo.getResourceMethod() == null ? "-" : resourceInfo.getResourceMethod().getName()));
    builder.append(String.format(DUMP_TEMPLATE, "Class",
        resourceInfo.getResourceClass() == null ? "-" : resourceInfo.getResourceClass().getCanonicalName()));

    builder.append("----- PATH PARAMETER -------------------\n");
    builder.append(dumpParameters(context.getUriInfo().getPathParameters()));

    builder.append("----- QUERY PARAMETER ------------------\n");
    builder.append(dumpParameters(context.getUriInfo().getQueryParameters()));

    builder.append("----- HTTP REQUEST HEADER --------------\n");
    for (String headerName : context.getHeaders().keySet()) {
      builder.append(String.format(DUMP_TEMPLATE, headerName, context.getHeaderString(headerName)));
    }

    log.debug(builder.toString());
  }

  /**
   * リクエストボディーをログ出力します.
   *
   * @param context ContainerRequestContext
   * @throws IOException IO例外
   */
  private void logRequestBody(ContainerRequestContext context) throws IOException {
    StringBuilder builder = new StringBuilder();

    builder.append("\n----- HTTP REQUEST BODY ----------------\n");
    try (Scanner sc = new Scanner(context.getEntityStream(), Encoding.UTF8)) {
      sc.useDelimiter("\\A");
      if (sc.hasNext()) {
        String body = sc.next();
        context.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        builder.append(body);
      }
    }

    log.debug(builder.toString());
  }

  /**
   * レスポンス情報をログ出力します.
   *
   * @param context ContainerResponseContext
   */
  private void logResponse(ContainerResponseContext context) {
    StringBuilder builder = new StringBuilder();
    builder.append("\n----- HTTP RESPONSE --------------------\n");
    builder.append(String.format(DUMP_TEMPLATE, "Status", context.getStatus()));

    builder.append("----- HTTP RESPONSE HEADER -------------\n");
    for (String headerName : context.getHeaders().keySet()) {
      builder.append(String.format(DUMP_TEMPLATE, headerName, context.getHeaderString(headerName)));
    }

    log.debug(builder.toString());
  }

  /**
   * レスポンスボディーをログ出力します.
   *
   * @param context WriterInterceptorContext
   * @throws IOException IO例外
   */
  private void logResponseBody(WriterInterceptorContext context) throws IOException {
    if (!(context.getEntity() instanceof ResponseBaseDto)) {
      context.proceed();
      return;
    }

    StringBuilder builder = new StringBuilder();
    OutputStream originalStream = context.getOutputStream();
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      context.setOutputStream(byteArrayOutputStream);
      context.proceed();
      builder.append("\n----- HTTP RESPONSE BODY ---------------\n");
      builder.append(byteArrayOutputStream.toString(Encoding.UTF8));
      byteArrayOutputStream.writeTo(originalStream);

    } finally {
      context.setOutputStream(originalStream);

    }

    log.debug(builder.toString());
  }

  /**
   * アクセス履歴をログ出力します.
   *
   * @param context ContainerRequestContext
   */
  private void logAccess(ContainerRequestContext context) {
    ACCESS_LOGGER.debug("Resource:/{}, Method-Type:{}, Method-Name:{}", context.getUriInfo().getPath(),
        context.getMethod(), resourceInfo.getResourceMethod() == null ? "-" : resourceInfo.getResourceMethod().getName());
  }

  /**
   * 性能情報をログ出力します.
   *
   * @param context ContainerRequestContext
   */
  private void logPerformance(ContainerRequestContext context) {
    String startTime = MDC.get(START_TIME);
    long executionTime = System.currentTimeMillis() - Long.parseLong(startTime);

    PERFORMANCE_LOGGER.debug("Total time: {} milliseconds, Resource: /{}, Method-Type: {}, Method-Name: {}", executionTime,
        context.getUriInfo().getPath(), context.getMethod(), resourceInfo.getResourceMethod().getName());
  }

  /**
   * パラメーター情報をダンプします.
   *
   * @param paramters パラメーター
   * @return パラメーターのダンプ
   */
  private StringBuilder dumpParameters(Map<String, List<String>> paramters) {
    StringBuilder dump = new StringBuilder();
    for (Map.Entry<String, List<String>> entry : paramters.entrySet()) {
      List<String> obj = entry.getValue();
      String value = null;
      if (obj != null && !obj.isEmpty()) {
        value = obj.get(0);
      }
      dump.append(String.format(DUMP_TEMPLATE, entry.getKey(), value));
    }
    return dump;
  }

}
