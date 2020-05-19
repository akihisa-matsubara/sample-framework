package jp.co.sample.framework.jsf.config;

import jp.co.sample.framework.core.config.ConfigUtils;
import lombok.experimental.UtilityClass;

/**
 * ページ設定.
 */
@UtilityClass
public class PageConfig {
  /** キー情報：セッションタイムアウトページ. */
  private static final String KEY_SESSION_TIMEOUT_PAGE = "sessionTimeoutPage";
  /** キー情報：不正操作ページ. */
  private static final String KEY_ILLEGAL_OPERATION_PAGE = "illegalOperationPage";

  /**
   * セッションタイムアウトページを取得します.
   *
   * @return セッションタイムアウトページ
   */
  public static String getSessionTimeoutPage() {
    return ConfigUtils.getAsString(KEY_SESSION_TIMEOUT_PAGE);
  }

  /**
   * 不正操作ページを取得します.
   *
   * @return 不正操作ページ
   */
  public static String getIllegalOperationPage() {
    return ConfigUtils.getAsString(KEY_ILLEGAL_OPERATION_PAGE);
  }

}